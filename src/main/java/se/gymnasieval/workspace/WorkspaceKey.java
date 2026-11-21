package se.gymnasieval.workspace;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
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

    /**
     * Läsnyckeln räknas fram ur redigeringsnyckeln. Den som har redigeringslänken kan alltid
     * visa läslänken, men SHA-256 går inte att räkna baklänges: läsnyckeln avslöjar aldrig
     * redigeringsnyckeln. Prefixet "view:" gör att läsnyckeln inte är samma sak som hashen
     * som sparas i databasen för redigeringsnyckeln.
     */
    static String deriveViewKey(String editKey) {
        byte[] digest = sha256("view:" + editKey);
        return ENCODER.encodeToString(Arrays.copyOf(digest, KEY_BYTES));
    }

    static boolean isValidFormat(String key) {
        return key != null && FORMAT.matcher(key).matches();
    }

    /** SHA-256 av nyckeln som hex (64 tecken). Det är bara detta som sparas i databasen. */
    static String hash(String key) {
        return HexFormat.of().formatHex(sha256(key));
    }

    private static byte[] sha256(String key) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.US_ASCII));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 saknas i JVM:en", e); // finns alltid i praktiken
        }
    }
}
