package com.sellertool.auth_server.config.auth;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final PrincipalDetailsService principalDetailsService;
    private final PasswordEncoder passwordEncoder;

    // 실제 인증에 대한 부분
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
//        System.out.println("========authenticate========");
        // AuthenticationFilter에서 생성된 토큰으로 아이디와 비밀번호 조회

        String USERNAME = token.getName();
        String PROVIDED_PASSWORD = token.getCredentials().toString();
        // UserDetailsService를 통해 DB에서 사용자 조회
        PrincipalDetails principalDetails = principalDetailsService.loadUserByUsername(USERNAME);

        String fullPassword = PROVIDED_PASSWORD + principalDetails.getSalt();

        // matches(입력값+salt를 인코딩한 값, DB에 인코딩되어 저장된 비밀번호)이 다르다면
        if (!passwordEncoder.matches(fullPassword, principalDetails.getPassword())) {
            throw new BadCredentialsException("아이디 또는 패스워드를 확인해 주세요.");
        }

        return new UsernamePasswordAuthenticationToken(principalDetails, fullPassword, principalDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
