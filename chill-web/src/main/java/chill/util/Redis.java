package chill.util;

import chill.env.ChillEnv;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Function;

public class Redis {
    private static JedisPool POOL = new JedisPool(URI.create(ChillEnv.getRedisURL()));

    public static Jedis get() {
        return POOL.getResource();
    }

    public static <T> T eval(Function<Jedis, T> toExec) {
        try (var redis = get()) {
            return toExec.apply(redis);
        }
    }

    public static void exec(Consumer<Jedis> toExec) {
        try (var redis = get()) {
            toExec.accept(redis);
        }
    }

    public static JedisPool getPool() {
        return POOL;
    }

}
