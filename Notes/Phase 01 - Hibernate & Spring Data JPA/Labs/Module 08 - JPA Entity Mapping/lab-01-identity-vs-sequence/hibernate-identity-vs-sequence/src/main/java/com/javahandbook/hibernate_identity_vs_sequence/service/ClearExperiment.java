package com.javahandbook.hibernate_identity_vs_sequence.service;

import org.springframework.stereotype.Service;

import com.javahandbook.hibernate_identity_vs_sequence.entity.EmployeeIdentity;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Service
public class ClearExperiment {

    private final EntityManager entityManager;

    public ClearExperiment(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public void run() {

        System.out.println("===== CREATE EMPLOYEES =====");

        EmployeeIdentity employee1 =
                new EmployeeIdentity("Employee-1");

        EmployeeIdentity employee2 =
                new EmployeeIdentity("Employee-2");

        entityManager.persist(employee1);
        entityManager.persist(employee2);

        entityManager.flush();

        System.out.println("Employee 1 ID = " + employee1.getId());
        System.out.println("Employee 2 ID = " + employee2.getId());

        System.out.println("===== MODIFY BEFORE CLEAR =====");

        employee1.setName("Employee-1-Updated");
        employee2.setName("Employee-2-Updated");

        System.out.println("===== CLEAR PERSISTENCE CONTEXT =====");

        entityManager.clear();

        System.out.println("===== MODIFY AFTER CLEAR =====");

        employee1.setName("Employee-1-After-Clear");
        employee2.setName("Employee-2-After-Clear");

        entityManager.flush();

        System.out.println("===== AFTER FLUSH =====");
    }
}