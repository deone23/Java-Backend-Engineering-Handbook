# 🧬 Module 1 --- Java Generics

> **Purpose:** Write reusable, type-safe Java code while preserving
> compile-time relationships between data types.\
> **Java baseline:** Java 17  
**Estimated Revision Time:** 35–45 minutes

## 🎯 Learning Objectives

-   Explain why Generics exist and how they prevent runtime type errors.
-   Implement generic classes and methods.
-   Apply bounded type parameters and multiple bounds.
-   Select `?`, `? extends`, and `? super` using PECS.
-   Understand type erasure, its restrictions, and bridge methods.
-   Apply Generics to Collections and Spring Boot API/service designs.

## 1. 🧩 Why Generics?

Before Generics, collections could store unrelated types. Retrieval
required casting, and an incorrect cast failed at runtime.

``` java
List values = new ArrayList();
values.add("Java");
values.add(100);
String language = (String) values.get(1); // ClassCastException
```

With Generics, element types are checked at compile time:

``` java
List<String> languages = new ArrayList<>();
languages.add("Java");
// languages.add(100); // Compilation error
String language = languages.get(0); // No explicit cast
```

**✨ **Benefits:** compile-time type safety, fewer casts, reusable
algorithms, and clearer contracts.

![Compile-time type safety](generics-type-safety.svg)

## 2. 📦 Generic Classes

A generic class declares one or more type parameters.

``` java
public class Box<T> {
    private T value;

    public Box(T value) { this.value = value; }
    public T getValue() { return value; }
    public void setValue(T value) { this.value = value; }
}
```

``` java
Box<String> textBox = new Box<>("Java");
String text = textBox.getValue();

Box<Integer> numberBox = new Box<>(100);
Integer number = numberBox.getValue();
```

`T` is a placeholder for a type supplied when the class is used.

### 🧱 Multiple type parameters

``` java
public class Pair<K, V> {
    private final K key;
    private final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() { return key; }
    public V getValue() { return value; }
}
```

``` java
Pair<Long, String> employee = new Pair<>(101L, "Rahul");
```

📝 **Common conventions:** `T` (type), `E` (element), `K` (key), `V` (value),
`N` (number), `R` (result). These are conventions, not keywords.

## 3. 🛠️ Generic Methods

A generic method declares its own type parameter before its return type.

``` java
public class CollectionUtils {
    public static <T> void print(T value) {
        System.out.println(value);
    }

    public static <T> T first(List<T> values) {
        return values.get(0);
    }
}
```

``` java
CollectionUtils.print("Java");
CollectionUtils.print(42);

List<String> names = List.of("Rahul", "Amit");
String firstName = CollectionUtils.first(names);
```

The compiler infers `T`. In `first(names)`, `T` is `String`, so the
return type is `String`.

### 🔀 Class-level T vs method-level U

``` java
class Calculator<T> {
    private T value;

    public <U> void display(U input) {
        System.out.println(value);
        System.out.println(input);
    }
}
```

``` java
Calculator<Integer> calculator = new Calculator<>();
calculator.display("Spring"); // T = Integer, U = String
```

The type parameters are independent. Use a class-level parameter when it
is part of the object's state/contract; use a method-level parameter
when only that method needs it.

## 4. 🔒 Bounded Type Parameters

A bound restricts the types that can be supplied.

### ⬆️ Upper bound with extends

``` java
public static <T extends Number> double sum(T a, T b) {
    return a.doubleValue() + b.doubleValue();
}
```

`Integer`, `Double`, and other `Number` subtypes are accepted; `String`
is not. The bound guarantees access to `Number` methods.

### ⚖️ Comparable bound

``` java
public static <T extends Comparable<T>> T findMax(T a, T b) {
    return a.compareTo(b) > 0 ? a : b;
}
```

The bound guarantees that `T` can be compared with another `T`.

### 🔗 Multiple bounds

``` java
class Employee {
    void work() { }
}

interface Identifiable {
    long getId();
}

public static <T extends Employee & Identifiable> void process(T employee) {
    employee.work();
    System.out.println(employee.getId());
}
```

Rules: - At most one class bound. - The class bound, if present, comes
first. - Interface bounds follow, separated by `&`.

Example: `<T extends Person & Payable & Taxable>`.

## 5. ❓ Wildcards and Invariance

Java Generics are **invariant**:

``` java
List<Integer> integers = new ArrayList<>();
// List<Number> numbers = integers; // Compilation error
```

Although `Integer extends Number`, `List<Integer>` is not a subtype of
`List<Number>`. If allowed, a `Double` could be added through the
`List<Number>` reference to a list intended to contain only Integers.

### ❔ Unbounded wildcard: `?`

``` java
public static void printAll(List<?> values) {
    for (Object value : values) {
        System.out.println(value);
    }
}
```

`List<?>` means a list of some unknown element type. It accepts
`List<String>`, `List<Employee>`, `List<Integer>`, etc. Read elements as
`Object`. You cannot add an ordinary non-null value because the actual
element type is unknown (`null` is technically allowed).

Use it when the element type is irrelevant.

### 📤 Upper-bounded wildcard: `? extends T`

``` java
public static double total(List<? extends Number> values) {
    double result = 0;
    for (Number value : values) {
        result += value.doubleValue();
    }
    return result;
}
```

Accepts `List<Integer>`, `List<Double>`, and `List<Number>`. - Safe to
read each element as `Number`. - Cannot add an ordinary non-null
`Number` or subtype because the actual list's element type is unknown.

Think **producer**: the collection produces values for the method to
read.

### 📥 Lower-bounded wildcard: `? super T`

``` java
public static void addDefaults(List<? super Integer> destination) {
    destination.add(10);
    destination.add(20);
}
```

Accepts `List<Integer>`, `List<Number>`, and `List<Object>`. - Safe to
add an `Integer`. - When reading, the only universally safe type is
`Object`.

Think **consumer**: the collection consumes values supplied by the
method.

### PECS --- Producer Extends, Consumer Super

``` java
public static <T> void copy(
        List<? super T> destination,
        List<? extends T> source) {
    for (T item : source) {
        destination.add(item);
    }
}
```

``` java
List<Integer> source = List.of(10, 20, 30);
List<Number> destination = new ArrayList<>();
copy(destination, source); // T inferred as Integer
```

-   `source` produces `T`: `? extends T`.
-   `destination` consumes `T`: `? super T`.

![PECS producer and consumer model](generics-pecs.svg)

> **TIP:** PECS is a design guideline. An `extends` collection can be
> read; ordinary non-null values cannot safely be added to it.

### 🧭 Wildcard selection

  Requirement                                             Signature
  ------------------------------------------------------- ---------------------
  Accept any list; element type not needed                `List<?>`
  Read values as `T` or subtype of `T`                    `List<? extends T>`
  Add `T` values to a collection                          `List<? super T>`
  Preserve a type relationship across inputs and output   `<T>`

### 🔍 `<T>` vs `<?>`

``` java
public static <T> T first(List<T> values) {
    return values.get(0);
}

public static Object firstUnknown(List<?> values) {
    return values.get(0);
}
```

For `List<String>`, `first(values)` returns `String`, while
`firstUnknown(values)` returns `Object`. Use `<T>` when the method must
preserve a relationship between input and return type. Use `<?>` when
that relationship does not need to be named.

## 6. ⚙️ Type Erasure

Java implements Generics primarily through compile-time checks and
**type erasure**. The compiler removes/translates generic type
information when producing bytecode.

Source:

``` java
List<String> names = new ArrayList<>();
names.add("Java");
String name = names.get(0);
```

Conceptually, the erased form behaves like:

``` java
List names = new ArrayList();
names.add("Java");
String name = (String) names.get(0);
```

The compiler inserts the required cast at retrieval. This is a
conceptual illustration, not literal generated source.

🧠 **Erasure rules:** - Unbounded `T` erases to `Object`. - A bounded parameter
erases to its leftmost bound. - `<T extends Number>` erases to
`Number`. - Generic arguments such as `String` in `List<String>` are not
generally available as runtime type information.

Java introduced Generics while preserving compatibility with
pre-generics code and the existing JVM type system.

![Type erasure conceptual compilation flow](generics-type-erasure.svg)

## 7. 🚧 Restrictions Caused by Type Erasure

### 🚫 Cannot instantiate a type parameter

``` java
class Box<T> {
    // T value = new T(); // Compilation error
}
```

The runtime does not know which concrete constructor to invoke. Pass a
factory, `Supplier<T>`, or `Class<T>` instead.

``` java
class Factory<T> {
    private final Class<T> type;

    Factory(Class<T> type) { this.type = type; }

    T create() throws ReflectiveOperationException {
        return type.getDeclaredConstructor().newInstance();
    }
}
```

This requires an accessible no-argument constructor; a supplier/factory
may be a better design.

### 🔎 Cannot check a parameterized type with instanceof

``` java
Object value = new ArrayList<String>();
// value instanceof List<String> // Compilation error
boolean isList = value instanceof List<?>; // Valid
```

`List<?>` is reifiable: runtime can check that the object is a List, but
not its generic element type.

### 🧺 Cannot create generic arrays

``` java
// T[] values = new T[10]; // Compilation error
// List<String>[] lists = new List<String>[10]; // Compilation error
```

Arrays enforce component type at runtime, while generic arguments are
erased. Prefer `List<T>`.

### 🧩 Static members cannot use a class's type parameter

``` java
class Box<T> {
    private T value;
    // static T getValue() { return value; } // Compilation error
}
```

A static method can declare its own type parameter:

``` java
class Utility {
    public static <T> T identity(T value) {
        return value;
    }
}
```

### ⚠️ No overloads that differ only by erased generic arguments

``` java
// void process(List<String> values) { }
// void process(List<Integer> values) { } // Same erased signature: process(List)
```

Both erase to `process(List)`, causing a name clash.

## 8. 🌉 Bridge Methods

Type erasure can make a generic override appear to have a different
JVM-level signature. The compiler may generate a **bridge method** to
preserve polymorphism.

``` java
class Parent<T> {
    public void process(T value) {
        System.out.println("Parent: " + value);
    }
}

class Child extends Parent<String> {
    @Override
    public void process(String value) {
        System.out.println("Child: " + value);
    }
}
```

After erasure, the parent method uses `process(Object)` and the child
implementation uses `process(String)`. The compiler generates a bridge
method conceptually similar to:

``` java
// Conceptual compiler-generated method:
public void process(Object value) {
    process((String) value);
}
```

This bridge lets a call through the erased parent signature reach the
child implementation.

Inspect declared methods:

``` java
for (var method : Child.class.getDeclaredMethods()) {
    System.out.println(
        method.getName()
        + " | bridge=" + method.isBridge()
        + " | synthetic=" + method.isSynthetic()
    );
}
```

Bridge methods are compiler-generated; developers normally do not write
them manually.

![Bridge method execution path](generics-bridge-method.svg)

> **IMPORTANT:** `Parent<Integer> ref = new Child()` does not compile
> when `Child extends Parent<String>`. Generic types are invariant.

## 9. 🧰 Generics in Collections and Backend Code

### 🪆 Nested generic types

``` java
Map<String, List<Employee>> employeesByDepartment = new HashMap<>();
```

The key is a department name; each value is a list of employees.

### 🔄 Transform one generic type into another

``` java
public static <T, R> List<R> convert(
        List<T> source,
        Function<T, R> converter) {

    List<R> result = new ArrayList<>();
    for (T item : source) {
        result.add(converter.apply(item));
    }
    return result;
}
```

``` java
List<String> employeeNames =
        convert(employees, Employee::getName);
```

Here `T` is `Employee` and `R` is `String`.

### 🌐 Generic API response

``` java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getters and setters
}
```

Reuse one response structure:

``` java
ApiResponse<Employee> oneEmployee;
ApiResponse<List<Employee>> employeeList;
ApiResponse<Product> oneProduct;
```

### Spring Boot controller --- one entity

``` java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<Employee>> getEmployee(
        @PathVariable Long id) {

    return employeeService.findById(id)
        .map(employee -> ResponseEntity.ok(
            new ApiResponse<>(
                true, "Employee fetched successfully", employee
            )
        ))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiResponse<>(
                false, "Employee not found", null
            )));
}
```

### Spring Boot controller --- collection

``` java
@GetMapping
public ResponseEntity<ApiResponse<List<Employee>>> getAllEmployees() {
    List<Employee> employees = employeeService.findAll();

    ApiResponse<List<Employee>> response = new ApiResponse<>(
        true, "Employees fetched successfully", employees
    );

    return ResponseEntity.ok(response);
}
```

Read nested types from inside out: - `Employee`: one employee. -
`List<Employee>`: a list of employees. - `ApiResponse<List<Employee>>`:
one API response whose `data` is a list. -
`ResponseEntity<ApiResponse<List<Employee>>>`: HTTP response wrapping
that API response.

Do not confuse `ApiResponse<List<Employee>>` with
`List<ApiResponse<Employee>>`; they represent different structures.

### 🗄️ Spring Data JPA

``` java
public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {
}
```

`JpaRepository<T, ID>` uses `T` for the entity (`Employee`) and `ID` for
its identifier (`Long`).

A generic base service can help when entities genuinely share
operations. Avoid generalizing domain services just to remove a few
lines; domain-specific services are often clearer.

## 10. ⚠️ Common Mistakes

  -----------------------------------------------------------------------------
  Mistake                     Why it is wrong         Better approach
  --------------------------- ----------------------- -------------------------
  Assign `List<Integer>` to   Generics are invariant  Use a suitable wildcard
  `List<Number>`                                      

  Use `List<Object>` to       It does not accept      Use `List<?>`
  accept every list           `List<Employee>`        

  Add to                      Actual element subtype  Use
  `List<? extends Number>`    is unknown              `List<? super Integer>`
                                                      to add Integers

  Return `Object` when caller Loses type relationship Use `<T> T`
  needs same input type                               

  Create `new T()`            Runtime type is erased  Pass a factory,
                                                      `Supplier<T>`, or
                                                      `Class<T>`

  Use                         Generic argument is not Use `instanceof List<?>`
  `instanceof List<String>`   reifiable               

  Create `new T[10]`          Generic array creation  Prefer `List<T>`
                              is unsafe               

  Use class-level `T` in a    Static context has no   Declare method-level
  static member               instance type parameter `<T>` if appropriate

  Overload by `List<String>`  Same erased signature   Use distinct method names
  vs `List<Integer>`                                  or another parameter
  -----------------------------------------------------------------------------

## 11. 🧪 Coding and Debugging Lab

### Lab A --- Type-safe generic utility

``` java
public final class ListUtils {
    private ListUtils() { }

    public static <T> T first(List<T> values) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("List must not be empty");
        }
        return values.get(0);
    }

    public static int count(List<?> values) {
        return values.size();
    }
}
```

### Lab B --- PECS copy

``` java
public static <T> void copy(
        List<? super T> destination,
        List<? extends T> source) {
    for (T item : source) {
        destination.add(item);
    }
}
```

Test with `List<Integer>` as source and `List<Number>` as destination.

### Lab C --- Generic API response

Create: - `ApiResponse<T>` - `Employee` - `EmployeeService` returning
`Optional<Employee>` - A controller endpoint returning
`ResponseEntity<ApiResponse<Employee>>` - A collection endpoint
returning `ResponseEntity<ApiResponse<List<Employee>>>`

Test found and not-found cases. The not-found response should have HTTP
404 and `data = null`.

### 🐞 Debugging checklist

-   Does the compiler infer the expected `T`?
-   Are generic arguments invariant?
-   Is a wildcard needed instead of a concrete type?
-   Is the collection a producer, consumer, or both?
-   Does the method need to preserve the input type in its return type?
-   Is the error compile-time or runtime?

## 12. 🎤 Interview Corner

**Q1. Why were Generics introduced?**\
To provide compile-time type safety, reduce casts, and enable reusable
type-safe code.

**Q2. What is type erasure?**\
The compiler removes/translates generic type information when generating
bytecode. Unbounded type parameters erase to `Object`; bounded
parameters erase to their leftmost bound.

**Q3. Difference between `<T>` and `<?>`?**\
`<T>` declares a named type variable that can connect parameters and
return types. `<?>` represents an unknown type when that relationship
does not need to be named.

**Q4. Explain PECS.**\
Producer Extends, Consumer Super. Use `? extends T` to read `T` values
and `? super T` to accept `T` values.

**Q5. Why is `List<Integer>` not assignable to `List<Number>`?**\
Generics are invariant. Allowing it could permit adding a `Double` to a
list intended for Integers.

**Q6. Why can't we instantiate `T`?**\
The concrete type is not available in the required form at runtime after
erasure. Pass a factory, `Supplier<T>`, or `Class<T>` instead.

**Q7. Why can't we use `instanceof List<String>`?**\
The runtime cannot verify the erased generic argument.
`instanceof List<?>` is valid.

**Q8. What is a bridge method?**\
A compiler-generated method that preserves polymorphic overriding when
generic methods are erased.

**Q9. What does `JpaRepository<Employee, Long>` mean?**\
The entity type is `Employee`; its identifier type is `Long`.

**Q10. Difference between `ApiResponse<List<Employee>>` and
`List<ApiResponse<Employee>>`?**\
The former is one response containing a list. The latter is a list of
individually wrapped employee responses.

## 13. 🚀 Best Practices

-   Prefer the diamond operator: `new ArrayList<>()`.
-   Use the narrowest useful type in public APIs.
-   Prefer `List<?>` when the element type is irrelevant.
-   Use PECS for flexible producer/consumer APIs.
-   Keep generic type relationships explicit and meaningful.
-   Prefer collections over generic arrays.
-   Avoid raw types except when interacting with unavoidable legacy
    APIs.
-   Do not over-generalize domain services.
-   Keep API response wrappers consistent and strongly typed.

## 14. 🔗 Module Connections

-   **OOP:** Generic abstractions work with interfaces, inheritance, and
    polymorphism.
-   **Collections:** `List<E>`, `Set<E>`, `Map<K,V>`, and queues rely
    heavily on Generics.
-   **Streams and Lambdas:** `Function<T,R>`, `Predicate<T>`, and
    `Consumer<T>` enable reusable pipelines.
-   **Spring Data JPA:** Repository interfaces use entity and ID type
    parameters.
-   **Spring Boot APIs:** Generic response wrappers preserve endpoint
    data types.

## 15. ⚡ Quick Revision

``` text
Generics
├── Type safety
├── Generic class:       Box<T>
├── Generic method:      <T> T method(...)
├── Bounds:              <T extends Number>
├── Multiple bounds:     <T extends Base & I1 & I2>
├── Wildcards
│   ├── ?               unknown type
│   ├── ? extends T     producer / read
│   └── ? super T       consumer / write
├── PECS                 Producer Extends, Consumer Super
├── Type erasure         T -> Object (or leftmost bound)
├── Restrictions         new T(), generic instanceof, generic arrays
├── Bridge methods       preserve polymorphism after erasure
└── Backend use          ApiResponse<T>, JpaRepository<T, ID>
```

**One-line memory rule:** Use `<T>` to preserve type relationships,
`? extends T` to read from a producer, and `? super T` to write to a
consumer.
