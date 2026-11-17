# 🧩 Module 01 — Object-Oriented Programming (OOP)

> **Scope:** Core Java OOP concepts, object design, contracts, and practical design principles.  
> **Java baseline:** Java 17  
> **Purpose:** Build maintainable, extensible, testable object-oriented Java applications.

> **Visual legend:** 🎯 objective · 🧠 concept · ⚙️ internal working · 💻 code · 🏢 office scenario · 🐞 debugging · 🧪 lab · 🎤 interview · ⚡ revision  
> **Callouts:** 💡 TIP = useful shortcut · ⚠️ WARNING = common risk · ❗ IMPORTANT = must-know behaviour · 📝 NOTE = clarification

## 🎯 Learning Objectives

- Explain the four pillars of OOP and identify where each is useful.
- Distinguish method overloading, overriding, and static method hiding.
- Design abstractions using interfaces and abstract classes.
- Apply composition, encapsulation, and immutability.
- Implement `equals()` and `hashCode()` correctly.
- Recognize and apply the SOLID principles.
- Translate OOP concepts into practical service-layer designs.

## 1. 🧱 OOP Fundamentals

Object-oriented programming models a system as collaborating objects. An object combines **state** (data) and **behaviour** (operations).

![The four pillars of OOP](../../Images/Module-01-Core-Java/oop-pillars.svg)

```text
Class (blueprint)
   |
   | instantiate
   v
Object
 +----------------------+
 | State: fields        |
 | Behaviour: methods   |
 +----------------------+
```

```java
public class Employee {
    private final long id;
    private String name;

    public Employee(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public void rename(String newName) {
        this.name = newName;
    }

    public String getName() {
        return name;
    }
}
```

- `Employee` is a class.
- `new Employee(...)` creates an object.
- `id` and `name` represent state.
- `rename()` represents behaviour.
- `private` protects internal state from uncontrolled external access.

### Class vs Object

| Class | Object |
|---|---|
| Blueprint/type | Runtime instance |
| Defines fields and methods | Holds actual field values |
| Declared in source code | Created at runtime |
| Example: `Employee` | Example: `new Employee(101, "Asha")` |

## 2. 🏛️ The Four Pillars of OOP

### 🔒 2.1 Encapsulation

**Definition:** Bundle state and behaviour together, and control access to an object's internal state.

```java
public class BankAccount {
    private double balance;

    public BankAccount(double openingBalance) {
        if (openingBalance < 0) {
            throw new IllegalArgumentException("Opening balance cannot be negative");
        }
        this.balance = openingBalance;
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit must be positive");
        }
        balance += amount;
    }

    public double getBalance() {
        return balance;
    }
}
```

**Execution flow**
1. Caller invokes `deposit()`.
2. The object validates the requested operation.
3. The object updates its private state.
4. The caller reads state through a controlled method.

**Why it matters:** Business rules stay close to the data they protect. A caller cannot directly set an invalid balance.

> **IMPORTANT:** Encapsulation is not merely generating getters and setters. Unrestricted setters can expose the same uncontrolled mutation that `public` fields would.

### 🎭 2.2 Abstraction

**Definition:** Expose what an object can do while hiding implementation details.

```java
public interface PaymentMethod {
    void pay(double amount);
}
```

A caller depends on the capability:

```java
public class CheckoutService {
    private final PaymentMethod paymentMethod;

    public CheckoutService(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void checkout(double amount) {
        paymentMethod.pay(amount);
    }
}
```

The checkout flow does not need to know whether payment is processed by a card, UPI, or wallet implementation.

```text
CheckoutService
      |
      v
PaymentMethod (abstraction)
      ^
      |
 +----+----------------+
 |         |           |
Card      UPI         Wallet
```

### 🌳 2.3 Inheritance

**Definition:** Create a new class from an existing class, inheriting accessible state and behaviour.

```java
public class Employee {
    public void work() {
        System.out.println("Working");
    }
}

public class Developer extends Employee {
    public void writeCode() {
        System.out.println("Writing code");
    }
}
```

```java
Developer developer = new Developer();
developer.work();      // inherited
developer.writeCode(); // declared in Developer
```

Use inheritance when the subtype genuinely satisfies the contract of the parent type (**is-a** relationship).

> **WARNING:** Do not use inheritance only to reuse a few methods. It creates coupling between the child and parent and can make future changes risky.

### 🔁 2.4 Polymorphism

**Definition:** A common parent or interface reference can represent objects of different concrete types. The actual object's implementation determines an overridden instance method at runtime.

```java
interface NotificationSender {
    void send(String message);
}

class EmailSender implements NotificationSender {
    public void send(String message) {
        System.out.println("Email: " + message);
    }
}

class SmsSender implements NotificationSender {
    public void send(String message) {
        System.out.println("SMS: " + message);
    }
}
```

```java
NotificationSender sender = new EmailSender();
sender.send("Order confirmed"); // EmailSender implementation

sender = new SmsSender();
sender.send("Order shipped");   // SmsSender implementation
```

![Runtime polymorphism and dynamic method dispatch](../../Images/Module-01-Core-Java/oop-polymorphism.svg)

**Runtime dispatch**

```text
NotificationSender reference
          |
          v
  actual object at runtime
       /       \
 EmailSender  SmsSender
      |           |
   send()       send()
```

**Office scenario:** A notification service supports email and SMS. New channels can be added through the same interface without changing the caller's high-level workflow.

## 3. 🔀 Method Overloading, Overriding, and Hiding

### 3.1 Overloading

Same method name in the same class, but a different parameter list. It is resolved at compile time.

```java
class Calculator {
    int add(int a, int b) {
        return a + b;
    }

    double add(double a, double b) {
        return a + b;
    }

    int add(int a, int b, int c) {
        return a + b + c;
    }
}
```

- Parameter count, types, or order must differ.
- Changing only the return type is not sufficient.
- Compile-time selection uses the declared argument types and overload rules.

### 3.2 Overriding

A subclass provides a compatible implementation of an inherited instance method. Runtime dispatch selects the implementation based on the actual object.

```java
class Animal {
    void sound() {
        System.out.println("Generic sound");
    }
}

class Dog extends Animal {
    @Override
    void sound() {
        System.out.println("Bark");
    }
}
```

```java
Animal animal = new Dog();
animal.sound(); // Bark
```

Rules and notes:
- The method signature must match; the return type must be the same or covariant.
- Access cannot be more restrictive in the overriding method.
- `final` methods cannot be overridden.
- `private` methods are not overridden because they are not inherited.
- Static methods are hidden, not overridden.
- `@Override` lets the compiler verify the intent.

### 3.3 Static Method Hiding

Static methods are associated with the class. The selected method depends on the reference's declared type, not runtime object type.

```java
class Parent {
    static void show() {
        System.out.println("Parent");
    }
}

class Child extends Parent {
    static void show() {
        System.out.println("Child");
    }
}

Parent ref = new Child();
ref.show(); // Parent
```

> **INTERVIEW TIP:** Instance methods use runtime polymorphism. Static methods are resolved using the reference/class type.

| Feature | Overloading | Overriding | Static hiding |
|---|---|---|---|
| Method name | Same | Same | Same |
| Parameters | Different | Same signature | Same signature |
| Inheritance required | No | Yes | Yes |
| Binding | Compile time | Runtime | Compile time |
| Applies to | Methods | Instance methods | Static methods |

## 4. 🔌 Interface vs Abstract Class

### Interface

Use an interface to define a capability or contract that unrelated classes can implement.

```java
public interface ReportExporter {
    byte[] export(Report report);
}
```

Java 17 interfaces can contain:
- Abstract instance methods.
- `default` instance methods.
- `static` methods.
- `private` helper methods (including private static methods).
- Constants, which are implicitly `public static final`.

An interface does not hold per-instance state.

### Abstract class

Use an abstract class when related classes share common state or a common partial implementation.

```java
public abstract class BaseReportExporter {
    private final String format;

    protected BaseReportExporter(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public abstract byte[] export(Report report);
}
```

| Interface | Abstract class |
|---|---|
| Defines a contract/capability | Defines a shared base abstraction |
| A class can implement multiple interfaces | A class can extend only one class |
| No per-instance fields | Can hold instance state |
| No constructors | Can have constructors |
| Supports default and static methods | Supports concrete and abstract methods |

**Decision guide**
- “Can perform this capability” → interface.
- “Is a specialized form of this base type and shares state/implementation” → consider abstract class.
- Prefer interfaces at service boundaries when implementations may vary.

## 5. 🧱 Composition vs Inheritance

**Composition** means an object uses or owns another object to delegate work. It commonly models a **has-a** relationship.

```java
class Engine {
    void start() {
        System.out.println("Engine started");
    }
}

class Car {
    private final Engine engine;

    Car(Engine engine) {
        this.engine = engine;
    }

    void start() {
        engine.start();
    }
}
```

```text
Car ----has-a----> Engine
```

Use composition when behaviour can vary independently or when a class should delegate responsibilities to collaborators.

| Inheritance | Composition |
|---|---|
| Is-a relationship | Has-a / uses-a relationship |
| Stronger compile-time coupling | Collaborators can be replaced |
| Child inherits parent implementation | Object delegates to collaborator |
| Useful for genuine subtype relationships | Often better for flexible behaviour reuse |

> **BEST PRACTICE:** Prefer composition over inheritance when the relationship is not a genuine subtype relationship.

## 6. ⚖️ `equals()` and `hashCode()` Contract

The default `Object.equals()` compares object identity. Override it when domain objects need logical equality.

```java
import java.util.Objects;

public class Employee {
    private final long id;
    private final String name;

    public Employee(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Employee other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

This example defines employees as equal when their IDs match.

### Contract

1. **Reflexive:** `x.equals(x)` is true.
2. **Symmetric:** if `x.equals(y)`, then `y.equals(x)`.
3. **Transitive:** if `x.equals(y)` and `y.equals(z)`, then `x.equals(z)`.
4. **Consistent:** repeated calls return the same result while equality-relevant state is unchanged.
5. **Non-null:** `x.equals(null)` is false.
6. If `x.equals(y)` is true, `x.hashCode() == y.hashCode()` must be true.
7. Equal hash codes do **not** require objects to be equal.

```text
HashSet / HashMap key lookup
          |
          v
      hashCode()
          |
          v
    candidate bucket
          |
          v
       equals()
          |
    +-----+------+
    |            |
  match        no match
    |            |
same key      distinct key
```

**What breaks if only `equals()` is overridden?**
- Two logically equal objects may have different identity-based hash codes.
- They may be placed in different hash buckets.
- A `HashSet` can then contain both objects even though `equals()` says they are equal.
- The class violates the contract.

### Mutable key warning

If fields used by `equals()` and `hashCode()` change after an object is inserted into a `HashSet` or used as a `HashMap` key, subsequent lookup may search a different bucket.

> **IMPORTANT:** Keep equality-defining key state immutable while the object is stored in a hash-based collection.

## 7. 🧊 Immutability and Defensive Copies

An immutable object cannot be observably changed after construction.

Typical design:
- Make the class `final` when appropriate.
- Make fields `private final`.
- Initialize all fields in the constructor.
- Do not provide mutators.
- Defensively copy mutable input and output values.

```java
import java.util.List;

public final class Team {
    private final List<String> members;

    public Team(List<String> members) {
        this.members = List.copyOf(members);
    }

    public List<String> getMembers() {
        return members;
    }
}
```

`List.copyOf()` creates an unmodifiable snapshot and rejects null elements. Changes to the caller's original list do not change this list.

**Shallow vs deep immutability**
- The list structure is unmodifiable.
- If list elements are mutable objects, those objects may still change.
- A record is also only shallowly immutable: its component references are final, but referenced objects may be mutable.

### Records (Java 16+)

Records are concise data-carrier classes.

```java
public record EmployeeDto(long id, String name) {}
```

The compiler provides a canonical constructor, accessors (`id()`, `name()`), `equals()`, `hashCode()`, and `toString()`.

Use records for data transfer/value-style types. Add validation or defensive copying in a compact constructor when needed:

```java
public record TeamDto(List<String> members) {
    public TeamDto {
        members = List.copyOf(members);
    }
}
```

> **NOTE:** Record components are accessed using `members()`, not JavaBean-style `getMembers()`.

## 8. 🧭 SOLID Principles

| Principle | Meaning | Practical design signal |
|---|---|---|
| **S — Single Responsibility** | A class should have one cohesive reason to change | Separate order persistence from email delivery |
| **O — Open/Closed** | Open for extension, closed for modification | Add a new payment implementation behind an interface |
| **L — Liskov Substitution** | A subtype must be usable wherever its base type is expected without breaking correctness | Subclasses honour parent method contracts |
| **I — Interface Segregation** | Clients should not depend on methods they do not use | Split a large interface into focused capability interfaces |
| **D — Dependency Inversion** | High-level policy should depend on abstractions, not concrete low-level details | Inject `EmailSender` interface rather than constructing SMTP client inside service |

### Example: SRP and DIP

Problem: `OrderService` validates an order, saves it, and sends an email directly. Email-provider changes now force changes to order logic.

Improvement:
```java
interface EmailSender {
    void sendOrderConfirmation(Order order);
}

class OrderService {
    private final EmailSender emailSender;

    OrderService(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    void placeOrder(Order order) {
        // validate and persist order
        emailSender.sendOrderConfirmation(order);
    }
}
```

- Separating email delivery into its own component improves SRP.
- Injecting an abstraction supports DIP and makes the service easier to test.
- The example omits persistence details intentionally.

## 9. 🐞 Common Mistakes and Debugging

| Mistake | Why it is a problem | Better approach |
|---|---|---|
| Public mutable fields | Callers can bypass validation | Encapsulate state |
| Getters/setters for every field by default | May expose uncontrolled mutation | Expose domain operations |
| Inheritance only for code reuse | Creates fragile coupling | Prefer composition |
| Override `equals()` but not `hashCode()` | Breaks hash collection behaviour | Override both consistently |
| Mutate a hash key after insertion | Lookup/removal may fail | Use immutable key state |
| Assume records are deeply immutable | Referenced objects may still mutate | Copy mutable components |
| Put unrelated methods in one interface | Clients depend on unused operations | Apply ISP |
| Instantiate dependencies inside services | Harder to replace and test | Constructor-inject abstractions |

### Debugging scenario: duplicate employees in a `HashSet`

**Symptom:** Two employees with the same business ID both appear in a set.

**Checks**
1. Is `equals()` based on the business ID?
2. Is `hashCode()` based on the same equality-relevant state?
3. Is that state stable after insertion?
4. Are both objects actually instances of the same equality domain?

## 10. 🧪 Practical Lab — Payment Strategy

**Goal:** Implement a checkout flow that can use different payment providers without changing the checkout service.

Suggested lab location:
`Labs/Module-01-Core-Java/Lab-01-Payment-Strategy/`

Suggested tasks:
1. Define `PaymentMethod` with `pay(double amount)`.
2. Implement `CardPayment` and `UpiPayment`.
3. Create `CheckoutService` that receives `PaymentMethod` through its constructor.
4. Run the same checkout flow with both implementations.
5. Add validation for non-positive payment amounts.
6. Add a third implementation without modifying `CheckoutService`.
7. Write unit tests using a test double for `PaymentMethod`.

**Expected design**
```text
Client
  |
  v
CheckoutService
  |
  v
PaymentMethod
  ^
  +---- CardPayment
  |
  +---- UpiPayment
```

**Acceptance criteria**
- `CheckoutService` does not instantiate concrete payment classes.
- Each implementation honours the same contract.
- Adding a new payment type does not require editing checkout orchestration.
- Invalid amounts are rejected consistently.

## 11. 🎤 Interview Corner

**Q1. What is runtime polymorphism?**  
When an overridden instance method is selected based on the actual object at runtime, even when the reference type is a parent or interface.

**Q2. Can static methods be overridden?**  
No. A static method in a subclass hides the parent's static method.

**Q3. Can we overload a method by changing only its return type?**  
No. The parameter list must differ.

**Q4. Why must `equals()` and `hashCode()` be overridden together?**  
Hash-based collections use the hash code to find candidate buckets and equality to identify the matching key. Equal objects must have equal hash codes.

**Q5. Why is a mutable object risky as a `HashMap` key?**  
Changing equality/hash fields can change its computed bucket location after insertion, making it difficult to retrieve or remove.

**Q6. Interface or abstract class — how do you choose?**  
Use an interface for a capability/contract and an abstract class for a related family sharing state or partial implementation.

**Q7. Why prefer composition over inheritance?**  
Composition allows behaviour to be delegated and replaced without binding a subtype to a parent implementation hierarchy.

**Q8. Are Java records immutable?**  
Their component fields are final, but immutability is shallow unless mutable components are defensively copied or otherwise protected.

**Q9. Explain SRP using a service example.**  
A service that handles order processing and email delivery has multiple reasons to change. Move email delivery into a separate collaborator.

**Q10. How does constructor injection support DIP?**  
The service receives an abstraction from outside instead of constructing a concrete dependency itself.

## 12. ⚡ Quick Summary

- OOP combines state and behaviour in objects.
- Encapsulation protects state; abstraction hides implementation; inheritance models genuine is-a relationships; polymorphism enables interchangeable implementations.
- Overloading is compile-time selection; overriding is runtime instance-method dispatch; static methods are hidden.
- Interfaces define capabilities; abstract classes can share state and implementation.
- Prefer composition when the relationship is has-a/uses-a.
- `equals()` and `hashCode()` must follow their contract.
- Avoid mutating fields that determine hash identity while objects are hash keys.
- Records are shallowly immutable; defensively copy mutable components.
- SOLID improves cohesion, extensibility, substitutability, focused interfaces, and dependency direction.

## 13. 🔗 Module Connections

- **Collections:** `equals()` and `hashCode()` directly affect `HashSet` and `HashMap`; comparators and natural ordering affect sorted collections.
- **Generics:** Interfaces and classes can define type-safe reusable contracts.
- **Exception Handling:** Domain objects and services can communicate invalid operations through well-defined exceptions.
- **Testing:** Interface-based design enables mocks/fakes and isolated unit tests.
- **Spring:** Constructor injection and interface-driven service design are common dependency-injection patterns.
