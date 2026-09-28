package com.sentinel.core.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.elytron.security.common.BcryptUtil;
import org.junit.jupiter.api.Test;

class AuthServiceTest {
    @Test
    void normalizesEmailForCaseInsensitiveIdentity() {
        assertEquals("person@example.com", AuthService.normalizeEmail("  Person@Example.COM "));
    }

    @Test
    void passwordIsStoredAsVerifiableBcryptHash() {
        String hash = BcryptUtil.bcryptHash("a-strong-password");

        assertNotEquals("a-strong-password", hash);
        assertTrue(BcryptUtil.matches("a-strong-password", hash));
    }

    @Test
    void refreshHashIsDeterministicAndNotTheRawToken() {
        assertEquals(AuthService.sha256("opaque-token"), AuthService.sha256("opaque-token"));
        assertNotEquals("opaque-token", AuthService.sha256("opaque-token"));
    }

    @Test
    void rejectsPasswordsLongerThanBcryptUtf8Limit() {
        String password = "a".repeat(71) + "é";

        assertTrue(password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72);
        assertTrue(!new AuthDtos.Credentials("person@example.com", password).hasValidPasswordByteLength());
    }
}