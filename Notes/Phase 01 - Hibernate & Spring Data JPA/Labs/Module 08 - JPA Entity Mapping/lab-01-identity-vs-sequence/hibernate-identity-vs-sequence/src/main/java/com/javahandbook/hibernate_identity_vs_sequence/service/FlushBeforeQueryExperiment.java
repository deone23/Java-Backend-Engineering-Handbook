package com.javahandbook.hibernate_identity_vs_sequence.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.javahandbook.hibernate_identity_vs_sequence.entity.EmployeeIdentity;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Service
public class FlushBeforeQueryExperiment {

    private final EntityManager entityManager;

    public FlushBeforeQueryExperiment(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public void run() {


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
                ).getResultList();

        System.out.println("===== AFTER QUERY =====");
        System.out.println("Employees found = " + employees.size());
    }
}