package com.javahandbook.hibernate_identity_vs_sequence.service;

import org.springframework.stereotype.Service;

import com.javahandbook.hibernate_identity_vs_sequence.entity.EmployeeIdentity;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Service
public class DetachExperiment {

    private final EntityManager entityManager;

    public DetachExperiment(EntityManager entityManager) {
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

        System.out.println("===== DETACH EMPLOYEE =====");

        entityManager.detach(employee);

        employee.setName("Changed After Detach");

        System.out.println("===== AFTER SET NAME =====");

        entityManager.flush();

        System.out.println("===== AFTER FLUSH =====");
    }
}