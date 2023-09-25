package chill.job.impl;

import chill.db.ChillMigrations;
import chill.job.ChillJob;
import chill.job.ChillJobWorker;
import chill.job.model.JobEntity;
import chill.job.model.Migrations;
import com.google.gson.Gson;

public class DefaultChillJobWorker extends ChillJobWorker {
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
        while (true) {
            if (isPaused()) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("getting job");
            ChillJob job = JobEntity.dequeue();
            System.out.println("got job: " + job);
            if (job != null) {
                long start = System.currentTimeMillis();

                String error = null;
                try {
                    job.run();
                } catch (Exception e) {
                    error = e.toString();
                }

                long waitingPeriod = Math.max(0, System.currentTimeMillis() - start);
            }
        }
    }

    @Override
    public void submit(ChillJob job) {
        JobEntity entity = new JobEntity();
        entity.setStatus(JobEntity.Status.PENDING);
        entity.setId(job.getJobId().toString());
        entity.setJobJson(new Gson().toJson(job));
        entity.createOrThrow();
    }

    @Override
    public void cancel(ChillJob job) {
        JobEntity entity = JobEntity.find.byPrimaryKey(job.getJobId().toString());
        if (entity != null) {
            entity.setStatus(JobEntity.Status.CANCELLED);
            entity.update();
        }
    }
}
