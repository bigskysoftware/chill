package chill.util;

import chill.env.ChillEnv;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class Redis {

    public static final RedissonClient REDISSON = initRedisson();

    private static RedissonClient initRedisson() {
        Config config = new Config();
        String redisURL = ChillEnv.getRedisURL();
        config.useSingleServer().setAddress(redisURL);
        return Redisson.create(config);
    }
}
