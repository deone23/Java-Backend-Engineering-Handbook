# Module 05 - Dirty Checking & Flush Mechanism

> [!INFO]
> ## Learning Objectives
> After completing this chapter, you will be able to:
> - Explain what Dirty Checking is and why Hibernate needs it.
> - Understand how Hibernate automatically detects entity changes.
> - Explain the purpose of the Snapshot.
> - Understand the relationship between Managed Entities and Snapshots.
> - Understand Hibernate's internal `EntityEntry` and `loadedState`.
> - Explain why no explicit `update()` call is required in JPA.
> - Visualize the complete Dirty Checking lifecycle.
> - Answer Dirty Checking interview questions with confidence.

---

# Why Do We Need Dirty Checking?

Imagine the following code:

```java
@Transactional
public void increaseSalary() {

    Employee employee = entityManager.find(Employee.class, 1L);

    employee.setSalary(500000);

}
```

Notice something interesting.

There is:

- ❌ No `save()`
- ❌ No `update()`
- ❌ No SQL written by us

Yet Hibernate eventually executes:

```sql
UPDATE employee
SET salary = 500000
WHERE id = 1;
```

The obvious question is:

> **How did Hibernate know that the salary changed?**

The answer is:

> **Dirty Checking**

Dirty Checking is one of Hibernate's most important features and is the reason we can simply modify Java objects without explicitly writing update statements.

---

# What is Dirty Checking?

> [!NOTE]
> **Dirty Checking** is the mechanism by which Hibernate automatically detects changes made to **Managed Entities** and generates the required SQL statements during `flush()`.

Think of it like Microsoft Word.

```text
Original Document
        │
        ▼
Edit Document
        │
        ▼
Save
        │
        ▼
Compare Changes
        │
        ▼
Store Latest Version
```

Hibernate follows the same philosophy.

```text
Load Entity
      │
      ▼
Modify Entity
      │
      ▼
Flush
      │
      ▼
Compare Current State with Original State
      │
      ▼
Generate SQL
```

---

# Hibernate Does NOT Watch Every Setter

Many beginners believe Hibernate continuously watches methods like:

```java
employee.setSalary(500000);
employee.setName("Vinay");
employee.setAge(30);
```

This is **not** how Hibernate works.

Calling a setter simply modifies the Managed Entity inside the Persistence Context.

```text
Persistence Context

Employee
salary = 500000
```

At this point:

- No Dirty Checking
- No SQL
- No Comparison

Hibernate postpones all of this work until synchronization is required.

---

# The Secret Behind Dirty Checking

When Hibernate loads an entity:

```java
Employee employee =
        entityManager.find(Employee.class, 1L);
```

Hibernate performs two operations.

## 1. Creates a Managed Entity

```text
Persistence Context

Employee
id = 1
name = Vinay
salary = 100000
```

---

## 2. Creates an Internal Snapshot

```text
Snapshot

id = 1
name = Vinay
salary = 100000
```

Hibernate now has two versions of the same data.

```text
Persistence Context

Managed Entity
──────────────
Employee
id = 1
name = Vinay
salary = 100000


Snapshot
────────
id = 1
name = Vinay
salary = 100000
```

The Managed Entity represents the **current working copy**.

The Snapshot represents the **original database state**.

---

# What Happens When We Modify the Entity?

Suppose we execute:

```java
employee.setSalary(500000);
```

Only the Managed Entity changes.

```text
Persistence Context

Managed Entity
──────────────
salary = 500000


Snapshot
────────
salary = 100000
```

Notice something important.

The Snapshot never changes.

Hibernate intentionally preserves the original values because they will be needed later during Dirty Checking.

---

# When Does Dirty Checking Actually Happen?

Dirty Checking **does not** occur when setters are called.

Instead, Hibernate waits until a synchronization point such as:

- `flush()`
- Transaction Commit
- Automatic Flush before a query (`FlushMode.AUTO`)

Only then does Hibernate compare:

```text
Managed Entity
        │
        ▼
Current State

VS

Snapshot
        │
        ▼
Original State
```

If differences are found:

```text
salary

100000

↓

500000
```

Hibernate marks the entity as **Dirty**.

---

# Dirty Checking Lifecycle

```text
find()
   │
   ▼
SELECT
   │
   ▼
Managed Entity Created
   │
   ▼
Snapshot Created
   │
─────────────────────────────
│
│ employee.setSalary()
│ employee.setName()
│ employee.setAge()
│
│ (No Comparison Yet)
│
─────────────────────────────
          │
          ▼
       flush()
          │
          ▼
Dirty Checking
(Current vs Snapshot)
          │
          ▼
Difference Found
          │
          ▼
Generate UPDATE
```

---

# Internal Architecture

Hibernate internally maintains something conceptually similar to this.

```text
Persistence Context

Employee@6ab31
        │
        ▼
EntityEntry
        │
        ├── Status
        ├── Version
        ├── Identifier
        └── loadedState
```

The important part for Dirty Checking is:

```text
loadedState
```

This stores the Snapshot.

---

# Does Hibernate Create Another Employee Object?

No.

Hibernate **does not** create another `Employee` instance.

Instead, it stores only the property values.

Conceptually:

```text
Managed Entity

Employee@12345
```

Snapshot:

```text
Object[]

[
1,
"Vinay",
"[email protected]",
30,
100000
]
```

This approach consumes much less memory than duplicating every entity object.

---

# Why Object[] Instead of Another Entity?

Hibernate does not care about:

- Business methods
- Validation logic
- Helper methods

It only needs the column values.

Example:

```text
Index 0 → id

Index 1 → name

Index 2 → email

Index 3 → age

Index 4 → salary
```

During Dirty Checking, Hibernate compares these stored values with the current Managed Entity.

---

# Internal Comparison

Suppose:

Current Entity

```text
salary = 500000
```

Snapshot

```text
salary = 100000
```

Comparison:

```text
Managed Entity

500000

↓

Snapshot

100000
```

Result:

```text
Different
```

Entity becomes:

```text
Dirty
```

Hibernate then schedules an UPDATE statement.

---

# Important Q&A

> [!QUESTION]
> **Does Hibernate immediately execute SQL when a setter is called?**

**No.**

Hibernate only modifies the Managed Entity inside the Persistence Context.

Dirty Checking happens later during `flush()`.

---

> [!QUESTION]
> **Why doesn't Hibernate compare after every setter call?**

Because that would be extremely inefficient.

Instead, Hibernate allows multiple modifications and performs a single comparison during `flush()`.

---

> [!QUESTION]
> **What does Hibernate compare during Dirty Checking?**

It compares:

- Current Managed Entity
- Original Snapshot (`loadedState`)

---

> [!QUESTION]
> **What is a Snapshot?**

A Snapshot is the original database state stored internally when an entity becomes Managed.

It is later used for Dirty Checking.

---

> [!QUESTION]
> **Where is the Snapshot stored?**

Inside Hibernate's internal `EntityEntry` as a conceptual `Object[]` called `loadedState`.

---

> [!QUESTION]
> **Does Hibernate create another Entity object for the Snapshot?**

No.

It stores only the property values internally, making the implementation more memory efficient.

---

# Interview Corner

> **How does Hibernate know an entity has changed without calling `update()`?**

A good interview answer:

> Hibernate stores both the Managed Entity and an internal Snapshot when an entity is loaded. During `flush()`, it compares the current state of the Managed Entity with the Snapshot. If any differences are found, Hibernate marks the entity as Dirty and automatically generates the required SQL statements.

---

# Key Takeaways

> [!SUCCESS]
> - Dirty Checking works only for **Managed Entities**.
> - Hibernate stores an internal Snapshot when an entity becomes Managed.
> - The Snapshot remains unchanged throughout the transaction.
> - Dirty Checking occurs during `flush()`, not during setter execution.
> - Hibernate compares the Managed Entity with the Snapshot to detect changes.
> - The Snapshot is stored internally as an `Object[]` (`loadedState`) inside an `EntityEntry`.
> - No explicit `update()` call is required because Dirty Checking automatically generates SQL.
---

# Flush Mechanism

Dirty Checking alone does not generate SQL.

Hibernate still needs to decide **when** to synchronize the Persistence Context with the database.

That responsibility belongs to **Flush**.

> [!NOTE]
> **Flush** is the process of synchronizing all pending changes from the **Persistence Context** to the database.

A common misconception is:

> **Flush = Commit**

This is **incorrect**.

Flush and Commit are two completely different operations.

---

# Dirty Checking vs Flush

Think of the complete process as two independent steps.

```text
Modify Managed Entity
        │
        ▼
Dirty Checking
(Current vs Snapshot)
        │
        ▼
Changes Found
        │
        ▼
Flush
        │
        ▼
Generate SQL
        │
        ▼
Database Executes SQL
```

Dirty Checking answers:

> **"What changed?"**

Flush answers:

> **"Send those changes to the database."**

---

# What Happens During Flush?

Consider the following code.

```java
Employee employee = entityManager.find(Employee.class, 1L);

employee.setSalary(500000);

entityManager.flush();
```

Let's see what Hibernate does internally.

### Step 1

```text
Managed Entity

salary = 500000
```

Snapshot

```text
salary = 100000
```

---

### Step 2

Dirty Checking compares both.

```text
100000

↓

500000
```

Difference detected.

---

### Step 3

Hibernate generates SQL.

```sql
UPDATE employee
SET salary = 500000
WHERE id = 1;
```

---

### Step 4

The SQL is immediately sent to the database.

However...

The transaction is **still running**.

Nothing has been committed yet.

---

# Flush Does NOT Commit the Transaction

This is probably the most important concept in this chapter.

```text
flush()

↓

Execute SQL

↓

Transaction Still Open

↓

commit()

↓

Changes Become Permanent
```

Flush only synchronizes.

Commit permanently saves the transaction.

---

# Flush vs Commit

| Flush | Commit |
|--------|---------|
| Synchronizes Persistence Context with Database | Ends the transaction |
| Executes SQL | Makes changes permanent |
| Can still Rollback | Cannot Rollback after success |
| Transaction remains active | Transaction finishes |
| May execute multiple times | Executes once |

---

# Internal Timeline

```text
Transaction Starts
        │
        ▼
find()
        │
        ▼
Managed Entity Created
        │
        ▼
setSalary(500000)
        │
        ▼
Dirty Checking
        │
        ▼
flush()
        │
        ▼
UPDATE Sent To Database
        │
        ▼
Transaction Still Active
        │
        ▼
commit()
        │
        ▼
Database Permanently Stores Data
```

---

# What If an Exception Occurs After Flush?

Example:

```java
Employee employee = entityManager.find(Employee.class, 1L);

employee.setSalary(500000);

entityManager.flush();

throw new RuntimeException();
```

Hibernate executes:

```sql
UPDATE employee
SET salary = 500000
WHERE id = 1;
```

But immediately after that:

```text
RuntimeException

↓

Transaction Rollback
```

Final database value:

```text
salary = 100000
```

Why?

Because although SQL was executed, the transaction never committed.

The database simply rolls back the transaction.

---

# Automatic Flush (FlushMode.AUTO)

By default, Hibernate uses:

```java
FlushModeType.AUTO
```

This means Hibernate automatically flushes before:

- Transaction Commit
- JPQL Query
- Criteria Query
- Native Query (when required)

This guarantees query consistency.

---

# Why Does Hibernate Flush Before a Query?

Suppose:

```java
Employee employee = entityManager.find(Employee.class, 1L);

employee.setSalary(500000);

entityManager.createQuery(
    "FROM Employee",
    Employee.class
).getResultList();
```

Notice:

We never called:

```java
entityManager.flush();
```

Still, Hibernate performs:

```sql
UPDATE employee
SET salary = 500000
WHERE id = 1;
```

before executing:

```sql
SELECT *
FROM employee;
```

---

# Why?

Suppose Hibernate skipped the flush.

Database:

```text
salary = 100000
```

Persistence Context:

```text
salary = 500000
```

Executing:

```sql
SELECT salary
FROM employee;
```

would return:

```text
100000
```

while the Managed Entity contains:

```text
500000
```

Now the application has two different versions of the same employee.

Hibernate prevents this inconsistency by automatically flushing before executing the query.

---

# AUTO Flush Flow

```text
Pending Changes
        │
        ▼
JPQL Query About To Execute
        │
        ▼
Automatic Flush
        │
        ▼
UPDATE Executes
        │
        ▼
SELECT Executes
        │
        ▼
Query Sees Latest Data
```

---

# FlushMode.COMMIT

Hibernate also provides another flush mode.

```java
entityManager.setFlushMode(FlushModeType.COMMIT);
```

Now Hibernate behaves differently.

It no longer flushes before queries.

Instead, it waits until:

```text
commit()
```

or

```java
entityManager.flush();
```

is explicitly called.

---

# Example

```java
entityManager.setFlushMode(FlushModeType.COMMIT);

Employee employee = entityManager.find(Employee.class, 1L);

employee.setSalary(500000);

entityManager.createQuery(
    "FROM Employee",
    Employee.class
).getResultList();
```

Result:

No automatic UPDATE.

Hibernate directly executes:

```sql
SELECT *
FROM employee;
```

The query reads:

```text
salary = 100000
```

because the database has not yet been synchronized.

Later:

```text
commit()

↓

flush()

↓

UPDATE
```

---

# AUTO vs COMMIT

| FlushMode.AUTO | FlushMode.COMMIT |
|----------------|------------------|
| Flush before queries | No automatic flush before queries |
| Queries always see latest changes | Queries may see stale database values |
| Default mode | Performance optimization |
| Higher consistency | Fewer database synchronizations |

---

# Why Use FlushMode.COMMIT?

Sometimes an application executes many read queries after making changes that do not need to be immediately visible.

Example:

```java
updateEmployee();

query1();

query2();

query3();

commit();
```

With `AUTO`:

```text
Flush

↓

Query

↓

Flush

↓

Query

↓

Flush

↓

Query
```

Potentially multiple synchronizations.

With `COMMIT`:

```text
Query

↓

Query

↓

Query

↓

Commit

↓

Single Flush
```

This reduces unnecessary database work.

---

# Important Q&A

> [!QUESTION]
> **Does flush() commit the transaction?**

No.

Flush only synchronizes the Persistence Context with the database.

The transaction remains active.

---

> [!QUESTION]
> **Can SQL execute before commit()?**

Yes.

Hibernate executes SQL during flush().

The transaction can still be rolled back.

---

> [!QUESTION]
> **If an exception occurs after flush() but before commit(), what remains in the database?**

The original data remains.

The database rolls back all changes because the transaction never committed.

---

> [!QUESTION]
> **Why does Hibernate automatically flush before executing a query?**

To ensure the query sees the latest changes made within the current transaction.

Without automatic flushing, queries could return stale or inconsistent data.

---

> [!QUESTION]
> **When does Hibernate automatically flush?**

With `FlushMode.AUTO`:

- Before Commit
- Before JPQL Queries
- Before Criteria Queries
- Before Native Queries (when required)

---

> [!QUESTION]
> **When is FlushMode.COMMIT useful?**

When immediate synchronization is unnecessary and reducing database flush operations can improve performance.

---

# Interview Corner

> **What is the difference between Flush and Commit?**

A good interview answer:

> Flush synchronizes pending changes from the Persistence Context to the database by executing SQL, but the transaction remains active. Commit permanently saves those changes by completing the transaction. If an exception occurs after flush but before commit, the database rolls back the executed SQL.

---

# Key Takeaways

> [!SUCCESS]
> - Dirty Checking detects **what changed**.
> - Flush decides **when SQL should be executed**.
> - Flush does **not** commit the transaction.
> - SQL may execute before commit().
> - Rollback can still undo SQL executed during flush().
> - `FlushMode.AUTO` automatically flushes before queries.
> - `FlushMode.COMMIT` delays flushing until commit or an explicit flush().
> - Understanding Flush vs Commit is one of the most important Hibernate interview topics.
---

# Dirty Checking Internals

So far we've learned:

- Hibernate creates a Managed Entity.
- Hibernate stores an internal Snapshot.
- During `flush()`, Hibernate compares both.
- If differences are found, Hibernate generates SQL.

Now let's go one level deeper and understand **how Hibernate performs Dirty Checking internally**.

Understanding this section will help you explain **why Dirty Checking is efficient, where its limitations are, and how modern Hibernate improves it.**

---

# Traditional Dirty Checking

Suppose we load an employee.

```java
Employee employee =
        entityManager.find(Employee.class, 1L);
```

Internally Hibernate creates:

```text
Persistence Context
        │
        ▼
Managed Entity
        │
        ▼
EntityEntry
        │
        ▼
loadedState (Snapshot)
```

At this point both contain identical values.

```text
Managed Entity        Snapshot
salary = 100000  ↔  salary = 100000
```

Nothing is dirty.

---

# Multiple Changes Before Flush

Suppose the application performs several modifications.

```java
employee.setSalary(200000);

employee.setSalary(300000);

employee.setSalary(400000);

employee.setSalary(500000);
```

Hibernate still performs:

- No SQL
- No Dirty Checking
- No Snapshot Comparison

The Managed Entity simply keeps changing.

```text
100000
  ↓
200000
  ↓
300000
  ↓
400000
  ↓
500000
```

When `flush()` occurs, Hibernate compares only the **final state** with the Snapshot.

```text
Snapshot
100000

↓

Current
500000
```

Only one UPDATE is generated.

---

# Dirty Checking Compares the Final State

Hibernate never cares about intermediate values.

Example:

```java
employee.setSalary(100000);

employee.setSalary(200000);

employee.setSalary(300000);

employee.setSalary(500000);
```

Hibernate generates:

```sql
UPDATE employee
SET salary = 500000
WHERE id = 1;
```

Not:

```sql
UPDATE salary = 100000;

UPDATE salary = 200000;

UPDATE salary = 300000;

UPDATE salary = 500000;
```

Hibernate always synchronizes the **final state**.

---

# Dirty Checking with Multiple Entities

Suppose we execute:

```java
List<Employee> employees =
        employeeRepository.findAll();
```

Assume the database returns:

- 1000 Employees

Hibernate now manages:

```text
1000 Managed Entities
1000 Snapshots
```

Suppose only one employee changes.

```java
employees.get(500).setSalary(500000);
```

A common assumption is:

> Hibernate already knows only Employee #500 changed.

This is **not true** in traditional Hibernate.

---

# What Happens During Flush?

Traditional Hibernate performs comparison for **every Managed Entity**.

```text
Employee 1      Compare

Employee 2      Compare

Employee 3      Compare

...

Employee 1000   Compare
```

Hibernate doesn't know which entity changed.

It discovers the changes by comparing every Snapshot with every Managed Entity.

---

# Why Can This Become Expensive?

Suppose:

- 1000 Managed Entities
- 20 fields per entity

Traditional Dirty Checking may perform:

```text
1000 Entities
×
20 Fields
──────────
20,000 Field Comparisons
```

Even if only one employee changed.

Eventually Hibernate generates:

```sql
UPDATE employee
SET salary = 500000
WHERE id = 500;
```

Only one UPDATE.

But thousands of comparisons were required.

---

# Why Doesn't Hibernate Know Which Entity Changed?

Consider:

```java
employee.setSalary(500000);
```

To Java, this is simply a normal setter method.

Hibernate is not automatically notified that:

> "Salary has changed."

Instead, Hibernate waits until `flush()`.

Then it performs Snapshot comparison.

This design was simple, reliable and portable across JVM implementations when Hibernate was originally developed.

---

# Evolution of Dirty Checking

As enterprise applications became larger, applications started managing:

- Thousands of entities
- Hundreds of transactions
- Large Persistence Contexts

Comparing every Managed Entity during every flush became increasingly expensive.

Hibernate introduced a significant optimization:

> **Bytecode Enhancement**

---

# What is Bytecode Enhancement?

Bytecode Enhancement modifies entity classes during build time or startup.

Instead of waiting until `flush()` to discover changes, Hibernate can record changes as they happen.

Conceptually:

```text
Setter Called
      ↓
Entity Marked Dirty
      ↓
flush()
      ↓
Process Dirty Entities
```

Instead of:

```text
flush()
      ↓
Compare Every Entity
      ↓
Find Dirty Ones
```

This greatly reduces unnecessary comparisons.

---

# Traditional Dirty Checking vs Bytecode Enhancement

| Traditional Dirty Checking | Bytecode Enhancement |
|----------------------------|----------------------|
| Snapshot comparison during flush | Tracks changes immediately |
| Compares every Managed Entity | Focuses on Dirty entities |
| More CPU work | Better performance |
| Default approach | Optional optimization |

---

# Does Bytecode Enhancement Remove Snapshots?

No.

Snapshots are still required for many Hibernate features.

Bytecode Enhancement simply reduces the amount of work Hibernate performs during Dirty Checking.

Think of it as an optimization rather than a replacement.

---

# Common Misconceptions

> [!WARNING]
> **Dirty Checking happens after every setter call.**

❌ Incorrect.

Dirty Checking happens during `flush()`.

---

> [!WARNING]
> **Hibernate executes UPDATE after every setter.**

❌ Incorrect.

Hibernate generates SQL only when synchronization occurs.

---

> [!WARNING]
> **Hibernate always compares only modified entities.**

❌ Incorrect.

Traditional Hibernate compares all Managed Entities.

Only Bytecode Enhancement allows Hibernate to optimize this process.

---

# Important Q&A

> [!QUESTION]
> **If only one entity changes, does traditional Hibernate compare only that entity?**

No.

Traditional Hibernate compares every Managed Entity with its Snapshot during `flush()`.

---

> [!QUESTION]
> **Why can Dirty Checking become expensive?**

Because every Managed Entity and every mapped field may need to be compared during each flush.

---

> [!QUESTION]
> **Why was Bytecode Enhancement introduced?**

To reduce unnecessary Snapshot comparisons and improve Dirty Checking performance in large Persistence Contexts.

---

> [!QUESTION]
> **Does Hibernate generate multiple UPDATE statements if a setter is called multiple times?**

No.

Hibernate generates SQL for the final state of the entity.

---

> [!QUESTION]
> **Does Bytecode Enhancement eliminate Dirty Checking?**

No.

It optimizes Dirty Checking by helping Hibernate identify modified entities more efficiently.

---

# Interview Corner

> **Why is Bytecode Enhancement considered a performance optimization?**

A strong interview answer:

> Traditional Hibernate discovers changes by comparing every Managed Entity with its Snapshot during `flush()`. In applications with many Managed Entities, this can require thousands of field comparisons. Bytecode Enhancement allows Hibernate to record modifications as they occur, reducing unnecessary comparisons and improving overall performance while still preserving Hibernate's Dirty Checking mechanism.

---

# Key Takeaways

> [!SUCCESS]
> - Hibernate synchronizes only the final state of a Managed Entity.
> - Traditional Dirty Checking compares every Managed Entity during `flush()`.
> - Large Persistence Contexts increase Dirty Checking cost.
> - Bytecode Enhancement improves performance by tracking changes earlier.
> - Bytecode Enhancement is an optimization, not a replacement for Dirty Checking.
> - Understanding Dirty Checking internals helps explain many Hibernate performance characteristics.
---

# Office Scenarios & Debugging

Understanding Dirty Checking and Flush becomes much more valuable when you can recognize them in real-world applications.

The following scenarios are commonly encountered in enterprise projects.

---

# Scenario 1 — Why Did Hibernate Execute UPDATE Without save()?

```java
@Transactional
public void updateEmployee() {

    Employee employee =
            entityManager.find(Employee.class, 1L);

    employee.setSalary(500000);

}
```

Many developers ask:

> "Where is `save()`?"

There isn't one.

Flow:

```text
find()
↓
Managed Entity
↓
Modify Entity
↓
Dirty Checking
↓
Flush
↓
UPDATE
↓
Commit
```

This is the normal Hibernate lifecycle.

---

# Scenario 2 — Multiple Setter Calls

```java
employee.setSalary(200000);

employee.setSalary(300000);

employee.setSalary(400000);

employee.setSalary(500000);
```

Hibernate generates:

```sql
UPDATE employee
SET salary = 500000
WHERE id = 1;
```

Only the final state matters.

---

# Scenario 3 — flush() Does NOT Mean Commit

```java
employee.setSalary(500000);

entityManager.flush();

throw new RuntimeException();
```

Flow:

```text
setSalary()
↓
Dirty Checking
↓
UPDATE
↓
Exception
↓
Rollback
```

Final database value:

```text
salary = 100000
```

Although SQL executed, the transaction never committed.

---

# Scenario 4 — Automatic Flush Before Query

```java
employee.setSalary(500000);

entityManager.createQuery(
    "FROM Employee",
    Employee.class
).getResultList();
```

Flow:

```text
Pending Changes
↓
AUTO Flush
↓
UPDATE
↓
SELECT
```

The query always sees the latest data.

---

# Scenario 5 — FlushMode.COMMIT

```java
entityManager.setFlushMode(
        FlushModeType.COMMIT);

employee.setSalary(500000);

entityManager.createQuery(
        "FROM Employee",
        Employee.class
).getResultList();
```

Flow:

```text
Pending Changes
↓
SELECT
↓
Old Database Value
↓
Commit
↓
Flush
↓
UPDATE
```

The query may return stale data because Hibernate delays synchronization until commit.

---

# Common Mistakes

> [!WARNING]
> **Mistake 1**
>
> Thinking `flush()` commits the transaction.

Reality:

```text
flush()
↓
Execute SQL

commit()
↓
Make SQL Permanent
```

---

> [!WARNING]
> **Mistake 2**
>
> Calling `save()` after modifying a Managed Entity.

Unnecessary.

Dirty Checking already tracks the changes.

---

> [!WARNING]
> **Mistake 3**
>
> Assuming every setter immediately executes SQL.

Incorrect.

Hibernate waits until synchronization.

---

> [!WARNING]
> **Mistake 4**
>
> Assuming SQL execution means data is permanently stored.

Incorrect.

Only `commit()` makes changes permanent.

---

> [!WARNING]
> **Mistake 5**
>
> Assuming Hibernate knows which entity changed.

Traditional Hibernate does not.

It discovers changes by comparing Snapshots during `flush()`.

---

# Best Practices

> [!TIP]
> Modify only **Managed Entities** whenever possible.
>
> Dirty Checking works automatically.

---

> [!TIP]
> Avoid calling `flush()` unless there is a valid business reason.
>
> Unnecessary flushing increases database interaction.

---

> [!TIP]
> Do not confuse:
>
> - Dirty Checking
> - Flush
> - Commit
>
> They are three independent steps.

---

> [!TIP]
> Use `FlushMode.COMMIT` only after understanding its effect on query consistency.

---

> [!TIP]
> Large Persistence Contexts increase Dirty Checking cost.
>
> Keep transactions reasonably small.

---

# Important Q&A

> [!QUESTION]
> **Why doesn't Hibernate require `update()` for Managed Entities?**

Because Dirty Checking automatically detects modifications and generates SQL during `flush()`.

---

> [!QUESTION]
> **What exactly does Dirty Checking compare?**

The current Managed Entity with its original Snapshot (`loadedState`).

---

> [!QUESTION]
> **Why doesn't Hibernate compare entities immediately after every setter?**

To avoid unnecessary work. Hibernate performs comparison only during synchronization.

---

> [!QUESTION]
> **Can SQL execute before commit()?**

Yes.

SQL may execute during `flush()`, but the transaction can still roll back.

---

> [!QUESTION]
> **Why can changes disappear even after an UPDATE statement executed?**

Because the UPDATE occurred inside an uncommitted transaction. A rollback restores the previous database state.

---

> [!QUESTION]
> **Why does Hibernate flush before executing JPQL queries?**

To guarantee that the query sees the latest changes made inside the current transaction.

---

> [!QUESTION]
> **What is the default Flush Mode?**

`FlushModeType.AUTO`

---

> [!QUESTION]
> **When should FlushMode.COMMIT be used?**

When reducing unnecessary flush operations is more important than immediate query consistency.

---

> [!QUESTION]
> **Why was Bytecode Enhancement introduced?**

To optimize Dirty Checking by reducing unnecessary Snapshot comparisons in large Persistence Contexts.

---

# Interview Cheat Sheet

| Question | Short Answer |
|-----------|--------------|
| What is Dirty Checking? | Hibernate automatically detects changes made to Managed Entities. |
| When does Dirty Checking occur? | During `flush()`. |
| Does setter execution trigger SQL? | No. |
| What does Hibernate compare? | Managed Entity vs Snapshot (`loadedState`). |
| Where is Snapshot stored? | Inside `EntityEntry` as an internal `Object[]`. |
| Does `flush()` commit the transaction? | No. |
| Can SQL execute before commit()? | Yes. |
| Can rollback undo SQL executed during flush()? | Yes. |
| Default Flush Mode? | `AUTO`. |
| Why Bytecode Enhancement? | To reduce Dirty Checking overhead. |

---

# Quick Revision

```text
find()
↓
SELECT
↓
Managed Entity
+
Snapshot Created
↓
Business Logic
(setters)
↓
No SQL Yet
↓
flush()
↓
Dirty Checking
(Current vs Snapshot)
↓
Changes Found
↓
Generate UPDATE
↓
Database Executes SQL
↓
Transaction Still Active
↓
commit()
↓
Changes Become Permanent
↓
Persistence Context Destroyed
↓
Entity becomes Detached
```

---

# Chapter Summary

> [!SUCCESS]
> After completing this chapter, you should now be able to:
>
> - Explain how Dirty Checking works internally.
> - Describe the purpose of the Snapshot.
> - Explain the role of `EntityEntry` and `loadedState`.
> - Differentiate Dirty Checking, Flush and Commit.
> - Explain why Hibernate automatically generates UPDATE statements.
> - Describe the behavior of `FlushMode.AUTO` and `FlushMode.COMMIT`.
> - Explain why SQL execution and transaction commit are different.
> - Describe why traditional Dirty Checking compares all Managed Entities.
> - Explain the motivation behind Bytecode Enhancement.
> - Debug real-world Hibernate synchronization issues with confidence.

---

# What's Next?

In the next chapter, we'll move to:

> **Module 06 – First-Level Cache**

We'll build on everything learned so far and understand:

- First-Level Cache internals
- Identity Map
- Why `e1 == e2` is true
- Cache lifecycle
- `find()` vs repeated `find()`
- `clear()`, `detach()` and cache eviction
- Office debugging scenarios
- Performance benefits
- Frequently asked interview questions

This module will complete your understanding of the **Persistence Context**, which is the foundation of Hibernate.