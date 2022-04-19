package com.sellertool.auth_server.config.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellertool.auth_server.config.exception.CsrfAccessDeniedException;
import com.sellertool.auth_server.config.exception.CsrfExpiredJwtException;
import com.sellertool.auth_server.config.exception.CsrfNullPointerException;
import com.sellertool.auth_server.domain.message.dto.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class CsrfExceptionFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        System.out.println("============CsrfExceptionFilter============");
        try{
            filterChain.doFilter(request, response);
        } catch (CsrfNullPointerException | CsrfAccessDeniedException | CsrfExpiredJwtException e){
            log.error(e.getMessage());
            errorResponse(response, HttpStatus.FORBIDDEN, "invalid_csrf", e.getMessage());
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
