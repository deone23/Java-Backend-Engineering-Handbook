package com.javahandbook.hibernate_identity_vs_sequence.service;

import org.springframework.stereotype.Service;

import com.javahandbook.hibernate_identity_vs_sequence.entity.EmployeeIdentity;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Service
public class MergeExperiment {

    private final EntityManager entityManager;

    public MergeExperiment(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public void run() {

        System.out.println("===== CREATE EMPLOYEE =====");

        EmployeeIdentity employee =
                new EmployeeIdentity("Original Employee");

        entityManager.persist(employee);
        entityManager.flush();

        System.out.println("Original ID = " + employee.getId());

        System.out.println("===== DETACH =====");

        entityManager.detach(employee);

        employee.setName("Updated Employee");

        System.out.println("Detached object name = "
                + employee.getName());

        System.out.println("===== MERGE =====");

        EmployeeIdentity managedEmployee =
                entityManager.merge(employee);

        System.out.println("Detached object == Managed object ? "
                + (employee == managedEmployee));

        System.out.println("Detached object name = "
                + employee.getName());

        System.out.println("Managed object name = "
                + managedEmployee.getName());

        System.out.println("===== FLUSH =====");

        entityManager.flush();

        System.out.println("===== AFTER FLUSH =====");
    }
}