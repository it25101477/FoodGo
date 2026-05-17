package com.foodgo.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtil {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String storedPassword) {
        if (storedPassword == null || storedPassword.isEmpty()) {
            return false;
        }
        if (!storedPassword.startsWith("$2a$") && !storedPassword.startsWith("$2b$")) {
            return rawPassword.equals(storedPassword);
        }
        return encoder.matches(rawPassword, storedPassword);
    }
}
