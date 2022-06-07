package chill.rpc;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.*;

/**
 * This class provides a very simple Redis-based RPC mechanism, allowing you to register and invoke interfaces as
 * RPC object without any additional coordination.
 *
 * Pretty sweet.
 */
public class RPC {

    private static Logger LOG = LoggerFactory.getLogger(RPC.class);

    // Responses must be consumed within five minutes TODO: make parameterized?
    public static final int RESPONSE_EXPIRATION_SECONDS = 5 * 60;
    private static final ConcurrentHashMap<Class, Object> MOCKS = new ConcurrentHashMap<>();

    // default Redis host is localhost
    private static String defaultRedisUrl = "";

    // default RPC caller timeout is 1 minute
    private static int defaultInvocationTimeout = 60;

    // default RPC callee parameters
    private static int defaultListenerThreads = 5;
    private static int defaultListenerTimeout = 20;
    private static int defaultMaxResponseThreads = Integer.MAX_VALUE;

    public static String getDefaultRedisUrl() {
        return defaultRedisUrl;
    }

    public static void setDefaultRedisUrl(String defaultRedisUrl) {
        RPC.defaultRedisUrl = defaultRedisUrl;
    }

    public static int getDefaultInvocationTimeout() {
        return defaultInvocationTimeout;
    }

    public static void setDefaultInvocationTimeout(int defaultInvocationTimeout) {
        RPC.defaultInvocationTimeout = defaultInvocationTimeout;
    }

    public static int getDefaultListenerThreads() {
        return defaultListenerThreads;
    }

    public static void setDefaultListenerThreads(int defaultListenerThreads) {
        RPC.defaultListenerThreads = defaultListenerThreads;
    }

    public static int getDefaultListenerTimeout() {
        return defaultListenerTimeout;
    }

    public static void setDefaultListenerTimeout(int defaultListenerTimeout) {
        RPC.defaultListenerTimeout = defaultListenerTimeout;
    }

    public static int getDefaultMaxResponseThreads() {
        return defaultMaxResponseThreads;
    }

    public static void setDefaultMaxResponseThreads(int defaultMaxResponseThreads) {
        RPC.defaultMaxResponseThreads = defaultMaxResponseThreads;
    }

    //=======================================================
    // Constants For Message API
    //=======================================================
    public static final String CHILL_RPC_PREFIX = "chillrpc:";
    public static final String METHOD_SLOT = "method";
    public static final String ID_SLOT = "id";
    public static final String ARG_SLOT_PREFIX = "arg";
    public static final String INVOCATION_ID_SEPARATOR = ":";
    public static final String RETURN_SLOT = "returned";
    public static final String EXCEPTION_TYPE_SLOT = "exception";
    public static final String EXCEPTION_MESSAGE_SLOT = "message";

    //=======================================================
    // RPC Caller API
    //=======================================================

    /**
     * Returns a new instance of the interface that will invoke the methods over RPC through Redis.
     * This method uses the {@link #defaultInvocationTimeout} field for the timeout
     *
     * @param sampleInterfaceClass the interface to invoke over RPC
     * @param <T>                  same
     * @return an instance of that interface that will invoke methods over RPC
     */
    public static <T> T make(Class<T> sampleInterfaceClass) {
        return new RPCClientImpl(defaultInvocationTimeout).make(sampleInterfaceClass);
    }

    /**
     * Returns a new Builder on which #make can be invoked, with the specified timeout.  This allows for
     * a custom timeout for this particular RPC invocation
     *
     * @param timeout the RPC timeout
     * @return a new RPCImpl with the given timeout set as the default
     */
    public static RPCClientImpl withTimeout(int timeout) {
        return new RPCClientImpl(timeout);
    }

    /**
     * A simple wrapper class that holds a timeout for timing out the RPC call and that can make new proxy
     * instance that dispatch methods over RPC
     */
    public static class RPCClientImpl {

        // The timeout associated with this instance
        private final int timeoutInSeconds;
        private String redisUrl;

        private RPCClientImpl(int timeoutInSeconds) {
            this.timeoutInSeconds = timeoutInSeconds;
            this.redisUrl = getDefaultRedisUrl();
        }

        public RPCClientImpl withRedisUrl(String redisUrl) {
            this.redisUrl = redisUrl;
            return this;
        }

        public <T> T make(Class<T> sampleInterfaceClass) {
            return (T) Proxy.newProxyInstance(sampleInterfaceClass.getClassLoader(),
                    new Class[]{sampleInterfaceClass},
                    (proxy, method, args) -> invoke(redisUrl, sampleInterfaceClass, method.getName(), timeoutInSeconds, args));
        }

    }

    /**
     * The crux RPC invocation method.  This method will invoke the given RPC method through Redis by generating
     * a UUID representing this invocation, serializing the invocation information and posting it to the appropriate
     * redis queue for this class.  It will then wait for a response from the remote system by waiting on a redis
     * queue based on the naming conventions.
     *
     * @param <T>        the response type
     * @param redisUrl
     * @param clazz      the interface to invoke the method on
     * @param methodName the method name to invoke remotely
     * @param timeout    the time in seconds to wait for a response
     * @param args       the arguments to be serialized and sent to the remote system
     * @return the response from the remote system
     * @throws
     */
    public static <T> T invoke(String redisUrl, Class clazz, String methodName, int timeout, Object... args) {

        LOG.info("Invoking {}::{} with timeout {} and args {}", clazz, methodName, timeout, args);

        // resolve the method
        Method method = findMethod(clazz, methodName);

        Object mockImplementation = MOCKS.get(clazz);
        if (mockImplementation != null) {
            LOG.info("Mock implementation found for {}::{}, using that instead.", clazz, methodName);
            try {
                return (T) method.invoke(mockImplementation, args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // determine the queue for this class
        var queueName = getQueueNameForClass(clazz);

        // generate a UUID for this invocation
        var invocationID = UUID.randomUUID().toString();

        // create a map of the method to be invoked, the UUID (for the response) and
        // the args, serialized as JSON
        HashMap<String, Object> map = new HashMap<>();
        map.put(METHOD_SLOT, methodName);
        map.put(ID_SLOT, invocationID);

        Gson gson = new Gson();
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            String argName = ARG_SLOT_PREFIX + i;
            if (i < args.length) {
                map.put(argName, gson.toJson(args[i]));
            } else {
                map.put(argName, null);
            }
        }

        // Serialize the whole map
        String argsAsJSON = gson.toJson(map);

        LOG.info("Method payload for invocation ID {}: {}.", invocationID, argsAsJSON);
        Jedis jedis = new Jedis(redisUrl);

        // invoke the method by pushing it onto the method queue
        jedis.rpush(queueName, argsAsJSON);

        // wait for a response from the given response queue
        List<String> blpop = jedis.blpop(timeout, queueName + INVOCATION_ID_SEPARATOR + invocationID);

        // if we get null back that means we timed out
        if (blpop == null) {
            RuntimeException runtimeException = new RuntimeException("The rpc call " + clazz.getName() + "::" + methodName + " timed out");
            LOG.error("Chill RPC Timeout: ", runtimeException);
            throw runtimeException;
        }

        // the response payload is in the second slot
        String response = blpop.get(1);
        LOG.info("Response payload for invocation ID {}: {}.", invocationID, response);
        Map responseMap = gson.fromJson(response, Map.class);

        // if it starts w/ the exception string, handle the exception
        if (responseMap.containsKey(EXCEPTION_MESSAGE_SLOT)) {
            handleClientSideException(clazz, methodName, gson, responseMap);
        }
        // otherwise deserialize it according to the return type of this method
        String returnStr = String.valueOf(responseMap.get(RETURN_SLOT));
        return (T) gson.fromJson(returnStr, method.getReturnType());
    }


    private static void handleClientSideException(Class clazz, String methodName, Gson gson, Map exceptionMap) {
        Throwable exception;
        try {
            // attempt to instantiate the exception thrown on the other side of the wire
            Class<?> aClass = Class.forName(String.valueOf(exceptionMap.get(EXCEPTION_TYPE_SLOT)));
            Constructor<?> constructor = aClass.getConstructor(String.class);
            exception = (Throwable) constructor.newInstance(String.valueOf(exceptionMap.get(EXCEPTION_MESSAGE_SLOT)));
        } catch (Exception e) {
            // if that doesn't work, just throw a generic runtime exception (TODO: make a specific type of exception?)
            RuntimeException runtimeException = new RuntimeException("An exception occurred during the the rpc call " + clazz.getName() + "::" + methodName + exceptionMap.toString());
            LOG.error("Remote exception during RPC: ", runtimeException);
            throw runtimeException;
        }

        LOG.error("Remote exception during RPC: ", exception);
        // if the instantiated exception is a RuntimeException toss it
        if (exception instanceof RuntimeException rte) {
            throw rte;
            // otherwise wrap (TODO: we could depend on TheMissingUtilities and force throw here)
        } else {
            throw new RuntimeException("An exception occurred during the the rpc call " + clazz.getName() + "::" + methodName, exception);
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
        return new RPCService<T>(clazz);
    }

    private static Method findMethod(Class clazz, String methodName) {
        Optional<Method> first = Arrays.stream(clazz.getMethods()).filter(method -> method.getName().equals(methodName)).findFirst();
        Method method = first.get();
        return method;
    }

    private static String getQueueNameForClass(Class clazz) {
        return CHILL_RPC_PREFIX + clazz.getName();
    }

    public static <T> Thread async(Callable<T> rpcOp, Invocable<T> callback) {
        Thread thread = new Thread(() -> {
            try {
                T result = rpcOp.call();
                callback.call(result);
            } catch (Exception e) {
                //TODO API?
                throw new RuntimeException(e);
            }
        });
        thread.start();
        return thread;
    }

    public interface Invocable<T> {
        void call(T result);
    }

    public static class RPCService<T> {

        private ExecutorService listenerService;
        private ExecutorService executorService;
        private final Class<T> clazz;

        // config
        private int listenerThreads = getDefaultListenerThreads();
        private int listenTimeout = getDefaultListenerTimeout();
        private int maxResponseThreads = getDefaultListenerTimeout();
        private String redisURL = getDefaultRedisUrl();

        private Object instance;

        public RPCService(Class<T> clazz) {
            this.clazz = clazz;
        }

        public RPCService<T> withListenerThreads(int count) {
            listenerThreads = count;
            return this;
        }

        public RPCService<T> withListenerTimeout(int seconds) {
            listenTimeout = seconds;
            return this;
        }

        public RPCService<T> withMaxResponseThreads(int count) {
            maxResponseThreads = count;
            return this;
        }

        public RPCService<T> with(T implementation) {
            if (this.instance != null) {
                throw new IllegalStateException("Service already started!");
            }
            this.instance = implementation;
            start();
            return this;
        }

        private RPCService<T> start() {
            listenerService = Executors.newFixedThreadPool(listenerThreads);

            executorService = new ThreadPoolExecutor(0,
                    maxResponseThreads, 60L, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>());

            Runnable runnable = new RPCQueueListener(redisURL, clazz, listenTimeout, instance, executorService);
            for (int i = 0; i < listenerThreads; i++) {
                listenerService.submit(runnable);
            }
            return this;
        }

        public void shutdown() {
            listenerService.shutdown();
            executorService.shutdown();
        }

        public List<Runnable> shutdownNow() {
            List<Runnable> runnables = new ArrayList<>(listenerService.shutdownNow());
            runnables.addAll(executorService.shutdownNow());
            return runnables;
        }

        public boolean isShutdown() {
            return listenerService.isShutdown() && executorService.isShutdown();
        }

        public boolean isTerminated() {
            return listenerService.isTerminated() && executorService.isTerminated();
        }

    }

    private static class RPCQueueListener<T> implements Runnable {

        private final String redisUrl;
        private final Class<T> clazz;
        private final int listenTimeout;
        private Object handler;
        private final ExecutorService executorService;

        public RPCQueueListener(String redisUrl, Class<T> clazz, int listenTimeout, Object handler, ExecutorService functionHandlerExecutor) {
            this.redisUrl = redisUrl;
            this.clazz = clazz;
            this.listenTimeout = listenTimeout;
            this.executorService = functionHandlerExecutor;
            this.handler = handler;
        }

        @Override
        public void run() {
            Jedis jedis = new Jedis(getDefaultRedisUrl());
            var queueName = getQueueNameForClass(clazz);

            while (true) {
                // wait for invocation of this interface
                try {
                    // give thread a chance to interrupt
                    Thread.sleep(0);
                    // the response slot is the queue name followed by a colon followed by the invocation UUID
                    LOG.info("RPC Server: Waiting for invocation of {}", queueName);
                    var messageFromInvoker = jedis.blpop(listenTimeout, queueName);
                    if (messageFromInvoker != null) {
                        executorService.execute(new RPCFunctionInvocationHandler<>(redisUrl, handler, clazz, messageFromInvoker));
                    } else {
                        LOG.info("Listener for {} timed out, re-listening", queueName);
                    }
                } catch (InterruptedException e) {
                    LOG.error("RPC Server: InterruptedException", e);
                    break; // thread has been killed
                }
            }
        }

    }

    private static class RPCFunctionInvocationHandler<T> implements Runnable {

        private final Object instance;
        private final Class<T> clazz;
        private final List<String> messageFromInvoker;
        private final String redisUrl;

        public RPCFunctionInvocationHandler(String redisUrl, Object instance, Class<T> clazz, List<String> messageFromInvoker) {
            this.redisUrl = redisUrl;
            this.instance = instance;
            this.clazz = clazz;
            this.messageFromInvoker = messageFromInvoker;
        }

        public void run() {
            var queueName = getQueueNameForClass(clazz);
            Jedis jedis = new Jedis(redisUrl);
            String responseRedisKey = null;
            Gson gson = new Gson();
            try {
                // listen for an invocation
                // rpc payload is in slot 1
                String rpcPayload = messageFromInvoker.get(1);
                Map rpcPayloadMap = gson.fromJson(rpcPayload, Map.class);

                LOG.info("RPC Server: Handling invocation {}", rpcPayload);

                // get the method out
                String methodName = String.valueOf(rpcPayloadMap.get(METHOD_SLOT));

                // resolve it
                Method method = findMethod(clazz, methodName);

                // get out the invocation ID
                String invocationId = (String) rpcPayloadMap.get(ID_SLOT);

                responseRedisKey = queueName + INVOCATION_ID_SEPARATOR + invocationId;

                // collect the parameters and deseralize them based on the method parameters
                Class<?>[] parameterTypes = method.getParameterTypes();
                ArrayList args = new ArrayList(parameterTypes.length);
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> parameterType = parameterTypes[i];
                    Object argVal = rpcPayloadMap.get(ARG_SLOT_PREFIX + i);
                    if (argVal != null) {
                        args.add(gson.fromJson(String.valueOf(argVal), parameterType));
                    }
                }

                // invoke the method
                Object result = method.invoke(instance, args.toArray());
                HashMap responseMap = new HashMap<>();
                String resultAsJSON = gson.toJson(result);
                responseMap.put(RETURN_SLOT, resultAsJSON);

                String responseMapStr = gson.toJson(responseMap);

                LOG.info("RPC Server: Responding to {} with {}", responseRedisKey, responseMapStr);

                // push the response into the response queue for this invocation
                jedis.rpush(responseRedisKey, responseMapStr);

                // expire the response after a certain amount of time in case the caller died
                jedis.expire(responseRedisKey, RESPONSE_EXPIRATION_SECONDS);
            } catch (InvocationTargetException ite) {
                // unwrap InvocationTargetExceptions so we get the real exception that happened
                Throwable e = ite.getCause();
                if (responseRedisKey != null) {
                    HashMap exceptionInfo = new HashMap<>();
                    exceptionInfo.put(EXCEPTION_TYPE_SLOT, e.getClass().getName());
                    exceptionInfo.put(EXCEPTION_MESSAGE_SLOT, e.getMessage());
                    String exceptionPayload = gson.toJson(exceptionInfo);
                    LOG.error("RPC Server: passing exception back to invoker: {}", exceptionPayload);
                    jedis.rpush(responseRedisKey, exceptionPayload);
                    jedis.expire(responseRedisKey, RESPONSE_EXPIRATION_SECONDS);
                } else {
                    RuntimeException runtimeException = new RuntimeException(ite);
                    LOG.error("RPC Server: Unhandled exception", runtimeException);
                    throw runtimeException;
                }
            } catch (Exception e) {
                // Probably an error in the implementation, return it anyway
                if (responseRedisKey != null) {
                    HashMap exceptionInfo = new HashMap<>();
                    exceptionInfo.put(EXCEPTION_TYPE_SLOT, e.getClass().getName());
                    exceptionInfo.put(EXCEPTION_MESSAGE_SLOT, e.getMessage());
                    String exceptionPayload = gson.toJson(exceptionInfo);
                    LOG.error("RPC Server: passing exception back to invoker: {}", exceptionPayload);
                    jedis.rpush(responseRedisKey, exceptionPayload);
                    jedis.expire(responseRedisKey, RESPONSE_EXPIRATION_SECONDS);
                } else {
                    RuntimeException runtimeException = new RuntimeException(e);
                    LOG.error("RPC Server: Unhandled exception", runtimeException);
                    throw runtimeException;
                }
            }
        }

    }

}
