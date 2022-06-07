package chill.workers;


public class Foreman {

    public static final Foreman INSTANCE = new Foreman();

    public static void foo() {

    }

    public void initClient() {
//        JobRunr.configure()
//                .useStorageProvider(new JedisRedisStorageProvider(JEDIS_WORKER_POOL))
//                .initialize();
    }

    public void initWorkers() {
//        JobRunr.configure()
//                .useStorageProvider(new JedisRedisStorageProvider(JEDIS_WORKER_POOL))
//                .useBackgroundJobServer()
//                .initialize();
    }

    public void initWorkersAndWeb() {
//        JobRunr.configure()
//                .useStorageProvider(new JedisRedisStorageProvider(JEDIS_WORKER_POOL))
//                .useBackgroundJobServer()
//                .useDashboard(ChillEnv.getJobDashboardPort())
//                .initialize();
    }
}
