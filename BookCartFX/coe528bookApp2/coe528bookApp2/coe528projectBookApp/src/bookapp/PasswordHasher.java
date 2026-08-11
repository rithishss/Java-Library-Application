package bookapp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// PasswordHasher.java
// Salted PBKDF2, using only what the JDK already provides so the project picks
// up no new dependency.
//
// A stored password looks like:
//
//     pbkdf2$100000$<salt base64>$<hash base64>
//
// The salt is per-password, so two people choosing the same password do not end
// up with the same stored value, and the iteration count is written into the
// string so it can be raised later without invalidating existing passwords.
public class PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2";
    private static final int ITERATIONS = 100000;
    private static final int KEY_BITS = 256;
    private static final int SALT_BYTES = 16;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(String plainPassword) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] key = derive(plainPassword, salt, ITERATIONS);

        return PREFIX + "$" + ITERATIONS
                + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(key);
    }

    // True once a stored value is in the format above rather than plain text.
    public static boolean isHashed(String stored) {
        return stored != null && stored.startsWith(PREFIX + "$");
    }

    // Anything not yet hashed is compared directly, so a database still holding
    // plain text keeps working until Database has migrated it.
    public static boolean matches(String plainPassword, String stored) {
        if (plainPassword == null || stored == null) {
            return false;
        }
        if (!isHashed(stored)) {
            return plainPassword.equals(stored);
        }

        String[] parts = stored.split("\\$");
        if (parts.length != 4) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            // Constant-time, so a wrong password cannot be narrowed down by
            // timing how long the comparison took.
            return MessageDigest.isEqual(expected, derive(plainPassword, salt, iterations));
        } catch (IllegalArgumentException e) {
            System.out.println("Malformed stored password: " + e.getMessage());
            return false;
        }
    }

    private static byte[] derive(String plainPassword, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(plainPassword.toCharArray(), salt, iterations, KEY_BITS);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // Both algorithms are required of every JDK, so this cannot happen
            // in practice. Failing loudly beats returning something guessable.
            throw new IllegalStateException("Cannot hash password: " + e.getMessage(), e);
        }
    }
}
