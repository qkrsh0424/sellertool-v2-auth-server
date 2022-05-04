package com.sellertool.auth_server.domain.naver.email.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NaverEmailRequestDto {
    private String senderAddress;   // 발송자 Email주소
    private String senderName;  // 발송자 이름
    private String title;   // 제목
    private String body;    // 본문
    private List<ReceipientForRequest> recipients;  // 수신자목록

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ReceipientForRequest {
        private String address; // 수신자 Email주소
        private String type;    // 수신자 유형(R: 수신자, C: 참조인, B: 숨은참조)
    }
}
