package com.javahandbook.hibernate_identity_vs_sequence.service;

import org.springframework.stereotype.Service;

import com.javahandbook.hibernate_identity_vs_sequence.entity.EmployeeIdentity;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Service
public class DirtyCheckingExperiment {

    private final EntityManager entityManager;

    public DirtyCheckingExperiment(EntityManager entityManager) {
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

        System.out.println("===== BEFORE CHANGE =====");
        System.out.println("Employee Name = " + employee.getName());

        employee.setName("C");

        System.out.println("===== AFTER SET NAME =====");

        System.out.println("No flush yet...");

        System.out.println("===== END OF METHOD =====");
        
    }

}