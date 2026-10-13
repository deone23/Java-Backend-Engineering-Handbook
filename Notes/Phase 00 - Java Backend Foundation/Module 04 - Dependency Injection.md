# Module 04 — Dependency Injection

> **Phase:** 00 - Java Backend Foundation
>
> **Difficulty:** Beginner → Intermediate
>
> **Estimated Revision Time:** 30 Minutes

---

## Learning Objectives

After completing this module, you should be able to:

- Explain what Dependency Injection (DI) is.
- Understand the Inversion of Control (IoC) principle.
- Explain how Spring performs Dependency Injection.
- Differentiate between Constructor, Setter, and Field Injection.
- Explain why Constructor Injection is the recommended approach.
- Understand dependency resolution inside the Spring Container.

---

# Problem Statement

Consider the following service.

```java
@Service
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }
}
```

Notice that we never write:

```java
EmployeeRepository repository =
        new EmployeeRepository();

EmployeeService service =
        new EmployeeService(repository);
```

Yet the application works perfectly.

**Question:**

> Who created the `EmployeeRepository` object?

> Who passed it into the constructor?

The answer is **Spring**.

---

# What is a Dependency?

A dependency is any object that another object requires to perform its work.

Example

```java
@Service
public class EmployeeService {

    private final EmployeeRepository repository;

}
```

Here,

```text
EmployeeService
        │
        ▼
EmployeeRepository
```

`EmployeeService` depends on `EmployeeRepository`.

Therefore,

`EmployeeRepository` is a dependency of `EmployeeService`.

---

# What is Dependency Injection?

Dependency Injection means:

> Instead of creating dependencies yourself, another component provides them.

In Spring Boot, that component is the **Spring Container (ApplicationContext)**.

---

# Without Dependency Injection

Imagine building the application manually.

```java
EmployeeRepository repository =
        new EmployeeRepository();

EmployeeService service =
        new EmployeeService(repository);

EmployeeController controller =
        new EmployeeController(service);
```

Every dependency must be created manually.

As applications grow, this becomes difficult to maintain.

---

# With Dependency Injection

Spring creates every Bean.

```text
ApplicationContext
        │
        ▼
EmployeeRepository Bean
        │
        ▼
EmployeeService Bean
        │
        ▼
EmployeeController Bean
```

Developers simply declare dependencies.

Spring supplies them automatically.

---

# Inversion of Control (IoC)

Before Spring:

```text
Application

↓

Creates Objects

↓

Controls Lifecycle
```

After Spring:

```text
Application

↓

Declares Requirements

↓

Spring Creates Objects

↓

Spring Controls Lifecycle
```

This reversal of responsibility is called **Inversion of Control (IoC)**.

Dependency Injection is the most common implementation of IoC.

---

# Internal Execution

Suppose we have the following classes.

```java
@Repository
public class EmployeeRepository {

}
```

```java
@Service
public class EmployeeService {

    public EmployeeService(EmployeeRepository repository) {

    }

}
```

```java
@RestController
public class EmployeeController {

    public EmployeeController(EmployeeService service) {

    }

}
```

During application startup:

```text
Application Starts
        │
        ▼
Component Scan
        │
        ▼
Find Repository
        │
        ▼
Create Repository Bean
        │
        ▼
Find Service
        │
        ▼
Resolve Constructor Dependency
        │
        ▼
Inject Repository Bean
        │
        ▼
Create Service Bean
        │
        ▼
Find Controller
        │
        ▼
Resolve Constructor Dependency
        │
        ▼
Inject Service Bean
        │
        ▼
Create Controller Bean
        │
        ▼
Application Ready
```

Spring performs dependency resolution automatically.

---

# Constructor Injection

Example

```java
@Service
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {

        this.repository = repository;

    }

}
```

Spring looks at the constructor.

It notices that an `EmployeeRepository` is required.

The container searches for an existing Bean of that type.

If found, it injects the Bean automatically.

---

# Why Constructor Injection?

Advantages

- Dependencies are mandatory.
- Object becomes immutable.
- Easier unit testing.
- Better readability.
- Avoids partially initialized objects.

Constructor Injection is the recommended approach in Spring Boot.

---

# Setter Injection

Example

```java
@Service
public class EmployeeService {

    private EmployeeRepository repository;

    @Autowired
    public void setRepository(
            EmployeeRepository repository) {

        this.repository = repository;
    }

}
```

Advantages

- Dependency can be changed later.

Disadvantages

- Dependency is optional.
- Object may remain partially initialized.

---

# Field Injection

Example

```java
@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository repository;

}
```

Although this works, it has several drawbacks.

Problems

- Harder to unit test.
- Hidden dependencies.
- Difficult to create immutable classes.
- Reflection-based injection.

---

# Injection Comparison

| Injection Type | Recommended | Why |
|----------------|------------|-----|
| Constructor | ✅ Yes | Mandatory dependencies, immutable, testable |
| Setter | ⚠ Sometimes | Optional dependencies |
| Field | ❌ No | Hidden dependencies and difficult testing |

---

# What Happens if No Bean Exists?

Suppose the Service requires:

```java
public EmployeeService(EmployeeRepository repository) {

}
```

But there is no Bean of type `EmployeeRepository`.

Application startup fails.

Example

```text
No qualifying bean of type
'EmployeeRepository'
available
```

Spring refuses to create an incomplete object.

This prevents runtime errors.

---

# Real Project Example

Employee Registration

```text
ApplicationContext
        │
        ▼
EmployeeRepository Bean
        │
        ▼
EmployeeService Bean
        │
        ▼
EmployeeController Bean
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
Database
```

Notice that no object is created using `new`.

All dependencies come from the Spring Container.

---

# Common Mistakes

> ⚠️ **Common Mistake**

❌ Creating Spring Beans manually.

```java
EmployeeService service =
        new EmployeeService(repository);
```

✅ Let Spring create the Bean.

---

> ⚠️ **Common Mistake**

❌ Using Field Injection everywhere.

```java
@Autowired
private EmployeeRepository repository;
```

✅ Prefer Constructor Injection.

---

> ⚠️ **Common Mistake**

❌ Making dependencies optional when they are required.

✅ Use constructor parameters for mandatory dependencies.

---

> ⚠️ **Common Mistake**

❌ Confusing IoC with Dependency Injection.

✅ IoC is the principle.

Dependency Injection is one implementation of IoC.

---

# Best Practices

- Prefer Constructor Injection.
- Keep dependencies immutable using `final`.
- Let Spring manage Bean creation.
- Avoid manual object creation for Spring-managed components.
- Keep constructors lightweight.

---

# Interview Questions

## Beginner

- What is Dependency Injection?
- What is a dependency?
- What is Inversion of Control?

## Intermediate

- Why is Constructor Injection preferred?
- Why is Field Injection discouraged?
- How does Spring resolve dependencies?

## Senior

Your application contains the following Beans:

```text
EmployeeController

↓

EmployeeService

↓

EmployeeRepository
```

Explain, step by step:

- How Spring creates these Beans.
- How dependencies are resolved.
- What happens if `EmployeeRepository` is missing.

---

# Knowledge Check

Before moving to the next module, verify that you can answer "Yes" to the following questions.

- [ ] Can I explain Dependency Injection without memorizing the definition?
- [ ] Can I explain Inversion of Control?
- [ ] Can I explain how Spring resolves constructor dependencies?
- [ ] Can I explain why Constructor Injection is preferred?
- [ ] Can I explain why Field Injection is discouraged?
- [ ] Can I draw the complete dependency resolution flow?

---

# Module Summary

Dependency Injection allows objects to receive their dependencies from the Spring Container instead of creating them manually.

Spring scans application components, creates the required Beans, resolves constructor dependencies, and injects them automatically.

Constructor Injection is the preferred approach because it produces immutable, testable, and fully initialized objects.

---

# Quick Revision

```text
Application Starts
        │
        ▼
Component Scan
        │
        ▼
Create Repository Bean
        │
        ▼
Inject Repository
        │
        ▼
Create Service Bean
        │
        ▼
Inject Service
        │
        ▼
Create Controller Bean
        │
        ▼
Application Ready
```

| Concept | Meaning |
|----------|---------|
| Dependency | Object required by another object |
| Dependency Injection | Spring provides dependencies |
| IoC | Spring controls object creation |
| Constructor Injection | Recommended injection style |
| Field Injection | Avoid in production code |

---

## Related Modules

**Previous**

- Module 03 — Spring Bean Lifecycle

**Next**

- Module 05 — Bean Scope & Thread Safety

**Related Glossary Terms**

- Dependency Injection
- Inversion of Control (IoC)
- ApplicationContext
- Spring Bean
- Constructor Injection
- Bean Lifecycle
- Component Scan