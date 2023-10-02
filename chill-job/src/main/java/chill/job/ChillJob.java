package chill.job;

import chill.db.ChillRecord;
import chill.job.impl.DefaultChillJobWorker;
import chill.job.model.JobEntity;
import chill.utils.TheMissingUtils;
import com.google.gson.Gson;

import java.util.Objects;

public abstract class ChillJob {
    private final ChillJobId jobId;
    transient JobEntity entity;
    transient ChillJobWorker worker;

    public ChillJob() {
        this(new ChillJobId(), null);
    }

    public ChillJob(String tag) {
        this(new ChillJobId(tag), null);
    }

    public ChillJob(String tag, ChillJobWorker worker) {
        this(new ChillJobId(tag), worker);
    }

    public ChillJob(ChillJobId jobId, ChillJobWorker worker) {
        this.jobId = Objects.requireNonNull(jobId);
        this.worker = worker;
    }

    protected ChillJobWorker getWorker() {
        if (worker == null) {
            worker = ChillJobWorker.getDefaultInstance();
        }
        return worker;
    }

    public static ChillJob fromRecord(JobEntity record) {
        Gson gson = new Gson();
        String jobJson = record.getJobJson();
        Class<?> jobClass = TheMissingUtils.safely(() -> Class.forName(record.getJobClass()));
        ChillJob job = (ChillJob) gson.fromJson(jobJson, jobClass);
        job.entity = record;
        return job;
    }

    public ChillJobId getJobId() {
        return jobId;
    }

    public void submit() {
        getWorker().submit(this);
    }

    public abstract void run() throws Exception;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChillJob chillJob = (ChillJob) o;
        return Objects.equals(jobId, chillJob.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(jobId: " + jobId + ")";
    }
}
