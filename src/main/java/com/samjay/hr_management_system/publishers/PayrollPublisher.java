package com.samjay.hr_management_system.publishers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samjay.hr_management_system.dtos.request.PayrollMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

import static com.samjay.hr_management_system.constants.Constant.PAYROLL_QUEUE;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollPublisher {

    private final Sender sender;

    private final ObjectMapper objectMapper;

    public Mono<Void> queuePayroll(PayrollMessage payrollMessage) {

        return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(payrollMessage))
                .flatMap(bytes -> {

                    Flux<OutboundMessage> outbound = Flux.just(new OutboundMessage("", PAYROLL_QUEUE, bytes));

                    return sender.sendWithPublishConfirms(outbound)
                            .doOnNext(result -> {

                                if (result.isAck())
                                    log.info("Payroll message queued successfully for employeeId: {}", payrollMessage.getEmployeeId());

                                else
                                    log.error("Payroll message failed to queue for employeeId: {}", payrollMessage.getEmployeeId());
                            })
                            .doOnError(e -> log.error("Error queueing payroll message for employeeId: {}", payrollMessage.getEmployeeId(), e))
                            .then();
                })
                .onErrorResume(JsonProcessingException.class, e -> {

                    log.error("Failed to serialize payroll message for employeeId: {}", payrollMessage.getEmployeeId(), e);

                    return Mono.empty();
                });
    }
}
