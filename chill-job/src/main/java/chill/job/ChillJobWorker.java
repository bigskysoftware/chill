package chill.job;

import chill.job.model.JobEntity;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ChillJobWorker {
    protected final UUID serverId;
    protected final int numWorkers;
    protected AtomicBoolean paused = new AtomicBoolean(false);

    public ChillJobWorker() {
        this(4);
    }

    public ChillJobWorker(int numWorkers) {
        this.serverId = UUID.randomUUID();
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
}
