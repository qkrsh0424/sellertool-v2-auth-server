package com.sellertool.auth_server.config.auth;

import java.util.Optional;

import com.sellertool.auth_server.domain.user.entity.UserEntity;
import com.sellertool.auth_server.domain.user.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PrincipalDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public PrincipalDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> userOpt = userRepository.findByUsername(username);
        
        if(userOpt.isPresent()) {
            return new PrincipalDetails(userOpt.get());
        } else {
            throw new UsernameNotFoundException("아이디 또는 패스워드를 확인해 주세요.");
        }
    }
}
