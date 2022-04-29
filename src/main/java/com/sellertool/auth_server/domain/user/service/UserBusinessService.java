package com.sellertool.auth_server.domain.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellertool.auth_server.domain.email.MailRequest;
import com.sellertool.auth_server.domain.email.ReceipientForRequest;
import com.sellertool.auth_server.domain.exception.dto.*;
import com.sellertool.auth_server.domain.user.dto.UserDto;
import com.sellertool.auth_server.domain.user.entity.UserEntity;
import com.sellertool.auth_server.domain.user.vo.UserVo;
import com.sellertool.auth_server.utils.UserAuthInfoUtils;
import com.sellertool.auth_server.utils.CustomCookieInterface;
import com.sellertool.auth_server.utils.DataFormatUtils;
import com.sellertool.auth_server.utils.UserInfoAuthTokenUtils;
import com.twilio.Twilio;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.WebUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@RequiredArgsConstructor
public class UserBusinessService {

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

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Object getInfoOwn() {
        if (!userService.isLogin()) {
            throw new InvalidUserAuthException("로그인이 필요한 서비스 입니다.");
        }

        UUID USER_ID = userService.getUserId();
        UserVo userVo = UserVo.toVo(userService.searchUserByUserId(USER_ID));
        return userVo;
    }

    @Transactional(readOnly = true)
    public Object checkUsernameDuplicate(Map<String, Object> params) {
        Object usernameObj = params.get("username");
        Map<String, Object> resultData = new HashMap<>();

        if (usernameObj == null) {
            resultData.put("isEmpty", true);
            resultData.put("isDuplicated", false);
        }
        String USERNAME = usernameObj.toString();

        if (!userService.isDuplicatedUsername(USERNAME)) {
            resultData.put("isEmpty", false);
            resultData.put("isDuplicated", false);
        } else {
            resultData.put("isEmpty", false);
            resultData.put("isDuplicated", true);
        }
        return resultData;
    }

    /*
    로그인 체크
    자신의 요청인지 체크
    엔터티 불러오기
    업데이트 데이터 셋
     */
    @Transactional
    public void updateInfoOwn(HttpServletRequest request, HttpServletResponse response, UserDto userDto) {
        /*
        로그인 체크
         */
        if (!userService.isLogin()) {
            throw new InvalidUserAuthException("로그인이 필요한 서비스 입니다.");
        }
        UUID USER_ID = userService.getUserId();

        /*
        자신의 요청인지 체크
         */
        if (!userDto.getId().equals(USER_ID)) {
            throw new AccessDeniedPermissionException("잘못된 접근 방법 입니다.");
        }

        /*
        엔터티 불러오기
         */
        UserEntity userEntity = userService.searchUserByUserId(USER_ID);

        /*
        이메일 검증
         */
        if (!userEntity.getEmail().equals(userDto.getEmail())) {
            try {
                String email = userDto.getEmail();
                DataFormatUtils.checkEmailFormat(email);    // 이메일 형식 체크

                Cookie verifiedToken = WebUtils.getCookie(request, "st_email_auth_vf_token");

                String emailAuthToken = verifiedToken.getValue();
                String EMAIL_AUTH_JWT_KEY = email + EMAIL_AUTH_JWT_SECRET;

                Jwts.parser().setSigningKey(EMAIL_AUTH_JWT_KEY).parseClaimsJws(emailAuthToken).getBody();

                // st_email_auth_vf_token 제거
                ResponseCookie emailAuthVerifiedToken = ResponseCookie.from("st_email_auth_vf_token", null)
                        .domain(CustomCookieInterface.COOKIE_DOMAIN)
                        .sameSite("Strict")
                        .path("/")
                        .maxAge(0)
                        .build();
                response.addHeader(HttpHeaders.SET_COOKIE, emailAuthVerifiedToken.toString());
            } catch (ExpiredJwtException e) {     // 토큰 만료
                throw new UserInfoAuthJwtException("이메일 인증 토큰이 만료되었습니다.");
            } catch (NullPointerException e) {   // Phone Auth Number 쿠키가 존재하지 않는다면
                throw new UserInfoAuthJwtException("이메일 인증을 먼저 진행해주세요.");
            } catch (Exception e) {
                throw new UserInfoAuthJwtException("이메일 인증 오류");
            }
        }

        /*
         전화번호 검증
         */
        if (!userEntity.getPhoneNumber().equals(userDto.getPhoneNumber())) {
            try {
                String phoneNumber = userDto.getPhoneNumber();
                DataFormatUtils.checkPhoneNumberFormat(phoneNumber);    // 전화번호 형식 체크

                Cookie verifiedToken = WebUtils.getCookie(request, "st_phone_auth_vf_token");

                String phoneAuthToken = verifiedToken.getValue();
                String PHONE_AUTH_JWT_KEY = phoneNumber + PHONE_AUTH_JWT_SECRET;

                Jwts.parser().setSigningKey(PHONE_AUTH_JWT_KEY).parseClaimsJws(phoneAuthToken).getBody();

                // st_phone_auth_vf_token 제거
                ResponseCookie phoneAuthVerifiedToken = ResponseCookie.from("st_phone_auth_vf_token", null)
                        .domain(CustomCookieInterface.COOKIE_DOMAIN)
                        .sameSite("Strict")
                        .path("/")
                        .maxAge(0)
                        .build();
                response.addHeader(HttpHeaders.SET_COOKIE, phoneAuthVerifiedToken.toString());
            } catch (ExpiredJwtException e) {     // 토큰 만료
                throw new UserInfoAuthJwtException("전화번호 인증 토큰이 만료되었습니다.");
            } catch (NullPointerException e) {   // Phone Auth Number 쿠키가 존재하지 않는다면
                throw new UserInfoAuthJwtException("전화번호 인증을 먼저 진행해주세요.");
            } catch (Exception e) {
                throw new UserInfoAuthJwtException("전화번호 인증 오류");
            }
        }

        /*
        업데이트 데이터 셋 (영속성 업데이트)
         */
        userEntity.setName(userDto.getName());
        userEntity.setNickname(userDto.getNickname());
        userEntity.setEmail(userDto.getEmail());
        userEntity.setPhoneNumber(userDto.getPhoneNumber());
    }

    /*
    로그인 체크
    새 비밀번호, 새 비밀번호 확인 비교 체크
    새 비밀번호 형식 체크
    현재 비밀번호 체크
    새로운 솔트 부여
    패스워드 및 솔트 값 업데이트
     */
    @Transactional
    public void changePassword(Map<String, Object> password) {
        /*
        로그인 체크
         */
        if (!userService.isLogin()) {
            throw new InvalidUserAuthException("로그인이 필요한 서비스 입니다.");
        }
        UUID USER_ID = userService.getUserId();
        String currentPassword = password.get("currentPassword").toString();
        String newPassword = password.get("newPassword").toString();
        String checkPassword = password.get("checkPassword").toString();

        /*
        새 비밀번호, 새 비밀번호 확인 비교 체크
         */
        if (!newPassword.equals(checkPassword)) {
            throw new NotMatchedFormatException("새 비밀번호를 확인해 주세요.");
        }

        /*
        새 비밀번호 형식 체크
         */
        boolean isContainNumbers = Pattern.compile("[0-9]").matcher(newPassword).find();
        boolean isContainAlphabets = Pattern.compile("[a-z]").matcher(newPassword).find();
        boolean isContainSymbols = Pattern.compile("[!@#$%^&*()\\-_=+\\\\\\/\\[\\]{};:\\`\"',.<>\\/?\\|~]").matcher(newPassword).find();

        if (!isContainNumbers || !isContainAlphabets || !isContainSymbols) {
            throw new NotMatchedFormatException("새 비밀번호 형식을 확인해 주세요. (문자, 숫자, 특수문자가 반드시 포함되어야 합니다.)");
        }

        if (newPassword.length() < 8 || newPassword.length() > 20) {
            throw new NotMatchedFormatException("새 비밀번호 형식을 확인해 주세요. (비밀번호 길이가 맞지 않습니다.)");
        }

        /*
        현재 비밀번호 체크
         */
        UserEntity userEntity = userService.searchUserByUserId(USER_ID);
        String currentSalt = userEntity.getSalt();

        if (!passwordEncoder.matches(currentPassword + currentSalt, userEntity.getPassword())) {
            throw new NotMatchedFormatException("현재 비밀번호를 확인해 주세요.");
        }

        String newSalt = UUID.randomUUID().toString();
        String encNewPassword = passwordEncoder.encode(newPassword + newSalt);

        /*
        패스워드 및 솔트 값 업데이트 (영속성 업데이트)
         */
        userEntity.setPassword(encNewPassword);
        userEntity.setSalt(newSalt);
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
                throw new UserInfoAuthJwtException("인증 요청이 올바르지 않습니다.");
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
            throw new UserInfoAuthJwtException("인증 요청이 올바르지 않습니다.");
        } catch (IllegalArgumentException e) {
            throw new UserInfoAuthJwtException("인증 요청이 올바르지 않습니다.");
        } catch (UnsupportedJwtException e) {
            throw new UserInfoAuthJwtException("인증 요청이 올바르지 않습니다.");
        } catch (MalformedJwtException e) {
            throw new UserInfoAuthJwtException("인증 요청이 올바르지 않습니다.");
        } catch (SignatureException e) {
            throw new UserInfoAuthJwtException("인증 요청이 올바르지 않습니다.");
        }
    }

    private String readFromInputStream(InputStream inputStream)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    public void getEmailAuthNumber(Map<String, Object> params, HttpServletResponse response) throws IOException {
        Long time = System.currentTimeMillis();
        String sendEmail = params.get("email").toString();
        DataFormatUtils.checkEmailFormat(sendEmail);    // 이메일 형식 체크

        String authNum = UserAuthInfoUtils.generateAuthNumber();
        String sendMessage = "";
//        String sendMessage = "[셀러툴] 본인확인 인증번호[" + authNum + "]입니다. \"타인 노출 금지\"";

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
            throw new UserInfoAuthJwtException("인증 요청시간이 만료되었습니다.");
        } catch (NullPointerException e) {   // Email Auth Number 쿠키가 존재하지 않는다면
            throw new UserInfoAuthJwtException("인증 요청이 올바르지 않습니다.");
        } catch (IllegalArgumentException e) {
            throw new UserInfoAuthJwtException("인증 요청이 올바르지 않습니다.");
        } catch (UnsupportedJwtException e) {
            throw new UserInfoAuthJwtException("인증 요청이 올바르지 않습니다.");
        } catch (MalformedJwtException e) {
            throw new UserInfoAuthJwtException("인증 요청이 올바르지 않습니다.");
        } catch (SignatureException e) {
            throw new UserInfoAuthJwtException("인증 요청이 올바르지 않습니다.");
        }
    }
}
