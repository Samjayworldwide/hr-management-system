# **HR Management System (HRMS)**

An enterprise-grade Human Resource Management System built with **Java Spring Boot**, designed to streamline employee management, department operations, leave workflows, payroll processing, and organizational HR processes.
The system emphasizes **scalability**, **security**, **performance**, and **maintainability**, leveraging modern technologies like **RabbitMQ**, **Redis**, **PostgreSQL**, **JWT**, **Swagger/OpenAPI**, and **Docker**.

---

## **Features Overview**

### **Core Modules**

* **Employee Management**
* **Department Management**
* **Leave Request Management**
* **Payroll Processing (Async)**
* **Authentication & Authorization (JWT)**
* **RBAC (Admin, HR, Employee)**
* **Auditing & Logging**
* **API Documentation with Swagger**

### **Tech Stack**

* **Backend:** Spring Boot (Web, JPA, Security, Validation)
* **Database:** MySQL
* **Cache:** Redis
* **Message Broker:** RabbitMQ
* **Security:** Spring Security, JWT
* **Docs:** Springdoc OpenAPI (Swagger UI)
* **Containers:** Docker

---

# **Table of Contents**

1. [Architecture Overview](#architecture-overview)
2. [Features](#features)

   * [Employee Management](#employee-management)
   * [Department Management](#department-management)
   * [Leave Request Management](#leave-request-management)
   * [Payroll Processing](#payroll-processing)
   * [Authentication & RBAC](#authentication--authorization)
   * [Auditing & Logging](#auditing--logging)
3. [Non-Functional Requirements](#non-functional-features)
4. [Tech Stack](#tech-stack)
5. [Setup & Installation](#setup--installation)
6. [API Documentation](#api-documentation)
7. [Project Structure](#project-structure)
8. [Contributing](#contributing)
9. [License](#license)

---

# **Architecture Overview**

The HRMS follows a **layered architecture**:

```
Controller Layer → Service Layer → Repository Layer → Database
                  ↓
               RabbitMQ
                  ↓
               Payroll Worker
```

### **Key Architectural Principles**

* SOLID and Clean Code practices
* Clear separation of concerns
* Stateless authentication with JWT
* Async job processing with RabbitMQ
* Redis caching for high-performance reads
* PostgreSQL indexes for large datasets
* Containerization with Docker

---

# **Features**

## 1. **Employee Management**

* Create employee (Admin-only)
* Read employee records (Admin/HR)
* Employees can view and update their own profile (limited)
* Update sensitive fields (Admin-only)
* Search employees by name, email, department
* Pagination & filtering
* Cascading delete of related data

---

## 2. **Department Management**

* Create, update, delete departments
* Assign employees to departments
* HR/Admin can view department details
* Employees can view their own departments
* Business rule–driven permissions

---

## 3. **Leave Request Management**

* Employees submit leave requests (SICK, VACATION, etc.)
* HR/Admin approve or reject requests
* Leave status tracking (PENDING → APPROVED → REJECTED)
* Leave balance tracking and automatic deduction
* View leave history (per employee, HR overview)

---

## 4. **Payroll Processing**

* Trigger payroll calculation via API
* Asynchronous processing using RabbitMQ
* Compute net pay using:

* base salary
* deductions
* bonuses
  - Update payroll status (PENDING → PROCESSED → PAID)
  - View payroll history & payslips
  - Audit logs for payroll operations

---

## 5. **Authentication & Authorization**

* JWT-based stateless authentication
* Login endpoint returning signed JWT
* Password hashing using BCrypt
* Role-Based Access Control (RBAC):

* **Admin:** Full access
* **HR:** Manage leave & payroll, view employees
* **Employee:** Basic access to own data

---

## 6. **Auditing & Logging**

* Database-level auditing
* Action logs for:

* Employee creation
* Updates to salary, role, department
* Leave approvals
* Payroll triggers/schedulers

---

# **Non-Functional Features**

## 1. **Performance Optimization**

* Redis caching for employee data
* Cache TTL and eviction logic
* Asynchronous operations for long-running tasks
* PostgreSQL indexes on critical fields

## 2. **Security**

* JWT validation middleware
* Input validation with Bean Validation
* Password hashing using BCrypt
* Prevention of SQL injection & XSS

## 3. **Maintainability**

* Layered architecture
* SOLID principles
* Centralized error handling
* Unit tests (JUnit + Mockito)
* Clean, modular packaging

---

# **Tech Stack**

| Layer         | Technology                  |
| ------------- | --------------------------- |
| Backend       | Java 17+, Spring Boot       |
| Security      | Spring Security, JWT        |
| Database      | MySQL                       |
| Caching       | Redis                       |
| Messaging     | RabbitMQ                    |
| Documentation | Swagger (Springdoc OpenAPI) |
| Build Tool    | Maven                       |
| Containers    | Docker & Docker Compose     |

---

# **Setup & Installation**

### **Prerequisites**

* Java 17+
* Maven
* MySQL
* Redis
* RabbitMQ
* Docker (optional but recommended)

### **1. Clone the Repository**

### **2. Configure Environment**

### **3. Build and Run**

### **4. Run with Docker**

---

# **API Documentation**

Once the application is running:

 **Swagger UI:**

http://http://localhost:8080/swagger-doc/swagger-ui/index.html)

---

# **Contributing**

Contributions are welcome!
Please create a pull request or open an issue for discussion.

---

# **License**

This project is licensed under the **MIT License**.

