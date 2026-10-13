package com.javahandbook.hibernate_identity_vs_sequence.service;

import org.springframework.stereotype.Service;

import com.javahandbook.hibernate_identity_vs_sequence.entity.EmployeeIdentity;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Service
public class FlushRollbackExperiment {

    private final EntityManager entityManager;

    public FlushRollbackExperiment(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public void run() {

        System.out.println("===== CREATE EMPLOYEE =====");

        EmployeeIdentity employee =
                new EmployeeIdentity("Original Employee");

        entityManager.persist(employee);

        entityManager.flush();

        System.out.println("Employee ID = " + employee.getId());

        employee.setName("Will Be Rolled Back");

        System.out.println("===== BEFORE UPDATE FLUSH =====");

        entityManager.flush();

        System.out.println("===== AFTER UPDATE FLUSH =====");

        throw new RuntimeException("Force rollback");
    }
}