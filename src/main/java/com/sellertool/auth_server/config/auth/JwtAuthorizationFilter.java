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

//        CSRF ?????? ??? excludeUrls ??? ????????? url ??? ????????? ?????? ?????? ??????.
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
//            ????????? ?????? ????????? ?????? ????????? ???????????? ????????? ?????? ??? ????????? ?????? ?????? ??????.
            claims = Jwts.parser().setSigningKey(accessTokenSecret).parseClaimsJws(accessToken).getBody();
            UUID id = UUID.fromString(claims.get("id").toString());
            String roles = claims.get("roles").toString();

            if (claims == null) {
                throw new AuthorizationAccessDeniedException("????????? ?????? ???????????????.");
            }

            //        ????????? ????????? IP ??? ?????? IP ??? ???????????? ????????? ???????????? ????????? ?????? 401 return
            if (!clientIp.equals(claims.get("ip"))) {
                throw new AuthorizationAccessDeniedException("????????? ?????? ???????????????.");
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
            throw new AuthorizationAccessDeniedException("????????? ?????? ???????????????.");
        } catch (MalformedJwtException e) {
            throw new AuthorizationAccessDeniedException("????????? ?????? ???????????????.");
        } catch (SignatureException e) {
            throw new AuthorizationAccessDeniedException("????????? ?????? ???????????????.");
        } catch (IllegalArgumentException e) {
            throw new AuthorizationAccessDeniedException("????????? ?????? ???????????????.");
        } catch (Exception e) {
            throw new AuthorizationAccessDeniedException("????????? ?????? ???????????????.");
        }
    }

    private void saveAuthenticationToSecurityContextHolder(UserEntity userEntity) {
        PrincipalDetails principalDetails = new PrincipalDetails(userEntity);

        // Jwt ?????? ????????? ????????? ????????? ???????????? Authentication ????????? ???????????????.
        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null,
                principalDetails.getAuthorities());

        // ????????? ??????????????? ????????? ???????????? Authentication ????????? ??????.
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
