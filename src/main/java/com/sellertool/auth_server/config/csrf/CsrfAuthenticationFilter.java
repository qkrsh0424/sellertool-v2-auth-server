package com.sellertool.auth_server.config.csrf;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellertool.auth_server.config.exception.CsrfAccessDeniedException;
import com.sellertool.auth_server.config.exception.CsrfExpiredJwtException;
import com.sellertool.auth_server.config.exception.CsrfNullPointerException;
import com.sellertool.auth_server.domain.exception.dto.AccessDeniedPermissionException;
import com.sellertool.auth_server.domain.message.dto.Message;
import com.sellertool.auth_server.utils.CsrfTokenUtils;

import io.jsonwebtoken.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 수정
 * 1. csrf 쿠키를 두개를 넘겨준다. (1) => csrf_jwt_token : csrf_token 값을 가진 JWT, (2) => csrf_token : UUID
 */
@Slf4j
public class CsrfAuthenticationFilter extends OncePerRequestFilter {

    private String csrfTokenSecret;

    public CsrfAuthenticationFilter(String csrfTokenSecret) {
        this.csrfTokenSecret = csrfTokenSecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // GET 메소드는 CsrfFilter를 타지 않는다
        if (request.getMethod().equals("GET")) {
            chain.doFilter(request, response);
            return;
        } else {
            try {
//            일종의 저장소
                Cookie csrfJwt = WebUtils.getCookie(request, "csrf_jwt");

                String csrfJwtToken = csrfJwt.getValue();
//            실제 CSRF 토큰 값
                String csrfToken = request.getHeader("X-XSRF-TOKEN");

                Claims claims = Jwts.parser().setSigningKey(csrfTokenSecret).parseClaimsJws(csrfJwtToken).getBody();

                // Cookie값과 csrf설정 헤더값이 동일하지 않다면
                if (!claims.get("csrfId").equals(csrfToken)) {
                    throw new CsrfAccessDeniedException("This is invalid Csrf token.");
                } else {
                    chain.doFilter(request, response);
                    return;
                }
            } catch (ExpiredJwtException e) {     // 토큰 만료
                throw new CsrfExpiredJwtException("Csrf jwt expired.");
            } catch (NullPointerException e) {   // CSRF 쿠키가 존재하지 않는다면
                throw new CsrfNullPointerException("Csrf cookie does not exist.");
            } catch(IllegalArgumentException e){
                throw new CsrfNullPointerException("Csrf jwt does not exist.");
            } catch (UnsupportedJwtException e){
                throw new CsrfAccessDeniedException("ClaimsJws argument does not represent an Claims JWS");
            } catch (MalformedJwtException e){
                throw new CsrfAccessDeniedException("ClaimsJws string is not a valid JWS. ");
            } catch (SignatureException e){
                throw new CsrfAccessDeniedException("ClaimsJws JWS signature validation fails");
            }
        }
    }
}
