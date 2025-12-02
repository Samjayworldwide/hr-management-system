package com.samjay.hr_management_system.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Connection;
import com.samjay.hr_management_system.dtos.request.EmailDetails;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.Receiver;

import java.util.Objects;

import static com.samjay.hr_management_system.constants.Constant.EMAIL_QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailListener {

    private final Receiver receiver;

    private final ObjectMapper objectMapper;

    private final Mono<Connection> connectionMono;

    private final JavaMailSender javaMailSender;

    private Disposable subscription;

    @Value("${email.username}")
    private String emailSender;

    @PostConstruct
    public void startListening() {

        log.info("Starting email listener for queue: {}", EMAIL_QUEUE);

        subscription = receiver.consumeAutoAck(EMAIL_QUEUE)
                .flatMap(message -> {

                    log.info("Received payroll message: {}", new String(message.getBody()));

                    try {

                        EmailDetails emailDetails = objectMapper.readValue(message.getBody(), EmailDetails.class);

                        return sendEmail(emailDetails);

                    } catch (Exception e) {

                        log.error("Error deserializing email message", e);

                        return Mono.empty();

                    }
                })
                .doOnError(e -> log.error("Error processing email message", e))
                .subscribe();
    }

    private Mono<Void> sendEmail(EmailDetails emailDetails) {

        return Mono.fromRunnable(() -> {

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

                        log.error("Failed to send email to: {}", emailDetails.getRecipient(), e);

                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @PreDestroy
    public void stopListening() {

        log.info("Stopping email listener");

        if (subscription != null && !subscription.isDisposed()) {

            subscription.dispose();

        }

        try {

            Objects.requireNonNull(connectionMono.block()).close();

        } catch (Exception e) {

            log.error("Error closing RabbitMQ connection", e);

        }
    }
}
