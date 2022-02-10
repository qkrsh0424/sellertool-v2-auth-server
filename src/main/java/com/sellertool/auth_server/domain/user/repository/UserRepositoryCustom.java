package com.sellertool.auth_server.domain.user.repository;

import com.sellertool.auth_server.domain.user.entity.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface UserRepositoryCustom {
    List<UserEntity> qSelectList(Map<String, Object> params);
}
