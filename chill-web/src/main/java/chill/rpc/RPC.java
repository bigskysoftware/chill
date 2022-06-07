package chill.rpc;

import chill.util.Redis;
import chill.utils.ChillLogs;
import org.redisson.api.RRemoteService;

import java.util.concurrent.*;

import static chill.utils.TheMissingUtils.safely;

/**
 * This class provides a very simple Redis-based RPC mechanism, allowing you to register and invoke interfaces as
 * RPC object without any additional coordination.
 *
 * Pretty sweet.
 */
public class RPC {

    private static ChillLogs.LogCategory LOG = ChillLogs.get(RPC.class);
    private static final ConcurrentHashMap<Class, Object> MOCKS = new ConcurrentHashMap<>();

    //=======================================================
    // RPC Caller API
    //=======================================================

    public static <T> T make(Class<T> clazz) {
        Object mock = MOCKS.get(clazz);
        if (mock != null) {
            return (T) mock;
        } else {
            RRemoteService remoteService = Redis.REDISSON.getRemoteService(clazz.getName());
            return remoteService.get(clazz);
        }
    }

    //====================================================
    //  Mock API
    //====================================================
    public static <T> void registerMock(Class<T> clazz, T instance) {
        MOCKS.put(clazz, instance);
    }

    public static <T> void deRegisterMock(Class<T> clazz) {
        MOCKS.remove(clazz);
    }

    //=======================================================
    // RPC Calleer API
    //=======================================================

    /**
     * Registers a handler for a given interface
     *
     * @param clazz the interface to handle
     */
    public static <T> RPCService<T> implement(Class<T> clazz) {
        return safely(() -> {
            RRemoteService remoteService = Redis.REDISSON.getRemoteService(clazz.getName());
            return new RPCService(clazz, remoteService);
        });
    }

    public static class RPCService<T> {
        private final RRemoteService service;
        private final Class<T> clazz;

        public RPCService(Class<T> clazz, RRemoteService service) {
            this.service = service;
            this.clazz = clazz;
        }

        public RPCService<T> with(T implementation) {
            service.register(clazz, implementation);
            return this;
        }
    }

}
