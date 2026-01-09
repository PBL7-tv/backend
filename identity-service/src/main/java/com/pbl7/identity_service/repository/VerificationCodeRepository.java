package com.pbl7.identity_service.repository;

import com.pbl7.identity_service.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    VerificationCode findByEmail(String email);

    VerificationCode findByOtp(String otp);
}
