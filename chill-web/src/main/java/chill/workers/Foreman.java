package chill.workers;


import org.jobrunr.configuration.JobRunr;
import org.jobrunr.storage.nosql.redis.JedisRedisStorageProvider;

public class Foreman {

    public static final Foreman INSTANCE = new Foreman();

    public void initClient() {
        JobRunr.configure()
                .useStorageProvider(new JedisRedisStorageProvider(Redis.))
                .initialize();
    }

    public void initWorkers() {
        JobRunr.configure()
                .useStorageProvider(new JedisRedisStorageProvider(JEDIS_WORKER_POOL))
                .useBackgroundJobServer()
                .initialize();
    }

    public void initWorkersAndWeb() {
        JobRunr.configure()
                .useStorageProvider(new JedisRedisStorageProvider(JEDIS_WORKER_POOL))
                .useBackgroundJobServer()
                .useDashboard(ChillEnv.getJobDashboardPort())
                .initialize();
    }
}
