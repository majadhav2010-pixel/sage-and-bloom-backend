package com.example.ecommerce.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class LoginAttemptService {

    public static final int MAX_ATTEMPTS = 5;
    public static final long LOCKOUT_DURATION_SECONDS = 900; // 15 minutes

    private final Map<String, AttemptInfo> attemptsCache = new ConcurrentHashMap<>();

    private static class AttemptInfo {
        int attempts;
        Instant lockoutUntil;

        AttemptInfo(int attempts, Instant lockoutUntil) {
            this.attempts = attempts;
            this.lockoutUntil = lockoutUntil;
        }
    }

    public void loginSucceeded(String key) {
        attemptsCache.remove(key.toLowerCase().trim());
    }

    public void loginFailed(String key) {
        String cleanKey = key.toLowerCase().trim();
        AttemptInfo info = attemptsCache.getOrDefault(cleanKey, new AttemptInfo(0, null));
        info.attempts++;
        if (info.attempts >= MAX_ATTEMPTS) {
            info.lockoutUntil = Instant.now().plusSeconds(LOCKOUT_DURATION_SECONDS);
            log.warn("Account/IP locked out due to multiple failed login attempts: {}", cleanKey);
        }
        attemptsCache.put(cleanKey, info);
    }

    public boolean isBlocked(String key) {
        String cleanKey = key.toLowerCase().trim();
        AttemptInfo info = attemptsCache.get(cleanKey);
        if (info == null) {
            return false;
        }

        if (info.lockoutUntil != null) {
            if (Instant.now().isBefore(info.lockoutUntil)) {
                return true;
            } else {
                // Lockout expired
                attemptsCache.remove(cleanKey);
                return false;
            }
        }

        return false;
    }

    public long getRemainingLockoutSeconds(String key) {
        String cleanKey = key.toLowerCase().trim();
        AttemptInfo info = attemptsCache.get(cleanKey);
        if (info != null && info.lockoutUntil != null) {
            long remaining = info.lockoutUntil.getEpochSecond() - Instant.now().getEpochSecond();
            return Math.max(0, remaining);
        }
        return 0;
    }
}
