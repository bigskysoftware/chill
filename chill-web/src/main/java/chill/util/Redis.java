package chill.util;

import chill.env.ChillEnv;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.net.URI;

public class Redis {
    private static JedisPool POOL = new JedisPool(URI.create(ChillEnv.getRedisURL()));

    public static Jedis get() {
        return POOL.getResource();
    }
}
