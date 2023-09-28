package chill.job.impl;

import chill.db.ChillMigrations;
import chill.db.ChillRecord;
import chill.job.ChillJob;
import chill.job.ChillJobId;
import chill.job.ChillJobWorker;
import chill.job.model.JobEntity;
import chill.job.model.Migrations;
import chill.job.model.QueueEntity;
import chill.utils.TheMissingUtils;
import com.google.gson.Gson;
import org.eclipse.jetty.util.thread.TimerScheduler;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultChillJobWorker extends ChillJobWorker {
    private static DefaultChillJobWorker worker = null;

    public static DefaultChillJobWorker getInstance() {
        if (worker == null) worker = new DefaultChillJobWorker();
        return worker;
    }

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
//        manager = Thread.ofVirtual().name("chill-job-pool-manager").start(this::manage);
        manager = new TimerScheduler();
        TheMissingUtils.safely(() -> {
            manager.start();
        });
        manager.schedule(managerTask(), 5, TimeUnit.SECONDS);
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
            int activeJobs = getActiveJobs();
            if (activeJobs < numWorkers) {
                int numToSpawn = numWorkers - activeJobs;

                System.out.println("update query");
                var numReceived = QueueEntity
                        .where("status = ?", JobEntity.Status.PENDING)
                        .limit(numToSpawn)
                        .updateAll(
                                "status = ?, worker_id = ?",
                                JobEntity.Status.RUNNING,
                                serverId.toString()
                        );

                if (numReceived != 0) {
                    var items = QueueEntity
                            .where("status = ?", JobEntity.Status.RUNNING)
                            .limit(numReceived)
                            .orderBy("timestamp")
                            .toList();
                }
            }

            manager.schedule(managerTask(), 5, TimeUnit.SECONDS);
        });
    }

    @Override
    public void submit(ChillJob job) {
        ChillRecord.inTransaction(() -> {
            var jobEntity = new JobEntity()
                    .withStatus(JobEntity.Status.PENDING)
                    .withId(job.getJobId().toString())
                    .withJobJson(new Gson().toJson(job))
                    .withJobClass(job.getClass().getName())
                    .createOrThrow();

            new QueueEntity()
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
    public JobEntity.Status getJobStatus(ChillJobId jobId) {
        return null;
    }
}
