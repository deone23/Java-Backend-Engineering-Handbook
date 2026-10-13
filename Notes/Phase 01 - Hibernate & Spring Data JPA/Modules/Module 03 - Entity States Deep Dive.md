# Module 03 - Entity States Deep Dive

## Learning Objectives

After completing this chapter, you should be able to:

- Explain why Hibernate uses entity states.
- Identify the four entity states.
- Determine an entity's current state during execution.
- Predict Hibernate's behavior based on an entity's state.
- Understand how entities move between different states.
- Explain why Dirty Checking works only for Managed entities.

---

# Why Entity States Exist

Hibernate continuously answers one fundamental question:

> **"Should I synchronize this Java object with the database?"**

To answer this, Hibernate classifies every entity into one of four states.

```text
Java Object
      │
      ▼
Current State?
      │
 ┌────┼────┬────┐
 │    │    │    │
 ▼    ▼    ▼    ▼
Transient
Managed
Detached
Removed
```

The current state determines:

- Whether Hibernate tracks changes.
- Whether SQL should be generated.
- Whether Dirty Checking is active.
- Whether the entity belongs to the Persistence Context.

---

# The Four Entity States

Every JPA entity always exists in one of these states.

| State | Inside Persistence Context | Dirty Checking | Database Row |
|--------|----------------------------|----------------|--------------|
| Transient | ❌ | ❌ | Usually No |
| Managed | ✅ | ✅ | Yes |
| Detached | ❌ | ❌ | Yes |
| Removed | ✅ | ✅ | Pending DELETE |

These states are mutually exclusive.

At any moment, an entity can belong to only one state.

---

# Complete Lifecycle Overview

An entity moves between different states throughout its lifecycle.

```text
new
 │
 ▼
Transient
 │
persist()
 ▼
Managed
 ├─────────────┐
 │             │
 │ detach()    │ remove()
 ▼             ▼
Detached     Removed
 │             │
 │ merge()     │
 └──────┬──────┘
        ▼
      Managed
```

Every EntityManager operation changes the state of an entity.

Understanding these transitions explains almost every Hibernate behavior.

---

# Transient State

A **Transient** entity is a normal Java object that Hibernate does not know about.

Example

```java
Employee employee = new Employee();

employee.setName("Vinay");
employee.setSalary(100000);
```

Current State

```text
Transient
```

Characteristics

- Created using `new`.
- Not stored inside the Persistence Context.
- Not tracked by Hibernate.
- Dirty Checking is inactive.
- No corresponding database row (in most cases).

Execution Flow

```text
new Employee()
      │
      ▼
Transient
```

Changing a Transient object has no effect on the database.

```java
employee.setSalary(500000);
```

Hibernate completely ignores these changes.

---

# Why Doesn't Hibernate Track Transient Objects?

Consider the following code.

```java
Employee employee = new Employee();

employee.setSalary(100000);
```

Hibernate has no way of knowing whether:

- The object should be stored.
- The object is temporary.
- The object will ever be persisted.

Tracking every Java object would consume unnecessary memory and processing time.

Instead, Hibernate starts tracking an entity only after it becomes **Managed**.

---

# Managed State

A **Managed** entity is an object that belongs to the Persistence Context.

Once an entity becomes Managed:

- Hibernate tracks every modification.
- Dirty Checking becomes active.
- The entity is synchronized with the database during `flush()` or transaction commit.

Example

```java
Employee employee =
    entityManager.find(Employee.class, 1L);
```

Current State

```text
Managed
```

Execution Flow

```text
Database
    │
 SELECT
    │
    ▼
Persistence Context
    │
    ▼
Managed Entity
```

---

# Characteristics of a Managed Entity

A Managed entity:

- Exists inside the Persistence Context.
- Participates in Dirty Checking.
- Is automatically synchronized with the database.
- Can be refreshed.
- Can be detached.
- Can be removed.

Example

```java
Employee employee =
    entityManager.find(Employee.class, 1L);

employee.setSalary(500000);
```

No explicit update method is required.

Hibernate detects the change automatically.

Execution Flow

```text
Managed Entity
      │
setSalary()
      │
Dirty Checking
      │
flush()/commit()
      │
UPDATE
```

---

# Why Managed Entities Matter

Most Hibernate features depend on the Managed state.

For example:

| Feature | Requires Managed Entity |
|---------|-------------------------|
| Dirty Checking | ✅ |
| Automatic UPDATE | ✅ |
| refresh() | ✅ |
| remove() | ✅ |
| contains() | ✅ |
| Lazy Loading | ✅ |

If an entity is not Managed, most of these features no longer work.

---

# Persistence Context and Managed State

A Managed entity always belongs to a Persistence Context.

```text
EntityManager
      │
      ▼
Persistence Context
      │
 ┌────┴──────────────┐
 │ Employee(id=1)    │
 │ Department(id=10) │
 │ Address(id=20)    │
 └───────────────────┘
```

The Persistence Context guarantees:

> **One database row = One Managed Java object**

This guarantee enables reliable Dirty Checking and prevents duplicate representations of the same row.

---

# Important Notes

- Every entity always belongs to one state.
- Hibernate tracks only Managed entities.
- Transient objects are ordinary Java objects.
- Dirty Checking begins only after an entity becomes Managed.
- Every Managed entity resides inside the Persistence Context.
- A database row is represented by only one Managed object within a Persistence Context.

---

# Common Mistakes

❌ Assuming every Java object is tracked by Hibernate.

✔ Hibernate tracks only Managed entities.

---

❌ Assuming creating an object using `new` inserts it into the database.

✔ A newly created object is still Transient.

---

❌ Expecting Dirty Checking to work for Transient entities.

✔ Dirty Checking works only for Managed entities.

---

❌ Confusing a Java object with a database record.

✔ A Java object becomes persistent only after it enters the Persistence Context.

---

# Important Q&A

### Q1

What is the first state of every newly created entity?

**Answer**

Transient.

Every object created using `new` starts in the Transient state.

---

### Q2

Does Hibernate track a Transient entity?

**Answer**

No.

Hibernate starts tracking an entity only after it becomes Managed.

---

### Q3

Which entity state participates in Dirty Checking?

**Answer**

Managed.

Dirty Checking does not work for Transient, Detached, or Removed entities.

---

### Q4

Can a Managed entity update the database automatically?

**Answer**

Yes.

When a Managed entity changes, Hibernate detects the modification and synchronizes it with the database during `flush()` or transaction commit.

---

### Q5

Why does Hibernate need entity states?

**Answer**

Entity states allow Hibernate to determine:

- Whether an entity should be tracked.
- Whether SQL should be generated.
- Whether Dirty Checking should run.
- Whether the entity belongs to the Persistence Context.

---

# Summary

- Hibernate classifies every entity into one of four states.
- The entity state determines Hibernate's behavior.
- Transient entities are ordinary Java objects.
- Managed entities belong to the Persistence Context.
- Dirty Checking is active only for Managed entities.
- Most Hibernate features depend on the Managed state.

---

# Quick Revision

- `new` → Transient
- Managed → Inside Persistence Context
- Dirty Checking → Managed only
- Managed → Automatic synchronization
- Transient → Hibernate ignores changes
- One Persistence Context → One Managed object per database row

---

## Next Topics

- Detached State
- Removed State
- State Transition Rules
- Lifecycle Timeline
- Real-world Scenarios
- Interview ---

# Detached State

A **Detached** entity is an object that was previously Managed but is no longer associated with the Persistence Context.

The Java object still exists, but Hibernate has stopped managing it.

Execution Flow

```text
Managed
   │
detach()
   │
   ▼
Detached
```

Example

```java
Employee employee =
    entityManager.find(Employee.class, 1L);

entityManager.detach(employee);
```

Current State

```text
Detached
```

---

# Characteristics of a Detached Entity

A Detached entity:

- Exists outside the Persistence Context.
- Is no longer tracked by Hibernate.
- Does not participate in Dirty Checking.
- Can still be modified like any normal Java object.
- Requires `merge()` to become Managed again.

| Property | Detached |
|----------|----------|
| Inside Persistence Context | ❌ |
| Dirty Checking | ❌ |
| Automatic UPDATE | ❌ |
| Java Object Exists | ✅ |
| Database Row Exists | ✅ |

---

# What Happens After detach()?

Consider the following example.

```java
Employee employee =
    entityManager.find(Employee.class, 1L);

entityManager.detach(employee);

employee.setSalary(500000);
```

Execution Flow

```text
Database
    │
 SELECT
    │
    ▼
Managed
    │
detach()
    ▼
Detached
    │
setSalary()
    ▼
Hibernate Ignores Changes
```

The salary changes only inside the Java object.

Since Hibernate is no longer tracking the entity, no SQL is generated.

---

# Why Doesn't Dirty Checking Work?

Dirty Checking depends on the Persistence Context.

```text
Persistence Context

Employee(id=1)
```

After `detach()`:

```text
Persistence Context

(empty)

Java Heap

Employee(id=1)
```

Since the entity no longer exists inside the Persistence Context, Hibernate has nothing to compare during `flush()`.

---

# How Does an Entity Become Detached?

An entity can become Detached in several ways.

## EntityManager.detach()

```java
entityManager.detach(employee);
```

Removes a single entity from the Persistence Context.

---

## EntityManager.clear()

```java
entityManager.clear();
```

Detaches every Managed entity.

```text
Before clear()

Persistence Context

Employee
Department
Address

↓

After clear()

Persistence Context

(empty)
```

---

## EntityManager.close()

Closing the EntityManager destroys the Persistence Context.

```text
EntityManager Closed
        │
        ▼
Persistence Context Destroyed
        │
        ▼
All Managed Entities Become Detached
```

---

## Transaction Completion (Spring Boot)

In a Spring Boot application, the Persistence Context is usually transaction-scoped.

```text
@Transactional
      │
Business Logic
      │
Commit
      │
EntityManager Closed
      │
Persistence Context Destroyed
      │
Managed → Detached
```

This is why entities become Detached after a transaction completes.

---

# Removed State

A **Removed** entity is scheduled for deletion.

The database row still exists temporarily, but Hibernate will delete it during `flush()` or transaction commit.

Execution Flow

```text
Managed
   │
remove()
   ▼
Removed
   │
flush()/commit()
   ▼
DELETE
```

---

# Characteristics of a Removed Entity

| Property | Removed |
|----------|----------|
| Inside Persistence Context | ✅ |
| Dirty Checking | Technically Yes* |
| Scheduled for DELETE | ✅ |
| Database Row Exists | Until Flush/Commit |

> **Note:** Although the entity remains inside the Persistence Context, Hibernate ignores further field modifications because the row is already scheduled for deletion.

---

# What Happens After remove()?

```java
Employee employee =
    entityManager.find(Employee.class, 1L);

entityManager.remove(employee);
```

Current State

```text
Removed
```

Immediately after `remove()`:

- SQL has not executed.
- The entity still exists in the Persistence Context.
- Hibernate marks it for deletion.

Execution Flow

```text
Managed
   │
remove()
   ▼
Removed
   │
Commit
   ▼
DELETE
```

---

# Can We Modify a Removed Entity?

```java
Employee employee =
    entityManager.find(Employee.class, 1L);

entityManager.remove(employee);

employee.setSalary(500000);
```

Execution Flow

```text
Managed
   │
remove()
   ▼
Removed
   │
setSalary()
   ▼
Ignored
   │
DELETE
```

The salary change is ignored because Hibernate has already decided to delete the row.

---

# Comparing the Four States

| Property | Transient | Managed | Detached | Removed |
|-----------|-----------|----------|-----------|----------|
| Java Object Exists | ✅ | ✅ | ✅ | ✅ |
| Database Row Exists | Usually ❌ | ✅ | ✅ | Until Commit |
| Inside Persistence Context | ❌ | ✅ | ❌ | ✅ |
| Dirty Checking | ❌ | ✅ | ❌ | Limited* |
| Automatic UPDATE | ❌ | ✅ | ❌ | ❌ |
| Scheduled for DELETE | ❌ | ❌ | ❌ | ✅ |

---

# Complete State Transition Diagram

```text
                    new
                     │
                     ▼
                Transient
                     │
               persist()
                     ▼
                 Managed
             ┌────┼──────┐
             │    │      │
      detach() remove() close()/clear()
             │    │      │
             ▼    ▼      ▼
        Detached Removed Detached
             │
         merge()
             │
             ▼
          Managed
```

---

# Lifecycle Timeline

```text
Create Object
      │
      ▼
Transient
      │
persist()
      ▼
Managed
      │
Business Logic
      ▼
detach()
      ▼
Detached
      │
merge()
      ▼
Managed
      │
remove()
      ▼
Removed
      │
Commit
      ▼
Deleted
```

---

# Important Notes

- Detached entities are ordinary Java objects.
- Dirty Checking stops immediately after an entity becomes Detached.
- Removed entities remain inside the Persistence Context until deletion.
- Removing an entity does not immediately execute SQL.
- Closing the EntityManager detaches all Managed entities.
- `clear()` detaches every Managed entity.

---

# Common Mistakes

❌ Assuming Detached entities automatically update the database.

✔ Detached entities are ignored until merged.

---

❌ Assuming `remove()` immediately deletes the row.

✔ Deletion occurs during `flush()` or transaction commit.

---

❌ Assuming Removed entities disappear instantly.

✔ They remain in the Persistence Context until the transaction synchronizes.

---

❌ Confusing Detached and Removed states.

✔ Detached means "not tracked."

✔ Removed means "tracked and scheduled for deletion."

---

# Important Q&A

### Q1

What happens when an entity becomes Detached?

**Answer**

Hibernate stops tracking it.

Dirty Checking no longer works.

---

### Q2

Can a Detached entity update the database automatically?

**Answer**

No.

It must first become Managed again using `merge()`.

---

### Q3

What happens immediately after calling `remove()`?

**Answer**

- Entity state becomes Removed.
- SQL has not executed.
- Hibernate schedules a DELETE.

---

### Q4

Can we modify a Removed entity?

**Answer**

Yes, the Java object can still change, but Hibernate ignores those modifications because the entity is already scheduled for deletion.

---

### Q5

What is the main difference between Detached and Removed?

**Answer**

Detached:

- Not managed.
- No SQL operations occur automatically.

Removed:

- Still managed.
- Scheduled for deletion during `flush()` or transaction commit.

---

# Summary

- Detached entities are no longer managed by Hibernate.
- Dirty Checking applies only to Managed entities.
- Removed entities remain inside the Persistence Context until deletion.
- `detach()`, `clear()`, and `close()` all remove entities from the Persistence Context.
- Understanding these state transitions makes Hibernate behavior predictable.

---

# Quick Revision

- Managed → `detach()` → Detached
- Managed → `remove()` → Removed
- Detached → `merge()` → Managed
- `clear()` → Detach all entities
- `close()` → Destroy Persistence Context
- Removed → `DELETE` during `flush()` / `commit()`
---

# Hibernate Behavior Across Entity States

The current state of an entity determines how Hibernate behaves.

The following diagram summarizes Hibernate's behavior.

```text
                Entity State
                     │
     ┌───────────────┼────────────────┐
     │               │                │
     ▼               ▼                ▼
 Track Changes   Generate SQL   Synchronize DB
```

Hibernate first determines the entity's state before deciding whether it should perform Dirty Checking, execute SQL, or ignore the object entirely.

---

# Dirty Checking Across Different States

Dirty Checking is Hibernate's mechanism for automatically detecting changes made to Managed entities.

However, Dirty Checking is **state-dependent**.

| Entity State | Dirty Checking |
|--------------|----------------|
| Transient | ❌ No |
| Managed | ✅ Yes |
| Detached | ❌ No |
| Removed | Limited* |

Dirty Checking works only when Hibernate owns the object through the Persistence Context.

---

## Transient Example

```java
Employee employee = new Employee();

employee.setSalary(500000);
```

Object State

```text
new Employee()
      │
      ▼
Transient
      │
setSalary()
      ▼
Hibernate Unaware
```

Result

- No SQL
- No tracking
- No Dirty Checking

---

## Managed Example

```java
Employee employee =
    entityManager.find(Employee.class, 1L);

employee.setSalary(500000);
```

Internal Flow

```text
Managed
   │
setSalary()
   │
Dirty Checking
   │
flush()/commit()
   ▼
UPDATE
```

Result

Hibernate automatically generates:

```sql
UPDATE employee
SET salary = 500000
WHERE id = 1;
```

No explicit update method is required.

---

## Detached Example

```java
Employee employee =
    entityManager.find(Employee.class, 1L);

entityManager.detach(employee);

employee.setSalary(500000);
```

Object State

```text
Managed
   │
detach()
   ▼
Detached
   │
setSalary()
   ▼
Ignored
```

Result

- Java object changes
- Database remains unchanged
- No Dirty Checking

---

## Removed Example

```java
Employee employee =
    entityManager.find(Employee.class, 1L);

entityManager.remove(employee);

employee.setSalary(500000);
```

Internal Flow

```text
Managed
   │
remove()
   ▼
Removed
   │
setSalary()
   ▼
Ignored
   │
DELETE
```

Hibernate has already scheduled the row for deletion, so updating fields serves no purpose.

---

# State Transition Examples

## Scenario 1

```java
Employee employee = new Employee();
```

Current State

```text
Transient
```

SQL Generated

```text
No
```

---

## Scenario 2

```java
Employee employee = new Employee();

entityManager.persist(employee);
```

Transition

```text
Transient
      │
persist()
      ▼
Managed
```

SQL

```text
INSERT
```

Generated during:

- `flush()`
- Transaction commit

---

## Scenario 3

```java
Employee employee =
    entityManager.find(Employee.class, 1L);

entityManager.detach(employee);
```

Transition

```text
Managed
   │
detach()
   ▼
Detached
```

SQL

```text
None
```

---

## Scenario 4

```java
Employee employee =
    entityManager.find(Employee.class, 1L);

entityManager.remove(employee);
```

Transition

```text
Managed
   │
remove()
   ▼
Removed
```

SQL

```text
DELETE
```

Generated during:

- `flush()`
- Commit

---

## Scenario 5

```java
Employee detached = ...

Employee managed =
    entityManager.merge(detached);
```

Transition

```text
Detached
   │
merge()
   ▼
Managed
```

The returned object is Managed.

The original object remains Detached.

---

# Lifecycle Timeline

A typical entity moves through these states during a business transaction.

```text
new Employee()
        │
        ▼
Transient
        │
persist()
        ▼
Managed
        │
Business Logic
        ▼
flush()
        ▼
Database Updated
        │
Transaction Ends
        ▼
Detached
```

Notice that after the transaction completes, the entity is no longer Managed.

---

# Real-World Example

Consider an employee management system.

### Step 1

```java
Employee employee =
    employeeRepository.findById(1L).get();
```

State

```text
Managed
```

---

### Step 2

```java
employee.setSalary(800000);
```

State

```text
Managed
```

Dirty Checking starts tracking the modification.

---

### Step 3

Transaction commits.

Hibernate executes:

```sql
UPDATE employee
SET salary = 800000
WHERE id = 1;
```

---

### Step 4

Method returns.

Spring closes the transaction.

Current State

```text
Detached
```

---

### Step 5

```java
employee.setSalary(900000);
```

Result

```text
No UPDATE
```

The object still exists, but Hibernate no longer manages it.

---

# Why Hibernate Uses Entity States

Without entity states, Hibernate would need to monitor every Java object created in the JVM.

Imagine an application with:

- Thousands of DTOs
- Millions of temporary objects
- Collections
- Utility classes

Tracking everything would consume excessive memory and processing time.

Instead, Hibernate tracks only Managed entities.

Benefits

- Better performance
- Lower memory usage
- Efficient Dirty Checking
- Predictable synchronization

---

# Internal Decision Process

Whenever Hibernate encounters an entity, it effectively asks:

```text
Is it Managed?
        │
   ┌────┴────┐
   │         │
 Yes         No
 │            │
Track      Ignore
Changes
```

If the entity is Managed:

- Track modifications
- Perform Dirty Checking
- Generate SQL when required

Otherwise, Hibernate ignores changes.

---

# Important Notes

- Dirty Checking depends entirely on the Managed state.
- Detached entities behave like ordinary Java objects.
- Removed entities remain managed until deletion.
- Hibernate never tracks Transient objects.
- Entity state determines whether SQL will be generated.

---

# Common Mistakes

❌ Expecting Detached entities to update automatically.

✔ Dirty Checking works only for Managed entities.

---

❌ Assuming transaction completion leaves entities Managed.

✔ Transaction completion usually detaches entities.

---

❌ Believing Hibernate continuously watches every Java object.

✔ Hibernate monitors only Managed entities inside the Persistence Context.

---

❌ Confusing Java object lifetime with entity lifecycle.

✔ A Java object may still exist long after Hibernate stops managing it.

---

# Important Q&A

### Q1

Which entity state participates in Dirty Checking?

**Answer**

Managed.

---

### Q2

Can a Detached entity generate an UPDATE automatically?

**Answer**

No.

Hibernate ignores Detached entities until they are merged.

---

### Q3

Why does Hibernate ignore Transient objects?

**Answer**

Because they do not belong to the Persistence Context.

---

### Q4

After a transaction commits, what usually happens to Managed entities?

**Answer**

They become Detached because the Persistence Context is destroyed.

---

### Q5

Why does Hibernate maintain entity states?

**Answer**

Entity states allow Hibernate to determine:

- Whether the object should be tracked.
- Whether SQL should be generated.
- Whether Dirty Checking should run.
- Whether synchronization with the database is required.

---

# Summary

- Entity state controls Hibernate's behavior.
- Dirty Checking works only for Managed entities.
- Detached entities require `merge()` before Hibernate tracks them again.
- Removed entities remain managed until deletion.
- Understanding entity states makes Hibernate behavior predictable.

---

# Quick Revision

- Transient → Hibernate unaware
- Managed → Hibernate tracks changes
- Detached → Java object only
- Removed → Scheduled for DELETE
- Dirty Checking → Managed only
- Transaction ends → Managed becomes Detached
- Hibernate tracks entities only inside the Persistence Context

---

## Next Topics

- Advanced Interview Questions
- State Prediction Scenarios
- Common Debugging Cases
- Complete State Revision
---

# Predict the Entity State

The following scenarios test your understanding of entity states.

Instead of memorizing APIs, always answer these five questions:

1. What is the current entity state?
2. Is the entity inside the Persistence Context?
3. Will Dirty Checking work?
4. Will Hibernate generate SQL?
5. Why?

---

## Scenario 1

```java
Employee employee = new Employee();

employee.setSalary(500000);
```

### Analysis

| Question | Answer |
|----------|--------|
| Current State | Transient |
| Persistence Context | ❌ |
| Dirty Checking | ❌ |
| SQL Generated | ❌ |

Reason

The object was created using `new` and Hibernate is completely unaware of it.

---

## Scenario 2

```java
Employee employee = new Employee();

entityManager.persist(employee);

employee.setSalary(500000);
```

### Analysis

| Question | Answer |
|----------|--------|
| Current State | Managed |
| Persistence Context | ✅ |
| Dirty Checking | ✅ |
| SQL Generated | INSERT |

Reason

After `persist()`, the entity becomes Managed.

Hibernate now tracks every modification.

---

## Scenario 3

```java
Employee employee =
    entityManager.find(Employee.class, 1L);

employee.setSalary(500000);
```

### Analysis

| Question | Answer |
|----------|--------|
| Current State | Managed |
| Persistence Context | ✅ |
| Dirty Checking | ✅ |
| SQL Generated | UPDATE |

Reason

`find()` loads the entity into the Persistence Context.

Dirty Checking detects the salary modification automatically.

---

## Scenario 4

```java
Employee employee =
    entityManager.find(Employee.class, 1L);

entityManager.detach(employee);

employee.setSalary(500000);
```

### Analysis

| Question | Answer |
|----------|--------|
| Current State | Detached |
| Persistence Context | ❌ |
| Dirty Checking | ❌ |
| SQL Generated | ❌ |

Reason

The Java object changes, but Hibernate has stopped tracking it.

---

## Scenario 5

```java
Employee employee =
    entityManager.find(Employee.class, 1L);

entityManager.remove(employee);
```

### Analysis

| Question | Answer |
|----------|--------|
| Current State | Removed |
| Persistence Context | ✅ |
| Dirty Checking | Limited |
| SQL Generated | DELETE |

Reason

Hibernate schedules the row for deletion.

Actual SQL executes during `flush()` or transaction commit.

---

## Scenario 6

```java
Employee employee =
    entityManager.find(Employee.class, 1L);

entityManager.remove(employee);

employee.setSalary(900000);
```

### Analysis

| Question | Answer |
|----------|--------|
| Current State | Removed |
| Persistence Context | ✅ |
| Salary Updated | ❌ |
| DELETE Executed | ✅ |

Reason

The row is already scheduled for deletion.

Further modifications are ignored.

---

## Scenario 7

```java
Employee employee =
    entityManager.find(Employee.class, 1L);

entityManager.detach(employee);

Employee managed =
    entityManager.merge(employee);
```

### Analysis

| Question | Answer |
|----------|--------|
| employee State | Detached |
| managed State | Managed |
| Same Object | ❌ |

Reason

`merge()` copies the Detached object's state into a Managed entity.

The original object remains Detached.

---

# State Comparison Cheat Sheet

| Feature | Transient | Managed | Detached | Removed |
|----------|-----------|----------|-----------|----------|
| Java Object Exists | ✅ | ✅ | ✅ | ✅ |
| Database Row Exists | Usually ❌ | ✅ | ✅ | Until Commit |
| Inside Persistence Context | ❌ | ✅ | ❌ | ✅ |
| Dirty Checking | ❌ | ✅ | ❌ | Limited |
| Automatic INSERT | ❌ | Pending | ❌ | ❌ |
| Automatic UPDATE | ❌ | ✅ | ❌ | ❌ |
| Automatic DELETE | ❌ | ❌ | ❌ | Pending |

---

# State Transition Cheat Sheet

```text
new
 │
 ▼
Transient
 │
persist()
 ▼
Managed
 │
 ├─────────────┐
 │             │
 │ detach()    │ remove()
 ▼             ▼
Detached     Removed
 │
 │ merge()
 ▼
Managed
```

---

# How Hibernate Thinks

Whenever Hibernate encounters an entity, it follows a simple decision process.

```text
               Entity
                  │
                  ▼
      Is it Managed?
          │
    ┌─────┴─────┐
    │           │
   Yes          No
    │           │
Track Changes   Ignore
    │
Dirty Checking
    │
Generate SQL
```

This simple model explains almost every Hibernate behavior.

---

# Real-World Examples

## Creating a New Employee

```text
new Employee()
      │
Transient
      │
persist()
      │
Managed
      │
commit()
      │
INSERT
```

---

## Updating an Existing Employee

```text
find()
      │
Managed
      │
setSalary()
      │
Dirty Checking
      │
UPDATE
```

---

## Editing After Transaction Ends

```text
find()
      │
Managed
      │
Commit
      │
Detached
      │
setSalary()
      │
Ignored
```

---

## Deleting an Employee

```text
find()
      │
Managed
      │
remove()
      │
Removed
      │
DELETE
```

---

# Common Debugging Questions

When something unexpected happens, ask these questions first.

### Why didn't Hibernate execute an UPDATE?

Possible reasons:

- Entity is Detached.
- Entity is Transient.
- Dirty Checking is not active.
- Transaction never committed.

---

### Why didn't Hibernate execute an INSERT?

Possible reasons:

- `persist()` was never called.
- Transaction rolled back.
- Entity was never Managed.

---

### Why didn't Hibernate execute a DELETE?

Possible reasons:

- `remove()` was not called.
- Entity was Detached.
- Transaction rolled back.

---

### Why is Dirty Checking not working?

Possible reasons:

- Entity is Detached.
- EntityManager is closed.
- Persistence Context no longer exists.

---

# Common Mistakes

❌ Assuming every Java object is persistent.

✔ Only Managed entities are persistent.

---

❌ Expecting Detached entities to synchronize automatically.

✔ Detached entities require `merge()`.

---

❌ Forgetting that transaction completion detaches entities.

✔ Managed entities usually become Detached after the Persistence Context is destroyed.

---

❌ Confusing object lifetime with entity lifetime.

✔ A Java object may exist long after Hibernate stops managing it.

---

# Best Practices

- Keep business logic inside a transaction whenever possible.
- Avoid modifying Detached entities unless they will be merged.
- Always know the current state of your entity.
- Let Hibernate perform Dirty Checking instead of writing unnecessary update logic.
- Think in terms of entity states rather than SQL statements.

---

# Summary

Entity States are the foundation of Hibernate.

Every Hibernate feature—including Dirty Checking, Persistence Context, EntityManager operations, Flush, Lazy Loading, and Transactions—depends on understanding these four states.

Whenever you're unsure why Hibernate behaves a certain way, ask one question:

> **"What is the current state of this entity?"**

Most Hibernate problems can be solved by answering that single question.

---

# Quick Revision

### Four Entity States

```text
Transient
Managed
Detached
Removed
```

### State Changes

```text
persist() → Managed

detach() → Detached

merge() → Managed

remove() → Removed
```

### Dirty Checking

```text
Transient → ❌

Managed → ✅

Detached → ❌

Removed → Limited
```

### Persistence Context

```text
Managed → Inside

Removed → Inside

Transient → Outside

Detached → Outside
```

### One-Line Memory Trick

> **Transient is unknown, Managed is tracked, Detached is forgotten, Removed is scheduled for deletion.**

---

# Related Topics

The concepts in this chapter directly support the following topics:

- Persistence Context
- EntityManager
- Dirty Checking
- Flush Mechanism
- First-Level Cache
- Hibernate Internals
- JPA Entity Mapping
- Relationship Mapping
- Fetch Strategies
- `@Transactional`
- Transaction Propagation
- Transaction Isolation