package com.sentinel.core.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record Credentials(
            @NotBlank @Email @Size(max = 320) String email,
            @NotBlank @Size(min = 12, max = 72) String password) {
        @AssertTrue(message = "password must be at most 72 UTF-8 bytes")
        public boolean hasValidPasswordByteLength() {
            return password != null && password.getBytes(StandardCharsets.UTF_8).length <= 72;
        }
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record PublicUser(UUID id, String email, Instant createdAt) {
        static PublicUser from(User user) {
            return new PublicUser(user.id, user.email, user.createdAt);
        }
    }

    public record TokenResponse(String accessToken, String refreshToken, String tokenType,
                                long expiresIn, PublicUser user) {
    }

    public record MeResponse(UUID id, String email, Instant createdAt) {
        static MeResponse from(User user) {
            return new MeResponse(user.id, user.email, user.createdAt);
        }
    }
}