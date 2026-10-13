# Module 05 — Bean Scopes & Thread Safety

> **Phase:** 00 - Java Backend Foundation
>
> **Difficulty:** Beginner → Intermediate
>
> **Estimated Revision Time:** 40 Minutes

---

# Learning Objectives

After completing this module, you should be able to:

- Explain what Bean Scope means.
- Understand why Singleton is Spring's default scope.
- Differentiate between Singleton and Prototype Beans.
- Explain why Singleton Beans can become thread-unsafe.
- Understand why local variables are thread-safe.
- Diagnose concurrency issues in Spring applications.

---

# Problem Statement

Consider the following Service.

```java
@Service
public class EmployeeService {

    private int counter = 0;

    public void increment() {

        counter++;

        System.out.println(counter);

    }

}
```

Suppose **100 users** call:

```
/increment
```

at exactly the same time.

Question:

Will it always print

```text
1
2
3
...
100
```

Or can incorrect values appear?

Understanding the answer requires understanding Bean Scope.

---

# What is Bean Scope?

Bean Scope determines:

> **How many instances of a Bean Spring creates inside the ApplicationContext.**

Different scopes produce different numbers of objects.

---

# Bean Scopes

| Scope | Number of Objects |
|--------|------------------|
| Singleton | One Bean for the entire ApplicationContext |
| Prototype | New Bean every request from the container |
| Request | One Bean per HTTP request |
| Session | One Bean per HTTP session |
| Application | One Bean for the web application |

In backend interviews, Singleton and Prototype are the most commonly discussed.

---

# Singleton Scope

Singleton is the default scope in Spring.

```java
@Service
public class EmployeeService {

}
```

is equivalent to

```java
@Service
@Scope("singleton")
public class EmployeeService {

}
```

Only one object exists.

Example

```text
ApplicationContext

        │

        ▼

EmployeeService Bean

        │

        ├───────────── User 1

        ├───────────── User 2

        ├───────────── User 3

        └───────────── User 100
```

Every request shares the same object.

---

# Internal Execution

Application Startup

```text
Application Starts

        │

        ▼

Component Scan

        │

        ▼

Create EmployeeService Bean

        │

        ▼

Store inside ApplicationContext

        │

        ▼

Application Ready
```

When HTTP requests arrive

```text
Request 1

        │

        ▼

EmployeeService Bean

        ▲

Request 2

        ▲

Request 3

        ▲

Request 100
```

Spring does **not** create new Service objects.

Everyone shares the same Bean.

---

# Why Singleton?

Imagine an application with

- 500 Services
- 300 Repositories
- 200 Controllers

Now imagine

10,000 concurrent users.

If Spring created new objects for every request:

```text
500 Services

×

10,000 Users

=

5,000,000 Objects
```

Memory usage would explode.

Singleton dramatically reduces memory consumption.

---

# The Thread Safety Problem

Consider again

```java
@Service
public class EmployeeService {

    private int counter = 0;

    public void increment() {

        counter++;

    }

}
```

Suppose two threads execute simultaneously.

Initial value

```text
counter = 5
```

Execution

```text
Thread A reads 5

Thread B reads 5

Thread A writes 6

Thread B writes 6
```

Expected

```text
7
```

Actual

```text
6
```

One increment is lost.

This is called a **Race Condition**.

---

# Why Does This Happen?

Because both requests share the same object.

```text
Thread A

      │

      ▼

EmployeeService Bean

      ▲

Thread B
```

Both threads modify the same variable.

---

# Thread-Safe Example

Consider

```java
@Service
public class EmployeeService {

    public void createEmployee() {

        Employee employee =
                new Employee();

    }

}
```

Question

Is this thread-safe?

Yes.

Why?

Because

```java
Employee employee
```

is a **local variable**.

Every thread has its own stack.

Example

```text
Thread A Stack

Employee employee

-------------------------

Thread B Stack

Employee employee
```

The objects are independent.

No shared state exists.

---

# Local Variables vs Instance Variables

| Variable Type | Shared? | Thread Safe? |
|---------------|---------|--------------|
| Local Variable | No | ✅ Yes |
| Instance Variable | Yes (Singleton) | ❌ No |
| Static Variable | Shared by JVM | ❌ No |

Golden Rule

> Local variables live on the thread's execution stack.

> Instance variables live inside the shared Singleton Bean.

---

# Prototype Scope

Prototype creates a new Bean every time Spring is asked for one.

```java
@Service
@Scope("prototype")
public class EmployeeService {

}
```

Example

```text
Request Bean

↓

EmployeeService #1

Request Bean

↓

EmployeeService #2

Request Bean

↓

EmployeeService #3
```

Each object is independent.

Prototype reduces shared state but is rarely used for Controllers, Services, or Repositories.

---

# Real Project Example

Bad

```java
@Service
public class OrderService {

    private int ordersCreated;

}
```

Every request modifies the same variable.

Good

```java
@Service
public class OrderService {

    public void createOrder() {

        int discount = 0;

        Order order =
                new Order();

    }

}
```

Everything is method-local.

Thread-safe.

---

# Debugging Corner

### Problem

Counter values appear inconsistent under heavy load.

Example

Expected

```text
1
2
3
4
5
```

Actual

```text
1
2
2
4
5
```

### Why It Happens

Multiple threads modify the same instance variable simultaneously.

### Common Causes

- Mutable instance variables.
- Shared collections.
- Static variables.
- Singleton Beans storing request-specific data.

### Solution

- Prefer stateless Services.
- Keep variables inside methods.
- Use synchronization only when truly necessary.

---

# Common Mistakes

> ⚠️ **Common Mistake**

❌ Assuming Spring creates a new Service for every request.

✅ Singleton Services are shared.

---

> ⚠️ **Common Mistake**

❌ Storing request-specific data in instance variables.

```java
private Employee employee;
```

✅ Keep request data inside methods.

---

> ⚠️ **Common Mistake**

❌ Using mutable shared state inside Singleton Beans.

✅ Design Services to be stateless.

---

# Best Practices

- Keep Services stateless.
- Avoid mutable instance variables.
- Prefer local variables.
- Understand Bean Scope before introducing shared state.
- Use Prototype only when a new object per lookup is truly required.

---

# Interview Questions

## Beginner

- What is Bean Scope?
- What is the default Bean Scope?
- What is Singleton?

## Intermediate

- Why are Singleton Beans not automatically thread-safe?
- Why are local variables thread-safe?

## Senior

A production application occasionally returns duplicate order numbers.

The Service contains

```java
private int orderCounter;
```

Explain:

- Why the bug occurs.
- How Bean Scope contributes.
- How you would redesign the Service.

---

# Knowledge Check

Before moving to the next module, verify that you can answer "Yes" to the following questions.

- [ ] Can I explain Bean Scope?
- [ ] Can I explain why Singleton is the default?
- [ ] Can I explain why Singleton Beans are shared?
- [ ] Can I explain Race Conditions?
- [ ] Can I explain why local variables are thread-safe?
- [ ] Can I identify thread-safety problems in Spring Services?

---

# Module Summary

Bean Scope determines how many instances of a Bean Spring creates.

Spring uses Singleton by default because it is memory-efficient and suitable for stateless components.

Since Singleton Beans are shared across all requests, mutable instance variables can introduce race conditions.

The safest approach is to design Services as stateless and keep request-specific data inside method-local variables.

---

# Quick Revision

```text
ApplicationContext

        │

        ▼

Singleton Bean

        ▲

All HTTP Requests

↓

Shared Object

↓

Avoid Mutable Instance Variables

↓

Prefer Local Variables
```

| Scope | Objects Created |
|--------|-----------------|
| Singleton | One |
| Prototype | New object per lookup |
| Request | One per HTTP request |
| Session | One per HTTP session |

---

## Related Modules

**Previous**

- Module 04 — Dependency Injection

**Next**

- Module 06 — Spring Container Summary

**Related Glossary Terms**

- Spring Bean
- ApplicationContext
- Bean Scope
- Singleton Bean
- Prototype Bean
- Thread Safety
- Race Condition
- Dependency Injection