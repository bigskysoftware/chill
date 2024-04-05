package chill.job;

import chill.job.model.ChillJobEntity;
import chill.job.model.JobStatus;
import chill.utils.TheMissingUtils;
import com.google.gson.Gson;

import java.util.Objects;

public abstract class ChillJob {
    ChillJobId id;
    private transient ChillJobWorker worker = null;

    public ChillJob() {
        this(null, null);
    }

    public ChillJob(ChillJobWorker worker) {
        this(null, worker);
    }

    public ChillJob(ChillJobId id, ChillJobWorker worker) {
        this.id = id == null ? new ChillJobId() : id;
        this.worker = worker;
    }

    protected ChillJobWorker getWorker() {
        if (worker == null) {
            worker = ChillJobWorker.getDefaultInstance();
        }
        return worker;
    }

    public ChillJobId getJobId() {
        return id;
    }

    public void submit() {
        getWorker().submit(this);
    }

    protected abstract void run() throws Exception;

    public void executeInPlace() {
        TheMissingUtils.safely(this::run);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChillJob chillJob = (ChillJob) o;
        return Objects.equals(id, chillJob.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(jobId: " + id + ")";
    }

    public JobStatus getStatus() {
        return getWorker().getJobStatus(getJobId());
    }
}
