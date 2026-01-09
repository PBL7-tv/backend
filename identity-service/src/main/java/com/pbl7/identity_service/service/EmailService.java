package com.pbl7.identity_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    @Autowired
    private TemplateEngine templateEngine;

    public String generateEmailContent(String username, String verificationUrl) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("verificationUrl", verificationUrl);

        // Render the Thymeleaf template to a String
        return templateEngine.process("welcome-email", context);
    }
}
