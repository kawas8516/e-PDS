package com.ration.util;

import java.util.UUID;

public final class CSRFUtil {

    private CSRFUtil() {
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public static boolean validateToken(String sessionToken, String requestToken) {
        return sessionToken != null && sessionToken.equals(requestToken);
    }
}
