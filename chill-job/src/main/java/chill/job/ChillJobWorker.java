package chill.job;

import chill.db.ChillMigrations;
import chill.job.model.JobEntity;
import chill.job.model.Migrations;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ChillJobWorker {
    protected final UUID serverId;
    protected final int numWorkers;
    private final List<Thread> workers;
    protected AtomicBoolean paused = new AtomicBoolean(false);

    public ChillJobWorker() {
        this(4);
    }

    public ChillJobWorker(int numWorkers) {
        this.serverId = UUID.randomUUID();
        this.numWorkers = numWorkers;
        this.workers = new LinkedList<>();
    }

    public boolean isPaused() {
        return paused.get();
    }

    protected void addWorker(Thread worker) {
        if (worker != null && numWorkers > 0 && workers.size() < numWorkers) {
            workers.add(worker);
        }
    }

    public abstract void submit(ChillJob job);

    public abstract void cancel(ChillJob job);
}
