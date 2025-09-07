package com.waduclay.newsletteragentspringai.email;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:developer.wadu@gmail.com">Willdom Kahari</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String emailSender;
    @Value("${spring.mail.recipient}")
    private String emailRecipient;


    public void sendEmail(
            String subject,
            String message
    ) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_RELATED, StandardCharsets.UTF_8.name());
        messageHelper.setFrom(emailSender);

        messageHelper.setSubject(subject);

        try {
            messageHelper.setText(message, true);
            messageHelper.setTo(emailRecipient);
            mailSender.send(mimeMessage);
            log.info("Email sent successfully to: {}", emailRecipient);
        } catch (MessagingException e) {
            log.warn("Cannot send email to: {}", emailRecipient);
        }
    }

}
