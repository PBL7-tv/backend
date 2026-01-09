package com.pbl7.identity_service.utils;

import java.util.Random;

public class OtpUtil {

    public static String generateOtp() {
        int length = 6;

        Random random = new Random();

        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10));
        }

        return otp.toString();
    }
}
