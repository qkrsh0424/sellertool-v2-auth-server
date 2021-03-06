package com.sellertool.auth_server.config.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellertool.auth_server.config.exception.AuthorizationAccessDeniedException;
import com.sellertool.auth_server.domain.message.dto.Message;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthorizationExceptionFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        System.out.println("============JwtAuthorizationExceptionFilter============");
        try {
            filterChain.doFilter(request, response);
        } catch (AuthorizationAccessDeniedException e) {
            errorResponse(response, HttpStatus.UNAUTHORIZED, "auth_failed", e.getMessage());
            return;
        }
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
