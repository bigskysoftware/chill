package chill.job.impl;

import chill.job.ChillJob;
import chill.job.ChillJobRunner;

public class DefaultChillJobRunner extends ChillJobRunner {


    @Override
    public void handle(ChillJob job) {
        try {
            job.run();
        } catch (Exception e) {
            
        }
    }
}
