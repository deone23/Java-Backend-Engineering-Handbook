package com.javahandbook.hibernate_identity_vs_sequence.service;


import com.javahandbook.hibernate_identity_vs_sequence.entity.EmployeeSequence;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class EmployeeIdentityService {

    private final EntityManager entityManager;

    public EmployeeIdentityService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public void testIdentity() {

        EmployeeSequence employee = new EmployeeSequence("Employee-1");

        System.out.println("===== BEFORE persist =====");

        entityManager.persist(employee);

        System.out.println("===== AFTER persist =====");
        System.out.println("Employee ID = " + employee.getId());

        System.out.println("===== BEFORE flush =====");

        entityManager.flush();

        System.out.println("===== AFTER flush =====");
    }
}