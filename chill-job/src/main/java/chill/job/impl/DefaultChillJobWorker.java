package chill.job.impl;

import chill.db.ChillMigrations;
import chill.job.ChillJob;
import chill.job.ChillJobId;
import chill.job.ChillJobRunner;
import chill.job.ChillJobWorker;
import chill.job.model.JobEntity;
import chill.job.model.Migrations;
import chill.job.model.QueueEntity;
import chill.utils.TheMissingUtils;
import com.google.gson.Gson;

import java.sql.Timestamp;
import java.util.concurrent.Future;

public class DefaultChillJobWorker extends ChillJobWorker {
    private static DefaultChillJobWorker worker = null;

    public static DefaultChillJobWorker getInstance() {
        if (worker == null) worker = new DefaultChillJobWorker();
        return worker;
    }

    private static ChillMigrations migrations;

    public DefaultChillJobWorker() {
        this(4);
    }

    public DefaultChillJobWorker(int numWorkers) {
        super(numWorkers);

        if (migrations == null) {
            migrations = new ChillMigrations(Migrations.class);
            migrations.up();
        }

        for (int i = 0; i < numWorkers; i++) {
            addWorker(Thread.ofVirtual()
                    .name("default-chill-job-worker-" + i)
                    .start(this::worker));
        }
    }

    protected void worker() {
        ChillJobRunner runner = new DefaultChillJobRunner();
        while (true) {
            if (isPaused()) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            long start = System.currentTimeMillis();
            ChillJob job = JobEntity.dequeue();
            if (job != null) {
                System.out.println("got job: " + job);
                runner.handle(job);
            }
            long waitingPeriod = Math.max(0, System.currentTimeMillis() - start);

            if (waitingPeriod > 0) {
                try {
                    // todo: optimize?
                    Thread.sleep(waitingPeriod);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void submit(ChillJob job) {
        var jobEntity = new JobEntity()
                .withStatus(JobEntity.Status.PENDING)
                .withId(job.getJobId().toString())
                .withJobJson(new Gson().toJson(job))
                .withJobClass(job.getClass().getName())
                .createOrThrow();

        new QueueEntity()
                .withJobId(jobEntity)
                .createOrThrow();
    }

    @Override
    public void cancel(ChillJob job) {
        JobEntity entity = JobEntity.find.byPrimaryKey(job.getJobId().toString());
        if (entity != null) {
            entity.setStatus(JobEntity.Status.CANCELLED);
            entity.update();
        }
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
}
