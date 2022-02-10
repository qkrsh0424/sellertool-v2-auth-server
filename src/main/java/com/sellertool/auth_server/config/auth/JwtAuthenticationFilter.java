package com.sellertool.auth_server.config.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellertool.auth_server.config.exception.AuthenticationMethodNotAllowedException;
import com.sellertool.auth_server.domain.message.dto.Message;
import com.sellertool.auth_server.domain.refresh_token.entity.RefreshTokenEntity;
import com.sellertool.auth_server.domain.refresh_token.repository.RefreshTokenRepository;
import com.sellertool.auth_server.domain.user.entity.UserEntity;
import com.sellertool.auth_server.domain.user.repository.UserRepository;
import com.sellertool.auth_server.utils.CustomCookieInterface;
import com.sellertool.auth_server.utils.AuthTokenUtils;
import com.sellertool.auth_server.utils.CustomJwtInterface;
import com.sellertool.auth_server.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private String accessTokenSecret;
    private String refreshTokenSecret;
    private boolean postOnly = true;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                                   String accessTokenSecret, String refreshTokenSecret) {
        super.setAuthenticationManager(authenticationManager);
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.accessTokenSecret = accessTokenSecret;
        this.refreshTokenSecret = refreshTokenSecret;

        setFilterProcessesUrl("/auth/v1/login");
    }

    // 로그인 요청 시 실행
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
//        System.out.println("========attemptAuthentication========");

        // 로그인 메소드 POST로 제한
        if (this.postOnly && !request.getMethod().equals("POST")) {
            log.error("Authentication method not supported: " + request.getMethod());
            throw new AuthenticationMethodNotAllowedException("Authentication method not supported: " + request.getMethod());
        }

        try {
            UserEntity user = new ObjectMapper().readValue(request.getInputStream(), UserEntity.class);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
            return this.getAuthenticationManager().authenticate(authToken);
        } catch (IOException e) {
            log.error("getAuthenticationManager().authenticate() error.");
            throw new AuthenticationServiceException("입력하신 아이디 및 패스워드를 다시 확인해 주세요.");
        }
    }

    // 로그인 성공 시
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authentication) throws IOException, ServletException {
        AuthTokenUtils tokenUtils = new AuthTokenUtils(accessTokenSecret, refreshTokenSecret);

        UserEntity user = ((PrincipalDetails) authentication.getPrincipal()).getUser();
        UUID refreshTokenId = UUID.randomUUID();

        String[] ipAddress = this.getClientIpAddress(request).replaceAll(" ", "").split(",");
        String clientIp = ipAddress[0];

        String accessToken = tokenUtils.getJwtAccessToken(user.getId(), user.getRoles(), refreshTokenId, clientIp);
        String refreshToken = tokenUtils.getJwtRefreshToken(user.getId(), user.getRoles(), clientIp);

        // 리프레시 토큰 저장
        try {
            this.saveRefreshToken(user, refreshTokenId, refreshToken);
        } catch (Exception e) {
            log.error("refresh token DB save error.");
            throw new AuthenticationServiceException("잠시 후 다시 시도해 주세요.");
        }

        // 한 유저에게 발급된 리프레시 토큰이 특정 개수보다 많다면 리프레시 토큰 삭제
        try {
            this.deleteOldRefreshTokenForUser(user.getId(), user.getAllowedAccessCount());
        } catch (Exception e) {
            log.error("refresh token DB delete error.");
            throw new AuthenticationServiceException("잠시 후 다시 시도해 주세요.");
        }

        ResponseCookie tokenCookie = ResponseCookie.from("st_actoken", accessToken)
                .httpOnly(true)
                .domain(CustomCookieInterface.COOKIE_DOMAIN)
                .secure(CustomCookieInterface.SECURE)
                .sameSite("Strict")
                .path("/")
                .maxAge(CustomCookieInterface.JWT_TOKEN_COOKIE_EXPIRATION)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, tokenCookie.toString());

        ResponseCookie tokenExpireTimeCookie = ResponseCookie.from("st_auth_exp", "st_auth_exp")
                .domain(CustomCookieInterface.COOKIE_DOMAIN)
                .secure(CustomCookieInterface.SECURE)
                .sameSite("Strict")
                .path("/")
                .maxAge(CustomCookieInterface.JWT_TOKEN_COOKIE_EXPIRATION)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, tokenExpireTimeCookie.toString());

        Message message = new Message();

        message.setStatus(HttpStatus.OK);
        message.setMessage("success");
        message.setMemo("login");

        String msg = new ObjectMapper().writeValueAsString(message);
        response.setStatus(message.getStatus().value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(msg);
        response.getWriter().flush();
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
//        System.out.println("========unsuccessfulAuthentication========");
        Object exceptionClass = failed.getClass();

        /**
         * 405
         */
        if (
                exceptionClass.equals(AuthenticationMethodNotAllowedException.class) // 요청 메서드가 POST 가 아님.
        ) {
            errorResponse(response, HttpStatus.METHOD_NOT_ALLOWED, "method_not_allowed", failed.getMessage());
            return;
        }

        /**
         * 401
         */
        if (
                exceptionClass.equals(UsernameNotFoundException.class) // 계정 없음
                        || exceptionClass.equals(BadCredentialsException.class) // 비밀번호 불일치
//                        || exceptionClass.equals(AccountExpiredException.class) // 계정 만료
//                        || exceptionClass.equals(CredentialsExpiredException.class) // 비밀번호 만료
//                        || exceptionClass.equals(DisabledException.class) // 계정 비활성화
//                        || exceptionClass.equals(LockedException.class) // 계정 잠김
        ) {
            errorResponse(response, HttpStatus.UNAUTHORIZED, "auth_failed", failed.getMessage());
            return;
        }

        /**
         * 400
         */
        if (
                exceptionClass.equals(AuthenticationServiceException.class) // 각종 서비스 에러
        ) {
            errorResponse(response, HttpStatus.BAD_REQUEST, "service_failed", failed.getMessage());
            return;
        }

        errorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "error", "undefined error.");
    }

    // Create DB Refresh Token
    private void saveRefreshToken(UserEntity userEntity, UUID rtId, String refreshToken) {
        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                .id(rtId)
                .userId(userEntity.getId())
                .refreshToken(refreshToken)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        refreshTokenRepository.save(refreshTokenEntity);
    }

    // DELETE DB Refresh Token
    private void deleteOldRefreshTokenForUser(UUID userId, Integer allowedAccessCount) {
        refreshTokenRepository.deleteOldRefreshTokenForUser(userId.toString(), allowedAccessCount);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    private void errorResponse(HttpServletResponse response, HttpStatus status, String resMessage, String resMemo) throws IOException, ServletException {
        Message message = new Message();

        message.setStatus(status);
        message.setMessage(resMessage);
        message.setMemo(resMemo);

        String msg = new ObjectMapper().writeValueAsString(message);
        response.setStatus(message.getStatus().value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(msg);
        response.getWriter().flush();
    }
}
