# Java Backend Engineering Handbook
# Glossary

> **Purpose**
>
> This glossary contains definitions of commonly used Java Backend Engineering terms.
>
> It serves as a quick reference during learning, revision, and interview preparation.
>
> **Note:** Definitions are intentionally concise. Each topic is covered in depth in its respective module.

---

# How to Use This Glossary

- Use **Ctrl + F** in VS Code to search any term.
- Read the short definition here.
- Refer to the related module for detailed explanations.

---

# A

## API (Application Programming Interface)

A contract that allows one software system to communicate with another.

**Related Modules**

- REST APIs
- Controller Layer

---

## ApplicationContext

Spring's advanced IoC container responsible for creating, configuring, and managing Spring Beans.

**Related Modules**

- Spring Container
- Bean Lifecycle

---

## Auto Configuration

A Spring Boot feature that automatically configures beans based on dependencies and application properties.

**Related Modules**

- Spring Boot Internals

---

# B

## Bean

An object whose lifecycle is managed by the Spring Container.

---

## BeanFactory

The basic IoC container provided by Spring.

ApplicationContext extends BeanFactory.

---

## BeanPostProcessor

A Spring extension point that allows custom logic before and after bean initialization.

---

## Business Logic

Rules that define how an application behaves.

Usually implemented inside the Service layer.

---

# C

## Cache

Temporary storage used to improve application performance.

---

## Controller

The presentation layer responsible for receiving HTTP requests and returning HTTP responses.

---

## Component Scan

Spring mechanism that automatically discovers beans inside specified packages.

---

## Constructor Injection

A Dependency Injection technique where dependencies are provided through the constructor.

Recommended over field injection.

---

# D

## Dependency Injection (DI)

A design pattern where dependencies are provided by the container instead of being created manually.

---

## Dirty Checking

Hibernate mechanism that automatically detects changes made to managed entities.

---

## Detached Entity

An entity that was previously managed but is no longer associated with the Persistence Context.

---

## DTO (Data Transfer Object)

An object used to transfer data between layers or between the client and server.

---

# E

## Entity

A Java class mapped to a database table using JPA annotations.

---

## EntityManager

The core JPA interface responsible for interacting with the Persistence Context.

---

## Entity Lifecycle

The different states of an entity:

- Transient
- Managed
- Detached
- Removed

---

# F

## First-Level Cache

The cache maintained by the Persistence Context.

Enabled by default.

---

## Flush

The process of synchronizing the Persistence Context with the database.

---

# H

## Hibernate

A JPA implementation that performs Object Relational Mapping (ORM).

---

## HTTP

The protocol used for communication between clients and servers.

---

# I

## IoC (Inversion of Control)

A design principle where object creation and lifecycle are controlled by a framework instead of application code.

---

# J

## JPA (Jakarta Persistence API)

A specification that defines how Java objects should be mapped to relational databases.

---

## JDBC

Java's standard API for communicating directly with relational databases.

---

## JPQL

Object-oriented query language provided by JPA.

Queries entities instead of database tables.

---

## JSON

A lightweight data-interchange format commonly used in REST APIs.

---

# L

## Lazy Loading

A strategy where related data is loaded only when required.

---

## Lombok

A Java library that reduces boilerplate code by generating getters, setters, constructors, and more.

---

# M

## Managed Entity

An entity currently tracked by the Persistence Context.

Hibernate automatically performs Dirty Checking on managed entities.

---

## Mapper

A class responsible for converting DTOs into Entities and vice versa.

---

## Merge

A JPA operation that copies the state of a detached entity into a managed entity.

---

## Microservice

An independently deployable application responsible for a specific business capability.

---

# O

## ORM (Object Relational Mapping)

A technique that maps Java objects to relational database tables.

---

## OpenAPI

A specification used to document REST APIs.

Swagger is a common implementation.

---

# P

## Persistence Context

A collection of managed entities maintained by Hibernate during a transaction.

Acts as the First-Level Cache.

---

## PostgreSQL

An open-source relational database commonly used with Spring Boot applications.

---

## Proxy

A Hibernate-generated object that delays loading of related entities.

Commonly used for Lazy Loading.

---

# R

## Repository

The persistence layer responsible for interacting with the database.

---

## Response DTO

A DTO returned from the backend to the client.

---

## REST

Representational State Transfer.

An architectural style for designing web APIs.

---

# S

## Service

The business layer responsible for implementing application rules.

---

## Spring Bean

A bean created and managed by the Spring Container.

---

## Spring Boot

A framework that simplifies Spring application development through auto-configuration and starter dependencies.

---

## Spring Data JPA

A Spring project that simplifies JPA repository implementation.

---

## Singleton Bean

The default Spring Bean scope.

Only one instance exists per ApplicationContext.

---

# T

## Thread Safety

The property of code behaving correctly when accessed by multiple threads simultaneously.

---

## Transaction

A sequence of database operations executed as a single unit of work.

---

## Transient Entity

A newly created entity not yet managed by Hibernate.

---

# U

## Unit Test

A test that verifies a single unit of code in isolation.

---

# V

## Validation

The process of ensuring input data satisfies defined rules.

---

# W

## WireMock

A tool used to simulate external HTTP services during testing.

---

# Quick Comparison

| Term | Meaning |
|-------|---------|
| Bean | Spring-managed object |
| Entity | Database-mapped object |
| DTO | Data transfer object |
| Repository | Database layer |
| Service | Business logic |
| Controller | HTTP layer |
| Mapper | Object converter |
| Persistence Context | Collection of managed entities |
| Hibernate | ORM implementation |
| JPA | ORM specification |
| JDBC | Database connectivity API |

---

# Frequently Confused Terms

| Term 1 | Term 2 | Difference |
|---------|---------|------------|
| JPA | Hibernate | Specification vs Implementation |
| BeanFactory | ApplicationContext | Basic container vs Advanced container |
| Entity | DTO | Database object vs API object |
| Managed | Detached | Tracked vs Not tracked |
| persist() | merge() | New entity vs Detached entity |
| Flush | Commit | Sync changes vs Finalize transaction |
| Singleton | Prototype | One instance vs New instance |

---

# Interview Tip

When preparing for interviews, revise this glossary first before revisiting detailed modules.

Understanding the terminology clearly makes it much easier to explain concepts confidently during technical discussions.

---

# Future Terms

This glossary will grow as new phases are completed.

Upcoming additions include:

- Optimistic Locking
- Pessimistic Locking
- BeanFactoryPostProcessor
- Conditional Beans
- Virtual Threads
- Feign Client
- WebClient
- Circuit Breaker
- Bulkhead
- Saga Pattern
- Transactional Outbox
- Kafka Streams
- Schema Registry
- Redis Cache
- OAuth2
- OpenID Connect
- Micrometer
- OpenTelemetry
- Testcontainers
- GraalVM
- Spring Batch
- CQRS
- Event Sourcing