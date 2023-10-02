package chill.job;

import chill.job.impl.DefaultChillJobWorker;
import chill.job.model.ChillJobEntity;
import chill.job.model.JobStatus;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ChillJobWorker {
    private static ChillJobWorker defaultInstance;

    public static ChillJobWorker getDefaultInstance() {
        if (defaultInstance == null) {
            String className = new Exception().getStackTrace()[1].getClassName();
            System.out.printf("[%s %s] no default worker set, using %s%n", new Timestamp(System.currentTimeMillis()),
                    className, DefaultChillJobWorker.class.getName());
            defaultInstance = new DefaultChillJobWorker();
        }
        return defaultInstance;
    }

    public static ChillJobWorker setDefaultInstance(ChillJobWorker worker) {
        var out = defaultInstance;
        defaultInstance = worker;
        return out;
    }

    protected final UUID workerId;
    protected final int numWorkers;
    protected AtomicBoolean paused = new AtomicBoolean(false);

    public ChillJobWorker() {
        this(4);
    }

    public ChillJobWorker(int numWorkers) {
        this.workerId = UUID.randomUUID();
        this.numWorkers = numWorkers;
    }


    protected void setJobEntity(ChillJob job, ChillJobEntity entity) {
        job.id = ChillJobId.fromString(entity.getId());
        job.entity = entity;
    }

    public abstract void shutdown();
    public abstract int getActiveJobs();
    public abstract int getNumWorkers();

    public boolean isPaused() {
        return paused.get();
    }

    public void setPaused(boolean paused) {
        this.paused.set(paused);
    }

    public abstract void submit(ChillJob job);

    public abstract ChillJob fetchJob(ChillJobId id);
    public abstract boolean cancelJob(ChillJobId id);

    public abstract JobStatus getJobStatus(ChillJobId jobId);

    public abstract String getWorkerId();
}
