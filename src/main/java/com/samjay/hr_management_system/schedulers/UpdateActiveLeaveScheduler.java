package com.samjay.hr_management_system.schedulers;

import com.samjay.hr_management_system.repositories.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateActiveLeaveScheduler {

    private final LeaveRequestRepository leaveRequestRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "Africa/Lagos")
    public void updateActiveLeaves() {

        log.info("Starting update of active leave requests...");

        leaveRequestRepository.findAllByIsActive(true)
                .flatMap(leaveRequest -> {

                    if (leaveRequest.getExpectedReturnDate().isBefore(LocalDate.now())) {

                        leaveRequest.setActive(false);

                        return leaveRequestRepository.save(leaveRequest);

                    }

                    return Mono.empty();

                })
                .doOnNext(updatedLeave -> log.info("Deactivated leave request {}", updatedLeave.getId()))
                .doOnError(error -> log.error("Error while updating active leave requests", error))
                .doOnComplete(() -> log.info("Daily active leave update completed."))
                .subscribe();
    }
}
