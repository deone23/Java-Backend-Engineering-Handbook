package com.javahandbook.hibernate_identity_vs_sequence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "employee_sequence_allocation")
public class EmployeeSequenceAllocation {

    @Id
    @SequenceGenerator(
            name = "employee_seq_allocation",
            sequenceName = "employee_seq_allocation",
            allocationSize = 50
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "employee_seq_allocation"
    )
    private Long id;

    private String name;

    public EmployeeSequenceAllocation() {
    }

    public EmployeeSequenceAllocation(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
