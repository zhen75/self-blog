package com.example.selfblog.config;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {
    private static final int MAX_FAILURES = 3;
    private static final Duration ATTEMPT_WINDOW = Duration.ofHours(1);
    private static final Duration[] LOCK_DURATIONS = {
            Duration.ofHours(1),
            Duration.ofDays(1),
            Duration.ofDays(30)
    };

    private static final long MAX_TRACKED_IPS = 50_000;
    private final Cache<String, AttemptState> attempts = Caffeine.newBuilder()
            .maximumSize(MAX_TRACKED_IPS)
            .expireAfterAccess(Duration.ofDays(31))
            .build();
    private final Clock clock;

    public LoginAttemptService() {
        this(Clock.systemUTC());
    }

    LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public boolean isBlocked(String ipAddress) {
        AttemptState state = attempts.getIfPresent(ipAddress);
        return state != null && state.blockedUntil() != null
                && clock.instant().isBefore(state.blockedUntil());
    }

    public FailureResult recordFailure(String ipAddress) {
        Instant now = clock.instant();
        FailureResult[] result = new FailureResult[1];

        attempts.asMap().compute(ipAddress, (ip, previous) -> {
            AttemptState current = previous == null ? AttemptState.initial(now) : previous;
            boolean windowExpired = current.windowStarted() == null
                    || !now.isBefore(current.windowStarted().plus(ATTEMPT_WINDOW));
            int failures = windowExpired ? 1 : current.failures() + 1;
            Instant windowStarted = windowExpired ? now : current.windowStarted();

            if (failures >= MAX_FAILURES) {
                int level = Math.min(current.penaltyLevel(), LOCK_DURATIONS.length - 1);
                Instant blockedUntil = now.plus(LOCK_DURATIONS[level]);
                result[0] = new FailureResult(true, 0, blockedUntil);
                return new AttemptState(0, Math.min(level + 1, LOCK_DURATIONS.length - 1), null, blockedUntil);
            }

            result[0] = new FailureResult(false, MAX_FAILURES - failures, null);
            return new AttemptState(failures, current.penaltyLevel(), windowStarted, null);
        });

        return result[0];
    }

    public void recordSuccess(String ipAddress) {
        attempts.invalidate(ipAddress);
    }

    public record FailureResult(boolean blocked, int remainingAttempts, Instant blockedUntil) {
    }

    private record AttemptState(int failures, int penaltyLevel, Instant windowStarted, Instant blockedUntil) {
        private static AttemptState initial(Instant now) {
            return new AttemptState(0, 0, now, null);
        }
    }
}
