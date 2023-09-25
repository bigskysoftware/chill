package chill.job;

import chill.db.ChillRecord;
import chill.job.model.JobEntity;
import com.google.gson.Gson;

import java.util.Objects;

public abstract class ChillJob {
    private final ChillJobId jobId;

    public ChillJob() {
        this(new ChillJobId());
    }

    public ChillJob(String tag) {
        this(new ChillJobId(tag));
    }

    public ChillJob(ChillJobId jobId) {
        this.jobId = jobId;
    }

    public static ChillJob fromRecord(JobEntity record) {
        Gson gson = new Gson();
        String jobJson = record.getJobJson();
        Class<?> jobClass = Class.forName(record.getJobClass());
        return (ChillJob) gson.fromJson(jobJson, jobClass);
    }

    public ChillJobId getJobId() {
        return jobId;
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
