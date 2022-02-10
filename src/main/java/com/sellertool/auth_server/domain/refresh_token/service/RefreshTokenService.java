package com.sellertool.auth_server.domain.refresh_token.service;

import com.sellertool.auth_server.domain.exception.dto.InvalidUserAuthException;
import com.sellertool.auth_server.domain.refresh_token.entity.RefreshTokenEntity;
import com.sellertool.auth_server.domain.refresh_token.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    @Autowired
    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository
    ){
        this.refreshTokenRepository=refreshTokenRepository;
    }

    public RefreshTokenEntity searchRefreshToken(UUID refreshTokenId){
        return refreshTokenRepository.findById(refreshTokenId).orElseThrow(()->new InvalidUserAuthException("토큰이 만료 되었습니다."));
    }

    public void saveAndModify(RefreshTokenEntity refreshTokenEntity) {
        refreshTokenRepository.save(refreshTokenEntity);
    }
}
