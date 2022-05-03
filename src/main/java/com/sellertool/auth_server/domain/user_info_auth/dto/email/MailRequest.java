package com.sellertool.auth_server.domain.user_info_auth.dto.email;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MailRequest {
    private String senderAddress;   // 발송자 Email주소
    private String senderName;  // 발송자 이름
    private String title;   // 제목
    private String body;    // 본문
    private List<ReceipientForRequest> recipients;  // 수신자목록
}
