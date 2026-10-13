# Module 03 — Spring Bean Lifecycle

> **Phase:** 00 - Java Backend Foundation
>
> **Difficulty:** Beginner → Intermediate
>
> **Estimated Revision Time:** 35 Minutes

---

## Learning Objectives

After completing this module, you should be able to:

- Explain what a Spring Bean is.
- Describe the complete Bean Lifecycle.
- Explain the role of the Spring Container.
- Differentiate between object creation and bean creation.
- Explain Constructor Injection.
- Understand `@PostConstruct` and `@PreDestroy`.
- Explain why Spring manages objects instead of developers.

---

# Problem Statement

Imagine writing a Spring Boot application.

```java
@Service
public class EmployeeService {

}
```

Notice something interesting.

You never write:

```java
EmployeeService service = new EmployeeService();
```

Yet the following works perfectly.

```java
@RestController
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

}
```

Question:

> Where did this `EmployeeService` object come from?

---

# Why Do We Need a Spring Container?

Without Spring, developers must manually create every object.

Example

```java
EmployeeRepository repository =
        new EmployeeRepository();

EmployeeService service =
        new EmployeeService(repository);

EmployeeController controller =
        new EmployeeController(service);
```

Imagine an enterprise application with:

- 300 Services
- 200 Repositories
- 150 Controllers
- Thousands of dependencies

Manually creating every object quickly becomes unmanageable.

Spring solves this problem.

---

# What is a Spring Bean?

A **Spring Bean** is simply an object whose lifecycle is managed by the Spring Container.

Not every Java object is a Spring Bean.

Example

```java
Employee employee = new Employee();
```

This is **just a Java object**.

Spring knows nothing about it.

Now consider

```java
@Service
public class EmployeeService {

}
```

Spring discovers this class during component scanning.

It creates the object and manages its lifecycle.

This object becomes a Spring Bean.

---

# Spring Container

The Spring Container is responsible for:

- Creating Beans
- Injecting dependencies
- Managing Bean lifecycle
- Configuring Beans
- Destroying Beans

Think of it as an object factory with lifecycle management.

---

# Internal Execution

When the application starts, Spring performs the following steps.

```text
Application Starts
        │
        ▼
Component Scan
        │
        ▼
Find @Component
Find @Service
Find @Repository
Find @Controller
        │
        ▼
Create Bean
        │
        ▼
Constructor Execution
        │
        ▼
Dependency Injection
        │
        ▼
@PostConstruct
        │
        ▼
Bean Ready
        │
        ▼
Application Running
        │
        ▼
@PreDestroy
        │
        ▼
Bean Destroyed
```

This entire lifecycle is managed automatically.

---

# Bean Creation Example

Repository

```java
@Repository
public class EmployeeRepository {

    public EmployeeRepository() {
        System.out.println("Repository Constructor");
    }

    @PostConstruct
    public void init() {
        System.out.println("Repository Init");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("Repository Destroy");
    }

}
```

Service

```java
@Service
public class EmployeeService {

    public EmployeeService(EmployeeRepository repository) {
        System.out.println("Service Constructor");
    }

    @PostConstruct
    public void init() {
        System.out.println("Service Init");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("Service Destroy");
    }

}
```

Output

```text
Repository Constructor
Repository Init

Service Constructor
Service Init

Application Running

Repository Destroy
Service Destroy
```

---

# Why Repository is Created First

The Service depends on the Repository.

```text
EmployeeService
        │
        ▼
EmployeeRepository
```

Spring cannot create the Service until all required dependencies exist.

Therefore:

```text
Repository

↓

Service

↓

Controller
```

Dependency order determines creation order.

---

# Constructor Injection

Consider the following constructor.

```java
public EmployeeService(
        EmployeeRepository repository) {

}
```

Notice that we never write:

```java
new EmployeeRepository()
```

Spring automatically injects the Bean.

```text
ApplicationContext
        │
        ▼
EmployeeRepository Bean
        │
        ▼
EmployeeService Bean
```

This is called **Constructor Injection**.

---

# Why Constructor Injection?

Benefits:

- Mandatory dependencies
- Immutable fields
- Easier testing
- No NullPointerExceptions caused by missing dependencies

It is the recommended dependency injection approach.

---

# Bean Lifecycle Explained

## Step 1

Spring creates the object.

```text
new EmployeeService()
```

---

## Step 2

Constructor executes.

```java
EmployeeService(...)
```

---

## Step 3

Dependencies are injected.

```text
Repository Bean

↓

Service Bean
```

---

## Step 4

`@PostConstruct` executes.

Initialization logic belongs here.

Example:

- Cache loading
- Opening connections
- Initializing resources

---

## Step 5

Bean becomes ready.

The application can now use it.

---

## Step 6

Application shuts down.

Spring calls:

```java
@PreDestroy
```

Resource cleanup happens here.

---

# Object vs Bean

| Java Object | Spring Bean |
|-------------|-------------|
| Created using `new` | Created by Spring |
| Lifecycle managed by developer | Lifecycle managed by Spring |
| No Dependency Injection | Supports Dependency Injection |
| No lifecycle callbacks | Supports `@PostConstruct` and `@PreDestroy` |

Not every object is a Bean.

Every Bean is an object.

---

# Real Project Example

Application Startup

```text
Spring Boot Starts
        │
        ▼
Create Repository Beans
        │
        ▼
Create Service Beans
        │
        ▼
Create Controller Beans
        │
        ▼
Inject Dependencies
        │
        ▼
Application Ready
```

Once the application starts, all Singleton Beans are available in the `ApplicationContext`.

Controllers simply use these existing Beans.

---

# Common Mistakes

> ⚠️ **Common Mistake**

❌ Creating Service objects using `new`.

```java
EmployeeService service =
        new EmployeeService();
```

✅ Let Spring create the Bean.

---

> ⚠️ **Common Mistake**

❌ Performing heavy initialization inside constructors.

✅ Use `@PostConstruct` for initialization logic.

---

> ⚠️ **Common Mistake**

❌ Assuming every Java object is a Spring Bean.

✅ Only objects created and managed by Spring are Beans.

---

> ⚠️ **Common Mistake**

❌ Using field injection.

```java
@Autowired
private EmployeeRepository repository;
```

✅ Prefer constructor injection.

---

# Best Practices

- Prefer constructor injection.
- Keep constructors lightweight.
- Use `@PostConstruct` for initialization.
- Use `@PreDestroy` for cleanup.
- Let Spring manage application components.
- Avoid creating Beans manually.

---

# Interview Questions

## Beginner

- What is a Spring Bean?
- What is the Spring Container?
- What is Dependency Injection?

## Intermediate

- Explain the complete Bean Lifecycle.
- Why is constructor injection preferred?
- What is the purpose of `@PostConstruct`?

## Senior

Your application has the following dependency chain:

```text
Controller

↓

Service

↓

Repository
```

Explain in detail:

- Which Bean is created first?
- Why?
- What happens if the Repository Bean cannot be created?

---

# Knowledge Check

Before moving to the next module, verify that you can answer "Yes" to the following questions.

- [ ] Can I explain what a Spring Bean is?
- [ ] Can I explain the Bean Lifecycle from memory?
- [ ] Can I explain why Repository Beans are created before Service Beans?
- [ ] Can I explain Constructor Injection?
- [ ] Can I explain the purpose of `@PostConstruct`?
- [ ] Can I differentiate between a Java object and a Spring Bean?

---

# Module Summary

Spring manages application components using the **ApplicationContext**.

Instead of manually creating objects, developers declare components using annotations such as `@Service`, `@Repository`, and `@Controller`.

Spring creates these objects, injects their dependencies, invokes lifecycle callbacks, and destroys them gracefully when the application shuts down.

Understanding the Bean Lifecycle is fundamental to understanding how Spring Boot works internally.

---

# Quick Revision

```text
Application Starts
        │
        ▼
Component Scan
        │
        ▼
Create Bean
        │
        ▼
Constructor
        │
        ▼
Dependency Injection
        │
        ▼
@PostConstruct
        │
        ▼
Bean Ready
        │
        ▼
Application Running
        │
        ▼
@PreDestroy
        │
        ▼
Bean Destroyed
```

---

## Related Modules

**Previous**

- Module 02 — DTO, Entity & Mapper

**Next**

- Module 04 — Dependency Injection

**Related Glossary Terms**

- Spring Bean
- ApplicationContext
- BeanFactory
- Dependency Injection
- Constructor Injection
- Bean Lifecycle
- Component Scan
- PostConstruct
- PreDestroy