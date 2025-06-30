package com.torah.torahAI;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;

@Service
public class JwtUtils {
    private static final String SECRET = generateKey();
    private static final long EXPIRATION_MS = 3600_000; // 1 hour

    public static String generateToken(String email) {
        long now = System.currentTimeMillis();
        long exp = now + EXPIRATION_MS;

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", email);
        claims.put("iat", now / 1000);
        claims.put("exp", exp / 1000);

        JWSObject jws = new JWSObject(
                new JWSHeader(JWSAlgorithm.HS256),
                new Payload(claims)
        );

        try {
            jws.sign(new MACSigner(SECRET));
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }

        return jws.serialize();
    }

    public static String validateAndGetSubject(String token) throws Exception {
        JWSObject jws = JWSObject.parse(token);
        boolean verified = jws.verify(new MACVerifier(SECRET));
        if (!verified) {
            throw new Exception("Invalid signature");
        }

        Map<String, Object> claims = jws.getPayload().toJSONObject();
        long exp = (long) claims.get("exp");
        long now = System.currentTimeMillis() / 1000;
        if (now > exp) {
            throw new Exception("Token expired");
        }

        return (String) claims.get("sub");
    }

    private static String generateKey() {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[32];
        random.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}
