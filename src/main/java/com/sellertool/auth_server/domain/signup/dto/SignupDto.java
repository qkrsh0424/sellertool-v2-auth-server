package com.sellertool.auth_server.domain.signup.dto;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupDto {
    private String username;
    private String password;
    private String passwordCheck;
    private String nickname;
}
