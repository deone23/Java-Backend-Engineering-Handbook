# Module 02 — DTO, Entity & Mapper

> **Phase:** 00 - Java Backend Foundation
>
> **Difficulty:** Beginner → Intermediate
>
> **Estimated Revision Time:** 25 Minutes

---

## Learning Objectives

After completing this module, you should be able to:

- Explain the purpose of DTOs, Entities, and Mappers.
- Differentiate between request DTOs and response DTOs.
- Explain why returning an Entity directly is a bad practice.
- Understand why object conversion is delegated to a Mapper.
- Draw the complete request and response flow.

---

## Problem Statement

Suppose a client wants to create a new employee.

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

A common beginner question is:

> "Why can't we use the `Employee` class everywhere?"

Although it seems simpler, doing so creates security, maintenance, and architectural problems.

---

## Why Different Objects?

Every layer has a different responsibility.

| Object | Responsibility |
|---------|----------------|
| Request DTO | Accept data from the client |
| Entity | Represent the database table |
| Response DTO | Return safe data to the client |
| Mapper | Convert between objects |

Each object exists for a specific purpose.

---

## High-Level Flow

```text
                Client
                   │
                   ▼
      CreateEmployeeRequest (DTO)
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
              PostgreSQL
                   ▲
                   │
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

# Request DTO

A Request DTO represents data received from the client.

Example

```java
public class CreateEmployeeRequest {

    private String name;

    private String email;

    private Double salary;

    private String department;

}
```

Responsibilities:

- Receive request data.
- Validate user input.
- Hide internal database fields.

---

## Why Not Use Entity Here?

Consider the following entity.

```java
@Entity
public class Employee {

    @Id
    private Long id;

    private String password;

    private Double salary;

    private Double hikePercentage;

}
```

If this class is exposed directly:

- Client can modify internal fields.
- Sensitive information becomes visible.
- Database structure leaks to the outside world.

---

## Entity

An Entity represents a database table.

Example

```java
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private Double salary;

    private String department;

}
```

Responsibilities:

- Represent database data.
- Participate in ORM mapping.
- Be managed by Hibernate.

> 📌 **Important**
>
> An Entity belongs to the persistence layer, not the API layer.

---

## Response DTO

The client rarely needs every field stored in the database.

Instead, we return only the required data.

Example

```java
public class EmployeeResponse {

    private Long id;

    private String name;

    private String email;

}
```

Notice that fields such as:

- salary
- password
- hikePercentage

are intentionally excluded.

---

## Mapper

A Mapper converts one object into another.

```text
Request DTO
      │
      ▼
   Entity
      │
      ▼
Response DTO
```

Example

```java
@Component
public class EmployeeMapper {

    public Employee toEntity(CreateEmployeeRequest request) {

        Employee employee = new Employee();

        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setSalary(request.getSalary());
        employee.setDepartment(request.getDepartment());

        return employee;
    }

    public EmployeeResponse toResponse(Employee employee) {

        EmployeeResponse response = new EmployeeResponse();

        response.setId(employee.getId());
        response.setName(employee.getName());
        response.setEmail(employee.getEmail());

        return response;
    }

}
```

The Mapper has only one responsibility:

Convert objects.

---

## Why Doesn't the Service Perform Mapping?

Imagine the Service also performs object conversion.

```text
Service

├── Validation
├── Salary Rules
├── Email Validation
├── Department Validation
├── DTO → Entity Conversion
├── Entity → DTO Conversion
├── Repository Calls
```

Now the Service has multiple responsibilities.

Instead:

```text
Service
    │
    ├── Business Logic
    │
    ▼
Mapper
    │
    └── Object Conversion
```

This follows the **Single Responsibility Principle (SRP)**.

---

## Complete Request Flow

```text
Client
   │
   ▼
Request DTO
   │
   ▼
Controller
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

---

## Complete Response Flow

```text
Database
   │
   ▼
Entity
   │
   ▼
Mapper
   │
   ▼
Response DTO
   │
   ▼
Controller
   │
   ▼
Client
```

---

## Where Should Validation Be Done?

| Validation | Layer |
|------------|-------|
| Name is mandatory | Request DTO |
| Email format | Request DTO |
| Salary must be positive | Service |
| Email must be unique | Service |
| Department must exist | Service |

Rule:

- DTO → Input validation.
- Service → Business validation.

---

## Real Project Example

Employee Registration

```text
Client
   │
   ▼
CreateEmployeeRequest
   │
   ▼
Controller
   │
   ▼
Service

Business Rules

• Salary > 0
• Email Unique
• Department Exists

   │
   ▼
Mapper
   │
   ▼
Employee Entity
   │
   ▼
Repository
   │
   ▼
Database
   │
   ▼
Mapper
   │
   ▼
EmployeeResponse
   │
   ▼
Client
```

---

## Common Mistakes

> ⚠️ **Common Mistake**

❌ Returning Entity directly from the Controller.

✅ Return a Response DTO.

---

> ⚠️ **Common Mistake**

❌ Writing conversion logic inside the Controller.

✅ Delegate conversion to the Mapper.

---

> ⚠️ **Common Mistake**

❌ Placing business validation inside the DTO.

✅ Business validation belongs in the Service layer.

---

> ⚠️ **Common Mistake**

❌ Exposing internal database fields to the client.

✅ Return only the required fields.

---

## Best Practices

- Keep DTOs simple.
- Keep Entities persistence-focused.
- Keep Mappers dedicated to object conversion.
- Keep Services focused on business rules.
- Never expose Entities directly.

---

## Interview Questions

### Beginner

- What is a DTO?
- Why do we need an Entity?
- What is the purpose of a Mapper?

### Intermediate

- Why shouldn't we return an Entity directly?
- Why shouldn't the Service perform object mapping?

### Senior

A new requirement states that the API should no longer expose employee email addresses.

Which classes need to change?

Which classes remain unchanged?

Explain why.

---

## Knowledge Check

Before moving to the next module, verify that you can answer "Yes" to the following questions.

- [ ] Can I explain why DTOs exist?
- [ ] Can I explain why Entities should not be exposed?
- [ ] Can I explain the responsibility of a Mapper?
- [ ] Can I differentiate between input validation and business validation?
- [ ] Can I draw the request and response flow?
- [ ] Can I answer the senior interview scenario?

---

## Module Summary

A Spring Boot application uses different objects for different responsibilities.

- Request DTOs receive client input.
- Entities represent database tables.
- Response DTOs return safe data to the client.
- Mappers convert between these objects.
- Services focus only on business logic.

Separating responsibilities makes applications secure, maintainable, testable, and easy to extend.

---

## Quick Revision

| Component | Responsibility |
|-----------|----------------|
| Request DTO | Receive client data |
| Entity | Represent database table |
| Response DTO | Return safe response |
| Mapper | Convert objects |
| Service | Business logic |
| Repository | Database operations |

---

## Related Modules

**Previous**

- Module 01 — Backend Architecture

**Next**

- Module 03 — Spring Bean Lifecycle

**Related Glossary Terms**

- DTO
- Entity
- Mapper
- Repository
- Service
- Business Logic
- Controller
- JPA