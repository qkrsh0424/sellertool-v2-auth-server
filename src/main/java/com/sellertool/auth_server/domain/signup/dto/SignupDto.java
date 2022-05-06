package com.sellertool.auth_server.domain.signup.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupDto {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String passwordCheck;

    @NotBlank
    private String nickname;

    @NotBlank
    private String email;

    private boolean verifiedEmail;
}
