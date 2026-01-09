package com.pbl7.identity_service.controller;


import com.nimbusds.jose.JOSEException;
import com.pbl7.identity_service.dto.ApiResponse;
import com.pbl7.identity_service.dto.request.*;
import com.pbl7.identity_service.dto.response.AuthenticationResponse;
import com.pbl7.identity_service.dto.response.IntrospectResponse;
import com.pbl7.identity_service.dto.response.OtpResponse;
import com.pbl7.identity_service.service.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request) {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/send-otp")
     ApiResponse<String> sendOtp(@RequestBody OtpRequest req) throws Exception {
        return ApiResponse.<String>builder().result( authenticationService.sendOtp(req.getEmail())).build();
    }

    @PostMapping("/reset")
    ApiResponse<String> resetPassword(@RequestBody ResetPasswordRequest req ) throws Exception {
        authenticationService.resetPassword(req);
        return ApiResponse.<String>builder().result("reset password sent success").build();
    }
}
