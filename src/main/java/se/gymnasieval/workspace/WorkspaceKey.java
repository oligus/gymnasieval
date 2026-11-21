package se.gymnasieval.workspace;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Skapar och hashar hemliga nycklar för arbetsytor.
 * 16 slumpade byte (128 bitar) kodas som base64url utan utfyllnad, vilket ger 22 tecken
 * som fungerar direkt i en URL.
 */
final class WorkspaceKey {

    private static final int KEY_BYTES = 16;
    private static final Pattern FORMAT = Pattern.compile("[A-Za-z0-9_-]{22}");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private WorkspaceKey() {
    }

    static String generate() {
        byte[] bytes = new byte[KEY_BYTES];
        RANDOM.nextBytes(bytes);
        return ENCODER.encodeToString(bytes);
    }

    static boolean isValidFormat(String key) {
        return key != null && FORMAT.matcher(key).matches();
    }

    /** SHA-256 av nyckeln. Det är bara detta som sparas i databasen. */
    static byte[] hash(String key) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.US_ASCII));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 saknas i JVM:en", e); // finns alltid i praktiken
        }
    }
}
