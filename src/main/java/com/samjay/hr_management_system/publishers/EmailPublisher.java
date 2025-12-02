package com.samjay.hr_management_system.publishers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samjay.hr_management_system.dtos.request.EmailDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

import static com.samjay.hr_management_system.constants.Constant.EMAIL_QUEUE;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailPublisher {

    private final Sender sender;

    private final ObjectMapper objectMapper;


    public Mono<Void> queueEmail(EmailDetails emailDetails) {

        return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(emailDetails))
                .flatMap(bytes -> {

                    Flux<OutboundMessage> outbound = Flux.just(new OutboundMessage("", EMAIL_QUEUE, bytes));

                    return sender.sendWithPublishConfirms(outbound)
                            .doOnNext(result -> {

                                if (result.isAck())
                                    log.info("Email queued successfully for: {}", emailDetails.getRecipient());

                                else
                                    log.error("Email failed to queue for: {}", emailDetails.getRecipient());
                            })
                            .doOnError(e -> log.error("Error queueing email", e))
                            .then();
                })
                .onErrorResume(JsonProcessingException.class, e -> {

                    log.error("Failed to serialize email details", e);

                    return Mono.empty();

                });
    }
}
