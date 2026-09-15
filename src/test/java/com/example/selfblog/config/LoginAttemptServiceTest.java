package com.example.selfblog.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {
    @Test
    void blocksAfterThreeFailuresAndClearsAfterSuccess() {
        LoginAttemptService service = new LoginAttemptService(
                Clock.fixed(Instant.parse("2026-09-09T00:00:00Z"), ZoneOffset.UTC));

        assertFalse(service.recordFailure("192.0.2.1").blocked());
        assertFalse(service.recordFailure("192.0.2.1").blocked());
        assertTrue(service.recordFailure("192.0.2.1").blocked());
        assertTrue(service.isBlocked("192.0.2.1"));

        service.recordSuccess("192.0.2.1");
        assertFalse(service.isBlocked("192.0.2.1"));
    }
}
