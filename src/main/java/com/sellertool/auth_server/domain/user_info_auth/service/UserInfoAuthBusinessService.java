package com.sellertool.auth_server.domain.user_info_auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellertool.auth_server.domain.exception.dto.EmailAuthException;
import com.sellertool.auth_server.domain.exception.dto.UserInfoAuthJwtException;
import com.sellertool.auth_server.domain.user_info_auth.dto.email.MailRequest;
import com.sellertool.auth_server.domain.user_info_auth.dto.email.ReceipientForRequest;
import com.sellertool.auth_server.utils.CustomCookieInterface;
import com.sellertool.auth_server.utils.DataFormatUtils;
import com.sellertool.auth_server.utils.UserAuthInfoUtils;
import com.sellertool.auth_server.utils.UserInfoAuthTokenUtils;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import io.jsonwebtoken.*;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserInfoAuthBusinessService {

    @Value("${twilio.account.sid}")
    private String ACCOUNT_SID;

    @Value("${twilio.auth.token}")
    private String AUTH_TOKEN;

    @Value("${twilio.from.number}")
    private String FROM_NUMBER;

    @Value("${email.admin.id}")
    private String EMAIL_ID;

    @Value("${naver.cloud.platform.request.url}")
    private String MAIL_REQUEST_URL;

    @Value("${naver.cloud.platform.request.mail.api}")
    private String MAIL_REQUEST_API;

    @Value("${phone.auth.token.secret}")
    private String PHONE_AUTH_JWT_SECRET;

    @Value("${email.auth.token.secret}")
    private String EMAIL_AUTH_JWT_SECRET;

    private String KOREA_COUNTRY_NUMBER = "+82";

    public void getEmailAuthNumber(Map<String, Object> params, HttpServletResponse response) throws IOException {
        Long time = System.currentTimeMillis();
        String sendEmail = params.get("email").toString();
        DataFormatUtils.checkEmailFormat(sendEmail);    // 이메일 형식 체크

        String authNum = UserAuthInfoUtils.generateAuthNumber();
        String sendMessage = "";

        FileInputStream emailTemplate = new FileInputStream("src/main/resources/static/emailAuthTemplate");
        sendMessage = IOUtils.toString(emailTemplate, "UTF-8");
        sendMessage = sendMessage.replaceFirst("------", authNum);

        List<ReceipientForRequest> receipients = new ArrayList<>();
        ReceipientForRequest receipient = ReceipientForRequest.builder()
                .address(sendEmail)
                .type("R")
                .build();
        receipients.add(receipient);

        MailRequest mailRequest = MailRequest.builder()
                .senderAddress(EMAIL_ID)
                .senderName("Sellertool")
                .title("[셀러툴] 이메일 인증")
                .body(sendMessage)
                .recipients(receipients)
                .build();

        // 메일 전송
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(mailRequest);
            HttpHeaders headers = UserAuthInfoUtils.setApiRequestHeader(time);

            HttpEntity<String> body = new HttpEntity<>(jsonBody, headers);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
            restTemplate.postForObject(new URI(MAIL_REQUEST_URL + MAIL_REQUEST_API), body, HashMap.class);
        } catch(URISyntaxException e) {
            throw new EmailAuthException("이메일 전송이 불가능합니다.");
        } catch(JsonProcessingException e) {
            throw new EmailAuthException("이메일 전송이 불가능합니다.");
        } catch(InvalidKeyException e) {
            throw new EmailAuthException("이메일 전송이 불가능합니다.");
        } catch(NoSuchAlgorithmException e) {
            throw new EmailAuthException("이메일 전송이 불가능합니다.");
        } catch(UnsupportedEncodingException e) {
            throw new EmailAuthException("이메일 전송이 불가능합니다.");
        } catch(Exception e) {
            e.printStackTrace();
            throw new EmailAuthException("이메일 전송이 불가능합니다.");
        }

        String authNumToken = UserInfoAuthTokenUtils.getEmailAuthNumberJwtToken(authNum, sendEmail);

        ResponseCookie emailAuthToken = ResponseCookie.from("st_email_auth_token", authNumToken)
                .secure(CustomCookieInterface.SECURE)
                .domain(CustomCookieInterface.COOKIE_DOMAIN)
                .sameSite("Strict")
                .path("/")
                .maxAge(CustomCookieInterface.EMAIL_AUTH_COOKIE_EXPIRATION)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, emailAuthToken.toString());

        // st_email_auth_vf_token 제거
        ResponseCookie emailAuthVerifiedToken = ResponseCookie.from("st_email_auth_vf_token", null)
                .domain(CustomCookieInterface.COOKIE_DOMAIN)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, emailAuthVerifiedToken.toString());
    }

    public void verifyEmailAuthNumber(HttpServletRequest request, HttpServletResponse response, Map<String, Object> params) {
        try {
            String email = params.get("email").toString();
            String emailAuthNumber = params.get("emailAuthNumber").toString();
            DataFormatUtils.checkEmailFormat(email);    // 이메일 형식 체크

            Cookie authCookie = WebUtils.getCookie(request, "st_email_auth_token");

            String emailAuthJwtToken = authCookie.getValue();
            String EMAIL_AUTH_JWT_KEY = emailAuthNumber + EMAIL_AUTH_JWT_SECRET;

            // 인증번호 검증
            Claims claims = Jwts.parser().setSigningKey(EMAIL_AUTH_JWT_KEY).parseClaimsJws(emailAuthJwtToken).getBody();

            if(!claims.get("email").equals(email)) {
                throw new UserInfoAuthJwtException("인증 요청이 올바르지 않습니다.");
            }

            // 인증되었다면 새로운 JWT 쿠키 생성
            String authToken = UserInfoAuthTokenUtils.getEmailAuthVerifiedJwtToken(email);

            ResponseCookie emailAuthVerifiedToken = ResponseCookie.from("st_email_auth_vf_token", authToken)
                    .secure(CustomCookieInterface.SECURE)
                    .domain(CustomCookieInterface.COOKIE_DOMAIN)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(CustomCookieInterface.EMAIL_AUTH_VF_COOKIE_EXPIRATION)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, emailAuthVerifiedToken.toString());

            // st_email_auth_token 제거
            ResponseCookie emailAuthToken = ResponseCookie.from("st_email_auth_token", null)
                    .domain(CustomCookieInterface.COOKIE_DOMAIN)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(0)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, emailAuthToken.toString());
        } catch (ExpiredJwtException e) {     // 토큰 만료
            throw new UserInfoAuthJwtException("이메일 인증 요청시간이 만료되었습니다.");
        } catch (NullPointerException e) {   // Email Auth Number 쿠키가 존재하지 않는다면
            throw new UserInfoAuthJwtException("이메일 인증 요청이 올바르지 않습니다.");
        } catch (IllegalArgumentException e) {
            throw new UserInfoAuthJwtException("이메일 인증 요청이 올바르지 않습니다.");
        } catch (UnsupportedJwtException e) {
            throw new UserInfoAuthJwtException("이메일 인증 요청이 올바르지 않습니다.");
        } catch (MalformedJwtException e) {
            throw new UserInfoAuthJwtException("이메일 인증 요청이 올바르지 않습니다.");
        } catch (SignatureException e) {
            throw new UserInfoAuthJwtException("이메일 인증 요청이 올바르지 않습니다.");
        }
    }

    public void getPhoneAuthNumber(Map<String, Object> params, HttpServletResponse response) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        String phoneNumber = params.get("phoneNumber").toString();
        DataFormatUtils.checkPhoneNumberFormat(phoneNumber);    // 전화번호 형식 체크

        String authNum = UserAuthInfoUtils.generateAuthNumber();
        String sendPhoneNumber = KOREA_COUNTRY_NUMBER + phoneNumber;
        String sendMessage = "[셀러툴] 본인확인 인증번호[" + authNum + "]입니다. \"타인 노출 금지\"";

        // SMS 전송
        Message.creator(new PhoneNumber(sendPhoneNumber), new PhoneNumber(FROM_NUMBER), sendMessage).create();

        String authNumToken = UserInfoAuthTokenUtils.getPhoneAuthNumberJwtToken(authNum, phoneNumber);

        ResponseCookie phoneAuthToken = ResponseCookie.from("st_phone_auth_token", authNumToken)
                .secure(CustomCookieInterface.SECURE)
                .domain(CustomCookieInterface.COOKIE_DOMAIN)
                .sameSite("Strict")
                .path("/")
                .maxAge(CustomCookieInterface.PHONE_AUTH_COOKIE_EXPIRATION)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, phoneAuthToken.toString());

        ResponseCookie phoneAuthVerifiedToken = ResponseCookie.from("st_phone_auth_vf_token", null)
                .domain(CustomCookieInterface.COOKIE_DOMAIN)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, phoneAuthVerifiedToken.toString());
    }

    public void verifyPhoneAuthNumber(HttpServletRequest request, HttpServletResponse response, Map<String, Object> params) {
        try {
            String phoneNumber = params.get("phoneNumber").toString();
            String phoneAuthNumber = params.get("phoneAuthNumber").toString();
            DataFormatUtils.checkPhoneNumberFormat(phoneNumber);    // 전화번호 형식 체크

            Cookie authCookie = WebUtils.getCookie(request, "st_phone_auth_token");

            String phoneAuthJwtToken = authCookie.getValue();
            String PHONE_AUTH_JWT_KEY = phoneAuthNumber + PHONE_AUTH_JWT_SECRET;

            // 인증번호 검증
            Claims claims = Jwts.parser().setSigningKey(PHONE_AUTH_JWT_KEY).parseClaimsJws(phoneAuthJwtToken).getBody();

            if(!claims.get("phoneNumber").equals(phoneNumber)) {
                throw new UserInfoAuthJwtException("전화번호 인증 요청이 올바르지 않습니다.");
            }

            // 인증되었다면 새로운 JWT 쿠키 생성
            String authToken = UserInfoAuthTokenUtils.getPhoneAuthVerifiedJwtToken(phoneNumber);

            ResponseCookie phoneAuthVerifiedToken = ResponseCookie.from("st_phone_auth_vf_token", authToken)
                    .secure(CustomCookieInterface.SECURE)
                    .domain(CustomCookieInterface.COOKIE_DOMAIN)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(CustomCookieInterface.PHONE_AUTH_VF_COOKIE_EXPIRATION)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, phoneAuthVerifiedToken.toString());

            // st_phone_auth_token 제거
            ResponseCookie phoneAuthToken = ResponseCookie.from("st_phone_auth_token", null)
                    .domain(CustomCookieInterface.COOKIE_DOMAIN)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(0)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, phoneAuthToken.toString());
        } catch (ExpiredJwtException e) {     // 토큰 만료
            throw new UserInfoAuthJwtException("인증 요청시간이 만료되었습니다.");
        } catch (NullPointerException e) {   // Phone Auth Number 쿠키가 존재하지 않는다면
            throw new UserInfoAuthJwtException("전화번호 인증 요청이 올바르지 않습니다.");
        } catch (IllegalArgumentException e) {
            throw new UserInfoAuthJwtException("전화번호 인증 요청이 올바르지 않습니다.");
        } catch (UnsupportedJwtException e) {
            throw new UserInfoAuthJwtException("전화번호 인증 요청이 올바르지 않습니다.");
        } catch (MalformedJwtException e) {
            throw new UserInfoAuthJwtException("전화번호 인증 요청이 올바르지 않습니다.");
        } catch (SignatureException e) {
            throw new UserInfoAuthJwtException("전화번호 인증 요청이 올바르지 않습니다.");
        }
    }
}
