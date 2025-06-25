package com.example.e_commerce.service;
import com.example.e_commerce.entity.User;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine){
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendVerificationEmail(User user, String siteUrl) {
        String toAddress = user.getEmail();
        String fromAddress = "your-email@example.com";
        String senderName = "YourAppName";
        String subject = "Lütfen Hesabınızı Doğrulayın";

        String verifyURL = siteUrl + "/verify?token=" + user.getVerificationToken();

        Context context = new Context();
        context.setVariable("name", user.getFullName());
        context.setVariable("verificationUrl", verifyURL);

        // Template içeriği oluşturuluyor
        String content = templateEngine.process("email-verification", context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom(fromAddress, senderName);
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
