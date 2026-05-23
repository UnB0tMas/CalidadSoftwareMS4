// ruta: src/main/java/com/upsjb/ms4/util/HashUtil.java
package com.upsjb.ms4.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class HashUtil {

    private static final String SHA_256 = "SHA-256";

    private HashUtil() {
    }

    public static String sha256(String value) {
        return sha256(value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8));
    }

    public static String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            return HexFormat.of().formatHex(digest.digest(bytes == null ? new byte[0] : bytes));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Algoritmo SHA-256 no disponible.", ex);
        }
    }

    public static String sha256CanonicalJson(Object value) {
        return sha256(JsonUtil.toCanonicalJson(value));
    }

    public static boolean matchesSha256(String rawValue, String expectedHash) {
        if (expectedHash == null || expectedHash.isBlank()) {
            return false;
        }

        byte[] actual = sha256(rawValue).getBytes(StandardCharsets.UTF_8);
        byte[] expected = expectedHash.trim().toLowerCase().getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(actual, expected);
    }
}