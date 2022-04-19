package com.sellertool.auth_server.domain.user.dto;

import com.sellertool.auth_server.domain.user.entity.UserEntity;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private String nickname;
    private String roles;
    private String name;
    private String phoneNumber;

    public static UserDto toDto(UserEntity entity) {
        UserDto dto = UserDto.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .nickname(entity.getNickname())
                .name(entity.getName())
                .phoneNumber(entity.getPhoneNumber())
                .roles(entity.getRoles())
                .build();
        return dto;
    }
}
