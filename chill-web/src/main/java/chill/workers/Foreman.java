package chill.workers;

import chill.env.ChillEnv;
import chill.util.Redis;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.storage.nosql.redis.JedisRedisStorageProvider;

public class Foreman {

    public static final Foreman INSTANCE = new Foreman();
    public static final String CHILL_JOBRUNR_PREFIX = "chill";

    public void initClient() {
        JobRunr.configure()
                .useStorageProvider(new JedisRedisStorageProvider(Redis.getPool(), CHILL_JOBRUNR_PREFIX))
                .initialize();
    }

    public void initWorkers() {
        JobRunr.configure()
                .useStorageProvider(new JedisRedisStorageProvider(Redis.getPool(), CHILL_JOBRUNR_PREFIX))
                .useBackgroundJobServer()
                .initialize();
    }

    public void initWorkersAndWeb() {
        JobRunr.configure()
                .useStorageProvider(new JedisRedisStorageProvider(Redis.getPool(), CHILL_JOBRUNR_PREFIX))
                .useBackgroundJobServer()
                .useDashboard(ChillEnv.getJobDashboardPort())
                .initialize();
    }
}
