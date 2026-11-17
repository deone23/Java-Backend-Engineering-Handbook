# 🗂️ Module 01 — Java Collections Framework

> **Scope:** Collection interfaces, implementations, internal working, ordering, iteration, concurrency, and practical data-processing patterns.  
> **Java baseline:** Java 17  
> **Purpose:** Select the right data structure based on uniqueness, ordering, access patterns, and concurrency requirements.

> **Visual legend:** 🎯 objective · 🧠 concept · ⚙️ internal working · 💻 code · 🏢 office scenario · 🐞 debugging · 🧪 lab · 🎤 interview · ⚡ revision  
> **Callouts:** 💡 TIP = useful shortcut · ⚠️ WARNING = common risk · ❗ IMPORTANT = must-know behaviour · 📝 NOTE = clarification

## 🎯 Learning Objectives

- Understand the Collections Framework hierarchy and the role of `Map`.
- Select suitable `List`, `Set`, `Queue`, and `Map` implementations.
- Explain ArrayList, HashSet, HashMap, TreeMap, PriorityQueue, and ConcurrentHashMap internals.
- Implement correct equality and ordering contracts.
- Use iterators and collection utility methods safely.
- Solve common data-processing problems using collections.

## 1. 🧭 Collections Framework Overview

The Java Collections Framework provides interfaces, implementations, and algorithms for storing and processing groups of objects.

![Simplified Java Collections Framework hierarchy](../../Images/Module-01-Core-Java/collections-hierarchy.svg)

```text
Iterable
   |
Collection
   +---------------- List
   |                   +-- ArrayList
   |                   +-- LinkedList
   |
   +---------------- Set
   |                   +-- HashSet
   |                   +-- LinkedHashSet
   |                   +-- SortedSet / NavigableSet
   |                           +-- TreeSet
   |
   +---------------- Queue
                       +-- PriorityQueue
                       +-- Deque
                              +-- ArrayDeque
                              +-- LinkedList

Map (separate hierarchy; not a Collection)
   +-- HashMap
   +-- LinkedHashMap
   +-- SortedMap / NavigableMap
   |       +-- TreeMap
   +-- ConcurrentMap
           +-- ConcurrentHashMap
```

> **IMPORTANT:** `Map` is part of the Collections Framework, but it does **not** extend `Collection` or `Iterable`.

### Choose by requirement

![Collection selection guide](../../Images/Module-01-Core-Java/collection-selection.svg)

| Requirement | Typical choice |
|---|---|
| Indexed access, duplicates allowed | `ArrayList` |
| Unique values, no order guarantee | `HashSet` |
| Unique values, insertion order | `LinkedHashSet` |
| Unique values, sorted order | `TreeSet` |
| Key-value lookup | `HashMap` |
| Key-value lookup, insertion order | `LinkedHashMap` |
| Sorted key-value pairs | `TreeMap` |
| FIFO queue / deque / stack | `ArrayDeque` |
| Priority-based retrieval | `PriorityQueue` |
| Concurrent key-value access | `ConcurrentHashMap` |

## 2. 📋 List Implementations

### 🧱 2.1 ArrayList

`ArrayList` is a resizable array implementation of `List`.

```text
ArrayList
  size = 4
  capacity = 6
  +----+----+----+----+----+----+
  | 10 | 20 | 30 | 40 |    |    |
  +----+----+----+----+----+----+
    0    1    2    3
```

Important operations:
- `get(index)` — O(1).
- `set(index, value)` — O(1).
- `add(value)` — amortized O(1); occasional resize is O(n).
- `add(index, value)` — O(n) due to shifting.
- `remove(index)` — O(n) due to shifting.
- `contains(value)` — O(n), using equality checks.

Example:
```java
List<Integer> numbers = new ArrayList<>();
numbers.add(10);
numbers.add(20);
numbers.add(30);
numbers.add(1, 15); // [10, 15, 20, 30]
numbers.remove(2);  // removes index 2 -> [10, 15, 30]
```

**Index vs value removal**
```java
List<Integer> values = new ArrayList<>(List.of(10, 20, 30));
values.remove(2);                    // removes element at index 2 (30)
values.remove(Integer.valueOf(20));  // removes the value 20
```

### Capacity and resizing

- `size` = number of elements currently stored.
- `capacity` = backing-array length.
- A no-argument `ArrayList` constructor in Java 17 initially uses a shared empty array; storage is allocated on first insertion.
- The first insertion normally allocates capacity 10.
- Java 17 grows the backing array by approximately 1.5x when needed (implementation detail; e.g. 10 → 15 → 22).
- `new ArrayList<>(1000)` reserves capacity; it does not create 1000 elements.

> **TIP:** If the approximate number of elements is known, pre-sizing can reduce repeated resizing and copying.

### 🔗 2.2 LinkedList

`LinkedList` is a doubly linked list. Each node stores an element and links to previous/next nodes.

```text
null <- [10] <-> [20] <-> [30] -> null
```

- Access by index: O(n).
- Add/remove at ends: O(1).
- Insert/remove at a known node through an iterator: O(1) after reaching that position.
- Finding an arbitrary index/node: O(n).
- Extra memory per element for node links.

### ArrayList vs LinkedList

| Operation | ArrayList | LinkedList |
|---|---:|---:|
| `get(index)` | O(1) | O(n) |
| Append | Amortized O(1) | O(1) |
| Add/remove at head | O(n) | O(1) |
| Insert/remove at known iterator position | Shifting may be O(n) | O(1) |
| Memory overhead | Lower | Higher |

**Default recommendation:** Prefer `ArrayList` for ordinary list use. For queue/deque operations at both ends, consider `ArrayDeque` before choosing `LinkedList`.

## 3. 🎯 Set Implementations

A `Set` stores unique elements according to its equality/ordering rules.

### #️⃣ 3.1 HashSet

`HashSet` is backed internally by a `HashMap`. Set elements are stored as map keys with a shared dummy value.

```text
HashSet.add(employee)
        |
        v
  backing HashMap
  key = employee
  value = PRESENT (dummy)
```

- No guaranteed iteration order.
- Allows one `null` element.
- Average add/remove/contains: O(1), assuming good hash distribution.
- Uniqueness depends on `equals()` and `hashCode()`.

### `equals()` and `hashCode()` contract

1. If `a.equals(b)` is true, then `a.hashCode() == b.hashCode()` must be true.
2. Same hash code does not imply equality.
3. `equals()` should be reflexive, symmetric, transitive, consistent, and false for null.
4. Hash-based collections use hash to locate a candidate bucket and equality to identify a matching key.

```text
add / contains
      |
      v
 hashCode()
      |
      v
 bucket selection
      |
      v
 equals() against candidates
      |
  match? ---- yes ---> existing logical element
      |
      no
      v
 insert new element
```

**If only `equals()` is overridden:** equal objects can have different hash codes and land in different buckets, so a `HashSet` may store both. The contract is broken.

**Mutable-key problem:** If fields used in `equals()`/`hashCode()` change after insertion, lookup may compute a different bucket. Avoid mutating equality-defining state while stored.

### 🔒 3.2 LinkedHashSet

- Maintains insertion order.
- Uses hashing for uniqueness.
- Average add/remove/contains: O(1).
- Useful for de-duplicating while preserving first-seen order.

```java
Set<String> names = new LinkedHashSet<>();
names.add("Asha");
names.add("Ravi");
names.add("Asha");
// Iteration: Asha, Ravi
```

### 🌳 3.3 TreeSet

`TreeSet` stores unique values in sorted order and is backed by a `TreeMap` (red-black tree).

- Add/remove/contains: O(log n).
- Natural ordering or supplied `Comparator`.
- `compare(a, b) == 0` determines that values are equivalent for set uniqueness.
- Prefer comparator ordering consistent with `equals()`.

Navigable operations:
- `lower(x)` — greatest element strictly less than x.
- `floor(x)` — greatest element less than or equal to x.
- `higher(x)` — smallest element strictly greater than x.
- `ceiling(x)` — smallest element greater than or equal to x.

Example:
```java
TreeSet<Integer> values = new TreeSet<>(List.of(10, 20, 30, 40));
values.lower(30);   // 20
values.floor(30);   // 30
values.higher(30);  // 40
values.ceiling(30); // 30
```

## 4. 🗺️ Map Implementations

### ⚙️ 4.1 HashMap Internal Working

A Java 17 `HashMap` uses an array of buckets. A bucket can contain a linked list of nodes or, under suitable conditions, a red-black tree.

![HashMap bucket lookup and collision handling](../../Images/Module-01-Core-Java/hashmap-internals.svg)

```text
HashMap table
 index
  0  -> null
  1  -> [keyA, valueA] -> [keyB, valueB]
  2  -> [keyC, valueC]
  3  -> null
  ...
```

A node conceptually stores:
```text
hash | key | value | next
```

### `put(key, value)` flow

1. Compute the key's `hashCode()`.
2. Apply a hash-spreading operation (`h ^ (h >>> 16)` in Java 17 implementation).
3. Compute bucket index: `(capacity - 1) & hash`.
4. If bucket is empty, insert a node.
5. If occupied, compare hashes and keys (`equals()` for matching candidates).
6. If the key already exists, replace its value; otherwise insert a new node.
7. If size crosses resize threshold, resize the table.

### `get(key)` flow

1. Compute the spread hash.
2. Calculate the bucket index.
3. Search the bucket.
4. Match hash and key equality.
5. Return the value or null if no mapping exists.

### Complexity and collision handling

- Average `get`, `put`, `remove`: O(1) with well-distributed hashes.
- Collisions are valid; different keys can have the same hash code.
- Collision-heavy buckets may degrade; tree bins improve suitable searches to approximately O(log n).
- Treeification depends on thresholds and table capacity, not only bucket length.

### Java 17 default configuration (implementation details)

| Property | Value |
|---|---:|
| Default initial capacity | 16 |
| Default load factor | 0.75 |
| Initial resize threshold | 12 |
| Treeify threshold constant | 8 |
| Untreeify threshold constant | 6 |
| Minimum capacity for treeification | 64 |

The no-argument map allocates its table lazily. With capacity 16 and load factor 0.75, threshold is 12. When adding the 13th distinct mapping, it normally resizes to 32 and the new threshold becomes 24.

> **NOTE:** HashMap thresholds and tree-bin behaviour are implementation details, not general `Map` interface guarantees.

### Null handling

- `HashMap` allows one null key.
- It allows multiple null values.
- `get(key)` returns null both when the key is absent and when it maps to null.
- `containsKey(key)` distinguishes these cases.

```java
Map<String, String> map = new HashMap<>();
map.put("known", null);

map.get("known");            // null
map.get("missing");          // null
map.containsKey("known");    // true
map.containsKey("missing");  // false
```

### 🔒 4.2 LinkedHashMap

- HashMap-like lookup.
- Maintains insertion order by default.
- Can be configured for access order, useful as a building block for LRU-style caches.

### 🌳 4.3 TreeMap

`TreeMap` is a sorted map backed by a red-black tree.

- Keys are sorted by natural order or a comparator.
- `put`, `get`, `remove`: O(log n).
- Supports `firstKey`, `lastKey`, `lowerKey`, `floorKey`, `higherKey`, `ceilingKey`.
- Natural-order keys must be mutually comparable.
- Does not allow null keys under natural ordering; a custom comparator may define null handling.

## 5. ↕️ Comparable vs Comparator

### Comparable — natural ordering

The class implements `Comparable<T>` and defines `compareTo()`.

```java
class Employee implements Comparable<Employee> {
    private final int id;

    Employee(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(Employee other) {
        return Integer.compare(this.id, other.id);
    }
}
```

### Comparator — external/custom ordering

A separate comparator defines an ordering without modifying the class.

```java
Comparator<Employee> bySalaryDescending =
    Comparator.comparingDouble(Employee::getSalary).reversed();
```

Chaining:
```java
Comparator<Employee> employeeOrder =
    Comparator.comparing(Employee::getDepartment)
              .thenComparing(
                  Comparator.comparingDouble(Employee::getSalary).reversed()
              )
              .thenComparing(Employee::getName);
```

| Comparable | Comparator |
|---|---|
| `compareTo()` | `compare(a, b)` |
| Defined in the class | Defined externally |
| Natural/default ordering | Custom/multiple orderings |
| One primary natural order | Many comparator strategies |

> **TIP:** For integer fields such as IDs, prefer `Comparator.comparingInt(Employee::getId)` rather than converting to a double comparator.

## 6. 🚦 Queue, Deque, and PriorityQueue

### 6.1 Queue

A queue commonly follows FIFO (first in, first out).

| Operation | Throws on failure | Special value on failure |
|---|---|---|
| Insert | `add(e)` | `offer(e)` |
| Remove head | `remove()` | `poll()` |
| Examine head | `element()` | `peek()` |

### 6.2 ArrayDeque

`ArrayDeque` is a resizable circular-array implementation of `Deque`.

```text
Front <-> [A] [B] [C] <-> Rear
```

Stack usage:
```java
Deque<Integer> stack = new ArrayDeque<>();
stack.push(10);
stack.push(20);
int top = stack.pop(); // 20
```

Queue usage:
```java
Deque<Integer> queue = new ArrayDeque<>();
queue.offer(10);
queue.offer(20);
int first = queue.poll(); // 10
```

- Supports insertion/removal at both ends.
- Does not allow null elements.
- Usually preferred over legacy `Stack` for stack operations.
- Often preferable to `LinkedList` for ordinary queue/deque use due to array locality and lower per-element overhead.

### 6.3 PriorityQueue

`PriorityQueue` is an array-backed binary heap. It returns the highest-priority element according to its comparator; by default, the smallest element is the head.

```java
PriorityQueue<Integer> minHeap = new PriorityQueue<>();
minHeap.offer(30);
minHeap.offer(10);
minHeap.offer(20);

minHeap.peek(); // 10
minHeap.poll(); // 10
```

Max heap:
```java
PriorityQueue<Integer> maxHeap =
    new PriorityQueue<>(Comparator.reverseOrder());
```

| Operation | Complexity |
|---|---:|
| `offer` / `add` | O(log n) |
| `poll` / `remove()` head | O(log n) |
| `peek` / `element` | O(1) |
| `contains(Object)` | O(n) |
| `remove(Object)` | O(n) |

> **IMPORTANT:** Iterating over a PriorityQueue does not produce sorted order. Repeatedly call `poll()` to retrieve elements in priority order.

## 7. 🔐 ConcurrentHashMap and Synchronized Maps

### ConcurrentHashMap

A concurrent map designed for safe access by multiple threads.

- Does not allow null keys or null values.
- Uses techniques including CAS, volatile reads/writes, and synchronization at bin level when needed.
- Different bins can often be updated concurrently.
- Supports cooperative resizing.
- Not fully lock-free.
- Iterators are weakly consistent: they tolerate concurrent updates and do not throw `ConcurrentModificationException` for those updates.

Atomic per-key operations:
```java
map.putIfAbsent("user-1", user);
map.compute("counter", (key, oldValue) ->
    oldValue == null ? 1 : oldValue + 1
);
```

### `Collections.synchronizedMap()`

```java
Map<String, Integer> map =
    Collections.synchronizedMap(new HashMap<>());
```

- Wraps a map with synchronized access.
- Individual operations are synchronized on the wrapper's mutex.
- Iteration must be manually synchronized on the map.

```java
synchronized (map) {
    for (var entry : map.entrySet()) {
        System.out.println(entry);
    }
}
```

| ConcurrentHashMap | SynchronizedMap |
|---|---|
| Designed for concurrent access | Wrapper around a map |
| More concurrency across bins | Common mutex for operations |
| No null key/value | Depends on backing map for null support |
| Weakly consistent iterators | Iteration requires external synchronization |

**Selection:** Use `ConcurrentHashMap` for shared concurrent key-value access. Use a synchronized wrapper when its simpler locking model is appropriate and iteration is handled correctly.

## 8. 🔄 Iterator and ListIterator

### Iterator

```java
List<Integer> values = new ArrayList<>(List.of(10, 20, 30));
Iterator<Integer> iterator = values.iterator();

while (iterator.hasNext()) {
    Integer value = iterator.next();
    if (value == 20) {
        iterator.remove();
    }
}
```

`Iterator.remove()` is the supported way to remove the current element during iteration.

### ListIterator

Available for lists. Supports:
- Forward and backward traversal (`next`, `previous`).
- `add`, `set`, and `remove`.
- Index inspection (`nextIndex`, `previousIndex`).

### Fail-fast vs weakly consistent

| Fail-fast iterator | Weakly consistent iterator |
|---|---|
| Common in ArrayList/HashMap | Used by ConcurrentHashMap |
| May throw `ConcurrentModificationException` after unsupported structural modification | Tolerates concurrent modification |
| Best-effort bug detection | May reflect some concurrent updates |
| Not a concurrency-control mechanism | Not necessarily a snapshot |

ArrayList uses `modCount` and an iterator's expected modification count to detect many structural modifications. This is best-effort detection, not a guarantee and not thread safety.

> **WARNING:** Do not rely on `ConcurrentModificationException` for correctness. It is a diagnostic aid.

## 9. 🧰 Collections Utility Methods and Unmodifiable Views

The `Collections` utility class provides algorithms such as:
- `sort`, `reverse`, `shuffle`
- `min`, `max`, `frequency`
- `binarySearch` (the list must be sorted according to the same ordering)
- `swap`, `rotate`
- `unmodifiableList`, `synchronizedList`

Example:
```java
List<Integer> values = new ArrayList<>(List.of(10, 20, 30, 40));
Collections.rotate(values, 2);
// [30, 40, 10, 20] — positive distance rotates right
```

### Unmodifiable view vs immutable snapshot

```java
List<String> original = new ArrayList<>(List.of("A", "B"));

List<String> view = Collections.unmodifiableList(original);
List<String> snapshot = List.copyOf(original);

original.add("C");

System.out.println(view);     // [A, B, C]
System.out.println(snapshot); // [A, B]
```

- `Collections.unmodifiableList()` returns an unmodifiable **view** backed by the original list. Changes through another reference are visible.
- `List.copyOf()` returns an unmodifiable snapshot of the elements and rejects nulls.
- Both prevent structural changes through the returned list.
- `List.copyOf()` is not deep immutability if elements themselves are mutable.

## 10. 💻 Practical Coding Patterns

### 10.1 Remove duplicates and preserve insertion order

```java
List<Integer> input = List.of(4, 2, 4, 1, 2);
Set<Integer> unique = new LinkedHashSet<>(input);
List<Integer> result = new ArrayList<>(unique);
// [4, 2, 1]
```

### 10.2 Find duplicate values in detection order

```java
List<Integer> input = List.of(4, 2, 4, 1, 2, 4);
Set<Integer> seen = new HashSet<>();
Set<Integer> duplicates = new LinkedHashSet<>();

for (Integer value : input) {
    if (!seen.add(value)) {
        duplicates.add(value);
    }
}
// [4, 2]
```

`seen.add(value)` returns false when the value is already present.

### 10.3 Frequency count

```java
List<String> words = List.of("java", "sql", "java", "kafka");
Map<String, Integer> frequency = new LinkedHashMap<>();

for (String word : words) {
    frequency.put(word, frequency.getOrDefault(word, 0) + 1);
}
// java=2, sql=1, kafka=1
```

Use a map because the problem asks for value → count, not only uniqueness.

### 10.4 First non-repeating character

```java
String input = "swiss";
Map<Character, Integer> counts = new LinkedHashMap<>();

for (char ch : input.toCharArray()) {
    counts.put(ch, counts.getOrDefault(ch, 0) + 1);
}

Character answer = null;
for (var entry : counts.entrySet()) {
    if (entry.getValue() == 1) {
        answer = entry.getKey();
        break;
    }
}
// 'w'
```

`LinkedHashMap` preserves first-seen character order while storing counts.

### 10.5 Second-highest distinct salary

```java
TreeSet<Integer> salaries =
    new TreeSet<>(List.of(50000, 70000, 70000, 60000));

Integer secondHighest = salaries.size() < 2
    ? null
    : salaries.lower(salaries.last());
// 60000
```

`TreeSet` removes duplicates and sorts values. `lower(last())` gives the next distinct lower value.

### 10.6 Deduplicate employees by ID

Option A: Define `equals()` and `hashCode()` using employee ID, then use a `LinkedHashSet<Employee>`.

Option B: Explicitly map ID to employee:
```java
Map<Long, Employee> employeesById = new LinkedHashMap<>();
for (Employee employee : employees) {
    employeesById.putIfAbsent(employee.getId(), employee);
}
```

Use option B when the requirement is explicitly “one employee per ID” and retaining the first employee for each ID is desired.

## 11. ⏱️ Performance Reference

| Collection | Lookup | Insert | Remove | Ordering |
|---|---:|---:|---:|---|
| ArrayList | O(n) search / O(1) index | Amortized O(1) append | O(n) by index/value | Insertion/index order |
| LinkedList | O(n) search/index | O(1) at ends | O(1) at ends/known node | Insertion order |
| HashSet | Average O(1) | Average O(1) | Average O(1) | None guaranteed |
| LinkedHashSet | Average O(1) | Average O(1) | Average O(1) | Insertion order |
| TreeSet | O(log n) | O(log n) | O(log n) | Sorted |
| HashMap | Average O(1) | Average O(1) | Average O(1) | None guaranteed |
| LinkedHashMap | Average O(1) | Average O(1) | Average O(1) | Insertion/access order |
| TreeMap | O(log n) | O(log n) | O(log n) | Sorted keys |
| PriorityQueue | O(1) head | O(log n) | O(log n) head | Priority at head |
| ArrayDeque | Ends O(1) amortized | Ends O(1) amortized | Ends O(1) amortized | Deque order |

Complexities are typical expected costs; exact behaviour can depend on implementation, input, and comparator/hash quality.

## 12. 🏢 Real-World Office Scenarios

| Scenario | Suitable structure | Reason |
|---|---|---|
| Store API response items in order | ArrayList | Ordered, indexed access, duplicates allowed |
| Remove duplicate transaction IDs | HashSet | Fast uniqueness checks |
| Preserve first-seen customer IDs | LinkedHashSet | Uniqueness + insertion order |
| Count events by event type | HashMap | Key-to-count mapping |
| Keep report rows sorted by date/key | TreeMap | Sorted key traversal |
| Process highest-priority jobs first | PriorityQueue | Priority-based retrieval |
| Maintain shared in-memory counters by key | ConcurrentHashMap | Concurrent per-key operations |
| Maintain a bounded LRU-style cache | LinkedHashMap or dedicated cache | Access-order support / cache-specific features |

## 13. 🐞 Common Mistakes and Debugging

| Mistake | Correction |
|---|---|
| Assuming `Map` extends `Collection` | Map is a separate hierarchy |
| Using `remove(2)` to remove Integer value 2 from `List<Integer>` | Use `remove(Integer.valueOf(2))` |
| Assuming ArrayList capacity equals size | Capacity is backing storage; size is element count |
| Assuming LinkedList indexed access is O(1) | It must traverse nodes |
| Overriding equals without hashCode | Implement both consistently |
| Mutating a HashMap key after insertion | Keep equality/hash state stable |
| Assuming HashMap iteration order | Use LinkedHashMap if insertion order matters |
| Assuming TreeSet uniqueness uses only equals | Comparator/natural comparison result zero determines equivalence |
| Assuming PriorityQueue iterator is sorted | Repeatedly poll to retrieve priority order |
| Modifying a list directly during enhanced for-loop | Use iterator.remove or a suitable bulk operation |
| Assuming fail-fast means thread-safe | Fail-fast is only best-effort detection |
| Iterating synchronizedMap without locking | Synchronize on the wrapper during iteration |
| Using ConcurrentHashMap with null values | Null keys and values are prohibited |
| Treating unmodifiable view as a snapshot | Use `List.copyOf()` for a snapshot |

## 14. 🧪 Practical Lab Set

Suggested location:
`Labs/Module-01-Core-Java/Lab-02-Collections/`

Recommended exercises:
1. **ArrayList operations:** insert at an index, remove by index, remove by value, inspect size.
2. **HashSet equality:** add two distinct Employee objects with the same ID; test behaviour before and after implementing `equals()` and `hashCode()`.
3. **Mutable key experiment:** mutate a key's equality field after insertion and observe lookup behaviour.
4. **HashMap frequency counter:** count event types from a list of events.
5. **TreeSet salary exercise:** find highest and second-highest distinct salaries.
6. **PriorityQueue job scheduler:** process tasks by priority and verify that iteration is not sorted.
7. **Iterator safety:** remove matching items using `Iterator.remove()`.
8. **Concurrent map:** update a shared per-key counter using `compute()` and compare with a non-atomic read-modify-write approach.

Each lab should be a runnable Java 17 project with:
- `README.md` containing objective, run instructions, and expected output.
- Source code under `src/main/java`.
- Tests under `src/test/java` where appropriate.
- A debugging exercise and explanation of the observed behaviour.

## 15. 🎤 Interview Corner

**Q1. Is Map part of the Collections Framework?**  
Yes, but it does not extend `Collection` or `Iterable`.

**Q2. Why is ArrayList usually preferred over LinkedList?**  
ArrayList provides O(1) indexed access, compact storage, and good cache locality. LinkedList is useful for end operations or iterator-position operations, but indexed access is O(n).

**Q3. How does HashSet ensure uniqueness?**  
It uses a backing HashMap. Hash code locates candidate entries; equality determines whether an equal key already exists.

**Q4. What happens if equals is overridden but hashCode is not?**  
The equality/hash contract is violated. Equal objects may be stored in different buckets and both appear in a HashSet.

**Q5. Explain HashMap put operation.**  
Compute/spread hash, select bucket, compare candidate keys, update an existing mapping or insert a node, then resize if the threshold is crossed.

**Q6. What is a collision?**  
Different keys map to the same bucket. HashMap handles collisions within the bucket using nodes and, under suitable conditions, tree bins.

**Q7. Why is the default HashMap load factor 0.75?**  
It balances space usage and collision/resize costs. The threshold is capacity multiplied by load factor.

**Q8. Can HashMap store null?**  
Yes: one null key and multiple null values. `containsKey()` distinguishes absent from present-with-null.

**Q9. HashMap vs ConcurrentHashMap?**  
HashMap is not thread-safe. ConcurrentHashMap supports concurrent operations, disallows null keys/values, and uses finer-grained concurrency techniques.

**Q10. TreeMap vs HashMap?**  
HashMap provides average O(1) lookup without sorted-order guarantees; TreeMap maintains sorted keys with O(log n) operations.

**Q11. Comparable vs Comparator?**  
Comparable defines natural order in the type; Comparator defines an external/custom ordering.

**Q12. Why does PriorityQueue iteration not return sorted elements?**  
The internal heap only guarantees that the head has highest priority. The remaining array is not fully sorted.

**Q13. Fail-fast vs weakly consistent iterator?**  
Fail-fast iterators may throw CME after unsupported structural changes; weakly consistent iterators tolerate concurrent changes and may reflect some updates.

**Q14. How do you find the first non-repeating character?**  
Count characters in a LinkedHashMap, then scan entries in insertion order for the first count of one.

**Q15. How do you find the second-highest distinct salary?**  
Use a TreeSet to remove duplicates and sort; if at least two values exist, call `lower(last())`.

## 16. ⚡ Quick Summary

- Choose collections based on required ordering, uniqueness, access, and concurrency.
- ArrayList is the default general-purpose list.
- HashSet/HashMap rely on the equals/hashCode contract.
- LinkedHashSet/LinkedHashMap preserve insertion order.
- TreeSet/TreeMap maintain sorted order using comparison.
- PriorityQueue provides priority retrieval, not sorted iteration.
- ArrayDeque is a strong default for stack and queue operations.
- ConcurrentHashMap supports concurrent map access; it is not fully lock-free.
- Iterator removal is safer than direct structural mutation during iteration.
- An unmodifiable view is not the same as an independent snapshot.

## 17. 🔗 Module Connections

- **OOP:** `equals()`, `hashCode()`, interfaces, and comparator strategies depend on sound object contracts.
- **Generics:** Collections use generics to enforce compile-time type safety (`List<Employee>`, `Map<Long, Employee>`).
- **Streams:** Collections are common stream sources for filtering, mapping, grouping, and aggregation.
- **Multithreading:** Concurrent collections and synchronization become important when shared mutable state is accessed by multiple threads.
- **JVM/Memory:** Collection choice affects object allocation, reference overhead, locality, and memory footprint.
