package chill.utils;

import java.io.BufferedInputStream;
import java.util.Objects;

public class ResourceUtil {
    public static byte[] readResourceToBytes(Class<?> clazz, String resource) {
        var inner = Objects.requireNonNull(clazz.getResourceAsStream(resource), "resource not found: " + resource);
        try (var in = new BufferedInputStream(inner)) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
