package chill.job.impl;

import chill.db.ChillMigrations;
import chill.db.ChillRecord;
import chill.job.ChillJob;
import chill.job.ChillJobId;
import chill.job.ChillJobRunner;
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
    private ChillJobRunner runner;

    private ThreadPoolExecutor executor;
    private TimerScheduler manager;

    public DefaultChillJobWorker() {
        this(4, 60 * 1000, null);
    }

    public DefaultChillJobWorker(int numWorkers, long msKeepAlive, ChillJobRunner runner) {
        super(numWorkers);
        this.runner = runner == null ? new DefaultChillJobRunner() : runner;

        if (migrations == null) {
            migrations = new ChillMigrations(Migrations.class);
            migrations.up();
        }

        executor = new ThreadPoolExecutor(
                numWorkers,
                numWorkers * 2,
                msKeepAlive,
                TimeUnit.MILLISECONDS,
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
        return () -> {
            long startTime = System.currentTimeMillis();

            var entityCount = ChillJobEntity
                    .where("status = ?", JobStatus.PENDING)
                    .orderBy("timestamp")
                    .limit(numWorkers - executor.getActiveCount())
                    .updateAll(
                            "status = ?, worker_id = ?",
                            JobStatus.ASSIGNED,
                            workerId.toString()
                    );

            if (entityCount > 0) {
                var jobs = ChillJobEntity
                        .where("status = ?, worker_id = ?",
                                JobStatus.ASSIGNED, workerId.toString())
                        .limit(entityCount)
                        .toList();

                for (var job : jobs) {
                    var clazz = TheMissingUtils.safely(() -> Class.forName(job.getJobClass()));
                    ChillJob task = (ChillJob) new Gson().fromJson(job.getJobData(), clazz);
                    System.out.println("Executing job: " + task.getJobId());
                    executor.submit(() -> {
                        TheMissingUtils.safely(() -> {
                            runner.handle(task);
                        });
                    });
                }
            }

            Long endTime = System.currentTimeMillis();
            Long duration = endTime - startTime;
            Long sleepTime = Math.max(0, 5000 - duration);
            manager.schedule(managerTask(), sleepTime, TimeUnit.MILLISECONDS);
        };
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
