# Module 01 — Backend Architecture

> **Phase:** 00 - Java Backend Foundation
>
> **Difficulty:** Beginner → Intermediate
>
> **Estimated Revision Time:** 20 Minutes

---

## Learning Objectives

After completing this module, you should be able to:

- Explain why backend applications exist.
- Describe the responsibility of each backend layer.
- Draw the complete request flow of a Spring Boot application.
- Explain why Controllers, Services, Repositories, DTOs, Entities and Mappers are separated.
- Explain this architecture during an interview.

---

## Problem Statement

Imagine opening Amazon and clicking:

```
Add to Cart
```

The browser cannot:

- Validate business rules
- Save data
- Calculate discounts
- Process payments
- Store orders

Something must perform these operations.

That "something" is the **Backend Application**.

---

## What is a Backend Application?

A backend application is responsible for:

- Receiving client requests
- Applying business rules
- Interacting with databases
- Returning responses

Think of it as the brain of an application.

---

## Real-World Analogy

Imagine withdrawing cash from an ATM.

```text
Customer
   │
   ▼
Insert Card
   │
   ▼
Enter PIN
   │
   ▼
Validate PIN
   │
   ▼
Check Balance
   │
   ▼
Debit Account
   │
   ▼
Dispense Cash
```

A backend application behaves in a very similar way.

---

## High-Level Backend Architecture

```text
                Client
           (Web / Mobile)
                  │
                  ▼
            HTTP Request
                  │
                  ▼
            Controller
                  │
                  ▼
              Service
                  │
                  ▼
             Repository
                  │
                  ▼
             PostgreSQL
                  ▲
                  │
            HTTP Response
```

---

## Layer Responsibilities

| Layer | Responsibility |
|--------|----------------|
| Controller | Handles HTTP requests and responses |
| DTO | Transfers data between client and backend |
| Service | Contains business logic |
| Mapper | Converts DTOs ↔ Entities |
| Repository | Performs database operations |
| Entity | Represents database tables |
| Database | Stores application data |

Each layer has **one responsibility only**.

This follows the **Single Responsibility Principle (SRP)**.

---

## Complete Request Flow

Example:

```
POST /employees
```

Request Body

```json
{
  "name": "Vinay Mourya",
  "email": "vinay@example.com",
  "salary": 1200000,
  "department": "Engineering"
}
```

Execution Flow

```text
Client
  │
  ▼
EmployeeController
  │
  ▼
CreateEmployeeRequest (DTO)
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
PostgreSQL
  │
  ▼
Employee Entity
  │
  ▼
EmployeeMapper
  │
  ▼
EmployeeResponse (DTO)
  │
  ▼
Client
```

---

## Understanding Each Layer

### Controller

The Controller is the application's HTTP entry point.

Responsibilities:

- Accept HTTP requests.
- Validate request format.
- Call the Service layer.
- Return HTTP responses.

Example

```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {

    @PostMapping
    public EmployeeResponse create(
            @RequestBody CreateEmployeeRequest request) {

        return employeeService.create(request);
    }
}
```

> 💡 **Interview Tip**
>
> Controllers should be thin.
> Business logic should never be implemented here.

---

### DTO (Data Transfer Object)

DTOs exist to transfer data safely.

Example

```java
public class CreateEmployeeRequest {

    private String name;
    private String email;

}
```

DTOs:

- Hide internal fields.
- Represent API contracts.
- Prevent exposing database objects.

---

### Service

The Service layer contains business rules.

Typical responsibilities:

- Validate salary.
- Check duplicate emails.
- Verify department exists.
- Calculate bonuses.

Example

```java
public EmployeeResponse create(
        CreateEmployeeRequest request) {

    validateSalary(request);

    Employee employee =
            mapper.toEntity(request);

    repository.save(employee);

    return mapper.toResponse(employee);
}
```

---

### Mapper

The Mapper converts objects.

```text
DTO
 │
 ▼
Entity
 │
 ▼
Response DTO
```

Example

```java
Employee employee =
        mapper.toEntity(request);
```

The Service delegates object conversion to the Mapper instead of mixing responsibilities.

---

### Entity

An Entity represents a database table.

```java
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    private Long id;

    private String name;

    private String email;

}
```

> 📌 **Important**
>
> An Entity belongs to the persistence layer, not the API layer.

---

### Repository

Repositories interact with the database.

```java
public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

}
```

Repositories should not contain business logic.

---

## Why Not Return an Entity?

Consider the following entity.

```java
@Entity
public class Employee {

    private String password;

    private Double salary;

    private Double hikePercentage;

}
```

Returning this entity directly would expose confidential information.

Instead:

```text
Entity
   │
   ▼
Mapper
   │
   ▼
EmployeeResponse
```

Only the required fields are returned.

---

## Why Separate Layers?

Imagine placing everything inside the Controller.

```text
Controller

├── Validation
├── SQL Queries
├── Business Rules
├── Email Logic
├── Payment Logic
├── Logging
├── Response Creation
```

The Controller quickly becomes impossible to maintain.

Instead:

```text
Controller
      │
      ▼
Service
      │
      ▼
Repository
```

Each layer has one clear responsibility.

---

## Real Project Example

Employee Registration

```text
Client
   │
   ▼
POST /employees
   │
   ▼
Controller
   │
   ▼
Validate Request
   │
   ▼
Service
   │
   ▼
Check Email Exists
   │
   ▼
Repository
   │
   ▼
Database
   │
   ▼
Response DTO
```

---

## Common Mistakes

> ⚠️ **Common Mistake**

❌ Writing SQL inside Controllers.

✅ SQL belongs in the Repository.

---

> ⚠️ **Common Mistake**

❌ Returning Entity directly.

✅ Return a Response DTO.

---

> ⚠️ **Common Mistake**

❌ Business logic inside Repository.

✅ Business logic belongs in the Service layer.

---

> ⚠️ **Common Mistake**

❌ Performing database validation inside Controller.

✅ Business validation belongs in the Service layer.

---

## Interview Questions

### Beginner

- What is the responsibility of a Controller?
- Why do we need DTOs?
- What does a Repository do?

### Intermediate

- Why shouldn't Controllers contain business logic?
- Why shouldn't Entities be returned directly?

### Senior

Your application currently uses PostgreSQL.

Tomorrow the company decides to migrate to MongoDB.

Which layer changes the most?

Why?

---

## Best Practices

- Keep Controllers thin.
- Keep Services focused on business rules.
- Use DTOs for API communication.
- Never expose Entities directly.
- Keep Repositories responsible only for persistence.
- Use Mappers for object conversion.

---

## Knowledge Check

Before moving to the next module, verify that you can answer "Yes" to the following questions.

- [ ] Can I explain why backend applications exist?
- [ ] Can I draw the complete request flow?
- [ ] Can I explain the responsibility of every layer?
- [ ] Can I explain why DTOs exist?
- [ ] Can I explain why Mappers exist?
- [ ] Can I explain why Services contain business logic?
- [ ] Can I answer the senior interview scenario?

---

## Module Summary

A backend application receives client requests, applies business rules, interacts with the database, and returns responses.

A standard Spring Boot request flows through the following layers:

```text
Client
   │
   ▼
Controller
   │
   ▼
DTO
   │
   ▼
Service
   │
   ▼
Mapper
   │
   ▼
Entity
   │
   ▼
Repository
   │
   ▼
Database
```

Keeping each layer focused on a single responsibility makes the application easier to understand, maintain, test, and extend.

---

## Quick Revision

| Layer | Purpose |
|--------|---------|
| Controller | HTTP Communication |
| DTO | Data Transfer |
| Service | Business Logic |
| Mapper | Object Conversion |
| Entity | Database Representation |
| Repository | Database Access |

---

## Related Modules

**Previous**

- None

**Next**

- Module 02 — DTO, Entity & Mapper

**Related Glossary Terms**

- API
- Controller
- DTO
- Entity
- Mapper
- Repository
- Service
- Business Logic
- Spring Bean