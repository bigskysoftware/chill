import redis.clients.jedis.JedisPool;

public class GlobalChillJobManager implements ChillJobManager {
    private static GlobalChillJobManager instance;

    public static GlobalChillJobManager getInstance() {
        if (instance == null) {
            instance = new GlobalChillJobManager();
        }
        return instance;
    }

    public static void setInstance(GlobalChillJobManager instance) {
        GlobalChillJobManager.instance = instance;
    }

    protected final JedisPool jedisPool;
    protected final String redisKey;

    public GlobalChillJobManager() {
        this(null, null);
    }

    public GlobalChillJobManager(JedisPool jedisPool, String redisKey) {
        this.jedisPool = jedisPool == null ? new JedisPool() : jedisPool;
        this.redisKey = redisKey == null ? "chill-job" : redisKey;
    }

    public GlobalChillJobManager(JedisPool jedisPool) {
        this(jedisPool, null);
    }

    public GlobalChillJobManager(String redisKey) {
        this(null, redisKey);
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    @Override
    public ChillJob getJob(ChillJob.Id id) {
        try (var jedis = jedisPool.getResource()) {
            var json = jedis.hget(redisKey, id.toString());
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public ChillJob.Id submit(ChillJob job) {
        ensureNotSubmitted(job);
        try (var jedis = jedisPool.getResource()) {
            var key = jedis.hincrBy(redisKey, "$chill:id", 1);
            // job to json
            ChillJob.Id id = new ChillJob.Id(key);
            job.setId(id);
            jedis.hset(redisKey, id.toString(), "json");
            return id;
        }
    }

    @Override
    public void fetch(ChillJob job) {
        ensureSubmitted(job);
        try (var jedis = jedisPool.getResource()) {
            var json = jedis.hget(redisKey, job.getId().toString());
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void cancel(ChillJob job) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(ChillJob job) {
        try (var jedis = jedisPool.getResource()) {
            var json = jedis.hget(redisKey, job.getId().toString());
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void ensureSubmitted(ChillJob job) throws AssertionError {
        if (job.getId() == null) {
            throw new AssertionError("Job is not submitted");
        }
    }

    @Override
    public void ensureNotSubmitted(ChillJob job) throws AssertionError {
        if (job.getId() != null) {
            throw new AssertionError("Job is already submitted");
        }
    }
}
