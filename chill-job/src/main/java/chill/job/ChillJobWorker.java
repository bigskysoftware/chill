package chill.job;

import chill.job.model.JobEntity;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ChillJobWorker {
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

    public abstract JobEntity.Status getJobStatus(ChillJobId jobId);

    public abstract String getWorkerId();
}
