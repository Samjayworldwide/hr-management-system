package com.samjay.hr_management_system.initializers;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.Sender;

import static com.samjay.hr_management_system.constants.Constant.PAYROLL_QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayrollQueueInitializer {

    private final Sender sender;

    @PostConstruct
    public void declareQueue() {

        sender.declareQueue(QueueSpecification.queue(PAYROLL_QUEUE).durable(true))
                .doOnSuccess(declareOk -> log.info("Queue '{}' declared successfully", PAYROLL_QUEUE))
                .doOnError(e -> log.error("Failed to declare queue '{}'", PAYROLL_QUEUE, e))
                .block();
    }
}
