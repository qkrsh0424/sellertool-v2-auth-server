package com.sellertool.auth_server.utils;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CsrfTokenUtils {

    private static String csrfJwtSecret;

    public CsrfTokenUtils(String csrfJwtSecret) {
        this.csrfJwtSecret = csrfJwtSecret;
    }

    public static String getCsrfJwtToken(String csrfTokenId) {
        JwtBuilder builder = Jwts.builder()
            .setSubject("CSRF_JWT")
            .setHeader(createHeader())
            .setClaims(createClaims(csrfTokenId))
            .setExpiration(createTokenExpiration(CustomJwtInterface.CSRF_TOKEN_JWT_EXPIRATION))
            .signWith(SignatureAlgorithm.HS256, createSigningKey(csrfJwtSecret));

        return builder.compact();
    }

    private static Map<String, Object> createHeader() {
        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        header.put("alg", "HS256");
        header.put("regDate", System.currentTimeMillis());
        return header;
    }

    // JWT Palyod
    private static Map<String, Object> createClaims(String csrfTokenId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("csrfId", csrfTokenId);
        return claims;
    }

    private static Date createTokenExpiration(Integer expirationTime) {
        Date expiration = new Date(System.currentTimeMillis() + expirationTime);
        return expiration;
    }

    private static Key createSigningKey(String tokenSecret) {
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(tokenSecret);
        return new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS256.getJcaName()); 
    }

    public static boolean isValidToken(Cookie csrfCookie) {

        String csrfToken = csrfCookie.getValue();

        try {
            Claims claims = Jwts.parser().setSigningKey(csrfJwtSecret).parseClaimsJws(csrfToken).getBody();
            log.info("expireTime :" + claims.getExpiration());
            return true;
        } catch (ExpiredJwtException exception) {
            log.error("Token Expired");
            return false;
        } catch (JwtException exception) {
            log.error("Token Tampered");
            return false;
        } catch (NullPointerException exception) {
            log.error("Token is null");
            return false;
        }
    }
}
