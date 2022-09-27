package chill.utils;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import java.lang.reflect.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TheMissingUtils {

    public static final String EMPTY_STRING = "";
    private static final ForceThrower THROWER = generateForceThrower();

    public static <T> T safely(Callable<T> logic) {
        try {
            return logic.call();
        } catch (Exception e) {
            throw forceThrow(e);
        }
    }

    public static <T> T safelyOr(Callable<T> logic, T alt) {
        try {
            return logic.call();
        } catch (Exception e) {
            return alt;
        }
    }

    public static void safely(DangerousRunnable logic) {
        try {
            logic.run();
        } catch (Exception e) {
            throw forceThrow(e);
        }
    }

    public static String join(Object[] strings, String delimiter) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object string : strings) {
            if (first) {
                first = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(string);
        }
        return sb.toString();
    }

    public static String join(Iterable strings, String delimiter) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object string : strings) {
            if (first) {
                first = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(string);
        }
        return sb.toString();
    }

    public static <T> NiceList<T> concat(Collection<T> theOne, Collection<T> theOther) {
        NiceList<T> ts = new NiceList<T>(theOne);
        ts.addAll(theOther);
        return ts;
    }

    public static Map<String, Object> mapFrom(Object... args) {
        var returnMap = new LinkedHashMap<String, Object>();
        for (int i = 0; i < args.length; i = i + 2) {
            String name = String.valueOf(args[i]);
            if (i + 1 < args.length) {
                returnMap.put(name, args[i + 1]);
            } else {
                returnMap.put(name, null);
            }
        }
        return returnMap;
    }

    public static String snake(String str) {
        StringBuffer sb = new StringBuffer();
        char c = str.charAt(0);
        sb.append(Character.toLowerCase(c));
        for (int i = 1; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isUpperCase(ch)) {
                sb.append("_").append(Character.toLowerCase(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static String desnake(String str) {
        StringBuffer sb = new StringBuffer();
        char c = str.charAt(0);
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if ('_' == ch) {
                if (i + 1 < str.length()) {
                    i = i + 1;
                    sb.append(Character.toUpperCase(str.charAt(i)));
                }
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String decapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    public static long time(Runnable run) {
        long start = System.currentTimeMillis();
        run.run();
        long end = System.currentTimeMillis();
        return end - start;
    }

    public static NumberDoer n(int number) {
        return new NumberDoer(number);
    }

    public static NumberDoer fromZeroTo(int i) {
        return new NumberDoer(i);
    }

    public static void sleep(int millis) {
        safely(() -> Thread.sleep(millis));
    }

    public static <T> NiceList<T> filter(Iterable<T> ts, Predicate<? super T> predicate) {
        NiceList<T> result = new NiceList<>();
        for (T field : ts) {
            if (predicate.test(field)) {
                result.add(field);
            }
        }
        return result;
    }

    public static <R, T> NiceList<R> map(Iterable<T> ts, Function<? super T, ? extends R> mapper) {
        NiceList<R> result = new NiceList<>();
        for (T field : ts) {
            result.add(mapper.apply(field));
        }
        return result;
    }

    public static <T, C> NiceList<T> sortBy(Iterable<T> listToSort, Function<? super T, Comparable> toComp) {
        var niceList = new NiceList<T>(listToSort);
        niceList.sort((o1, o2) -> {
            var comp1 = toComp.apply(o1);
            var comp2 = toComp.apply(o2);
            if (comp1 == null && comp2 == null) {
                return 0;
            } else if (comp1 == null) {
                return -1;
            } else if (comp2 == null) {
                return 1;
            } else {
                return comp1.compareTo(comp2);
            }
        });
        return niceList;
    }

    public static <T> T first(List<T> lst) {
        if (lst.size() > 0) {
            return lst.get(0);
        } else {
            return null;
        }
    }

    public static <T> T first(Iterable<T> lst, Predicate<T> filter) {
        for (T t : lst) {
            if (filter.test(t)) {
                return t;
            }
        }
        return null;
    }

    public static <T> T newInstance(String className) {
        return safely(() -> newInstance(getClass(className)));
    }

    public static <T> Class<T> getClass(String className) {
        return (Class<T>) safely(() -> Class.forName(className));
    }

    public static <T> T newInstance(Class<T> otherRoutesFile) {
        T t = safely(() -> otherRoutesFile.newInstance());
        return t;
    }

    public static boolean isLowerCase(String name) {
        for (int i = 0; i < name.length(); i++) {
            if (!Character.isLowerCase(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static Object lazyStr(ToString toString) {
        return new Object() {
            @Override
            public String toString() {
                return toString.getString();
            }
        };
    }

    public static boolean isEmpty(String selector) {
        return selector == null || "".equals(selector);
    }

    public static Object getFieldValue(Object obj, String field) {
        if (obj != null) {
            Class<?> aClass = obj.getClass();
            Field actualField = getField(field, aClass);
            if (actualField == null) return null;
            return safely(() -> actualField.get(obj));
        }
        return null;
    }

    public static Field getField(String field, Class<?> aClass) {
        if (aClass == null) {
            return null;
        }
        try {
            Field actualField = aClass.getDeclaredField(field);
            actualField.setAccessible(true);
            return actualField;
        } catch (NoSuchFieldException e) {
            return getField(field, aClass.getSuperclass());
        }
    }

    public interface ToString {
        String getString();
    }

    public static <K, V> NiceMap<K, V> nice(Map<K, V> map) {
        return new NiceMap<>(map);
    }

    public static <T> NiceList<T> nice(T[] arr) {
        return new NiceList<>(arr);
    }

    public static <T> NiceList<T> nice(Iterable<T> arr) {
        return new NiceList<>(arr);
    }

    public static <T> NiceList<T> nice(Stream<T> stream) {
        return new NiceList<>(stream.collect(Collectors.toList()));
    }

    public interface DangerousRunnable {
        void run() throws Exception;

        default void runDangerously(){
            TheMissingUtils.safely(this::run);
        }
    }

    public interface SafeAutoCloseable extends AutoCloseable {
        @Override
        void close();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T adapt(Object it, Class<T>... to) {
        return (T) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), to, new InvocationHandler() {
            final Class itsClass = it.getClass();
            final ConcurrentHashMap<Method, Method> methodCache = new ConcurrentHashMap<>();

            @Override
            public Object invoke(Object proxy, Method interfaceMethod, Object[] args) throws Throwable {
                Method targetMethod = methodCache.computeIfAbsent(interfaceMethod, interfaceMethodOnMiss ->
                        safely(() -> itsClass.getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes())));
                if (targetMethod == null) {
                    throw new RuntimeException("Couldn't find a method on " + it + " that matches " + interfaceMethod.toString());
                } else {
                    return targetMethod.invoke(it, args);
                }
            }
        });
    }

    public static String toArgon2EncodedString(CharSequence rawPassword) {
        return toArgon2EncodedString(rawPassword, new Argon2Options());
    }

    public static String toArgon2EncodedString(CharSequence rawPassword, Argon2Options options) {
        byte[] hash = new byte[options.hashLength];
        Argon2Parameters params = options.toParameters();
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);
        generator.generateBytes(rawPassword.toString().toCharArray(), hash);
        return encodeHashAndParameters(hash, params);
    }

    public static RuntimeException forceThrow(Throwable throwable) {
        if (throwable instanceof InvocationTargetException) {
            THROWER.throwException(throwable.getCause());
        } else {
            THROWER.throwException(throwable);
        }
        return null;
    }

    private static ForceThrower generateForceThrower() {
        var tmpClass = new ClassLoader(TheMissingUtils.class.getClassLoader()) {
            public Class defineClass() {
                byte[] bytes = Base64.getDecoder().decode("yv66vgAAADQAEAEAHGNoaWxsL3V0aWxzL0ZvcmNlVGhyb3dlckltcGwHAAEBABBqYXZhL2xhbmcvT2JqZWN0BwADAQAYY2hpbGwvdXRpbHMvRm9yY2VUaHJvd2VyBwAFAQAVRm9yY2VUaHJvd2VySW1wbC5qYXZhAQAGPGluaXQ+AQADKClWDAAIAAkKAAQACgEADnRocm93RXhjZXB0aW9uAQAYKExqYXZhL2xhbmcvVGhyb3dhYmxlOylWAQAEQ29kZQEAClNvdXJjZUZpbGUAIQACAAQAAQAGAAAAAgABAAgACQABAA4AAAARAAEAAQAAAAUqtwALsQAAAAAAAQAMAA0AAQAOAAAADgABAAIAAAACK78AAAAAAAEADwAAAAIABw==");
                return defineClass("chill.utils.ForceThrowerImpl", bytes, 0, bytes.length);
            }
        }.defineClass();
        try {
            return (ForceThrower) tmpClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean matches(CharSequence rawPassword, String encodedPassword) {
        Argon2Parameters parameters = decodeParameters(encodedPassword);
        byte[] hash = decodeHash(encodedPassword);
        byte[] hashBytes = new byte[hash.length];
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(parameters);
        generator.generateBytes(rawPassword.toString().toCharArray(), hashBytes);
        return Arrays.equals(hash, hashBytes);
    }

    private static String encodeHashAndParameters(byte[] hash, Argon2Parameters parameters) {
        StringBuilder stringBuilder = new StringBuilder();

        // algorithm section
        stringBuilder.append("$");
        if (parameters.getType() == Argon2Parameters.ARGON2_d) {
            stringBuilder.append("argon2d");
        } else if (parameters.getType() == Argon2Parameters.ARGON2_id) {
            stringBuilder.append("argon2id");
        } else if (parameters.getType() == Argon2Parameters.ARGON2_i) {
            stringBuilder.append("argon2i");
        } else {
            throw new IllegalArgumentException("Unknown Argon Algorithm : " + parameters.getType());
        }

        // version section
        stringBuilder.append("$");
        stringBuilder.append("v=").append(parameters.getVersion());

        // options section
        stringBuilder.append("$");
        stringBuilder.append("m=").append(parameters.getMemory());
        stringBuilder.append(",");
        stringBuilder.append("t=").append(parameters.getIterations());
        stringBuilder.append(",");
        stringBuilder.append("p=").append(parameters.getLanes());

        // salt
        Base64.Encoder b64encoder = Base64.getEncoder().withoutPadding();
        stringBuilder.append("$");
        stringBuilder.append(b64encoder.encodeToString(parameters.getSalt()));

        // hash
        stringBuilder.append("$");
        stringBuilder.append(b64encoder.encodeToString(hash));
        return stringBuilder.toString();
    }

    private static byte[] decodeHash(String encodedPassword) {
        String[] strings = encodedPassword.split("\\$");
        String encodedHash = strings[strings.length - 1]; // get last element
        Base64.Decoder b64decoder = Base64.getDecoder();
        return b64decoder.decode(encodedHash);
    }

    private static Argon2Parameters decodeParameters(String encodedPassword) {
        var strings = new LinkedList<String>(Arrays.asList(encodedPassword.split("\\$")));

        strings.pop(); //discard leading blank

        // algorithm section
        var algorithm = strings.pop();
        var paramBuilder = new Argon2Parameters.Builder(getAlgorithmConstant(algorithm));

        // optional version section
        var versionOrPerfParams = strings.pop();
        if (versionOrPerfParams.startsWith("v=")) {
            paramBuilder.withVersion(parseIntArg(versionOrPerfParams));
            versionOrPerfParams = strings.pop();
        }

        // performance parameters section
        if (!versionOrPerfParams.matches("m=.*,t=.*,p=.*")) {
            throw new IllegalArgumentException("Perf params must be of form m=<int>,t=<int>,p=<int>");
        }
        var perfParameters = versionOrPerfParams.split(",");
        paramBuilder.withMemoryAsKB(parseIntArg(perfParameters[0]));
        paramBuilder.withIterations(parseIntArg(perfParameters[1]));
        paramBuilder.withParallelism(parseIntArg(perfParameters[2]));

        Base64.Decoder b64decoder = Base64.getDecoder();
        String salt = strings.pop();
        paramBuilder.withSalt(b64decoder.decode(salt));

        return paramBuilder.build();
    }

    private static int parseIntArg(String versionOrPerfParams1) {
        return Integer.parseInt(versionOrPerfParams1.substring(2));
    }

    private static int getAlgorithmConstant(String type) {
        if ("argon2d".equals(type)) {
            return Argon2Parameters.ARGON2_d;
        } else if ("argon2i".equals(type)) {
            return Argon2Parameters.ARGON2_i;
        } else if ("argon2id".equals(type)) {
            return Argon2Parameters.ARGON2_id;
        }
        throw new IllegalArgumentException("Unknown algorithm : " + type);
    }

    private static class Argon2Options {
        // defaults
        private int algorithm = Argon2Parameters.ARGON2_id;
        private int saltLength = 16;
        private int hashLength = 32;
        private int parallelism = 1;
        private int memory = 1 << 18;
        private int iterations = 3;

        public Argon2Options algorithm(int i) {
            algorithm = i;
            return this;
        }

        public Argon2Options saltLen(int i) {
            saltLength = i;
            return this;
        }

        public Argon2Options hashLen(int i) {
            hashLength = i;
            return this;
        }

        public Argon2Options parallelism(int i) {
            parallelism = i;
            return this;
        }

        public Argon2Options memory(int i) {
            memory = i;
            return this;
        }

        public Argon2Options iterations(int i) {
            iterations = i;
            return this;
        }

        public Argon2Parameters toParameters() {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[saltLength];
            random.nextBytes(salt);
            return new Argon2Parameters
                    .Builder(algorithm)
                    .withSalt(salt)
                    .withParallelism(parallelism)
                    .withMemoryAsKB(memory)
                    .withIterations(iterations)
                    .build();
        }
    }

    public static class NumberDoer {
        private final int n;

        public NumberDoer(int number) {
            this.n = number;
        }

        public void times(DangerousRunnable runnable) {
            for (int i = 0; i < n; i++) {
                safely(runnable);
            }
        }

        public void timesWithI(DangerousIntArg arg) {
            for (int i = 0; i < n; i++) {
                int finalI = i;
                safely(() -> arg.exec(finalI));
            }
        }

        public interface DangerousIntArg {
            void exec(int i);
        }
    }
}
