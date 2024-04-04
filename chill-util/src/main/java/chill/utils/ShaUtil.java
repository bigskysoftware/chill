package chill.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ShaUtil {
    public static String sha1(byte[] data) {
        try {
            var md = MessageDigest.getInstance("SHA-1");
            StringBuilder hex = new StringBuilder(new BigInteger(1, md.digest(data)).toString(16));
            while (hex.length() < 40) {
                hex.insert(0, "0");
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String sha1(String data) {
        return sha1(data.getBytes());
    }
}
