package com.samjay.hr_management_system.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OnboardingEmailEvent extends ApplicationEvent {

    private final String personalEmailAddress;

    private final String workEmailAddress;

    private final String firstName;

    private final String password;

    public OnboardingEmailEvent(Object source, String personalEmailAddress, String workEmailAddress, String firstName, String password) {

        super(source);

        this.personalEmailAddress = personalEmailAddress;

        this.workEmailAddress = workEmailAddress;

        this.firstName = firstName;

        this.password = password;
    }
}
