package com.pbl7.notification_service.service;

import com.pbl7.notification_service.dto.request.EmailRequest;
import com.pbl7.notification_service.dto.request.SendEmailRequest;
import com.pbl7.notification_service.dto.request.Sender;
import com.pbl7.notification_service.dto.response.EmailResponse;
import com.pbl7.notification_service.exception.AppException;
import com.pbl7.notification_service.exception.ErrorCode;
import com.pbl7.notification_service.repository.httpclient.EmailClient;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EmailService {

    EmailClient emailClient;


    @Value("${notification.email.brevo-apikey}")
    @NonFinal
    String apiKey;

    public EmailResponse sendEmail(SendEmailRequest request) {

        EmailRequest emailRequest = EmailRequest.builder()
                .sender(Sender.builder()
                        .name("VGear")
                        .email("hieungoc21793@gmail.com")
                        .build())
                .to(List.of(request.getTo()))
                .subject(request.getSubject())
                .htmlContent(request.getHtmlContent())
                .build();
        try {
            log.info("Sending email to " + request.getTo());
            return emailClient.sendEmail(apiKey, emailRequest);

        } catch (FeignException e){
            log.error("Failed to send email via Brevo: {}", e.contentUTF8());

            throw new AppException(ErrorCode.CANNOT_SEND_EMAIL);
        }
    }
}
