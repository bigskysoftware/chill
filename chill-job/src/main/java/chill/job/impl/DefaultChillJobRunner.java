package chill.job.impl;

import chill.job.ChillJob;
import chill.job.ChillJobRunner;
import chill.job.model.JobEntity;
import chill.utils.TheMissingUtils;

public class DefaultChillJobRunner extends ChillJobRunner {
    @Override
    public void handle(ChillJob job) {
        TheMissingUtils.safely(() -> {
            updateStatus(job, JobEntity.Status.RUNNING);
            try {
                job.run();
            } catch (Throwable t) {
                updateStatus(job, JobEntity.Status.ERRORED);
                throw t;
            }
            updateStatus(job, JobEntity.Status.COMPLETED);
        });
    }

    protected void updateStatus(ChillJob job, JobEntity.Status status) {
        new JobEntity()
                .withId(job.getJobId().toString())
                .withStatus(status)
                .update();
    }
}
