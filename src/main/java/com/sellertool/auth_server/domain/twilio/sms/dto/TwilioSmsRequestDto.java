package com.sellertool.auth_server.domain.twilio.sms.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TwilioSmsRequestDto {
    private String sendPhoneNumber;   // 전화번호
    private String sendMessage;  // 메세지
}
