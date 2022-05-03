package com.sellertool.auth_server.domain.user_info_auth.dto.email;

import lombok.*;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReceipientForRequest {
    private String address; // 수신자 Email주소
    private String type;    // 수신자 유형(R: 수신자, C: 참조인, B: 숨은참조)
}
