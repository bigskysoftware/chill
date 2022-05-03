package chill.workers;

import chill.env.ChillEnv;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.storage.nosql.redis.JedisRedisStorageProvider;
import redis.clients.jedis.JedisPool;

public class Foreman {

    public static final Foreman INSTANCE = new Foreman();

    private final JedisPool JEDIS_WORKER_POOL = new JedisPool(ChillEnv.getRedisURL());

    public void initClient() {
        JobRunr.configure()
                .useStorageProvider(new JedisRedisStorageProvider(JEDIS_WORKER_POOL))
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
