package com.sellertool.auth_server.domain.user.dto;

import com.sellertool.auth_server.domain.user.entity.UserEntity;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    @NotBlank
    private UUID id;

    @NotBlank
    private String username;

    @NotBlank
    private String nickname;

    @NotBlank
    private String roles;

    private String name;

    @NotBlank
    private String email;

    @NotBlank
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
