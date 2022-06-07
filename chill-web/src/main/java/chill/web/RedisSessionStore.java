package chill.web;

import chill.util.Redis;
import com.google.gson.Gson;
import org.eclipse.jetty.server.session.AbstractSessionDataStore;
import org.eclipse.jetty.server.session.SessionData;
import org.redisson.api.RBucket;

import java.util.Set;
import java.util.stream.Collectors;

public class RedisSessionStore extends AbstractSessionDataStore {

    private static final String SESSION_PREFIX = "jetty:sessions:";

    // TODO - move to factory/wrapper
    Gson gson = new Gson();

    @Override
    public void doStore(String id, SessionData data, long lastSaveTime) throws Exception {
        RBucket<String> bucket = getBucket(id);
        bucket.set(gson.toJson(data));
    }

    @Override
    public SessionData doLoad(String id) throws Exception {
        RBucket<String> bucket = getBucket(id);
        String session = String.valueOf(bucket.get());
        if (session != null) {
            return gson.fromJson(session, SessionData.class);
        } else {
            return null;
        }
    }

    private RBucket<String> getBucket(String id) {
        return Redis.REDISSON.getBucket(SESSION_PREFIX + id);
    }

    @Override
    public Set<String> doGetExpired(Set<String> candidates) {
        return candidates.stream()
                .map(s -> getBucket(s).get())
                .map(data -> gson.fromJson(data, SessionData.class))
                .filter(sessionData -> sessionData.isExpiredAt(System.currentTimeMillis()))
                .map(SessionData::getId).collect(Collectors.toSet());
    }

    @Override
    public boolean isPassivating() {
        return false;
    }

    @Override
    public boolean exists(String id) throws Exception {
        String session = getBucket(id).get();
        return session != null;
    }

    @Override
    public boolean delete(String id) throws Exception {
        return getBucket(id).delete();
    }
}
