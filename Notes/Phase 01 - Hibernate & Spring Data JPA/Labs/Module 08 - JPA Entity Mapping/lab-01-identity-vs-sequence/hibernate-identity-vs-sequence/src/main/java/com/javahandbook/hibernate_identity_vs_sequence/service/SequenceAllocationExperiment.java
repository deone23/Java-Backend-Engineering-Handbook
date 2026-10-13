package com.javahandbook.hibernate_identity_vs_sequence.service;


import com.javahandbook.hibernate_identity_vs_sequence.entity.EmployeeSequenceAllocation;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class SequenceAllocationExperiment {

    private final EntityManager entityManager;

    public SequenceAllocationExperiment(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public void run() {

        System.out.println("===== START SEQUENCE ALLOCATION EXPERIMENT =====");

        for (int i = 1; i <= 105; i++) {

            EmployeeSequenceAllocation employee =
                    new EmployeeSequenceAllocation("Employee-" + i);

            entityManager.persist(employee);

            System.out.println(
                    "Persisted Employee-" + i +
                    " | ID = " + employee.getId()
            );
        }

        System.out.println("===== BEFORE FLUSH =====");

        entityManager.flush();

        System.out.println("===== AFTER FLUSH =====");
    }
}
