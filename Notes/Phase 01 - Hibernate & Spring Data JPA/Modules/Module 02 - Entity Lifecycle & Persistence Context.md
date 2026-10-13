# Module 02 - Entity Lifecycle & Persistence Context

> **Phase:** 01 - Hibernate & Spring Data JPA
> 
> **Difficulty:** Beginner → Intermediate
> 
> **Estimated Revision Time:** 60 Minutes

---

# Learning Objectives

After completing this module, you should be able to:

- Explain what an Entity is.
- Understand the complete Entity Lifecycle.
- Explain what the Persistence Context is.
- Differentiate between Java Heap Memory and the Persistence Context.
- Understand when Hibernate starts managing an Entity.
- Explain why Persistence Context is called the First-Level Cache.
- Build the foundation required for Dirty Checking and Transactions.

---

# Problem Statement

Consider the following code:

```java
Employee employee = new Employee();

employee.setName("Vinay");
employee.setSalary(1200000);
```

Question:

- Is this object stored in the database?
- Does Hibernate know this object exists?
- Can Hibernate automatically save changes to it?

To answer these questions, we must understand the Persistence Context.

---

# What is an Entity?

An **Entity** is a Java object that represents a row in a database table.

Example

```java
@Entity
public class Employee {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private Double salary;

}
```

Each instance of this class represents one record in the `employees` table.

---

# Internal Memory View

When we create an object using `new`:

```java
Employee employee = new Employee();
```

Memory looks like this:

```text
JVM Heap

┌───────────────────────────────┐
│ Employee                      │
│ id = null                     │
│ name = Vinay                  │
│ salary = 1200000              │
└───────────────────────────────┘
```

At this point:

- The object exists only in JVM Heap Memory.
- Hibernate is **not** aware of this object.
- The object is **not** inside the Persistence Context.
- Nothing has been written to the database.

---

# What is the Persistence Context?

The **Persistence Context** is an in-memory container managed by Hibernate.

It stores references to Managed Entities during a transaction.

Think of it as Hibernate's workspace.

```text
Application

        │

        ▼

Persistence Context

        │

        ▼

Managed Entities
```

Hibernate continuously tracks every Managed Entity inside this container.

---

# Java Heap vs Persistence Context

Many developers think these are the same.

They are not.

| Java Heap | Persistence Context |
|-----------|---------------------|
| Managed by JVM | Managed by Hibernate |
| Stores all Java objects | Stores only Managed Entities |
| Created using `new` | Created by Hibernate |
| Exists regardless of Hibernate | Exists only during persistence operations |

---

# When Does Hibernate Become Aware?

Suppose we execute:

```java
Employee employee = new Employee();

employee.setName("Vinay");
```

Current state:

```text
JVM Heap

┌─────────────────────┐
│ Employee            │
└─────────────────────┘
```

Persistence Context

```text
Empty
```

Hibernate still does not know this object exists.

Now execute:

```java
employeeRepository.save(employee);
```

Execution Flow

```text
Application

↓

Spring Data JPA

↓

JPA

↓

Hibernate

↓

Persistence Context

↓

Managed Entity
```

The Entity becomes Managed.

---

# Internal Memory View

Before `save()`:

```text
JVM Heap

┌──────────────────────────────┐
│ Employee                     │
└──────────────────────────────┘

Persistence Context

Empty
```

After `save()`:

```text
JVM Heap

┌──────────────────────────────┐
│ Employee                     │
└──────────────────────────────┘
            │
            ▼
Persistence Context

┌──────────────────────────────┐
│ Managed Entity Reference     │
└──────────────────────────────┘
```

Hibernate now starts tracking this object.

---

# Why Does Hibernate Manage Entities?

Hibernate wants to know:

- Which objects changed?
- Which SQL should be generated?
- When should SQL be executed?
- Which objects are already loaded?

Managing Entities allows Hibernate to answer these questions automatically.

---

# Identity Guarantee

Within one Persistence Context:

```java
Employee emp1 = repository.findById(1L).get();

Employee emp2 = repository.findById(1L).get();
```

Both variables reference the same Managed Entity.

```text
emp1
   │
   ▼
┌──────────────────────────────┐
│ Employee(id=1)               │
└──────────────────────────────┘
   ▲
   │
emp2
```

Therefore:

```java
emp1 == emp2
```

returns

```text
true
```

This is called the **Identity Guarantee**.

---

# First-Level Cache

The Persistence Context also acts as Hibernate's **First-Level Cache**.

When Hibernate receives another request for the same Entity:

```java
repository.findById(1L);
```

It first checks the Persistence Context.

If the Entity is already Managed:

- No SQL is executed.
- Hibernate returns the existing object.

This improves performance and guarantees object identity.

---

# Real Execution Flow

```text
findById()

        │

        ▼

Persistence Context

        │

   Found?

   │        │

 Yes       No

 │          │

 ▼          ▼

Return    Execute SQL

Object      │

            ▼

      Store in Persistence Context

            │

            ▼

        Return Entity
```

---

# Why Doesn't `new` Make an Entity Managed?

Creating an object using `new` is purely a Java operation.

Example:

```java
Employee employee = new Employee();
```

Only the JVM participates.

Hibernate is not involved.

Hibernate becomes aware only when the application interacts with persistence operations such as:

```java
repository.save(employee);

entityManager.persist(employee);

repository.findById(id);
```

---

# Senior Engineer Notes

The Persistence Context is the heart of Hibernate.

Almost every advanced Hibernate feature depends on it:

- Dirty Checking
- Flush
- Transactions
- First-Level Cache
- Lazy Loading
- Optimistic Locking
- Entity Lifecycle

Without understanding the Persistence Context, these concepts often appear "magical."

Understanding it removes that mystery.

---

# Debugging Corner

### Problem

A developer creates an Entity using:

```java
new Employee();
```

They expect Hibernate to automatically update the database when fields change.

### Why It Doesn't Work

The object is only in JVM Heap Memory.

Hibernate never received the object.

It is not Managed.

---

### Solution

The Entity must enter the Persistence Context through a persistence operation such as:

```java
repository.save(employee);

entityManager.persist(employee);
```

---

# Common Mistakes

> ⚠️ **Common Mistake**

❌ Assuming every Entity object is Managed.

✅ Only Entities inside the Persistence Context are Managed.

---

> ⚠️ **Common Mistake**

❌ Believing `new` stores an object in the database.

✅ `new` creates a Java object only.

---

> ⚠️ **Common Mistake**

❌ Thinking Java Heap and Persistence Context are the same.

✅ They are managed by different systems and have different responsibilities.

---

# Interview Questions

## Beginner

- What is an Entity?
- What is the Persistence Context?
- Who manages the Persistence Context?

## Intermediate

- Explain the difference between Java Heap and the Persistence Context.
- When does Hibernate become aware of an Entity?
- Why is the Persistence Context called the First-Level Cache?

## Senior

A developer modifies an Entity after creating it with `new`.

No SQL is executed.

Explain:

- Why Hibernate ignored the object.
- Which memory area contained the object.
- What operation would make Hibernate manage it.

---

# Knowledge Check

Before moving to the next module, verify that you can answer "Yes" to the following questions.

- [ ] Can I explain what an Entity is?
- [ ] Can I explain what the Persistence Context is?
- [ ] Can I differentiate Java Heap and the Persistence Context?
- [ ] Can I explain when Hibernate becomes aware of an Entity?
- [ ] Can I explain why `new` does not make an Entity Managed?
- [ ] Can I explain why the Persistence Context is also called the First-Level Cache?

---

# Module Summary

An Entity is a Java object representing a database record.

Creating an Entity with `new` places it only in JVM Heap Memory.

Hibernate becomes aware of the Entity only when it enters the Persistence Context through persistence operations such as `save()`, `persist()`, or `find()`.

The Persistence Context stores Managed Entities, guarantees object identity, and serves as Hibernate's First-Level Cache.

This module establishes the foundation for understanding Dirty Checking, Flush, Transactions, and the complete Entity Lifecycle.

---

# Quick Revision

```text
new Employee()

↓

JVM Heap

↓

Hibernate Not Aware

↓

repository.save()

↓

Persistence Context

↓

Managed Entity

↓

Hibernate Tracks Changes
```

| Concept | Key Point |
|----------|-----------|
| Entity | Java object representing a database row |
| JVM Heap | Stores all Java objects |
| Persistence Context | Stores Managed Entities |
| Managed Entity | Tracked by Hibernate |
| First-Level Cache | Built into the Persistence Context |
| Identity Guarantee | Same Entity → Same Java Object |

---

## Related Modules

**Previous**

- Module 01 - Why ORM, JDBC, JPA & Hibernate

**Next**

- Module 03 - Dirty Checking & Flush Mechanism

**Related Glossary Terms**

- Entity
- Persistence Context
- Managed Entity
- First-Level Cache
- Entity Lifecycle
- Identity Guarantee
- JVM Heap
- Hibernate