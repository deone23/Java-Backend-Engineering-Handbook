package com.javahandbook.hibernate_identity_vs_sequence.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.javahandbook.hibernate_identity_vs_sequence.entity.EmployeeIdentity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.transaction.Transactional;

@Service
public class FlushCommitModeExperiment {

    private final EntityManager entityManager;

    public FlushCommitModeExperiment(EntityManager entityManager) {
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

        employee.setName("Changed Before Query");

        System.out.println("===== BEFORE QUERY =====");

        List<EmployeeIdentity> employees =
                entityManager.createQuery(
                        "SELECT e FROM EmployeeIdentity e",
                        EmployeeIdentity.class
                )
                .setFlushMode(FlushModeType.COMMIT)
                .getResultList();

        System.out.println("===== AFTER QUERY =====");
        System.out.println("Employees found = " + employees.size());

        System.out.println("===== END OF METHOD =====");
    }
}