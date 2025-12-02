package com.samjay.hr_management_system.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Connection;
import com.samjay.hr_management_system.dtos.request.PayrollMessage;
import com.samjay.hr_management_system.services.PayrollService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;

import java.util.Objects;

import static com.samjay.hr_management_system.constants.Constant.PAYROLL_QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayrollListener {

    private final Receiver receiver;

    private final ObjectMapper objectMapper;

    private final Mono<Connection> connectionMono;

    private final PayrollService payrollService;

    private Disposable subscription;

    @PostConstruct
    public void startListening() {

        log.info("Starting payroll listener for queue: {}", PAYROLL_QUEUE);

        subscription = receiver.consumeAutoAck(PAYROLL_QUEUE)
                .flatMap(message -> {

                    log.info("Received payroll message: {}", new String(message.getBody()));

                    try {

                        PayrollMessage payrollMessage = objectMapper.readValue(message.getBody(), PayrollMessage.class);

                        return payrollService.processPayroll(payrollMessage);

                    } catch (Exception e) {

                        log.error("Failed to deserialize payroll message", e);

                        return Mono.empty();
                    }

                })
                .doOnError(e -> log.error("Error processing payroll message", e))
                .subscribe();
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
