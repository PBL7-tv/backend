package com.pbl7.identity_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtpResponse {
    String email;
    String otp;
}
