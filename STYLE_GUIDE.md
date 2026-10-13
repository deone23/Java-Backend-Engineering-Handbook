# Java Backend Engineering Handbook
## Markdown Style Guide

> **Version:** 1.0
>
> This document defines the standard formatting, writing style, and organization for every Markdown file in this repository.
>
> The objective is to keep the handbook:
>
> - Clean
> - Compact
> - Consistent
> - Easy to revise
> - Easy to search
> - Professional enough for long-term reference

---

# 1. Repository Structure

```
Java-Backend-Engineering-Handbook/
│
├── README.md
├── STYLE_GUIDE.md
│
├── Assets/
│   ├── Images/
│   ├── Diagrams/
│   └── Interview Cheatsheets/
│
├── Phase 00 - Java Backend Foundation/
├── Phase 01 - Hibernate & Spring Data JPA/
├── Phase 02 - Spring Boot Core/
├── Phase 03 - REST APIs & Validation/
├── Phase 04 - Testing/
├── Phase 05 - Spring Security/
├── Phase 06 - Microservices/
├── Phase 07 - Kafka/
├── Phase 08 - Redis & Caching/
├── Phase 09 - Docker & Kubernetes/
├── Phase 10 - Observability/
├── Phase 11 - Enterprise Patterns/
├── Phase 12 - System Design/
│
└── Interview Preparation/
```

---

# 2. File Naming Convention

Every module must follow this format.

```
Module XX - Module Name.md
```

Examples

```
Module 01 - Backend Architecture.md

Module 02 - DTO Entity Mapper.md

Module 03 - Spring Bean Lifecycle.md
```

Avoid names like:

```
notes.md

spring.md

module1.md
```

---

# 3. Heading Hierarchy

Only one H1 heading is allowed.

```markdown
# Module 01 — Backend Architecture
```

Major sections use H2.

```markdown
## 1. Why Does a Backend Exist?
```

Subsections use H3.

```markdown
### Definition
```

For smaller topics, use bold text instead of additional heading levels.

```markdown
**Responsibilities**

- Receive Request
- Call Service
- Return Response
```

Never use:

```markdown
####

#####

######
```

Three heading levels are sufficient.

---

# 4. Module Header

Every module begins with the following format.

```markdown
# Module 01 — Backend Architecture

> **Phase:** 00 - Java Backend Foundation
>
> **Difficulty:** Beginner → Intermediate
>
> **Estimated Revision Time:** 20 Minutes
```

---

# 5. Standard Module Layout

Every module follows this structure.

```
Learning Objectives

Problem Statement

Theory

Internal Working

Architecture Diagram

Execution Flow

Code Examples

Real Project Example

Common Mistakes

Interview Questions

Best Practices

Module Summary

Quick Revision

Related Modules
```

---

# 6. Writing Style

Write like a technical book.

Use short sentences.

Explain one concept at a time.

Example

Good

```
Hibernate manages the entity.

The database is not updated yet.
```

Avoid

```
Hibernate is basically doing lots of internal magic that eventually...
```

Keep explanations direct.

---

# 7. Spacing Rules

Keep vertical spacing compact.

Correct

```markdown
- Spring Boot
- Hibernate
- Kafka
```

Avoid

```markdown
- Spring Boot

- Hibernate

- Kafka
```

Never insert blank lines between every bullet.

---

# 8. Horizontal Rules

Use horizontal rules only between major sections.

```
---
```

Do not separate every subsection with horizontal rules.

---

# 9. Code Blocks

Always specify the language.

Java

````markdown
```java
Employee employee =
        repository.findById(id)
                  .orElseThrow();
```