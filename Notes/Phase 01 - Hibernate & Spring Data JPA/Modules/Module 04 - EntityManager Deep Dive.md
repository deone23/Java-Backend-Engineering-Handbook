# Module 06 - EntityManager Deep Dive

## Learning Objectives

After completing this chapter, you should be able to:

- Explain the purpose of the `EntityManager`.
- Describe the relationship between the `EntityManager` and the Persistence Context.
- Understand how entities become Managed.
- Explain why Hibernate returns the same object within a Persistence Context.
- Predict the behavior of `find()` without looking at SQL logs.

---

# 1. EntityManager

The **EntityManager** is the primary JPA interface responsible for interacting with the Persistence Context.

Every operation performed through JPA—such as loading, persisting, updating, removing, refreshing, or detaching entities—is coordinated through the `EntityManager`.

### Responsibilities

- Load entities from the database
- Persist new entities
- Remove entities
- Refresh entity state
- Detach entities
- Manage the Persistence Context
- Coordinate Dirty Checking and synchronization

> **Definition:** The `EntityManager` manages the **Persistence Context**, while the Persistence Context manages entity objects.

---

# 2. Persistence Context

The **Persistence Context** is an in-memory workspace that stores and manages entity objects during a transaction.

Whenever Hibernate loads an entity, it is placed inside the Persistence Context and becomes a **Managed entity**.

```text
Database
  ↓
EntityManager
  ↓
Persistence Context
  ├── Employee(id=1)
  ├── Employee(id=2)
  └── Department(id=10)
```

As long as an entity remains inside the Persistence Context:

- Hibernate tracks every modification.
- Dirty Checking remains active.
- Hibernate synchronizes changes with the database during `flush()` or transaction commit.

---

# 3. Relationship Between EntityManager and Persistence Context

The relationship can be visualized as:

```text
Application
  ↓
EntityManager
  ↓
Persistence Context
  ↓
Managed Entities
```

The EntityManager itself does not directly track entities.

Instead, it delegates all entity management responsibilities to the Persistence Context.

---

# 4. Object Identity

One of Hibernate's most important guarantees is:

> **Within a single Persistence Context, one database row is represented by exactly one Managed Java object.**

Example

```java
Employee emp1 = entityManager.find(Employee.class, 1L);
Employee emp2 = entityManager.find(Employee.class, 1L);

System.out.println(emp1 == emp2);
```

Output

```text
true
```

Internal representation

```text
Persistence Context

Employee(id=1)
      ▲
   ┌──┴──┐
 emp1  emp2
```

Both variables reference the same Managed object.

### Why?

Maintaining a single Managed object prevents conflicting copies of the same database row and enables reliable Dirty Checking.

---

# 5. One EntityManager = One Persistence Context

Each `EntityManager` owns its own Persistence Context.

```text
EntityManager A
  ↓
Persistence Context A
  ↓
Employee(id=1)

────────────────────────

EntityManager B
  ↓
Persistence Context B
  ↓
Employee(id=1)
```

Although both objects represent the same database row, they are different Java objects.

Example

```java
EntityManager em1 = entityManagerFactory.createEntityManager();
EntityManager em2 = entityManagerFactory.createEntityManager();

Employee e1 = em1.find(Employee.class, 1L);
Employee e2 = em2.find(Employee.class, 1L);

System.out.println(e1 == e2);
```

Output

```text
false
```

Reason:

Each Persistence Context manages its own entity instances independently.

---

# 6. find()

The `find()` method loads an entity from the database and immediately places it inside the Persistence Context.

```java
Employee employee = entityManager.find(Employee.class, 1L);
```

Execution flow

```text
Database
  ↓
SELECT
  ↓
Persistence Context
  ↓
Managed Entity
```

Once the entity becomes Managed, Hibernate automatically tracks every change.

Example

```java
Employee employee = entityManager.find(Employee.class, 1L);

employee.setSalary(500000);
```

No explicit `update()` or `save()` call is required.

Dirty Checking detects the modification automatically.

---

# 7. Repeated find()

Calling `find()` multiple times for the same entity within the same Persistence Context does not execute another SQL query.

```java
Employee emp1 = entityManager.find(Employee.class, 1L);
Employee emp2 = entityManager.find(Employee.class, 1L);
```

Execution

```text
First find()
  ↓
SELECT
  ↓
Persistence Context

Second find()
  ↓
Return existing Managed object
```

Only one SQL statement is executed.

```sql
SELECT * FROM employee WHERE id = 1;
```

Result

```java
emp1 == emp2
```

```text
true
```

This behavior is provided by Hibernate's **First-Level Cache (Persistence Context)**.

---

# Important Notes

- Every `EntityManager` owns exactly one Persistence Context.
- Managed entities always reside inside the Persistence Context.
- Hibernate guarantees one Managed object per database row within a Persistence Context.
- `find()` returns a Managed entity.
- Repeated `find()` calls return the existing Managed object instead of executing another SQL query.
- Dirty Checking begins as soon as an entity becomes Managed.

---

# Common Mistakes

❌ Assuming the `EntityManager` directly manages entity objects.

✔ The Persistence Context manages entity objects.

---

❌ Assuming two different EntityManagers share the same Managed entities.

✔ Each EntityManager has its own independent Persistence Context.

---

❌ Expecting `find()` to execute a SQL query every time.

✔ Hibernate first checks the Persistence Context before querying the database.

---

# Important Q&A

### Q1. Who owns the Persistence Context?

**Answer**

The `EntityManager` owns the Persistence Context.

---

### Q2. Do two different EntityManagers share Managed entities?

**Answer**

No.

Each EntityManager has its own Persistence Context and manages its own entity instances.

---

### Q3.

```java
Employee e1 = entityManager.find(Employee.class, 1L);
Employee e2 = entityManager.find(Employee.class, 1L);
```

**Questions**

- Is `e1 == e2`?
- How many `SELECT` statements execute?

**Answer**

- `e1 == e2` → `true`
- Only one `SELECT` executes because the second `find()` returns the existing Managed object from the Persistence Context.

---

# Summary

- `EntityManager` is the entry point for JPA operations.
- The Persistence Context stores and manages entity objects.
- `find()` creates a Managed entity.
- Hibernate guarantees one Managed object per database row within a Persistence Context.
- Repeated `find()` calls return the same Java object.
- Different EntityManagers never share Managed entities.

---

# Quick Revision

- EntityManager → manages the Persistence Context.
- Persistence Context → manages entity objects.
- `find()` → loads a Managed entity.
- Managed entities participate in Dirty Checking.
- One database row = one Managed object (per Persistence Context).
- Different EntityManagers create different Managed objects.
- Repeated `find()` does not execute another SQL query.
- The Persistence Context acts as Hibernate's First-Level Cache.

---

# 8. persist()

The `persist()` method moves an entity from the **Transient** state to the **Managed** state.

Once an entity becomes Managed, Hibernate starts tracking it through the Persistence Context.

```java
Employee employee = new Employee();
employee.setName("Vinay");
employee.setSalary(100000);

entityManager.persist(employee);
```

Execution Flow

```text
new Employee()
      ↓
Transient
      ↓
persist()
      ↓
Managed
      ↓
Persistence Context
```

At this point:

- No SQL has been executed.
- The entity exists only inside the Persistence Context.
- Dirty Checking becomes active.

The `INSERT` statement is generated during:

- `flush()`
- Transaction `commit()`

> **Important:** `persist()` registers the entity with the Persistence Context. It does not immediately insert the row into the database.

---

# 9. Entity States

Every JPA entity exists in one of four states.

| State | Persistence Context | Dirty Checking | Database |
|--------|---------------------|----------------|----------|
| Transient | ❌ | ❌ | ❌ |
| Managed | ✅ | ✅ | ✅ / Pending INSERT |
| Detached | ❌ | ❌ | ✅ |
| Removed | ✅ | ✅ | Pending DELETE |

State transitions

```text
Transient
    ↓ persist()
Managed
    ↓ detach()
Detached
    ↓ merge()
Managed
    ↓ remove()
Removed
```

Understanding these four states explains nearly every behavior of Hibernate.

---

# 10. Managed Entity

A Managed entity is completely controlled by Hibernate.

Characteristics

- Stored inside the Persistence Context.
- Dirty Checking is active.
- Automatically synchronized with the database.
- Returned by `find()`.
- Returned by `merge()`.

Example

```java
Employee employee = entityManager.find(Employee.class, 1L);

employee.setSalary(500000);
```

Execution Flow

```text
find()
  ↓
Managed Entity
  ↓
setSalary()
  ↓
Dirty Checking
  ↓
Commit
  ↓
UPDATE
```

Notice that no explicit `update()` method is required.

---

# 11. Detached Entity

A Detached entity is an ordinary Java object that was once Managed but is no longer associated with the Persistence Context.

Example

```java
Employee employee = entityManager.find(Employee.class, 1L);

entityManager.detach(employee);

employee.setSalary(500000);
```

Execution Flow

```text
Managed
   ↓ detach()
Detached
   ↓
setSalary()
   ↓
No Dirty Checking
```

Although the Java object changes, Hibernate ignores those changes because it is no longer tracking the entity.

---

# 12. merge()

The `merge()` method copies the state of a Detached entity into a Managed entity.

The original Detached object remains Detached.

Example

```java
Employee detached = new Employee();
detached.setId(1L);
detached.setSalary(500000);

Employee managed = entityManager.merge(detached);
```

Execution Flow

```text
Detached Object
      │
Copy State
      ▼
Managed Object
      │
Persistence Context
```

Result

```java
System.out.println(detached == managed);
```

```text
false
```

Hibernate creates (or reuses) a Managed entity and copies the Detached object's state into it.

---

# 13. Why Doesn't merge() Return the Same Object?

Hibernate guarantees:

> **One database row = One Managed object within a Persistence Context.**

If the original Detached object became Managed directly, Hibernate could end up managing multiple Java objects representing the same database row.

Instead, it copies the Detached state into the Managed object.

```text
Detached Object
      │
      │ Copy State
      ▼
Managed Object
```

This design keeps object identity consistent throughout the transaction.

---

# 14. merge() When a Managed Entity Already Exists

Suppose the Persistence Context already contains the entity.

```java
Employee managed1 = entityManager.find(Employee.class, 1L);

Employee detached = new Employee();
detached.setId(1L);
detached.setSalary(500000);

Employee managed2 = entityManager.merge(detached);
```

Execution Flow

```text
Persistence Context

Employee(id=1)
      ▲
      │
Copy State
      │
Detached Employee(id=1)
```

Result

```java
managed1 == managed2
```

```text
true
```

Hibernate reuses the existing Managed object instead of creating another one.

---

# 15. Dirty Checking After merge()

After `merge()`, Hibernate tracks only the Managed object.

```java
Employee detached = new Employee();
detached.setId(1L);
detached.setSalary(500000);

Employee managed = entityManager.merge(detached);

managed.setSalary(700000);
```

Execution Flow

```text
Detached
   ↓ merge()
Managed
   ↓
setSalary(700000)
   ↓
Dirty Checking
   ↓
Commit
   ↓
UPDATE salary = 700000
```

The Detached object is ignored after the merge operation.

---

# Important Notes

- `persist()` changes an entity from Transient to Managed.
- `persist()` does not immediately execute an `INSERT`.
- Managed entities participate in Dirty Checking.
- Detached entities behave like normal Java objects.
- `merge()` copies state into a Managed entity.
- The original Detached object never becomes Managed.
- `merge()` may return an existing Managed object already present in the Persistence Context.

---

# Common Mistakes

❌ Assuming `persist()` immediately inserts data.

✔ SQL executes during `flush()` or transaction commit.

---

❌ Assuming `merge()` makes the original object Managed.

✔ `merge()` returns a Managed entity. The original object remains Detached.

---

❌ Modifying a Detached entity expecting Hibernate to update the database.

✔ Dirty Checking works only for Managed entities.

---

❌ Calling `persist()` for an existing Detached entity.

✔ Use `merge()` to synchronize Detached entity state.

---

# Important Q&A

### Q1. Which state does `persist()` move an entity to?

**Answer**

Transient → Managed

---

### Q2. Can a Detached entity update the database automatically?

**Answer**

No.

Detached entities are outside the Persistence Context, so Dirty Checking no longer tracks changes.

---

### Q3. Why does `merge()` return a different object?

**Answer**

Hibernate copies the Detached entity's state into a Managed entity inside the Persistence Context.

The original object remains Detached.

---

### Q4.

```java
Employee detached = ...

Employee managed = entityManager.merge(detached);

System.out.println(detached == managed);
```

**Question**

What is the output?

**Answer**

```text
false
```

Both references point to different Java objects.

---

### Q5.

```java
Employee managed1 = entityManager.find(Employee.class, 1L);

Employee detached = new Employee();
detached.setId(1L);

Employee managed2 = entityManager.merge(detached);
```

**Questions**

- Is `managed1 == managed2` true?
- Will Hibernate execute another `SELECT`?

**Answer**

- `managed1 == managed2` → `true`
- No additional `SELECT` executes because the Managed entity already exists in the Persistence Context.

---

### Q6.

```java
Employee employee = entityManager.find(Employee.class, 1L);

entityManager.detach(employee);

employee.setSalary(500000);
```

**Question**

Will Hibernate execute an `UPDATE`?

**Answer**

No.

The entity is Detached, so Hibernate ignores further changes.

---

### Q7.

```java
Employee employee = entityManager.find(Employee.class, 1L);

entityManager.detach(employee);

employee.setSalary(500000);

Employee managed = entityManager.merge(employee);

managed.setSalary(700000);
```

**Questions**

- How many `UPDATE` statements execute?
- What salary reaches the database?

**Answer**

- One `UPDATE`
- Final salary = `700000`

The Detached object is ignored. Only the Managed object returned by `merge()` participates in Dirty Checking.

---

# Summary

- `persist()` registers a new entity with the Persistence Context.
- Managed entities are tracked automatically.
- Detached entities are not tracked.
- `merge()` copies state into a Managed entity.
- Hibernate guarantees one Managed object per database row.
- Dirty Checking applies only to Managed entities.

---

# Quick Revision

- `persist()` → Transient → Managed
- SQL `INSERT` occurs during `flush()` or `commit()`
- Managed entities participate in Dirty Checking
- Detached entities are ordinary Java objects
- `merge()` copies state into a Managed entity
- `merge()` returns the Managed entity
- Original Detached object remains Detached
- Existing Managed entities are reused during `merge()`
---

# Entity Lifecycle Operations

## refresh()

The `refresh()` method reloads the entity from the database and overwrites the current in-memory state.

It is commonly used when the database has been modified externally or when the latest database state is required.

Example

```java
Employee employee = entityManager.find(Employee.class, 1L);

employee.setSalary(500000);

entityManager.refresh(employee);
```

Execution Flow

```text
Managed Entity
      │
refresh()
      ▼
SELECT
      ▼
Persistence Context Updated
```

After `refresh()`, the entity inside the Persistence Context contains exactly the same values as the database.

> **Important:** `refresh()` discards any unflushed modifications.

---

## refresh() After flush()

Consider the following example.

```java
Employee employee = entityManager.find(Employee.class, 1L);

employee.setSalary(500000);

entityManager.flush();

entityManager.refresh(employee);
```

Execution Flow

```text
Managed
  ↓
setSalary(500000)
  ↓
flush()
  ↓
UPDATE
  ↓
refresh()
  ↓
SELECT
```

Since `flush()` already synchronized the Persistence Context with the database, `refresh()` loads the same values again.

The entity still contains:

```text
salary = 500000
```

---

## contains()

The `contains()` method checks whether an entity is currently Managed by the Persistence Context.

```java
Employee employee = entityManager.find(Employee.class, 1L);

entityManager.contains(employee);
```

Output

```text
true
```

After detaching the entity

```java
entityManager.detach(employee);

entityManager.contains(employee);
```

Output

```text
false
```

Execution Flow

```text
Managed
   ↓
contains()
   ↓
true

detach()
   ↓
contains()
   ↓
false
```

> **Important:** `contains()` checks only whether Hibernate is managing the entity. It does not check whether the entity exists in the database.

---

## remove()

The `remove()` method changes a Managed entity into the **Removed** state.

```java
Employee employee = entityManager.find(Employee.class, 1L);

entityManager.remove(employee);
```

Execution Flow

```text
Managed
   ↓
remove()
   ↓
Removed
   ↓
Commit
   ↓
DELETE
```

Immediately after calling `remove()`:

- The entity still exists inside the Persistence Context.
- SQL has not yet been executed.
- Hibernate marks the entity for deletion.

The actual `DELETE` statement is generated during:

- `flush()`
- Transaction `commit()`

---

## Modifying a Removed Entity

Consider the following code.

```java
Employee employee = entityManager.find(Employee.class, 1L);

entityManager.remove(employee);

employee.setSalary(500000);
```

Although the salary changes in Java memory, Hibernate ignores those modifications.

Execution

```text
Managed
   ↓
remove()
   ↓
Removed
   ↓
setSalary()
   ↓
Ignored
   ↓
DELETE
```

Since the row is already scheduled for deletion, updating its fields has no effect.

---

## Removing a Detached Entity

```java
Employee employee = entityManager.find(Employee.class, 1L);

entityManager.detach(employee);

entityManager.remove(employee);
```

Result

```text
IllegalArgumentException
```

Reason

`remove()` accepts only Managed entities.

Detached entities must first become Managed again using `merge()`.

Correct approach

```java
Employee managed = entityManager.merge(employee);

entityManager.remove(managed);
```

---

## remove() Followed by merge()

```java
Employee employee = entityManager.find(Employee.class, 1L);

entityManager.remove(employee);

entityManager.merge(employee);
```

Since the entity is already in the **Removed** state inside the Persistence Context, `merge()` does not cancel the deletion.

The row is still deleted when the transaction commits.

Execution Flow

```text
Managed
   ↓
remove()
   ↓
Removed
   ↓
merge()
   ↓
Deletion Still Scheduled
```

> **Important:** Once an entity is marked for removal within the current Persistence Context, calling `merge()` does not undo the pending delete operation.

---

## getReference()

`getReference()` returns a **Hibernate Proxy** instead of immediately loading the entity.

```java
Employee employee =
    entityManager.getReference(Employee.class, 1L);
```

Execution Flow

```text
getReference()
      ↓
Proxy Object
      ↓
No SELECT
```

Initially, Hibernate knows only the entity identifier.

Other fields remain unloaded until first accessed.

---

## Proxy Initialization

```java
Employee employee =
    entityManager.getReference(Employee.class, 1L);

employee.getId();
```

Output

```text
No SELECT
```

The identifier is already available.

Now access another property.

```java
employee.getName();
```

Execution

```text
Proxy
   ↓
getName()
   ↓
SELECT
   ↓
Initialize Proxy
```

After initialization, the proxy behaves like a normal Managed entity.

---

## LazyInitializationException

Consider the following code.

```java
EntityManager em = entityManagerFactory.createEntityManager();

Employee employee = em.getReference(Employee.class, 1L);

em.close();

employee.getName();
```

Execution Flow

```text
Proxy Created
      ↓
EntityManager Closed
      ↓
Persistence Context Destroyed
      ↓
getName()
      ↓
LazyInitializationException
```

The proxy needs the Persistence Context to load missing data.

Once the Persistence Context is destroyed, Hibernate cannot initialize the proxy.

---

## Why getId() Still Works

```java
Employee employee =
    entityManager.getReference(Employee.class, 1L);

employee.getId();
```

The identifier is already stored inside the proxy.

No database access is required.

```text
Proxy
 ├── id = 1
 ├── name = Not Loaded
 └── salary = Not Loaded
```

Calling `getName()` requires a database query.

Calling `getId()` does not.

---

# Important Notes

- `refresh()` reloads the latest state from the database.
- `refresh()` discards unflushed changes.
- `contains()` checks whether an entity is Managed.
- `remove()` marks an entity for deletion.
- SQL `DELETE` executes during `flush()` or transaction commit.
- `remove()` requires a Managed entity.
- `getReference()` returns a proxy.
- Accessing unloaded fields initializes the proxy.
- Closing the Persistence Context before proxy initialization causes `LazyInitializationException`.

---

# Common Mistakes

❌ Assuming `remove()` immediately deletes the row.

✔ Deletion occurs during `flush()` or transaction commit.

---

❌ Expecting field updates after `remove()` to be persisted.

✔ Removed entities are scheduled for deletion.

---

❌ Calling `remove()` on a Detached entity.

✔ Reattach it using `merge()` first.

---

❌ Assuming `getReference()` executes a `SELECT`.

✔ It creates only a proxy initially.

---

❌ Accessing lazy properties after closing the EntityManager.

✔ Results in `LazyInitializationException`.

---

# Important Q&A

### Q1

```java
Employee employee = entityManager.find(Employee.class, 1L);

entityManager.refresh(employee);
```

**Question**

Will Hibernate execute a `SELECT`?

**Answer**

Yes.

`refresh()` always reloads the entity from the database.

---

### Q2

```java
employee.setSalary(500000);

entityManager.refresh(employee);
```

**Question**

What happens to the salary?

**Answer**

The unflushed value is discarded.

The entity now reflects the database state.

---

### Q3

```java
employee.setSalary(500000);

entityManager.flush();

entityManager.refresh(employee);
```

**Question**

What salary remains?

**Answer**

`500000`

`flush()` synchronized the database before `refresh()` executed.

---

### Q4

What does `contains()` check?

**Answer**

It checks whether the entity is currently Managed by the Persistence Context.

---

### Q5

What happens immediately after calling `remove()`?

**Answer**

- Entity state becomes **Removed**
- Hibernate continues tracking it
- SQL has not executed yet
- Deletion is scheduled

---

### Q6

Does modifying a Removed entity affect the database?

**Answer**

No.

Hibernate ignores further modifications because the entity is already scheduled for deletion.

---

### Q7

Does `getReference()` execute a `SELECT` immediately?

**Answer**

No.

It returns a proxy object.

---

### Q8

Why does `getId()` work without executing a query?

**Answer**

The proxy already contains the entity identifier.

---

### Q9

Why does `getName()` execute a query?

**Answer**

The property is not loaded until first accessed.

---

### Q10

Why does `LazyInitializationException` occur?

**Answer**

The proxy attempts to initialize after the Persistence Context has already been destroyed.

---

# Summary

- `refresh()` synchronizes the entity with the database.
- `contains()` checks Managed status.
- `remove()` schedules deletion.
- Removed entities ignore further modifications.
- `getReference()` creates lazy proxies.
- Proxies require an active Persistence Context.
- `LazyInitializationException` occurs when Hibernate cannot initialize a proxy.

---

# Quick Revision

- `refresh()` → Reload database state
- `contains()` → Managed or not?
- `remove()` → Managed → Removed
- `DELETE` → Flush / Commit
- `getReference()` → Proxy
- `getId()` → No SELECT
- `getName()` → SELECT
- Closed Persistence Context + Proxy → `LazyInitializationException`
---

# Persistence Context Synchronization

## flush()

The `flush()` method synchronizes the Persistence Context with the database.

It does **not** commit the transaction.

```java
Employee employee = entityManager.find(Employee.class, 1L);

employee.setSalary(500000);

entityManager.flush();
```

Execution Flow

```text
Managed Entity
      ↓
setSalary()
      ↓
Dirty Checking
      ↓
flush()
      ↓
UPDATE
```

After `flush()`:

- SQL has already been executed.
- The transaction is still active.
- The transaction can still be rolled back.

> **Important:** `flush()` synchronizes the Persistence Context. `commit()` permanently commits the database transaction.

---

## flush() vs commit()

| flush() | commit() |
|---------|----------|
| Synchronizes the Persistence Context | Commits the database transaction |
| Executes SQL | Ends the transaction |
| Changes can still be rolled back | Changes become permanent |
| Transaction remains active | Transaction ends |

---

## Multiple flush() Calls

A transaction may contain multiple `flush()` operations.

Example

```java
Employee employee = entityManager.find(Employee.class, 1L);

employee.setSalary(500000);

entityManager.flush();

employee.setSalary(700000);
```

Execution Flow

```text
salary = 500000
      ↓
flush()
      ↓
UPDATE 500000
      ↓
salary = 700000
      ↓
commit()
      ↓
UPDATE 700000
```

Two `UPDATE` statements are generated because Hibernate performs Dirty Checking after every flush.

---

## flush() Followed by detach()

```java
Employee employee = entityManager.find(Employee.class, 1L);

employee.setSalary(500000);

entityManager.flush();

entityManager.detach(employee);

employee.setSalary(700000);
```

Execution Flow

```text
Managed
   ↓
flush()
   ↓
UPDATE 500000
   ↓
detach()
   ↓
Detached
   ↓
Ignored
```

The second modification is ignored because the entity is no longer Managed.

Final database value

```text
500000
```

---

## Automatic Flush

By default, Hibernate uses:

```java
FlushModeType.AUTO
```

Before executing a query, Hibernate checks whether pending changes could affect the query result.

If required, Hibernate automatically performs a flush.

Example

```java
employee.setSalary(500000);

entityManager.createQuery(
    "select e from Employee e"
).getResultList();
```

Execution Flow

```text
setSalary()
      ↓
JPQL Query
      ↓
AUTO Flush
      ↓
UPDATE
      ↓
SELECT
```

This guarantees that queries always see the latest transaction state.

---

## Why AUTO Flush Exists

Suppose Hibernate skipped the automatic flush.

```text
Persistence Context

salary = 500000

Database

salary = 100000
```

The query would return outdated information from the database.

Automatic flushing keeps query results consistent with the Persistence Context.

---

## FlushMode.COMMIT

The flush mode can be changed explicitly.

```java
entityManager.setFlushMode(FlushModeType.COMMIT);
```

Execution

```text
setSalary()
      ↓
JPQL Query
      ↓
No Flush
      ↓
Old Database Value
      ↓
commit()
      ↓
UPDATE
```

With `FlushMode.COMMIT`, Hibernate postpones synchronization until transaction commit (or an explicit `flush()`).

---

## AUTO vs COMMIT

| AUTO | COMMIT |
|------|---------|
| Default mode | Optional optimization |
| Flush before queries when required | Flush only at commit or explicit `flush()` |
| Queries see latest transaction state | Queries may read stale database data |
| Recommended for most applications | Used for performance optimization |

---

# EntityManager Lifecycle

In a Spring Boot application, developers rarely create an `EntityManager` manually.

Spring creates and manages it automatically.

Execution Flow

```text
@Transactional
      ↓
Spring opens EntityManager
      ↓
Persistence Context Created
      ↓
Business Logic
      ↓
flush()
      ↓
commit()
      ↓
Persistence Context Destroyed
```

Each transaction receives its own Persistence Context.

When the transaction ends:

- Managed entities become Detached.
- Dirty Checking stops.
- The Persistence Context is destroyed.

---

# Choosing the Right Operation

| Requirement | Method |
|------------|--------|
| Load an entity | `find()` |
| Persist a new entity | `persist()` |
| Synchronize a Detached entity | `merge()` |
| Stop tracking an entity | `detach()` |
| Reload from the database | `refresh()` |
| Check Managed status | `contains()` |
| Delete an entity | `remove()` |
| Obtain a lazy proxy | `getReference()` |
| Synchronize pending changes | `flush()` |

---

# Common Mistakes

❌ Assuming `flush()` commits the transaction.

✔ It only synchronizes the Persistence Context.

---

❌ Assuming SQL execution means the transaction has finished.

✔ SQL can execute before commit and still be rolled back.

---

❌ Assuming every query executes against the latest database state.

✔ Hibernate may return Managed entities directly from the Persistence Context.

---

❌ Forgetting that transaction completion detaches all Managed entities.

✔ Once the Persistence Context is destroyed, Dirty Checking stops.

---

# Important Q&A

### Q1

```java
employee.setSalary(500000);

entityManager.flush();
```

**Question**

Has the transaction been committed?

**Answer**

No.

Only the SQL has been executed.

The transaction remains active and can still be rolled back.

---

### Q2

If an exception occurs after `flush()` but before `commit()`, what happens?

**Answer**

The transaction rolls back.

The database undoes the previously executed SQL because the transaction was never committed.

---

### Q3

```java
employee.setSalary(500000);

entityManager.flush();

employee.setSalary(700000);
```

**Questions**

- How many `UPDATE` statements execute?
- What salary reaches the database?

**Answer**

- Two `UPDATE` statements
- Final salary = `700000`

---

### Q4

```java
employee.setSalary(500000);

entityManager.flush();

entityManager.detach(employee);

employee.setSalary(700000);
```

**Question**

What salary reaches the database?

**Answer**

`500000`

The second modification is ignored because the entity is Detached.

---

### Q5

Will Hibernate automatically flush before executing a JPQL query?

**Answer**

Yes.

When using `FlushMode.AUTO`, Hibernate flushes pending changes whenever necessary to keep query results consistent.

---

### Q6

Why would someone choose `FlushMode.COMMIT`?

**Answer**

To delay synchronization until commit and avoid unnecessary flushes before intermediate queries, improving performance in specific scenarios.

---

### Q7

When does the Persistence Context get destroyed?

**Answer**

At the end of the transaction, when the `EntityManager` is closed.

All Managed entities become Detached.

---

# Summary

The Persistence Context is the heart of Hibernate.

Every important feature—Dirty Checking, First-Level Cache, object identity, lazy loading, entity lifecycle, and automatic synchronization—depends on it.

Understanding the Persistence Context makes Hibernate behavior predictable without relying on trial and error.

---

# Quick Revision

- `find()` → Load & Manage
- `persist()` → Transient → Managed
- `merge()` → Copy Detached state
- `detach()` → Stop Dirty Checking
- `refresh()` → Reload from database
- `contains()` → Check Managed status
- `remove()` → Schedule DELETE
- `getReference()` → Lazy proxy
- `flush()` → Synchronize with database
- `commit()` → Complete transaction
- One Persistence Context → One Managed object per database row
- End of transaction → Managed → Detached

---

# Related Topics

- Entity States
- Persistence Context
- Dirty Checking
- First-Level Cache
- `@Transactional`
- Transaction Propagation
- Transaction Isolation
- Hibernate Performance