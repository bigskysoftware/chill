package chill.job.impl;

import chill.job.ChillJob;
import chill.job.ChillJobRunner;
import chill.utils.TheMissingUtils;

public class DefaultChillJobRunner extends ChillJobRunner {
    @Override
    public void handle(ChillJob job) {
        TheMissingUtils.safely(job::run);
    }
}
