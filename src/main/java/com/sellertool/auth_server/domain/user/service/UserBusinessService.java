package com.sellertool.auth_server.domain.user.service;

import com.sellertool.auth_server.domain.exception.dto.*;
import com.sellertool.auth_server.domain.user.dto.UserDto;
import com.sellertool.auth_server.domain.user.entity.UserEntity;
import com.sellertool.auth_server.domain.user.vo.UserVo;
import com.sellertool.auth_server.utils.CustomCookieInterface;
import com.sellertool.auth_server.utils.DataFormatUtils;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

import java.util.*;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@RequiredArgsConstructor
public class UserBusinessService {

    @Value("${phone.auth.token.secret}")
    private String PHONE_AUTH_JWT_SECRET;

    @Value("${email.auth.token.secret}")
    private String EMAIL_AUTH_JWT_SECRET;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Object getInfoOwn(HttpServletResponse response) {
        // 이메일, 전화번호 인증 토큰 제거
        this.removeUserInfoAuthCookie(response);

        if (!userService.isLogin()) {
            throw new InvalidUserAuthException("로그인이 필요한 서비스 입니다.");
        }

        UUID USER_ID = userService.getUserId();
        UserVo userVo = UserVo.toVo(userService.searchUserByUserId(USER_ID));
        return userVo;
    }

    /*
    이메일 인증 요청 토큰 제거
    이메일 인증 성공 토큰 제거
    전화번호 인증 요청 토큰 제거
    전화번호 인증 성공 토큰 제거
     */
    public void removeUserInfoAuthCookie(HttpServletResponse response) {
        ResponseCookie rmEmailAuthToken = ResponseCookie.from("st_email_auth_token", null)
                .domain(CustomCookieInterface.COOKIE_DOMAIN)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie rmEmailAuthVerifiedToken = ResponseCookie.from("st_email_auth_vf_token", null)
                .domain(CustomCookieInterface.COOKIE_DOMAIN)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        // st_phone_auth_vf_token 제거
        ResponseCookie rmPhoneAuthToken = ResponseCookie.from("st_phone_auth_token", null)
                .domain(CustomCookieInterface.COOKIE_DOMAIN)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        // st_phone_auth_vf_token 제거
        ResponseCookie rmPhoneAuthVerifiedToken = ResponseCookie.from("st_phone_auth_vf_token", null)
                .domain(CustomCookieInterface.COOKIE_DOMAIN)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, rmEmailAuthToken.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, rmEmailAuthVerifiedToken.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, rmPhoneAuthToken.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, rmPhoneAuthVerifiedToken.toString());
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
        if (userDto.isVerifiedEmail() && userDto.getEmail() != null) {
            try {
                Cookie verifiedToken = WebUtils.getCookie(request, "st_email_auth_vf_token");

                String email = userDto.getEmail();
                DataFormatUtils.checkEmailFormat(email);    // 이메일 형식 체크

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

                userEntity.setEmail(userDto.getEmail());
            } catch (ExpiredJwtException e) {     // 토큰 만료
                throw new UserInfoAuthJwtException("이메일 인증 토큰이 만료되었습니다.");
            } catch (SignatureException e) {
                throw new UserInfoAuthJwtException("이메일 인증이 완료되지 않았습니다.");
            } catch (NullPointerException e) {   // Phone Auth Number 쿠키가 존재하지 않는다면
                throw new UserInfoAuthJwtException("이메일 인증이 완료되지 않았습니다.");
            }
        }

        /*
         전화번호 검증 (선택 값)
         */
        if (!(userDto.getPhoneNumber() == null || userDto.getPhoneNumber().

                isBlank())) {
            try {
                Cookie authToken = WebUtils.getCookie(request, "st_phone_auth_token");
                Cookie verifiedToken = WebUtils.getCookie(request, "st_phone_auth_vf_token");
                String USER_PHONE_NUMBER = userEntity.getPhoneNumber() == null ? "" : userEntity.getPhoneNumber();

                if (authToken == null) {
                    // 전화번호 입력값만 변경된 경우 제한
                    if (verifiedToken != null || !USER_PHONE_NUMBER.equals(userDto.getPhoneNumber())) {
                        String phoneNumber = userDto.getPhoneNumber();
                        DataFormatUtils.checkPhoneNumberFormat(phoneNumber);    // 전화번호 형식 체크

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
                    }
                } else {
                    throw new UserInfoAuthJwtException("전화번호 인증이 완료되지 않았습니다.");
                }
            } catch (ExpiredJwtException e) {     // 토큰 만료
                throw new UserInfoAuthJwtException("전화번호 인증 토큰이 만료되었습니다.");
            } catch (SignatureException e) {
                throw new UserInfoAuthJwtException("전화번호 인증이 올바르지 않습니다.");
            } catch (NullPointerException e) {   // Phone Auth Number 쿠키가 존재하지 않는다면
                throw new UserInfoAuthJwtException("전화번호 인증이 완료되지 않았습니다.");
            } catch (Exception e) {
                throw new UserInfoAuthJwtException("전화번호 인증이 완료되지 않았습니다.");
            }
        }

        /*
        업데이트 데이터 셋 (영속성 업데이트)
         */
        userEntity.setName(userDto.getName());
        userEntity.setNickname(userDto.getNickname());
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
}
