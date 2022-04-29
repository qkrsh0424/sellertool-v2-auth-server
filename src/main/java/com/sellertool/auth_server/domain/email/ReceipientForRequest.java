package com.sellertool.auth_server.domain.email;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReceipientForRequest {
    private String address; // 수신자 Email주소
    String type;    // 수신자 유형(R: 수신자, C: 참조인, B: 숨은참조)
}
