package com.sellertool.auth_server.config.referer;

import com.sellertool.auth_server.config.exception.RefererAccessDeniedException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 수정
 * 1. 리퍼러 체크는 Get 요청은 넘긴다.
 */
public class RefererAuthenticationFilter extends OncePerRequestFilter {
    final static List<String> refererWhiteList = Arrays.asList(
            "http://localhost:3000",
            "https://www.sellertl.com"
    );


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
//        System.out.println("============RefererAuthenticationFilter============");
        if (request.getMethod().equals("GET")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String referer = request.getHeader("Referer") != null ? request.getHeader("Referer") : null;

            /**
             * http://www.example.com/api => http://www.example.com
             * http://google.com:8081/api => http://google.com:8081
             * http://www.google.com/api => http://www.google.com
             * http://www.www.google.com/api => http://www.www.google.com
             * http://www.google.com?/api => http://www.google.com
             * http://localhost:8081/api => http://localhost:8081
             * https://localhost.com/api/ => https://localhost.com
             * http://localhost/api/ => http://localhost
             * http://www.localhost/api => http://www.localhost
             * http://www.localhost.com/api => http://www.localhost.com
             * https://api.sellertool.io/api => https://api.sellertool.io
             * https://www.sellertool.io:80/api => https://www.sellertool.io:80
             * https://www.sellertool.io:443/api => https://www.sellertool.io:443
             * http://localhost:80/api => http://localhost:80
             */
            String regex = "https?:\\/\\/(?:localhost|(?:w{1,3}\\.)?[^\\s.]*(?:\\.[a-z]+))*(?::\\d+)?(?![^<]*(?:<\\/\\w+>|\\/?>))";
            Pattern refererPattern = Pattern.compile(regex);
            Matcher match = refererPattern.matcher(referer);

            if (!match.find()) {
                // 올바른 url 패턴이 아닌경우
                throw new RefererAccessDeniedException("Referer URL pattern not matched.");
            } else {
                // referer white list에 없는 도메인인 경우
                if (!refererWhiteList.contains(match.group())) {
                    throw new RefererAccessDeniedException("Referer access denied.");
                }
            }
        } catch (NullPointerException e) {
            throw new RefererAccessDeniedException("Referer not found.");
        } catch (IllegalStateException e) {
            throw new RefererAccessDeniedException("Referer not allowed.");
        }

        chain.doFilter(request, response);
    }
}
