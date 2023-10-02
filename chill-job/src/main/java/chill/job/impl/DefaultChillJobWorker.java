package chill.job.impl;

import chill.db.ChillMigrations;
import chill.db.ChillRecord;
import chill.job.ChillJob;
import chill.job.ChillJobId;
import chill.job.ChillJobWorker;
import chill.job.model.ChillJobEntity;
import chill.job.model.JobStatus;
import chill.job.model.Migrations;
import chill.utils.TheMissingUtils;
import com.google.gson.Gson;
import org.eclipse.jetty.util.thread.TimerScheduler;

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
        throw new UnsupportedOperationException();
//        return () -> TheMissingUtils.safely(() -> {
//            try (var ignored = ChillRecord.quietly()) {
//                int activeJobs = getActiveJobs();
//                if (activeJobs < numWorkers) {
//                    int numToSpawn = numWorkers - activeJobs;
//
//                    var numReceived = QueueEntity
//                            .where("status = ?", JobStatus.PENDING)
//                            .limit(numToSpawn)
//                            .updateAll(
//                                    "status = ?, worker_id = ?, timestamp = ?",
//                                    JobStatus.RUNNING,
//                                    workerId.toString(),
//                                    new Timestamp(System.currentTimeMillis())
//                            );
//
//                    if (numReceived != 0) {
//                        var items = QueueEntity
//                                .where("status = ?", JobStatus.RUNNING)
//                                .limit(numReceived)
//                                .orderBy("timestamp")
//                                .toList();
//                        for (var item : items) {
//                            executor.submit(() -> TheMissingUtils.safely(() -> {
//                                var job = ChillJob.fromRecord(item.getJobId());
//                                job.run();
//                                item.delete();
//                            }));
//                        }
//                    }
//                }
//            }
//
//            manager.schedule(managerTask(), 5, TimeUnit.SECONDS);
//        });
    }

    @Override
    public void submit(ChillJob job) {
        ChillRecord.inTransaction(() -> {
            job.getEntity()
                    .withId(job.getJobId().toString())
                    .withStatus(JobStatus.PENDING)
                    .withWorkerId(workerId.toString())
                    .withJobClass(job.getClass().getName())
                    .withJobData(new Gson().toJson(job))
                    .createOrThrow();
        });
    }

    @Override
    public ChillJob fetchJob(ChillJobId id) {
        var entity = ChillJobEntity.find.byPrimaryKey(id.toString());
        if (entity == null) {
            return null;
        } else {
            return TheMissingUtils.safely(() -> {
                Gson gson = new Gson();
                Class<?> clazz = Class.forName(entity.getJobClass());
                ChillJob job = ((ChillJob) gson.fromJson(entity.getJobData(), clazz));
                setJobEntity(job, entity);
                return job;
            });
        }
    }

    @Override
    public boolean cancelJob(ChillJobId id) {
        return new ChillJobEntity()
                .withId(id.toString())
                .withStatus(JobStatus.PENDING)
                .delete() > 0;
    }

    @Override
    public JobStatus getJobStatus(ChillJobId jobId) {
        return ChillJobEntity
                .find
                .select(ChillJobEntity.status())
                .first()
                .getStatus();
    }

    @Override
    public String getWorkerId() {
        return workerId.toString();
    }
}
