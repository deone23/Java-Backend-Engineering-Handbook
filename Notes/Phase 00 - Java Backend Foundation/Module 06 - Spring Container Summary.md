# Module 06 — Complete Backend Request Flow

> **Phase:** 00 - Java Backend Foundation
>
> **Difficulty:** Beginner → Intermediate
>
> **Estimated Revision Time:** 35 Minutes

---

# Learning Objectives

After completing this module, you should be able to:

- Explain the complete request lifecycle in a Spring Boot application.
- Identify the responsibility of every backend layer.
- Understand how Spring Beans collaborate.
- Visualize where DTOs, Entities, Mappers, Services, and Repositories fit together.
- Explain the internal execution flow from an HTTP request to the database and back.

---

# Problem Statement

Suppose a client sends the following request.

```http
POST /employees
```

```json
{
    "name": "Vinay Mourya",
    "email": "vinay@example.com",
    "salary": 1200000,
    "department": "Engineering"
}
```

Question:

> How does this request travel through the application?

---

# High-Level Architecture

```text
                Client
                   │
                   ▼
            HTTP Request
                   │
                   ▼
      EmployeeController
                   │
                   ▼
      EmployeeService
                   │
                   ▼
      EmployeeMapper
                   │
                   ▼
      Employee Entity
                   │
                   ▼
   EmployeeRepository
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

---

# Internal Execution

Suppose the application has already started.

During startup:

```text
Application Starts
        │
        ▼
Component Scan
        │
        ▼
Repository Bean Created
        │
        ▼
Service Bean Created
        │
        ▼
Controller Bean Created
        │
        ▼
Application Ready
```

Notice:

The application creates these Beans only once because they are Singleton Beans.

---

# Step 1 — Client Sends Request

```http
POST /employees
```

Request Body

```json
{
    "name": "Vinay",
    "email": "vinay@example.com",
    "salary": 1200000,
    "department": "Engineering"
}
```

Spring MVC receives the HTTP request.

---

# Step 2 — Request DTO Creation

Spring automatically converts JSON into a Java object.

```java
CreateEmployeeRequest request;
```

This object is **not a Spring Bean**.

It is a normal Java object created specifically for this request.

---

# Step 3 — Controller

```java
@PostMapping
public EmployeeResponse createEmployee(
        @RequestBody CreateEmployeeRequest request) {

    return service.createEmployee(request);

}
```

Responsibilities

- Accept HTTP requests.
- Delegate work to the Service.
- Return HTTP responses.

No business logic belongs here.

---

# Step 4 — Service

```java
public EmployeeResponse createEmployee(
        CreateEmployeeRequest request) {

}
```

Responsibilities

- Business validation.
- Business rules.
- Transaction management.
- Coordination between components.

Examples

- Salary must be positive.
- Email must be unique.
- Department must exist.

---

# Step 5 — Mapper

The Service calls

```java
Employee employee =
        mapper.toEntity(request);
```

The Mapper converts

```text
Request DTO

↓

Entity
```

The Mapper performs only object conversion.

---

# Step 6 — Entity

Example

```java
@Entity
public class Employee {

}
```

At this point

```text
Employee employee
```

is simply a Java object in JVM Heap Memory.

Hibernate does not know about it yet.

State

```text
Transient
```

---

# Step 7 — Repository

The Service calls

```java
repository.save(employee);
```

Now

Spring Data JPA

↓

JPA

↓

Hibernate

↓

Persistence Context

↓

SQL Generation

↓

JDBC

↓

Database

---

# Internal Hibernate Flow

```text
Employee Object

        │

        ▼

Persistence Context

        │

        ▼

Managed Entity

        │

        ▼

Dirty Checking

        │

        ▼

Flush

        │

        ▼

SQL Generation

        │

        ▼

JDBC

        │

        ▼

PostgreSQL
```

This entire process happens automatically.

---

# Step 8 — Database

Hibernate generates SQL.

Example

```sql
INSERT INTO employees
(
    name,
    email,
    salary,
    department
)
VALUES
(
    ?,
    ?,
    ?,
    ?
);
```

JDBC executes the SQL.

The database stores the row.

---

# Step 9 — Response Creation

Repository returns

```java
Employee
```

The Service calls

```java
mapper.toResponse(employee);
```

The Mapper converts

```text
Entity

↓

Response DTO
```

---

# Step 10 — Response Sent

Controller returns

```java
EmployeeResponse
```

Spring converts the Java object into JSON.

Example

```json
{
    "id": 1,
    "name": "Vinay",
    "email": "vinay@example.com"
}
```

The client receives the response.

---

# Complete Request Flow

```text
Client

↓

HTTP Request

↓

Spring MVC

↓

Controller

↓

Request DTO

↓

Service

↓

Business Validation

↓

Mapper

↓

Entity (Transient)

↓

Repository

↓

Hibernate

↓

Persistence Context

↓

Managed Entity

↓

Dirty Checking

↓

Flush

↓

JDBC

↓

PostgreSQL

↓

Entity

↓

Mapper

↓

Response DTO

↓

Controller

↓

JSON Response

↓

Client
```

---

# Responsibilities of Each Layer

| Layer | Responsibility |
|--------|----------------|
| Controller | Handle HTTP requests |
| Request DTO | Receive input |
| Service | Business logic |
| Mapper | Object conversion |
| Entity | Database representation |
| Repository | Database operations |
| Hibernate | ORM |
| JDBC | Execute SQL |
| PostgreSQL | Persist data |

---

# Internal Object Flow

```text
JSON

↓

Request DTO

↓

Entity

↓

Managed Entity

↓

Database Row

↓

Managed Entity

↓

Response DTO

↓

JSON
```

---

# Where Are Beans Used?

| Object | Spring Bean? |
|---------|--------------|
| Controller | ✅ Yes |
| Service | ✅ Yes |
| Repository | ✅ Yes |
| Mapper | ✅ Yes |
| Request DTO | ❌ No |
| Response DTO | ❌ No |
| Entity | ❌ No |

---

# Debugging Corner

### Problem

`NullPointerException` while calling the Repository.

### Common Causes

- Repository is not a Spring Bean.
- Manual object creation using `new`.
- Dependency Injection failed.

---

### Problem

Controller returns every database column.

### Cause

Entity returned directly.

### Solution

Return a Response DTO.

---

### Problem

Business validation inside the Controller.

### Solution

Move validation into the Service layer.

---

# Common Mistakes

> ⚠️ **Common Mistake**

❌ Returning Entity directly from the Controller.

✅ Return a Response DTO.

---

> ⚠️ **Common Mistake**

❌ Writing SQL inside the Service.

✅ Use the Repository.

---

> ⚠️ **Common Mistake**

❌ Creating Services manually using `new`.

✅ Let Spring create Beans.

---

> ⚠️ **Common Mistake**

❌ Mixing responsibilities between layers.

✅ Follow the layered architecture.

---

# Senior Engineer Notes

A Spring Boot application is a collaboration of specialized components.

Each layer has one primary responsibility.

Keeping layers independent leads to:

- Better maintainability
- Easier testing
- Loose coupling
- Better scalability
- Cleaner architecture

Senior engineers focus on preserving these boundaries as projects evolve.

---

# Interview Questions

## Beginner

- Explain the request flow in Spring Boot.
- What is the role of a Controller?
- Why do we use DTOs?

## Intermediate

- Explain the path from `repository.save()` to the database.
- Why does the Service call the Mapper?
- At what point does Hibernate become aware of an Entity?

## Senior

Draw the complete request lifecycle from an incoming HTTP request to the database and back.

Explain:

- Which objects are Spring Beans.
- Which objects are ordinary Java objects.
- Where Hibernate participates.
- Where JDBC participates.
- When SQL is generated.
- When the transaction commits.

---

# Knowledge Check

Before moving to Phase 01, verify that you can answer "Yes" to the following questions.

- [ ] Can I draw the complete backend request flow?
- [ ] Can I explain the responsibility of every layer?
- [ ] Can I explain when Hibernate becomes aware of an Entity?
- [ ] Can I explain where JDBC participates?
- [ ] Can I identify which objects are Spring Beans?
- [ ] Can I explain the complete execution flow from HTTP request to database and back?

---

# Module Summary

A Spring Boot application processes requests through a sequence of specialized layers.

The Controller handles HTTP communication.

The Service contains business logic.

The Mapper converts objects.

The Repository delegates persistence to Spring Data JPA.

Hibernate manages Entities, tracks changes through the Persistence Context, generates SQL, and delegates execution to JDBC.

Understanding this complete flow provides the mental model required for advanced topics such as Persistence Context, Transactions, Caching, and Microservices.

---

# Quick Revision

```text
Client

↓

Controller

↓

Request DTO

↓

Service

↓

Mapper

↓

Entity

↓

Repository

↓

Hibernate

↓

JDBC

↓

Database

↓

Entity

↓

Mapper

↓

Response DTO

↓

Client
```

---

## Related Modules

**Previous**

- Module 05 — Bean Scopes & Thread Safety

**Next**

- **Phase 01 — Hibernate & JPA Fundamentals**
    - Module 01 — Why ORM? JDBC vs JPA vs Hibernate

**Related Glossary Terms**

- Spring Bean
- Controller
- Service
- Repository
- DTO
- Entity
- Mapper
- Persistence Context
- Hibernate
- JPA
- JDBC
- Dirty Checking
- Flush