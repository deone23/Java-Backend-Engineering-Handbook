package com.javahandbook.hibernate_identity_vs_sequence.service;

import org.springframework.stereotype.Service;

import com.javahandbook.hibernate_identity_vs_sequence.entity.EmployeeIdentity;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Service
public class RefreshExperiment {

    private final EntityManager entityManager;

    public RefreshExperiment(EntityManager entityManager) {
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
        System.out.println("Managed Name = " + employee.getName());

        System.out.println("===== CHANGE DATABASE DIRECTLY =====");

        entityManager.createNativeQuery(
                "UPDATE employee_identity " +
                "SET name = 'Changed Externally' " +
                "WHERE id = :id"
        )
        .setParameter("id", employee.getId())
        .executeUpdate();

        System.out.println("Managed Name BEFORE refresh = "
                + employee.getName());

        System.out.println("===== REFRESH =====");

        entityManager.refresh(employee);

        System.out.println("Managed Name AFTER refresh = "
                + employee.getName());
    }
}