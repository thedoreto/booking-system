package com.hotel.common.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

// Двойка RSA ключове за тестовете – като истинските (2048 бита), но създадена в паметта
public final class JwtTestKeys {

    private JwtTestKeys() {
    }

    public static KeyPair generate() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static JwtService jwtService(KeyPair keys) {
        return new JwtService(keys.getPrivate(), keys.getPublic());
    }
}
