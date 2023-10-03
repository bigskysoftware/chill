package chill.job.impl;

import chill.job.ChillJob;
import chill.job.ChillJobRunner;
import chill.job.model.JobStatus;
import chill.utils.TheMissingUtils;

public class DefaultChillJobRunner extends ChillJobRunner {
    @Override
    public void handle(ChillJob job) {
        TheMissingUtils.safely(() -> {
            updateStatus(job, JobStatus.RUNNING);
            try {
                job.run();
            } catch (Throwable t) {
                updateStatus(job, JobStatus.ERRORED);
                throw t;
            }
            updateStatus(job, JobStatus.COMPLETED);
        });
    }

    protected void updateStatus(ChillJob job, JobStatus status) {
        System.out.println("Updating status to " + status);
        job
                .getEntity()
                .withStatus(status)
                .save();
    }
}
