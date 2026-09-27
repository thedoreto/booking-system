package com.hotel.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyPair;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// RS256: токенът се подписва с частния ключ на хотела и се проверява с публичния
class JwtServiceTest {

    private static final KeyPair KEYS = JwtTestKeys.generate();
    private static final KeyPair OTHER_HOTEL_KEYS = JwtTestKeys.generate();

    private final JwtService jwtService = JwtTestKeys.jwtService(KEYS);

    @Test
    void issuedTokenIsVerified() {
        Claims claims = jwtService.extractClaims(jwtService.generateToken("user-1", "a@b.bg", "USER"));

        assertThat(claims.get("userId", String.class)).isEqualTo("user-1");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
        assertThat(claims.getSubject()).isEqualTo("a@b.bg");
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    void tokenOfAnotherHotelIsRejected() {
        String otherHotelToken = JwtTestKeys.jwtService(OTHER_HOTEL_KEYS).generateToken("user-1", "a@b.bg", "ADMIN");

        assertThatThrownBy(() -> jwtService.extractClaims(otherHotelToken)).isInstanceOf(Exception.class);
    }

    @Test
    void oldHs256TokenIsRejected() {
        String hs256 = token(Keys.hmacShaKeyFor("old-secret-old-secret-old-secret-12345".getBytes()),
                SignatureAlgorithm.HS256, 60_000);

        assertThatThrownBy(() -> jwtService.extractClaims(hs256)).isInstanceOf(Exception.class);
    }

    @Test
    void unsignedTokenIsRejected() {
        String unsigned = Jwts.builder().claim("userId", "user-1").compact();

        assertThatThrownBy(() -> jwtService.extractClaims(unsigned)).isInstanceOf(Exception.class);
    }

    @Test
    void expiredTokenIsRejected() {
        String expired = token(KEYS.getPrivate(), SignatureAlgorithm.RS256, -60_000);

        assertThatThrownBy(() -> jwtService.extractClaims(expired)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void readsKeysFromPemFiles(@TempDir Path dir) throws Exception {
        JwtService fromFiles = new JwtService(
                pem(dir.resolve("private.pem"), "PRIVATE KEY", KEYS.getPrivate().getEncoded()),
                pem(dir.resolve("public.pem"), "PUBLIC KEY", KEYS.getPublic().getEncoded()));

        // Токен от ключовете във файловете се проверява и със същите ключове в паметта – т.е. прочетени са вярно
        assertThat(jwtService.extractClaims(fromFiles.generateToken("user-1", "a@b.bg", "USER"))
                .get("userId", String.class)).isEqualTo("user-1");
    }

    @Test
    void readsKeyAsSingleLineBase64(@TempDir Path dir) throws Exception {
        Path publicKey = Files.writeString(dir.resolve("public.b64"),
                Base64.getEncoder().encodeToString(KEYS.getPublic().getEncoded()));

        JwtService fromFiles = new JwtService(
                pem(dir.resolve("private.pem"), "PRIVATE KEY", KEYS.getPrivate().getEncoded()),
                new FileSystemResource(publicKey));

        assertThat(fromFiles.extractClaims(jwtService.generateToken("user-1", "a@b.bg", "USER"))).isNotNull();
    }

    @Test
    void keysFromDifferentPairsStopTheStart() {
        assertThatThrownBy(() -> new JwtService(KEYS.getPrivate(), OTHER_HOTEL_KEYS.getPublic()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not a pair");
    }

    @Test
    void missingKeyFileStopsTheStart(@TempDir Path dir) {
        assertThatThrownBy(() -> new JwtService(new FileSystemResource(dir.resolve("missing.pem")),
                new FileSystemResource(dir.resolve("missing_public.pem"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot read JWT key file");
    }

    private static String token(Key key, SignatureAlgorithm algorithm, long expiresInMs) {
        return Jwts.builder()
                .claim("userId", "user-1")
                .setExpiration(new Date(System.currentTimeMillis() + expiresInMs))
                .signWith(key, algorithm)
                .compact();
    }

    private static FileSystemResource pem(Path file, String type, byte[] der) throws Exception {
        String body = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(der);
        Files.writeString(file, "-----BEGIN " + type + "-----\n" + body + "\n-----END " + type + "-----\n");
        return new FileSystemResource(file);
    }
}
