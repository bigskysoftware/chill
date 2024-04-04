package chill.workers;

import chill.job.ChillJobWorker;

public class Foreman {

    public static final Foreman INSTANCE = new Foreman();
    private ChillJobWorker worker;
//    public static final String CHILL_JOBRUNR_PREFIX = "chill";

    public void initClient() {
        worker = ChillJobWorker.getDefaultInstance();
    }

    public void initWorkersAndWeb() {
//        JobRunr.configure()
//                .useStorageProvider(new JedisRedisStorageProvider(Redis.getPool(), CHILL_JOBRUNR_PREFIX))
//                .useBackgroundJobServer()
//                .useDashboard(ChillEnv.getJobDashboardPort())
//                .initialize();
        // todo: add a ui
    }
}
