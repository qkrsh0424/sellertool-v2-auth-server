package com.sellertool.auth_server.utils;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class UserInfoAuthTokenUtils {

    private static String PHONE_AUTH_JWT_SECRET;
    private static String EMAIL_AUTH_JWT_SECRET;

    @Value("${phone.auth.token.secret}")
    public void setPhoneAuthJwtSecret(String phoneAuthJwtSecret) {
        UserInfoAuthTokenUtils.PHONE_AUTH_JWT_SECRET = phoneAuthJwtSecret;
    }

    @Value("${email.auth.token.secret}")
    public void setEmailAuthJwtSecret(String emailAuthJwtSecret) {
        UserInfoAuthTokenUtils.EMAIL_AUTH_JWT_SECRET = emailAuthJwtSecret;
    }

    public static String getPhoneAuthNumberJwtToken(String authNumber, String phoneNumber) {
        String PHONE_AUTH_JWT_KEY = authNumber + PHONE_AUTH_JWT_SECRET;

        JwtBuilder builder = Jwts.builder()
            .setSubject("PHONE_AUTH_JWT")
            .setHeader(createHeader())
            .setClaims(createPhoneAuthClaims(phoneNumber))
            .setExpiration(createTokenExpiration(CustomJwtInterface.PHONE_AUTH_TOKEN_JWT_EXPIRATION))
            .signWith(SignatureAlgorithm.HS256, createSigningKey(PHONE_AUTH_JWT_KEY));

        return builder.compact();
    }

    public static String getPhoneAuthVerifiedJwtToken(String phoneNumber) {
        String PHONE_AUTH_JWT_KEY = phoneNumber + PHONE_AUTH_JWT_SECRET;

        JwtBuilder builder = Jwts.builder()
                .setSubject("PHONE_AUTH_VF_JWT")
                .setHeader(createHeader())
                .setExpiration(createTokenExpiration(CustomJwtInterface.PHONE_AUTH_VF_TOKEN_JWT_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, createSigningKey(PHONE_AUTH_JWT_KEY));

        return builder.compact();
    }

    public static String getEmailAuthNumberJwtToken(String authNumber, String email) {
        String EMAIL_AUTH_JWT_KEY = authNumber + EMAIL_AUTH_JWT_SECRET;

        JwtBuilder builder = Jwts.builder()
                .setSubject("PHONE_AUTH_JWT")
                .setHeader(createHeader())
                .setClaims(createEmailAuthClaims(email))
                .setExpiration(createTokenExpiration(CustomJwtInterface.EMAIL_AUTH_TOKEN_JWT_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, createSigningKey(EMAIL_AUTH_JWT_KEY));

        return builder.compact();
    }

    public static String getEmailAuthVerifiedJwtToken(String email) {
        String EMAIL_AUTH_JWT_KEY = email + EMAIL_AUTH_JWT_SECRET;

        JwtBuilder builder = Jwts.builder()
                .setSubject("EMAIL_AUTH_VF_JWT")
                .setHeader(createHeader())
                .setExpiration(createTokenExpiration(CustomJwtInterface.EMAIL_AUTH_VF_TOKEN_JWT_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, createSigningKey(EMAIL_AUTH_JWT_KEY));

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
    private static Map<String, Object> createPhoneAuthClaims(String phoneNumber) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("phoneNumber", phoneNumber);
        return claims;
    }

    private static Map<String, Object> createEmailAuthClaims(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
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

    public static boolean isValidToken(Cookie phoneAuthNumCookie) {

        String phoneAuthToken = phoneAuthNumCookie.getValue();

        try {
            Claims claims = Jwts.parser().setSigningKey(PHONE_AUTH_JWT_SECRET).parseClaimsJws(phoneAuthToken).getBody();
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