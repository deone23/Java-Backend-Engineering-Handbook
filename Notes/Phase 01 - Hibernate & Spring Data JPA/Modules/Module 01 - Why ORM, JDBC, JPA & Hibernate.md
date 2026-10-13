# Module 01 - Why ORM, JDBC, JPA & Hibernate

> **Phase:** 01 - Hibernate & Spring Data JPA
>
> **Difficulty:** Beginner
>
> **Estimated Revision Time:** 45 Minutes

---

# Learning Objectives

After completing this module, you should be able to:

- Explain why ORM was introduced.
- Understand the limitations of plain JDBC.
- Differentiate between JDBC, JPA, Hibernate, and Spring Data JPA.
- Explain how data flows from a Java object to the database.
- Understand why changing the JPA Provider has minimal impact on business code.
- Build the mental model required for the rest of Hibernate.

---

# Problem Statement

Suppose we want to save an employee into PostgreSQL.

Without any framework, we need to:

- Open a database connection.
- Write SQL manually.
- Set query parameters.
- Execute the SQL.
- Handle exceptions.
- Close resources.

Example:

```java
Connection connection = DriverManager.getConnection(...);

PreparedStatement statement = connection.prepareStatement(
    "INSERT INTO employees(name, salary) VALUES (?, ?)"
);

statement.setString(1, employee.getName());
statement.setDouble(2, employee.getSalary());

statement.executeUpdate();

statement.close();
connection.close();
```

Imagine writing this code for hundreds of tables.

There must be a better way.

---

# What is ORM?

**ORM (Object Relational Mapping)** is a technique that maps Java objects to database tables.

Instead of writing SQL for every operation, developers work with Java objects.

Example:

```java
Employee employee = new Employee();

employee.setName("Vinay");
employee.setSalary(1200000);

employeeRepository.save(employee);
```

Hibernate generates the SQL automatically.

---

# Why Was ORM Introduced?

JDBC works well, but it becomes difficult to maintain in large enterprise applications.

Common problems:

- Repetitive SQL.
- Boilerplate code.
- Manual object mapping.
- Resource management.
- Database-specific SQL.
- Reduced developer productivity.

ORM allows developers to focus on business logic instead of persistence details.

---

# JDBC vs ORM

| Feature | JDBC | ORM (Hibernate) |
|----------|------|-----------------|
| SQL Writing | Manual | Automatic |
| Object Mapping | Manual | Automatic |
| Connection Handling | Manual | Managed |
| Boilerplate Code | High | Low |
| Productivity | Lower | Higher |
| Database Portability | Limited | Better |

---

# What is JDBC?

**JDBC (Java Database Connectivity)** is the standard Java API for communicating with relational databases.

Responsibilities:

- Open database connections.
- Execute SQL statements.
- Read query results.
- Close resources.

JDBC does **not** perform Object Relational Mapping.

---

# What is JPA?

**JPA (Jakarta Persistence API)** is a specification that defines how Java objects should be persisted.

JPA is **not** an implementation.

It only defines contracts.

Example:

```java
entityManager.persist(employee);
```

JPA specifies what `persist()` should do.

It does **not** define how it should be implemented.

---

# What is Hibernate?

Hibernate is the most popular implementation of the JPA specification.

Responsibilities:

- Object Relational Mapping
- Persistence Context management
- Dirty Checking
- SQL generation
- Caching
- Lazy Loading
- Transaction synchronization

Hibernate performs the actual work behind JPA.

---

# What is Spring Data JPA?

Spring Data JPA sits above JPA and provides repository abstractions.

Instead of writing DAO classes manually, developers create interfaces.

Example:

```java
public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

}
```

Spring automatically provides implementations for common CRUD operations.

---

# Relationship Between the Technologies

```text
Application Code
        │
        ▼
Spring Data JPA
        │
        ▼
JPA Specification
        │
        ▼
Hibernate
        │
        ▼
JDBC
        │
        ▼
PostgreSQL
```

Each layer has a different responsibility.

---

# Responsibilities of Each Layer

| Layer | Responsibility |
|--------|----------------|
| Spring Data JPA | Repository abstraction |
| JPA | Persistence specification |
| Hibernate | ORM implementation |
| JDBC | Execute SQL |
| PostgreSQL | Store data |

---

# Internal Execution Flow

Consider:

```java
employeeRepository.save(employee);
```

Execution flow:

```text
Application

↓

Spring Data JPA

↓

JPA

↓

Hibernate

↓

JDBC

↓

PostgreSQL
```

Even though we call only one method, multiple layers participate internally.

---

# Changing the JPA Provider

Suppose the project replaces Hibernate with EclipseLink.

Most changes are limited to:

- `pom.xml`
- `application.yml`
- Provider-specific configuration

Business logic remains largely unchanged because it depends on JPA contracts rather than Hibernate APIs.

---

# Why Doesn't the Service Layer Change?

Consider:

```java
employeeRepository.save(employee);
```

The Service interacts with the repository through JPA abstractions.

As long as the new provider correctly implements the JPA specification, the Service code does not need modification.

This demonstrates the benefit of programming against interfaces rather than implementations.

---

# Real-World Architecture

```text
Controller

↓

Service

↓

Spring Data JPA

↓

JPA

↓

Hibernate

↓

JDBC

↓

PostgreSQL
```

This layered architecture improves maintainability and allows implementation details to change independently.

---

# Debugging Corner

### Problem

Developers replace Hibernate with another JPA provider.

Question:

Should every Service class be rewritten?

Answer:

No.

Only provider-specific configuration changes in most applications because the business layer depends on JPA contracts.

---

# Common Mistakes

> ⚠️ **Common Mistake**

❌ Thinking that JPA is an ORM framework.

✅ JPA is only a specification.

---

> ⚠️ **Common Mistake**

❌ Thinking that Hibernate replaces JDBC.

✅ Hibernate internally uses JDBC to execute SQL.

---

> ⚠️ **Common Mistake**

❌ Thinking Spring Data JPA is Hibernate.

✅ Spring Data JPA is a higher-level abstraction built on top of JPA.

---

# Senior Engineer Notes

Enterprise applications should depend on abstractions whenever possible.

The business layer should remain independent of the underlying ORM implementation.

Replacing Hibernate with another JPA provider should require minimal changes outside configuration and provider-specific optimizations.

This loose coupling improves maintainability and reduces vendor lock-in.

---

# Interview Questions

## Beginner

- What is ORM?
- Why was ORM introduced?
- What is JDBC?
- What is JPA?
- What is Hibernate?

## Intermediate

- Explain the relationship between Spring Data JPA, JPA, Hibernate, and JDBC.
- Why doesn't replacing Hibernate usually affect the Service layer?
- Why is JPA called a specification?

## Senior

Your organization wants to migrate from Hibernate to EclipseLink.

Explain:

- Which layers are affected.
- Which layers remain unchanged.
- Why programming against JPA abstractions reduces migration effort.

---

# Knowledge Check

Before moving to the next module, verify that you can answer "Yes" to the following questions.

- [ ] Can I explain why ORM was introduced?
- [ ] Can I differentiate JDBC and Hibernate?
- [ ] Can I explain the purpose of JPA?
- [ ] Can I explain why Hibernate still uses JDBC?
- [ ] Can I describe the execution flow from `save()` to the database?
- [ ] Can I explain why changing the JPA Provider rarely affects business code?

---

# Module Summary

ORM simplifies database interaction by allowing developers to work with Java objects instead of writing SQL manually.

JPA defines the persistence contracts, Hibernate implements those contracts, Spring Data JPA provides convenient repository abstractions, and JDBC executes the generated SQL against the database.

Understanding the responsibility of each layer builds the foundation for learning Persistence Context, Entity Lifecycle, Dirty Checking, Transactions, and advanced Hibernate internals.

---

# Quick Revision

```text
Application

↓

Spring Data JPA

↓

JPA

↓

Hibernate

↓

JDBC

↓

PostgreSQL
```

| Technology | Purpose |
|------------|---------|
| JDBC | Execute SQL |
| JPA | Persistence Specification |
| Hibernate | ORM Implementation |
| Spring Data JPA | Repository Abstraction |

---

## Related Modules

**Previous**

- Phase 00 - Module 06 - Complete Backend Request Flow

**Next**

- Module 02 - Entity Lifecycle & Persistence Context

**Related Glossary Terms**

- ORM
- JDBC
- JPA
- Hibernate
- Spring Data JPA
- Repository
- Persistence