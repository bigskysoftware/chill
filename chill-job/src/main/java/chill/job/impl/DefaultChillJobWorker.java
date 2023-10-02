package chill.job.impl;

import chill.db.ChillMigrations;
import chill.db.ChillRecord;
import chill.job.ChillJob;
import chill.job.ChillJobId;
import chill.job.ChillJobWorker;
import chill.job.model.JobEntity;
import chill.job.model.JobStatus;
import chill.job.model.Migrations;
import chill.job.model.QueueEntity;
import chill.utils.TheMissingUtils;
import com.google.gson.Gson;
import org.eclipse.jetty.util.thread.TimerScheduler;

import java.sql.Timestamp;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultChillJobWorker extends ChillJobWorker {
    private static ChillMigrations migrations;

    //---

    private ThreadPoolExecutor executor;
    private TimerScheduler manager;

    public DefaultChillJobWorker() {
        this(4);
    }

    public DefaultChillJobWorker(int numWorkers) {
        super(numWorkers);

        if (migrations == null) {
            migrations = new ChillMigrations(Migrations.class);
            migrations.up();
        }

        executor = new ThreadPoolExecutor(
                numWorkers,
                numWorkers * 2,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                Thread.ofVirtual().name("chill-job-worker-pool").factory()
        );
        manager = new TimerScheduler();
        TheMissingUtils.safely(() -> {
            manager.start();
        });
        manager.schedule(managerTask(), 5, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
        TheMissingUtils.safely(() -> {
            executor.shutdown();
            manager.stop();
        });
    }

    @Override
    public int getActiveJobs() {
        return executor.getActiveCount();
    }

    @Override
    public int getNumWorkers() {
        return numWorkers;
    }

    protected Runnable managerTask() {
        return () -> TheMissingUtils.safely(() -> {
            try (var ignored = ChillRecord.quietly()) {
                int activeJobs = getActiveJobs();
                if (activeJobs < numWorkers) {
                    int numToSpawn = numWorkers - activeJobs;

                    var numReceived = QueueEntity
                            .where("status = ?", JobStatus.PENDING)
                            .limit(numToSpawn)
                            .updateAll(
                                    "status = ?, worker_id = ?, timestamp = ?",
                                    JobStatus.RUNNING,
                                    workerId.toString(),
                                    new Timestamp(System.currentTimeMillis())
                            );

                    if (numReceived != 0) {
                        var items = QueueEntity
                                .where("status = ?", JobStatus.RUNNING)
                                .limit(numReceived)
                                .orderBy("timestamp")
                                .toList();
                        for (var item : items) {
                            executor.submit(() -> TheMissingUtils.safely(() -> {
                                var job = ChillJob.fromRecord(item.getJobId());
                                job.run();
                                item.delete();
                            }));
                        }
                    }
                }
            }

            manager.schedule(managerTask(), 5, TimeUnit.SECONDS);
        });
    }

    @Override
    public void submit(ChillJob job) {
        ChillRecord.inTransaction(() -> {
            var jobEntity = new JobEntity()
                    .withId(job.getJobId().toString())
                    .withJobJson(new Gson().toJson(job))
                    .withJobClass(job.getClass().getName())
                    .createOrThrow();

            new QueueEntity()
                    .withStatus(JobStatus.PENDING)
                    .withWorkerId(getWorkerId())
                    .withJobId(jobEntity)
                    .createOrThrow();
        });
    }

    @Override
    public ChillJob fetchJob(ChillJobId id) {
        var entity = JobEntity.find.byPrimaryKey(id.toString());
        if (entity == null) {
            return null;
        } else {
            return ChillJob.fromRecord(entity);
        }
    }

    @Override
    public boolean cancelJob(ChillJobId id) {
        return JobEntity
                .where("job_id", id.toString())
                .join(QueueEntity.to.jobId)
                .and("status", JobStatus.PENDING)
                .deleteAll() > 0;
    }

    @Override
    public JobStatus getJobStatus(ChillJobId jobId) {
        return QueueEntity
                .where("job_id = ?", jobId.toString())
                .limit(1)
                .select(QueueEntity.field.status)
                .first()
                .getStatus();
    }

    @Override
    public String getWorkerId() {
        return workerId.toString();
    }
}
