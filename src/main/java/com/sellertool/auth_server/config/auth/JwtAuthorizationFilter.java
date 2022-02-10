package com.sellertool.auth_server.config.auth;

import com.sellertool.auth_server.config.exception.AuthorizationAccessDeniedException;
import com.sellertool.auth_server.domain.exception.dto.InvalidUserAuthException;
import com.sellertool.auth_server.domain.refresh_token.entity.RefreshTokenEntity;
import com.sellertool.auth_server.domain.refresh_token.repository.RefreshTokenRepository;
import com.sellertool.auth_server.domain.user.entity.UserEntity;
import com.sellertool.auth_server.domain.user.repository.UserRepository;
import com.sellertool.auth_server.utils.AuthTokenUtils;
import com.sellertool.auth_server.utils.CustomCookieInterface;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private UserRepository userRepository;
    private AuthTokenUtils tokenUtils;
    private RefreshTokenRepository refreshTokenRepository;
    private String accessTokenSecret;
    private String refreshTokenSecret;

    final static List<String> excludeUrls = Arrays.asList(
            "/auth/v1/csrf"
    );

    public JwtAuthorizationFilter(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            String accessTokenSecret,
            String refreshTokenSecret
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.accessTokenSecret = accessTokenSecret;
        this.refreshTokenSecret = refreshTokenSecret;
        this.tokenUtils = new AuthTokenUtils(accessTokenSecret, refreshTokenSecret);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();

//        CSRF 발급 등 excludeUrls 에 등록된 url 은 필터를 타지 않게 한다.
        if(excludeUrls.contains(path)){
            filterChain.doFilter(request, response);
            return;
        }

        Cookie jwtCookie = WebUtils.getCookie(request, "st_actoken");
        String[] ipAddress = this.getClientIpAddress(request).replaceAll(" ", "").split(",");
        String clientIp = ipAddress[0];

        if (jwtCookie == null) {
            filterChain.doFilter(request,response);
            return;
        }

        String accessToken = jwtCookie.getValue();

        Claims claims = null;

        try {
//            액세스 토큰 정보가 유효 하다면 컨텍스트 홀더에 저장 후 필터를 계속 타게 한다.
            claims = Jwts.parser().setSigningKey(accessTokenSecret).parseClaimsJws(accessToken).getBody();
            UUID id = UUID.fromString(claims.get("id").toString());
            String roles = claims.get("roles").toString();

            if (claims == null) {
                throw new AuthorizationAccessDeniedException("토큰이 만료 되었습니다.");
            }

            //        엑세스 토큰의 IP 와 접속 IP 가 일치하지 않다면 유효하지 않다고 판단 401 return
            if (!clientIp.equals(claims.get("ip"))) {
                throw new AuthorizationAccessDeniedException("토큰이 만료 되었습니다.");
            }

            UserEntity userEntity = UserEntity.builder()
                    .id(id)
                    .roles(roles)
                    .build();

            this.saveAuthenticationToSecurityContextHolder(userEntity);

            filterChain.doFilter(request, response);
            return;
        } catch (ExpiredJwtException e) {
            filterChain.doFilter(request,response);
            return;
        } catch (UnsupportedJwtException e) {
            throw new AuthorizationAccessDeniedException("토큰이 만료 되었습니다.");
        } catch (MalformedJwtException e) {
            throw new AuthorizationAccessDeniedException("토큰이 만료 되었습니다.");
        } catch (SignatureException e) {
            throw new AuthorizationAccessDeniedException("토큰이 만료 되었습니다.");
        } catch (IllegalArgumentException e) {
            throw new AuthorizationAccessDeniedException("토큰이 만료 되었습니다.");
        } catch (Exception e) {
            throw new AuthorizationAccessDeniedException("토큰이 만료 되었습니다.");
        }
    }

    private void saveAuthenticationToSecurityContextHolder(UserEntity userEntity) {
        PrincipalDetails principalDetails = new PrincipalDetails(userEntity);

        // Jwt 토큰 서명을 통해서 서명이 정상이면 Authentication 객체를 만들어준다.
        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null,
                principalDetails.getAuthorities());

        // 강제로 시큐리티의 세션에 접근하여 Authentication 객체를 저장.
        SecurityContextHolder.getContext().setAuthentication(authentication);
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
