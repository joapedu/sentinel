package com.sentinel.core.auth;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.security.SecureRandom;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AuthService {
    private static final SecureRandom RANDOM = new SecureRandom();

    @ConfigProperty(name = "sentinel.auth.access-token-minutes")
    long accessTokenMinutes;

    @ConfigProperty(name = "sentinel.auth.refresh-token-days")
    long refreshTokenDays;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "mp.jwt.verify.audiences")
    String audience;

    @Transactional
    public AuthDtos.TokenResponse register(AuthDtos.Credentials credentials) {
        String email = normalizeEmail(credentials.email());
        if (User.find("email", email).firstResult() != null) {
            throw new EmailAlreadyRegisteredException();
        }
        User user = new User();
        user.email = email;
        user.passwordHash = BcryptUtil.bcryptHash(credentials.password());
        user.persist();
        return issueTokens(user);
    }

    @Transactional
    public AuthDtos.TokenResponse login(AuthDtos.Credentials credentials) {
        User user = User.find("email", normalizeEmail(credentials.email())).firstResult();
        if (user == null || !user.active || !BcryptUtil.matches(credentials.password(), user.passwordHash)) {
            throw new InvalidCredentialsException();
        }
        return issueTokens(user);
    }

    @Transactional
    public AuthDtos.TokenResponse refresh(String rawToken) {
        RefreshSession session = RefreshSession.find("tokenHash", sha256(rawToken)).firstResult();
        Instant now = Instant.now();
        if (session == null || !session.isUsable(now)) {
            if (session != null && session.revokedAt != null) {
                revokeAll(session.user);
            }
            throw new InvalidCredentialsException();
        }
        session.revokedAt = now;
        return issueTokens(session.user);
    }

    @Transactional
    public void logout(String rawToken) {
        RefreshSession session = RefreshSession.find("tokenHash", sha256(rawToken)).firstResult();
        if (session != null && session.revokedAt == null) {
            session.revokedAt = Instant.now();
        }
    }

    public String issueAccessToken(User user) {
        Instant now = Instant.now();
        return Jwt.subject(user.id.toString())
            .issuer(issuer)
            .audience(audience)
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofMinutes(accessTokenMinutes)))
                .claim("jti", UUID.randomUUID().toString())
                .sign();
    }

    @Transactional
    AuthDtos.TokenResponse issueTokens(User user) {
        String rawRefresh = randomToken();
        RefreshSession session = new RefreshSession();
        session.user = user;
        session.tokenHash = sha256(rawRefresh);
        session.expiresAt = Instant.now().plus(Duration.ofDays(refreshTokenDays));
        session.persist();
        return new AuthDtos.TokenResponse(issueAccessToken(user), rawRefresh, "Bearer",
                accessTokenMinutes * 60, AuthDtos.PublicUser.from(user));
    }

    @Transactional
    void revokeAll(User user) {
        RefreshSession.update("revokedAt = ?1 where user = ?2 and revokedAt is null", Instant.now(), user);
    }

    static String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    static String sha256(String value) {
        try {
            return HexFormatHolder.format(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static String randomToken() {
        byte[] bytes = new byte[48];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static final class HexFormatHolder {
        static String format(byte[] bytes) {
            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        }
    }
}