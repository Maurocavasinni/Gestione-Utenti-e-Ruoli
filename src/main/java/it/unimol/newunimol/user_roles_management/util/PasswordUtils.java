package it.unimol.newunimol.user_roles_management.util;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.stereotype.Component;

@Component
public final class PasswordUtils {
    private static final Argon2 argon2 = Argon2Factory.create();

    public static String hashPassword(String password) {
        char[] passwordChars = password.toCharArray();

        try {
            return argon2.hash(2, 1024, 1, passwordChars);
        } finally {
            argon2.wipeArray(passwordChars);
        }
    }
    
    public static boolean verificaPassword(String hash, String password) {
        char[] passwordChars = password.toCharArray();

        try {
            return argon2.verify(hash, passwordChars);
        } finally {
            argon2.wipeArray(passwordChars);
        }
    }
}
