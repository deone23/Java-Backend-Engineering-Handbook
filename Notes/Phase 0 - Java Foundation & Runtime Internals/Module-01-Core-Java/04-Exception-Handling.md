# 04 --- Exception Handling

**Module:** 01 --- Core Java\
**☕ Java baseline:** Java 17\
**📈 Level:** Intermediate → Advanced\
**Estimated Revision Time:** 45--60 minutes

## 🎯 Learning Objectives

-   Explain Java's exception hierarchy and distinguish checked
    exceptions, unchecked exceptions, and errors.
-   Use `try`, `catch`, `finally`, `throw`, and `throws`.
-   Design custom exceptions and understand propagation.
-   Use try-with-resources and inspect suppressed exceptions.
-   Apply exception handling in Java and Spring Boot.
-   Predict tricky output involving `finally`, resource cleanup, and
    exception propagation.

## 🧠 1. Why Exception Handling?

An exception is an event that disrupts normal program execution. Java
represents exceptional conditions as objects. If no matching handler is
found, an uncaught exception terminates the current thread.

``` java
public static void main(String[] args) {
    int result = 10 / 0; // ArithmeticException
    System.out.println("This line is skipped");
}
```

Exception handling helps separate normal logic from failure handling,
preserve useful diagnostics, recover where possible, and release
resources.

## 🌳 2. Exception Hierarchy

``` text
                         Object
                            |
                         Throwable
                        /         \\
                     Error       Exception
                                  |
                           RuntimeException
```

-   **Error:** serious JVM/environment conditions; generally not routine
    application recovery targets. Examples: `OutOfMemoryError`,
    `StackOverflowError`, `NoClassDefFoundError`.
-   **Checked exception:** compiler requires catch or declaration with
    `throws`. Examples: `IOException`, `SQLException`,
    `ClassNotFoundException`.
-   **Unchecked exception:** descendants of `RuntimeException`; no
    compiler catch-or-declare requirement. Examples:
    `NullPointerException`, `ArithmeticException`,
    `IllegalArgumentException`.

  -----------------------------------------------------------------------
  Aspect                  Checked                 Unchecked
  ----------------------- ----------------------- -----------------------
  Hierarchy               `Exception` excluding   `RuntimeException`
                          RuntimeException        descendants
                          descendants             

  Compiler enforcement    Catch or declare        No requirement

  Typical use             External/recoverable    Invalid state,
                          failure contract        arguments, programming
                                                  errors
  -----------------------------------------------------------------------

Checked does not guarantee recovery is possible, and unchecked does not
mean it must never be handled. Choose based on the API contract and
expected caller response.

## 🧩 3. `try`, `catch`, `finally`

``` java
try {
    int result = 10 / 0;
} catch (ArithmeticException e) {
    System.out.println("Cannot divide by zero");
} finally {
    System.out.println("Finally executed");
}
System.out.println("Application continues");
```

Output:

``` text
Cannot divide by zero
Finally executed
Application continues
```

-   `try`: code that may throw.
-   `catch`: handles a matching exception.
-   `finally`: normally executes as control exits the try/catch, whether
    normally or exceptionally.

### 🧱 Multiple catch blocks

Catch specific types before general types. A `catch (Exception e)`
before `catch (IOException e)` makes the latter unreachable.

``` java
try {
    // risky operation
} catch (FileNotFoundException e) {
    // specific handling
} catch (IOException e) {
    // broader I/O handling
}
```

### 🔀 Multi-catch

Use when alternatives need identical handling:

``` java
try {
    // operation
} catch (IOException | SQLException e) {
    log.error("External operation failed", e);
}
```

The alternatives cannot have a parent-child relationship.

### 🪆 Nested try-catch

The nearest matching handler handles the exception. Nested `finally`
blocks execute as their respective scopes exit.

``` java
try {
    try {
        int x = 10 / 0;
    } catch (ArithmeticException e) {
        System.out.println("Inner catch");
    } finally {
        System.out.println("Inner finally");
    }
    System.out.println("Outer try continues");
} catch (Exception e) {
    System.out.println("Outer catch");
} finally {
    System.out.println("Outer finally");
}
```

Output: `Inner catch`, `Inner finally`, `Outer try continues`,
`Outer finally`.

## ⚖️ 4. `throw` vs `throws`

  `throw`                      `throws`
  ---------------------------- --------------------------------------
  Throws an exception object   Declares possible exception types
  Used inside method/block     Used in method/constructor signature
  Followed by an object        Followed by class names

``` java
public void validateAge(int age) {
    if (age < 18) {
        throw new IllegalArgumentException("Age must be at least 18");
    }
}

public void loadConfig() throws IOException {
    // operation may throw IOException
}
```

A method can declare multiple exception types:
`throws IOException, SQLException`.

## 🛠️ 5. Custom Exceptions

Use a custom exception when it communicates meaningful domain or
application failure.

### ⚡ Unchecked

``` java
public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
```

``` java
return employeeRepository.findById(id)
    .orElseThrow(() -> new EmployeeNotFoundException(
        "Employee not found: " + id));
```

### 📌 Checked

``` java
public class EmployeeFileProcessingException extends Exception {
    public EmployeeFileProcessingException(String message) {
        super(message);
    }

    public EmployeeFileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

Choose checked vs unchecked by considering whether callers are expected
to take meaningful recovery action, whether the failure is part of the
method contract, and whether compiler-enforced handling adds value.
Avoid unnecessary custom exception proliferation.

## 🔄 6. Exception Propagation

An unhandled exception moves up the call stack until a matching handler
is found.

``` text
main()
  |
  v
methodA()  -- matching catch
  |
  v
methodB()  -- no handler
  |
  v
methodC()  -- throws ArithmeticException
```

``` java
static void methodC() {
    System.out.println("C started");
    int result = 10 / 0;
    System.out.println("C completed");
}
static void methodB() {
    System.out.println("B started");
    methodC();
    System.out.println("B completed");
}
static void methodA() {
    System.out.println("A started");
    try {
        methodB();
    } catch (ArithmeticException e) {
        System.out.println("Handled in A");
    }
    System.out.println("A completed");
}
```

Output:

``` text
A started
B started
C started
Handled in A
A completed
```

Statements after the throwing operation in B and C are skipped. Once A
handles the exception, execution continues after its catch block.

## 7. `finally` --- Important Rules

### ↩️ Return in finally overrides a pending return

``` java
static int test() {
    try {
        return 10;
    } finally {
        return 20;
    }
}
```

Result: `20`. A return in `finally` overrides a pending return from
`try` or `catch`.

### ⚠️ Return in finally can suppress an exception

``` java
static int test() {
    try {
        int x = 10 / 0;
        return x;
    } finally {
        return 20;
    }
}
```

Result: `20`; the pending `ArithmeticException` is suppressed. **Never
return from `finally` in normal application code.**

### 💥 Exception in finally replaces pending exception

``` java
try {
    throw new ArithmeticException("Original");
} finally {
    throw new IllegalStateException("Finally");
}
```

`IllegalStateException` propagates. Ordinary try-finally does not
automatically attach the original exception as suppressed.

### 🔢 Primitive return vs mutation

``` java
static int test() {
    int value = 10;
    try {
        return value;
    } finally {
        value = 20;
        System.out.println("Finally: " + value);
    }
}
```

Output: `Finally: 20`, then `10`. The primitive return value is
evaluated before `finally`.

### 🧱 Object mutation vs reference reassignment

``` java
static StringBuilder test() {
    StringBuilder sb = new StringBuilder("Java");
    try {
        return sb;
    } finally {
        sb.append(" World");
    }
}
```

Returned content: `Java World`, because the saved reference points to
the same mutated object.

If `finally` instead does `sb = new StringBuilder("Spring Boot");`, the
returned content remains `Java`. Reassigning a local reference does not
replace the reference already evaluated for return.

### 🛑 `System.exit`

`finally` is not an absolute guarantee under JVM/process termination.
`System.exit(0)` initiates JVM shutdown; normal `finally` execution is
not guaranteed.

## 📦 8. Try-with-Resources

Use try-with-resources for `AutoCloseable` resources. Java closes them
automatically when leaving the block.

``` java
try (BufferedReader reader =
         Files.newBufferedReader(Path.of("employees.csv"))) {
    String line;
    while ((line = reader.readLine()) != null) {
        System.out.println(line);
    }
}
```

### 🔁 Multiple resources

Resources initialize in declaration order and close in reverse order
(LIFO).

``` java
try (Resource r1 = new Resource("R1");
     Resource r2 = new Resource("R2");
     Resource r3 = new Resource("R3")) {
    System.out.println("Inside try");
}
```

Order: R1 opened, R2 opened, R3 opened, Inside try, R3 closed, R2
closed, R1 closed.

### 🧵 Suppressed exceptions

If the try body throws and `close()` also throws, the try-body exception
remains primary and close failure is attached as suppressed.

``` java
class MyResource implements AutoCloseable {
    public void close() {
        System.out.println("Resource closed");
        throw new RuntimeException("Close failed");
    }
}

try (MyResource r = new MyResource()) {
    System.out.println("Inside try");
    throw new RuntimeException("Processing failed");
} catch (Exception e) {
    System.out.println("Caught: " + e.getMessage());
    System.out.println("Suppressed: " + e.getSuppressed().length);
    for (Throwable t : e.getSuppressed()) {
        System.out.println(t.getMessage());
    }
}
```

Output:

``` text
Inside try
Resource closed
Caught: Processing failed
Suppressed: 1
Close failed
```

  -----------------------------------------------------------------------
  Situation                           Result
  ----------------------------------- -----------------------------------
  Try succeeds, close succeeds        Normal completion

  Try succeeds, close fails           Close exception propagates

  Try fails, close succeeds           Try exception propagates

  Try fails, close fails              Try exception primary; close
                                      exception suppressed
  -----------------------------------------------------------------------

Java 9+ permits an existing final/effectively-final variable as a
resource: `try (reader) { ... }`.

## ✅ 9. Best Practices

1.  Catch only when you can handle, translate, or add useful context;
    otherwise propagate to an appropriate boundary.

2.  Catch specific exception types. Avoid broad catches without a
    boundary-level reason.

3.  Never swallow exceptions with empty catch blocks.

4.  Preserve causes when wrapping:

    ``` java
    catch (IOException e) {
        throw new EmployeeFileProcessingException("Unable to process file", e);
    }
    ```

5.  Do not use exceptions for normal control flow; consider conditions,
    return types, or `Optional`.

6.  Do not return from `finally`.

7.  Prefer try-with-resources for `AutoCloseable`.

8.  Avoid duplicate logging at every layer. Log where the failure is
    handled/reported and preserve stack traces.

9.  Do not expose stack traces, secrets, or internal implementation
    details to API clients.

10. Use meaningful domain exceptions.

11. A pre-check does not guarantee the later operation will succeed.

12. Keep API error responses consistent.

## 📂 10. Employee CSV Import Scenario

``` java
import java.io.*;
import java.nio.file.*;

public class EmployeeCsvImporter {
    public void importEmployees(String filePath)
            throws EmployeeFileNotFoundException {

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path must not be blank");
        }

        Path path = Path.of(filePath);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new EmployeeFileNotFoundException(
                "Employee file not found: " + filePath);
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Parse, validate and process one record.
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(
                "Unable to read employee file: " + filePath, e);
        }
    }
}
```

``` java
public class EmployeeFileNotFoundException extends Exception {
    public EmployeeFileNotFoundException(String message) {
        super(message);
    }
}
```

### ⏱️ TOCTOU: Time of Check to Time of Use

`Files.exists(path)` is only a point-in-time check. Another process
could delete, rename, or replace the file before it is opened. Keep
handling exceptions from the actual file operation (for example,
`NoSuchFileException` or another `IOException`). A pre-check can improve
a message but cannot guarantee success.

## 🌐 11. Spring Boot Global Exception Handling

Translate application exceptions into HTTP responses at the web boundary
instead of duplicating try-catch blocks in every controller.

### 🧩 Custom exception

``` java
public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
```

### ⚙️ Service

``` java
@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Employee getEmployee(Long id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(
                "Employee not found: " + id));
    }
}
```

### 🎮 Controller

``` java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/{id}")
    public Employee getEmployee(@PathVariable Long id) {
        return employeeService.getEmployee(id);
    }
}
```

### 📨 Error DTO and global handler

``` java
public record ApiErrorResponse(
    int status, String error, String message, String path
) {}
```

``` java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            EmployeeNotFoundException ex, HttpServletRequest request) {

        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiErrorResponse body = new ApiErrorResponse(
            status.value(), status.getReasonPhrase(),
            ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }
}
```

### 🛡️ Validation handler

For `@Valid` request-body validation, a handler can process
`MethodArgumentNotValidException`:

``` java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ApiErrorResponse> handleValidation(
        MethodArgumentNotValidException ex, HttpServletRequest request) {

    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .findFirst().orElse("Request validation failed");

    HttpStatus status = HttpStatus.BAD_REQUEST;
    ApiErrorResponse body = new ApiErrorResponse(
        status.value(), status.getReasonPhrase(), message,
        request.getRequestURI());

    return ResponseEntity.badRequest().body(body);
}
```

### 🔄 Flow

``` text
HTTP Request
    |
EmployeeController
    |
EmployeeService
    |
EmployeeRepository
    |
EmployeeNotFoundException
    |
Spring exception resolution
    |
@RestControllerAdvice / @ExceptionHandler
    |
HTTP 404 + ApiErrorResponse
```

  -----------------------------------------------------------------------
  Layer                               Responsibility
  ----------------------------------- -----------------------------------
  Controller                          HTTP request/response mapping

  Service                             Business rules and domain failures

  Repository                          Persistence operations

  `@RestControllerAdvice`             Translate exceptions into
                                      consistent HTTP responses
  -----------------------------------------------------------------------

Typical mappings: not found → 404; invalid request → 400;
unauthenticated → 401; forbidden → 403; unexpected server failure → 500.

## 🐞 12. Debugging and Production Scenarios

  ------------------------------------------------------------------------
  Symptom                  Possible cause          Investigation
  ------------------------ ----------------------- -----------------------
  `NullPointerException`   Null dereference        Inspect stack trace and
                                                   null-producing path

  `OutOfMemoryError`       Heap pressure/leak      Heap dump, allocation
                                                   profile, GC logs

  `StackOverflowError`     Deep/unbounded          Inspect recursive path
                           recursion               

  File disappears after    TOCTOU race             Handle actual
  check                                            file-operation failure

  Original exception       Replaced exception or   Inspect wrapping and
  missing                  lost cause              `getCause()`

  Cleanup masks business   Manual finally throws   Prefer
  failure                                          try-with-resources

  API always returns 500   Missing/incorrect       Inspect global handler
                           mapping                 

  Duplicate logs           Logging at every        Log at
                           propagation layer       handling/reporting
                                                   boundary
  ------------------------------------------------------------------------

Useful diagnostics: `getMessage()`, `getCause()`, `getStackTrace()`,
`getSuppressed()`. Use correlation IDs in structured logs and never log
credentials, tokens, or sensitive data.

## 🎤 13. Interview Questions and Tricky Output Predictions

### Q1 --- Return in finally

``` java
static int test() {
    try { return 10; }
    finally { return 20; }
}
```

**Result:** `20`. Finally return overrides pending return.

### Q2 --- Finally return suppresses exception

``` java
static int test() {
    try { int x = 10 / 0; return x; }
    finally { return 20; }
}
```

**Result:** `20`. Pending `ArithmeticException` is suppressed.

### Q3 --- Exception thrown from finally

``` java
try { throw new ArithmeticException("Original"); }
finally { throw new IllegalStateException("Finally"); }
```

**Propagates:** `IllegalStateException`. Ordinary finally does not
automatically suppress the original exception.

### Q4 --- Print in finally, return in try

``` java
static int test() {
    try { return 10; }
    finally { System.out.println("Finally executed"); }
}
```

Output: `Finally executed`, then `10`.

### Q5 --- Mutable object in finally

``` java
static StringBuilder test() {
    StringBuilder sb = new StringBuilder("Hello");
    try { return sb; }
    finally { sb.append(" World"); }
}
```

Returned content: `Hello World`.

### Q6 --- Reassigning object reference

``` java
static StringBuilder test() {
    StringBuilder sb = new StringBuilder("Java");
    try { return sb; }
    finally { sb = new StringBuilder("Spring Boot"); }
}
```

Returned content: `Java`.

### Q7 --- Nested execution order

``` java
try {
    try { int x = 10 / 0; }
    catch (ArithmeticException e) { System.out.print("B "); }
    finally { System.out.print("C "); }
    System.out.print("D ");
} catch (Exception e) {
    System.out.print("E ");
} finally {
    System.out.print("G");
}
```

Output: `B C D G`. Inner catch handles the error; both finally blocks
run; outer catch is skipped.

### Q8 --- `System.exit`

``` java
try {
    System.out.println("Inside try");
    System.exit(0);
} finally {
    System.out.println("Finally");
}
```

Usual output: `Inside try`. JVM termination does not guarantee finally
execution.

### Q9 --- Checked exception overriding

Parent declares `throws IOException`; child declares
`throws FileNotFoundException`: compiles because the child exception is
narrower. Child `throws Exception` is a compile-time error because it
broadens the checked contract.

### Q10 --- Constructor throws checked exception

A constructor may declare `throws`. If it throws, construction does not
complete and assignment to the reference does not complete. The caller
must catch or declare the checked exception.

### Q11 --- Try-with-resources suppression

If try throws `Processing failed` and close throws `Close failed`,
`Processing failed` remains primary and `Close failed` is in
`getSuppressed()`.

### Q12 --- Resource close order

Declared R1, R2, R3: initialize R1 → R2 → R3; close R3 → R2 → R1.

### Q13 --- Propagation

If C throws and neither C nor B handles it, it propagates to A. A's
matching catch handles it. Statements after the throw in B and C are
skipped.

### Q14 --- Spring global handler

`@RestControllerAdvice` with a matching `@ExceptionHandler` can
translate `EmployeeNotFoundException` into HTTP 404.

### Q15 --- Broad catch and lost cause

Problem:

``` java
catch (Exception e) {
    throw new RuntimeException("Failed");
}
```

When wrapping is justified, preserve the cause:

``` java
catch (DataAccessException e) {
    throw new EmployeeServiceException("Failed to fetch employee", e);
}
```

Avoid converting meaningful business exceptions into generic ones
unnecessarily.

## 🧪 14. Practice Lab

### Lab 1 --- Employee CSV import

Implement an importer that validates blank paths, uses a custom
missing-file exception, reads with try-with-resources, handles actual
I/O failures after a pre-check, and preserves the cause when wrapping.

### Lab 2 --- Spring Boot API

Implement `EmployeeNotFoundException`, service `orElseThrow`,
`GET /employees/{id}`, `ApiErrorResponse`, a global 404 handler, and a
validation 400 handler.

### Lab 3 --- Output prediction

Predict the output and explain:

``` java
static int test() {
    try { return 10; }
    finally { System.out.println("Cleanup"); }
}
```

Then modify it so finally throws an exception. Explain what the caller
observes.

### Lab 4 --- Cause preservation

Wrap an `IOException` in a custom exception and verify
`wrappedException.getCause()` returns the original exception.

## ⚡ 15. Quick Revision Sheet

  -----------------------------------------------------------------------
  Concept                             Remember
  ----------------------------------- -----------------------------------
  Checked exception                   Must be caught or declared

  Unchecked exception                 RuntimeException descendant; no
                                      compiler catch-or-declare
                                      requirement

  Error                               Serious JVM/environment condition

  `throw`                             Throws an exception object

  `throws`                            Declares possible exceptions

  Catch ordering                      Specific before general

  Propagation                         Moves up call stack until handled

  `finally`                           Normally runs as try/catch scope
                                      exits

  Return in finally                   Overrides pending return; avoid

  Try-with-resources                  Automatically closes AutoCloseable
                                      resources

  Multiple resources                  Initialize forward, close in
                                      reverse

  Suppressed exception                Secondary close failure attached to
                                      primary

  Custom exception                    Meaningful domain/application
                                      semantics

  Cause preservation                  Pass original exception to wrapper
                                      constructor

  Spring handler                      `@RestControllerAdvice` +
                                      `@ExceptionHandler`

  TOCTOU                              Pre-check cannot guarantee later
                                      operation succeeds
  -----------------------------------------------------------------------

## 💡 Interview Tips

-   Explain why `finally` can change a return result and why returning
    from it is dangerous.
-   Distinguish object mutation from local reference reassignment.
-   Explain resource closing order and suppressed exceptions.
-   Know checked-exception restrictions on overriding.
-   Trace propagation from repository/service to a Spring global
    handler.
-   Preserve causes, map appropriate HTTP statuses, and avoid leaking
    internal details.
-   Prefer handling the actual operation failure over relying only on a
    pre-check.

------------------------------------------------------------------------

**Topic status:** Exception Handling --- learning and assessment
completed.\
**Next planned topic:** Java 8+ Features.
