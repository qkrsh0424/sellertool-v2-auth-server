package com.sellertool.auth_server.domain.user.repository;

import java.util.Optional;
import java.util.UUID;

import com.sellertool.auth_server.domain.user.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer>, UserRepositoryCustom{
    
    UserEntity findByEmailAndPassword(String email, String password);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findById(UUID id);

}
