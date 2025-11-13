package com.samjay.hr_management_system.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity {

    @Column(value = "date_created")
    private LocalDateTime dateCreated;

    @Column(value = "date_updated")
    private LocalDateTime dateUpdated;

}
