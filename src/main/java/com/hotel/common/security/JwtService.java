package com.hotel.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

// JWT с RS256: подписва се с частния ключ на хотела (само тук), проверява се с публичния.
// Публичният ключ има и booking-ai (в Mongo, по hotelId) – той може да проверява токените, но не и да ги издава.
// Ключовете са PEM файлове извън git: jwt.private-key-location / jwt.public-key-location (напр. file:secrets/...).
@Service
public class JwtService {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    @Autowired
    public JwtService(@Value("${jwt.private-key-location}") Resource privateKeyFile,
                      @Value("${jwt.public-key-location}") Resource publicKeyFile) {
        this(readPrivateKey(privateKeyFile), readPublicKey(publicKeyFile));
    }

    public JwtService(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        checkKeysMatch();
    }

    public String generateToken(String userId, String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 30 * 60 * 1000L)) //30 minutes
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Частен и публичен ключ от различни двойки (напр. публичният на друг хотел) – спираме при старт,
    // вместо после всеки вход да дава 401
    private void checkKeysMatch() {
        try {
            extractClaims(generateToken("key-check", "key-check", "key-check"));
        } catch (Exception e) {
            throw new IllegalStateException("JWT private and public keys are not a pair", e);
        }
    }

    private static PrivateKey readPrivateKey(Resource file) {
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(readPem(file)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Invalid JWT private key (PKCS#8 RSA PEM expected): " + file, e);
        }
    }

    private static PublicKey readPublicKey(Resource file) {
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(readPem(file)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Invalid JWT public key (RSA PEM expected): " + file, e);
        }
    }

    // PEM (с -----BEGIN ...----- редовете) или само base64 на един ред
    private static byte[] readPem(Resource file) {
        String text;
        try {
            text = file.getContentAsString(StandardCharsets.US_ASCII);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read JWT key file: " + file, e);
        }
        String base64 = text.replaceAll("-----(BEGIN|END) [A-Z ]+-----", "").replaceAll("\\s", "");
        try {
            return Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("JWT key file is not PEM/base64: " + file, e);
        }
    }
}
