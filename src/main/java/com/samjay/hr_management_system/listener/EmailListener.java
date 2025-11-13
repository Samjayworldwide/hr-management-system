package com.samjay.hr_management_system.listener;

import com.samjay.hr_management_system.dtos.request.EmailDetails;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailListener {

    private final JavaMailSender javaMailSender;

    @Value("${email.username}")
    private String emailSender;

    @RabbitListener(queues = "${rabbitmq.queue.email.name}")
    public void sendEmail(EmailDetails emailDetails) {

        try {

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            mimeMessageHelper.setFrom(emailSender);

            mimeMessageHelper.setTo(emailDetails.getRecipient());

            mimeMessageHelper.setText(emailDetails.getMessageBody(), true);

            mimeMessageHelper.setSubject(emailDetails.getSubject());

            javaMailSender.send(mimeMessage);

            log.info("Email sent successfully to: {}", emailDetails.getRecipient());

        } catch (MessagingException e) {

            log.error("An error occurred sending email to: {}", emailDetails.getRecipient());

        }

    }
}
