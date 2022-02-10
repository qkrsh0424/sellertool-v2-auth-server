package com.sellertool.auth_server.domain.refresh_token.service;

import com.sellertool.auth_server.config.exception.AuthorizationAccessDeniedException;
import com.sellertool.auth_server.domain.exception.dto.InvalidUserAuthException;
import com.sellertool.auth_server.domain.refresh_token.entity.RefreshTokenEntity;
import com.sellertool.auth_server.domain.user.entity.UserEntity;
import com.sellertool.auth_server.utils.AuthTokenUtils;
import com.sellertool.auth_server.utils.CustomCookieInterface;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenBusinessService {
    @Value("${jwt.access.secret}")
    private String accessTokenSecret;

    @Value("${jwt.refresh.secret}")
    private String refreshTokenSecret;

    private final RefreshTokenService refreshTokenService;

    @Autowired
    public RefreshTokenBusinessService(
            RefreshTokenService refreshTokenService
    ) {
        this.refreshTokenService = refreshTokenService;
    }

    public void issueAccessToken(HttpServletRequest request, HttpServletResponse response) {
//        액세스 토큰 체크
        Cookie jwtCookie = WebUtils.getCookie(request, "st_actoken");
        String[] ipAddress = this.getClientIpAddress(request).replaceAll(" ", "").split(",");
        String clientIp = ipAddress[0];

        if (jwtCookie == null) {
            throw new InvalidUserAuthException("토큰이 만료 되었습니다.");
        }

        String accessToken = jwtCookie.getValue();

        Claims claims = null;
        boolean accessTokenExpired = false;

        try {
            claims = Jwts.parser().setSigningKey(accessTokenSecret).parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
//            액세스 토큰이 만료가 되었다면 리프레시 토큰 확인을 위해 accessTokenExpired = true 를 지정해준다.
            claims = e.getClaims();
            accessTokenExpired = true;
        } catch (UnsupportedJwtException e) {
            throw new InvalidUserAuthException("토큰이 만료 되었습니다.");
        } catch (MalformedJwtException e) {
            throw new InvalidUserAuthException("토큰이 만료 되었습니다.");
        } catch (SignatureException e) {
            throw new InvalidUserAuthException("토큰이 만료 되었습니다.");
        } catch (IllegalArgumentException e) {
            throw new InvalidUserAuthException("토큰이 만료 되었습니다.");
        } catch (Exception e) {
            throw new InvalidUserAuthException("토큰이 만료 되었습니다.");
        }

        //        만일에 액세스 토큰을 열람시 클레임이 없다면 401 return
        if (claims == null) {
            throw new InvalidUserAuthException("토큰이 만료 되었습니다.");
        }

//        엑세스 토큰의 IP 와 접속 IP 가 일치하지 않다면 유효하지 않다고 판단 401 return
        if (!clientIp.equals(claims.get("ip"))) {
            throw new InvalidUserAuthException("토큰이 만료 되었습니다.");
        }

        Object refreshTokenIdObj = claims.get("refreshTokenId");
        UUID REFRESH_TOKEN_ID = null;

        try {
            REFRESH_TOKEN_ID = UUID.fromString(refreshTokenIdObj.toString());
        } catch (IllegalArgumentException e) {
            throw new InvalidUserAuthException("토큰이 만료 되었습니다.");
        } catch (NullPointerException e) {
            throw new InvalidUserAuthException("토큰이 만료 되었습니다.");
        }

        //        액세스 토큰이 만료되어서 리프레시 토큰의 발급 여부를 확인한다.
        if (accessTokenExpired == true) {


            // 리프레시 토큰을 조회해 액세스 토큰 발급 여부 결정 - 액세스 클레임에서 refreshTokenId에 대응하는 RefreshToken값 조회
            RefreshTokenEntity refreshTokenEntity = refreshTokenService.searchRefreshToken(REFRESH_TOKEN_ID);

            Claims refreshTokenClaims = null;

            try {
                refreshTokenClaims = Jwts.parser().setSigningKey(refreshTokenSecret).parseClaimsJws(refreshTokenEntity.getRefreshToken()).getBody();
            } catch (Exception e) {
//                    리프레시 토큰이 유효하지 않다면 401 return
                throw new AuthorizationAccessDeniedException("토큰이 만료 되었습니다.");
            }

            UUID id = UUID.fromString(refreshTokenClaims.get("id").toString());
            String roles = refreshTokenClaims.get("roles").toString();

            AuthTokenUtils authTokenUtils = new AuthTokenUtils(accessTokenSecret, refreshTokenSecret);

            // 새로운 액세스 토큰과 리프레시 토큰을 발급
            String newAccessToken = authTokenUtils.getJwtAccessToken(id, roles, refreshTokenEntity.getId(), clientIp);
            String newRefreshToken = authTokenUtils.getJwtRefreshToken(id, roles, clientIp);

            refreshTokenEntity.setRefreshToken(newRefreshToken);
            refreshTokenEntity.setUpdatedAt(new Date(System.currentTimeMillis()));

            refreshTokenService.saveAndModify(refreshTokenEntity);

            ResponseCookie tokenCookie = ResponseCookie.from("st_actoken", newAccessToken)
                    .httpOnly(true)
                    .secure(CustomCookieInterface.SECURE)
                    .sameSite("Strict")
                    .domain(CustomCookieInterface.COOKIE_DOMAIN)
                    .path("/")
                    .maxAge(CustomCookieInterface.JWT_TOKEN_COOKIE_EXPIRATION)
                    .build();

            ResponseCookie tokenExpireTimeCookie = ResponseCookie.from("st_auth_exp", "st_auth_exp")
                    .domain(CustomCookieInterface.COOKIE_DOMAIN)
                    .secure(CustomCookieInterface.SECURE)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(CustomCookieInterface.JWT_TOKEN_COOKIE_EXPIRATION)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, tokenCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, tokenExpireTimeCookie.toString());
        }else{
            throw new InvalidUserAuthException("토큰이 만료 되었습니다.");
        }
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
}
