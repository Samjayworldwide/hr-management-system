package com.samjay.hr_management_system.handlers;

import com.samjay.hr_management_system.dtos.request.EmailDetails;
import com.samjay.hr_management_system.event.OnboardingEmailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailEventHandler {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.email.name}")
    private String emailExchange;

    @Value("${rabbitmq.binding.email.name}")
    private String emailRoutingKey;

    @EventListener
    @Async("taskExecutor")
    public void sendEmail(OnboardingEmailEvent onboardingEmailEvent) {

        try {
            String message = getOnboardingSuccessEmail(onboardingEmailEvent.getWorkEmailAddress(), onboardingEmailEvent.getPassword());

            EmailDetails emailDetails = EmailDetails.builder()
                    .messageBody(message)
                    .recipient(onboardingEmailEvent.getPersonalEmailAddress())
                    .subject("ONBOARDING COMPLETION")
                    .build();

            rabbitTemplate.convertAndSend(emailExchange, emailRoutingKey, emailDetails);

            log.info("Onboarding email queued for: {}", onboardingEmailEvent.getPersonalEmailAddress());

        } catch (Exception e) {

            log.error("Failed to queue onboarding email for: {}", onboardingEmailEvent.getPersonalEmailAddress(), e);

        }

    }

    private static String getOnboardingSuccessEmail(String email, String password) {

        String loginLink = "/login";

        return "<html>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6;'>" +
                "<h2>Welcome to Our HR Management System!</h2>" +
                "<p>Congratulations! You have been successfully onboarded into our HR Management System.</p>" +
                "<p>Your account has been created and is ready to use. Below are your login credentials:</p>" +
                "<p><strong>Email:</strong> " + email + "</p>" +
                "<p><strong>Password:</strong> " + password + "</p>" +
                "<p><strong>Important:</strong> Please change your password after your first login for security purposes.</p>" +
                "<p>To get started, please click the link below to login to your dashboard and complete your profile:</p>" +
                "<p><a href='" + loginLink + "'>Login to Dashboard</a></p>" +
                "<p>Make sure to complete your profile information to ensure seamless access to all HR services and benefits.</p>" +
                "<p>If you have any questions or need assistance, please don't hesitate to contact our HR support team.</p>" +
                "<br>" +
                "<p>If you did not expect this email or believe it was sent in error, please contact HR immediately.</p>" +
                "</body>" +
                "</html>";
    }
}
