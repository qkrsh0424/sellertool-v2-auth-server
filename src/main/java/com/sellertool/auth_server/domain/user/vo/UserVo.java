package com.sellertool.auth_server.domain.user.vo;

import com.sellertool.auth_server.domain.user.entity.UserEntity;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserVo {
    private UUID id;
    private String username;
    private String email;
    private String nickname;
    private String roles;

    public static UserVo toVo(UserEntity entity) {
        UserVo vo = UserVo.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .nickname(entity.getNickname())
                .roles(entity.getRoles())
                .build();
        return vo;
    }
}
