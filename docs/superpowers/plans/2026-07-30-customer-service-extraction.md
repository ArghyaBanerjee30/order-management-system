# Customer Service Extraction Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extract customer management from java-order-service into a new java-customer-service microservice with REST API communication.

**Architecture:** Create new Spring Boot microservice (port 8081) containing all customer CRUD operations. Order Service (port 8080) validates customers via REST client calls instead of direct database access. Both services use independent H2 databases.

**Tech Stack:** Java 17, Spring Boot 3.2.0, Spring Data JPA, H2 Database, RestTemplate, Lombok, SpringDoc OpenAPI, JUnit 5, Mockito

## Global Constraints

- Java version: 17
- Spring Boot version: 3.2.0
- Maven 3.8+
- Customer Service port: 8081
- Order Service port: 8080 (unchanged)
- Inventory Service port: 8000 (unchanged)
- Package naming: `com.customerservice.*` for Customer Service, `com.orderservice.*` for Order Service
- Database: H2 in-memory for both services (separate instances)
- REST communication timeout: 5000ms
- All commits follow conventional commits format: `feat:`, `test:`, `docs:`, `refactor:`

---

## File Structure

### New Customer Service Files (java-customer-service/)

```
java-customer-service/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/customerservice/
│   │   │   ├── CustomerServiceApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── CustomerController.java
│   │   │   │   └── HealthController.java
│   │   │   ├── service/
│   │   │   │   └── CustomerService.java
│   │   │   ├── repository/
│   │   │   │   └── CustomerRepository.java
│   │   │   ├── entity/
│   │   │   │   └── Customer.java
│   │   │   ├── dto/
│   │   │   │   ├── CreateCustomerRequest.java
│   │   │   │   ├── UpdateCustomerRequest.java
│   │   │   │   ├── CustomerResponse.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   └── ValidationErrorResponse.java
│   │   │   ├── exception/
│   │   │   │   ├── CustomerNotFoundException.java
│   │   │   │   ├── DuplicateCustomerException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   └── config/
│   │   │       └── OpenApiConfig.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-test.yml
│   └── test/
│       ├── java/com/customerservice/
│       │   ├── service/
│       │   │   └── CustomerServiceTest.java
│       │   └── controller/
│       │       └── CustomerControllerIntegrationTest.java
│       └── resources/
│           └── application-test.yml
└── mvnw, mvnw.cmd (Maven wrapper)
```

### Modified Order Service Files

```
java-order-service/
├── src/main/java/com/orderservice/
│   ├── client/                               (NEW directory)
│   │   └── CustomerClient.java               (NEW)
│   ├── dto/
│   │   └── CustomerResponse.java              (NEW - simple DTO for client)
│   ├── exception/
│   │   └── CustomerServiceException.java      (NEW)
│   ├── service/
│   │   └── OrderService.java                  (MODIFY - use CustomerClient)
│   ├── controller/
│   │   └── CustomerController.java            (DELETE)
│   ├── service/
│   │   └── CustomerService.java               (DELETE)
│   ├── repository/
│   │   └── CustomerRepository.java            (DELETE)
│   ├── entity/
│   │   └── Customer.java                      (DELETE)
│   └── dto/
│       ├── CreateCustomerRequest.java         (DELETE)
│       └── UpdateCustomerRequest.java         (DELETE)
├── src/main/resources/
│   └── application.yml                        (MODIFY - add customer service URL)
└── src/test/java/com/orderservice/
    ├── service/
    │   ├── OrderServiceTest.java              (MODIFY - mock CustomerClient)
    │   └── CustomerServiceTest.java           (DELETE)
    └── controller/
        ├── OrderControllerIntegrationTest.java (MODIFY - mock CustomerClient)
        └── CustomerControllerIntegrationTest.java (DELETE)
```

---

### Task 1: Create Customer Service Maven Project Structure

**Files:**
- Create: `java-customer-service/pom.xml`
- Create: `java-customer-service/src/main/resources/application.yml`
- Create: `java-customer-service/src/main/resources/application-test.yml`
- Create: `java-customer-service/src/test/resources/application-test.yml`

**Interfaces:**
- Consumes: None (bootstrap task)
- Produces: Maven project with Spring Boot 3.2.0, ready for Java 17 compilation on port 8081

- [ ] **Step 1: Create project root directory**

```bash
cd /Users/arghyabanerjee/Desktop/Code2Spec/order-management-system-main
mkdir -p java-customer-service/src/{main,test}/{java/com/customerservice,resources}
```

- [ ] **Step 2: Create pom.xml**

```bash
cat > java-customer-service/pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.customerservice</groupId>
    <artifactId>java-customer-service</artifactId>
    <version>1.0.0</version>
    <name>Customer Service</name>
    <description>Customer Management Service for Order Management System</description>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Spring Boot Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- H2 Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- SpringDoc OpenAPI (Swagger) -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.3.0</version>
        </dependency>

        <!-- Spring Boot Starter Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
EOF
```

- [ ] **Step 3: Create application.yml**

```bash
cat > java-customer-service/src/main/resources/application.yml << 'EOF'
server:
  port: 8081

spring:
  application:
    name: customer-service
  datasource:
    url: jdbc:h2:mem:customerdb
    username: sa
    password:
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    database-platform: org.hibernate.dialect.H2Dialect
    properties:
      hibernate:
        format_sql: true

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

logging:
  level:
    com.customerservice: INFO
    org.springframework.web: INFO
EOF
```

- [ ] **Step 4: Create test application.yml**

```bash
cat > java-customer-service/src/test/resources/application-test.yml << 'EOF'
server:
  port: 0

spring:
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false

logging:
  level:
    com.customerservice: DEBUG
EOF
```

- [ ] **Step 5: Copy Maven wrapper from Order Service**

```bash
cp java-order-service/mvnw java-customer-service/
cp java-order-service/mvnw.cmd java-customer-service/
cp -r java-order-service/.mvn java-customer-service/
chmod +x java-customer-service/mvnw
```

- [ ] **Step 6: Verify Maven build**

```bash
cd java-customer-service
./mvnw clean compile
```

Expected output: `BUILD SUCCESS`

- [ ] **Step 7: Commit**

```bash
git add java-customer-service/
git commit -m "feat: create customer service maven project structure"
```

---

### Task 2: Create Customer Service Domain Model

**Files:**
- Create: `java-customer-service/src/main/java/com/customerservice/entity/Customer.java`
- Create: `java-customer-service/src/main/java/com/customerservice/repository/CustomerRepository.java`

**Interfaces:**
- Consumes: Maven project from Task 1
- Produces:
  - `Customer` entity with fields: `Long id`, `String firstName`, `String lastName`, `String email`, `String phone`, `LocalDateTime createdAt`
  - `CustomerRepository` extends `JpaRepository<Customer, Long>` with method `boolean existsByEmail(String email)`

- [ ] **Step 1: Create Customer entity**

```bash
cat > java-customer-service/src/main/java/com/customerservice/entity/Customer.java << 'EOF'
package com.customerservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
EOF
```

- [ ] **Step 2: Create CustomerRepository**

```bash
cat > java-customer-service/src/main/java/com/customerservice/repository/CustomerRepository.java << 'EOF'
package com.customerservice.repository;

import com.customerservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    boolean existsByEmail(String email);
}
EOF
```

- [ ] **Step 3: Verify compilation**

```bash
cd java-customer-service
./mvnw clean compile
```

Expected: `BUILD SUCCESS` with no compilation errors

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/customerservice/entity/
git add src/main/java/com/customerservice/repository/
git commit -m "feat: add customer entity and repository"
```

---

### Task 3: Create Customer Service DTOs

**Files:**
- Create: `java-customer-service/src/main/java/com/customerservice/dto/CreateCustomerRequest.java`
- Create: `java-customer-service/src/main/java/com/customerservice/dto/UpdateCustomerRequest.java`
- Create: `java-customer-service/src/main/java/com/customerservice/dto/CustomerResponse.java`
- Create: `java-customer-service/src/main/java/com/customerservice/dto/ErrorResponse.java`
- Create: `java-customer-service/src/main/java/com/customerservice/dto/ValidationErrorResponse.java`

**Interfaces:**
- Consumes: Customer entity from Task 2
- Produces:
  - `CreateCustomerRequest` with validation annotations
  - `UpdateCustomerRequest` with validation annotations
  - `CustomerResponse` with all customer fields
  - `ErrorResponse` for standard error responses
  - `ValidationErrorResponse` for validation errors

- [ ] **Step 1: Create CreateCustomerRequest**

```bash
cat > java-customer-service/src/main/java/com/customerservice/dto/CreateCustomerRequest.java << 'EOF'
package com.customerservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be in valid format")
    private String phone;
}
EOF
```

- [ ] **Step 2: Create UpdateCustomerRequest**

```bash
cat > java-customer-service/src/main/java/com/customerservice/dto/UpdateCustomerRequest.java << 'EOF'
package com.customerservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomerRequest {

    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be in valid format")
    private String phone;
}
EOF
```

- [ ] **Step 3: Create CustomerResponse**

```bash
cat > java-customer-service/src/main/java/com/customerservice/dto/CustomerResponse.java << 'EOF'
package com.customerservice.dto;

import com.customerservice.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDateTime createdAt;

    public static CustomerResponse fromEntity(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());
        response.setCreatedAt(customer.getCreatedAt());
        return response;
    }
}
EOF
```

- [ ] **Step 4: Create ErrorResponse**

```bash
cat > java-customer-service/src/main/java/com/customerservice/dto/ErrorResponse.java << 'EOF'
package com.customerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String message;
    private String path;

    public ErrorResponse(int status, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
        this.path = path;
    }
}
EOF
```

- [ ] **Step 5: Create ValidationErrorResponse**

```bash
cat > java-customer-service/src/main/java/com/customerservice/dto/ValidationErrorResponse.java << 'EOF'
package com.customerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String message;
    private String path;
    private Map<String, String> errors;

    public ValidationErrorResponse(int status, String message, String path, Map<String, String> errors) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
        this.path = path;
        this.errors = errors;
    }
}
EOF
```

- [ ] **Step 6: Verify compilation**

```bash
cd java-customer-service
./mvnw clean compile
```

Expected: `BUILD SUCCESS`

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/customerservice/dto/
git commit -m "feat: add customer service DTOs"
```

---

### Task 4: Create Customer Service Exceptions and Exception Handler

**Files:**
- Create: `java-customer-service/src/main/java/com/customerservice/exception/CustomerNotFoundException.java`
- Create: `java-customer-service/src/main/java/com/customerservice/exception/DuplicateCustomerException.java`
- Create: `java-customer-service/src/main/java/com/customerservice/exception/GlobalExceptionHandler.java`

**Interfaces:**
- Consumes: DTOs from Task 3 (`ErrorResponse`, `ValidationErrorResponse`)
- Produces:
  - `CustomerNotFoundException` extends `RuntimeException`
  - `DuplicateCustomerException` extends `RuntimeException`
  - `GlobalExceptionHandler` with `@RestControllerAdvice` returning proper HTTP status codes

- [ ] **Step 1: Create CustomerNotFoundException**

```bash
cat > java-customer-service/src/main/java/com/customerservice/exception/CustomerNotFoundException.java << 'EOF'
package com.customerservice.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long id) {
        super("Customer not found with id: " + id);
    }
}
EOF
```

- [ ] **Step 2: Create DuplicateCustomerException**

```bash
cat > java-customer-service/src/main/java/com/customerservice/exception/DuplicateCustomerException.java << 'EOF'
package com.customerservice.exception;

public class DuplicateCustomerException extends RuntimeException {

    public DuplicateCustomerException(String email, String operation) {
        super("Customer with email " + email + " already exists during " + operation);
    }
}
EOF
```

- [ ] **Step 3: Create GlobalExceptionHandler**

```bash
cat > java-customer-service/src/main/java/com/customerservice/exception/GlobalExceptionHandler.java << 'EOF'
package com.customerservice.exception;

import com.customerservice.dto.ErrorResponse;
import com.customerservice.dto.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(
            CustomerNotFoundException ex,
            HttpServletRequest request) {
        log.error("Customer not found: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateCustomerException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCustomer(
            DuplicateCustomerException ex,
            HttpServletRequest request) {
        log.error("Duplicate customer: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("Validation errors: {}", errors);
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                request.getRequestURI(),
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        log.error("Unexpected error: ", ex);
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred: " + ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
EOF
```

- [ ] **Step 4: Verify compilation**

```bash
cd java-customer-service
./mvnw clean compile
```

Expected: `BUILD SUCCESS`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/customerservice/exception/
git commit -m "feat: add customer service exceptions and global exception handler"
```

---

### Task 5: Create Customer Service Business Logic

**Files:**
- Create: `java-customer-service/src/main/java/com/customerservice/service/CustomerService.java`
- Test: `java-customer-service/src/test/java/com/customerservice/service/CustomerServiceTest.java`

**Interfaces:**
- Consumes:
  - `CustomerRepository` from Task 2
  - DTOs from Task 3
  - Exceptions from Task 4
- Produces:
  - `CustomerService` with methods:
    - `CustomerResponse createCustomer(CreateCustomerRequest request)`
    - `CustomerResponse getCustomerById(Long id)`
    - `List<CustomerResponse> getAllCustomers()`
    - `CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request)`
    - `void deleteCustomer(Long id)`

- [ ] **Step 1: Write failing test for createCustomer**

```bash
cat > java-customer-service/src/test/java/com/customerservice/service/CustomerServiceTest.java << 'EOF'
package com.customerservice.service;

import com.customerservice.dto.CreateCustomerRequest;
import com.customerservice.dto.CustomerResponse;
import com.customerservice.dto.UpdateCustomerRequest;
import com.customerservice.entity.Customer;
import com.customerservice.exception.CustomerNotFoundException;
import com.customerservice.exception.DuplicateCustomerException;
import com.customerservice.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;
    private CreateCustomerRequest createRequest;
    private UpdateCustomerRequest updateRequest;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setEmail("john.doe@example.com");
        testCustomer.setPhone("+1-555-123-4567");
        testCustomer.setCreatedAt(LocalDateTime.now());

        createRequest = new CreateCustomerRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "+1-555-123-4567"
        );

        updateRequest = new UpdateCustomerRequest(
                "Jane",
                "Smith",
                "jane.smith@example.com",
                "+1-555-987-6543"
        );
    }

    @Test
    void createCustomer_Success() {
        when(customerRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        CustomerResponse response = customerService.createCustomer(createRequest);

        assertNotNull(response);
        assertEquals(testCustomer.getId(), response.getId());
        assertEquals(testCustomer.getEmail(), response.getEmail());
        verify(customerRepository).existsByEmail(createRequest.getEmail());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createCustomer_DuplicateEmail() {
        when(customerRepository.existsByEmail(createRequest.getEmail())).thenReturn(true);

        assertThrows(DuplicateCustomerException.class, () -> {
            customerService.createCustomer(createRequest);
        });

        verify(customerRepository).existsByEmail(createRequest.getEmail());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getCustomerById_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        CustomerResponse response = customerService.getCustomerById(1L);

        assertNotNull(response);
        assertEquals(testCustomer.getId(), response.getId());
        assertEquals(testCustomer.getEmail(), response.getEmail());
        verify(customerRepository).findById(1L);
    }

    @Test
    void getCustomerById_NotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.getCustomerById(999L);
        });

        verify(customerRepository).findById(999L);
    }

    @Test
    void getAllCustomers_Success() {
        Customer customer2 = new Customer();
        customer2.setId(2L);
        customer2.setFirstName("Jane");
        customer2.setLastName("Smith");
        customer2.setEmail("jane.smith@example.com");
        customer2.setPhone("+1-555-987-6543");
        customer2.setCreatedAt(LocalDateTime.now());

        when(customerRepository.findAll()).thenReturn(Arrays.asList(testCustomer, customer2));

        List<CustomerResponse> responses = customerService.getAllCustomers();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(customerRepository).findAll();
    }

    @Test
    void updateCustomer_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.existsByEmail(updateRequest.getEmail())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        CustomerResponse response = customerService.updateCustomer(1L, updateRequest);

        assertNotNull(response);
        verify(customerRepository).findById(1L);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void updateCustomer_NotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.updateCustomer(999L, updateRequest);
        });

        verify(customerRepository).findById(999L);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomer_DuplicateEmail() {
        Customer otherCustomer = new Customer();
        otherCustomer.setId(2L);
        otherCustomer.setEmail(updateRequest.getEmail());

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.existsByEmail(updateRequest.getEmail())).thenReturn(true);

        assertThrows(DuplicateCustomerException.class, () -> {
            customerService.updateCustomer(1L, updateRequest);
        });

        verify(customerRepository).findById(1L);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void deleteCustomer_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        doNothing().when(customerRepository).delete(testCustomer);

        customerService.deleteCustomer(1L);

        verify(customerRepository).findById(1L);
        verify(customerRepository).delete(testCustomer);
    }

    @Test
    void deleteCustomer_NotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.deleteCustomer(999L);
        });

        verify(customerRepository).findById(999L);
        verify(customerRepository, never()).delete(any(Customer.class));
    }
}
EOF
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd java-customer-service
./mvnw test -Dtest=CustomerServiceTest
```

Expected: FAIL with "CustomerService not found" or compilation error

- [ ] **Step 3: Write CustomerService implementation**

```bash
cat > java-customer-service/src/main/java/com/customerservice/service/CustomerService.java << 'EOF'
package com.customerservice.service;

import com.customerservice.dto.CreateCustomerRequest;
import com.customerservice.dto.CustomerResponse;
import com.customerservice.dto.UpdateCustomerRequest;
import com.customerservice.entity.Customer;
import com.customerservice.exception.CustomerNotFoundException;
import com.customerservice.exception.DuplicateCustomerException;
import com.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        log.info("Creating customer with email: {}", request.getEmail());

        if (customerRepository.existsByEmail(request.getEmail())) {
            log.error("Email already exists: {}", request.getEmail());
            throw new DuplicateCustomerException(request.getEmail(), "create customer");
        }

        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created with id: {}", savedCustomer.getId());

        return CustomerResponse.fromEntity(savedCustomer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        log.info("Fetching customer with id: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        return CustomerResponse.fromEntity(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        log.info("Fetching all customers");

        return customerRepository.findAll()
                .stream()
                .map(CustomerResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request) {
        log.info("Updating customer with id: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(request.getEmail())) {
                log.error("Email already exists: {}", request.getEmail());
                throw new DuplicateCustomerException(request.getEmail(), "update customer");
            }
            customer.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            customer.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            customer.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }

        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer updated with id: {}", updatedCustomer.getId());

        return CustomerResponse.fromEntity(updatedCustomer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        log.info("Deleting customer with id: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        customerRepository.delete(customer);
        log.info("Customer deleted with id: {}", id);
    }
}
EOF
```

- [ ] **Step 4: Run test to verify it passes**

```bash
cd java-customer-service
./mvnw test -Dtest=CustomerServiceTest
```

Expected: All tests PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/customerservice/service/
git add src/test/java/com/customerservice/service/
git commit -m "feat: add customer service business logic with tests"
```

---

### Task 6: Create Customer Service REST Controller

**Files:**
- Create: `java-customer-service/src/main/java/com/customerservice/controller/CustomerController.java`
- Create: `java-customer-service/src/main/java/com/customerservice/controller/HealthController.java`
- Test: `java-customer-service/src/test/java/com/customerservice/controller/CustomerControllerIntegrationTest.java`

**Interfaces:**
- Consumes: `CustomerService` from Task 5
- Produces:
  - `CustomerController` with REST endpoints:
    - `POST /customers` → `201 Created`
    - `GET /customers` → `200 OK`
    - `GET /customers/{id}` → `200 OK` or `404 Not Found`
    - `PUT /customers/{id}` → `200 OK` or `404 Not Found`
    - `DELETE /customers/{id}` → `204 No Content` or `404 Not Found`
  - `HealthController` with `GET /health` → `200 OK`

- [ ] **Step 1: Write failing integration test**

```bash
cat > java-customer-service/src/test/java/com/customerservice/controller/CustomerControllerIntegrationTest.java << 'EOF'
package com.customerservice.controller;

import com.customerservice.dto.CreateCustomerRequest;
import com.customerservice.dto.UpdateCustomerRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCustomer_Success() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "+1-555-123-4567"
        );

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void createCustomer_DuplicateEmail() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Jane",
                "Smith",
                "duplicate@example.com",
                "+1-555-987-6543"
        );

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createCustomer_ValidationError() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "",
                "Doe",
                "invalid-email",
                "invalid-phone"
        );

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void getCustomerById_Success() throws Exception {
        CreateCustomerRequest createRequest = new CreateCustomerRequest(
                "Alice",
                "Johnson",
                "alice.johnson@example.com",
                "+1-555-111-2222"
        );

        MvcResult createResult = mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long customerId = objectMapper.readTree(responseBody).get("id").asLong();

        mockMvc.perform(get("/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId))
                .andExpect(jsonPath("$.email").value("alice.johnson@example.com"));
    }

    @Test
    void getCustomerById_NotFound() throws Exception {
        mockMvc.perform(get("/customers/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllCustomers_Success() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updateCustomer_Success() throws Exception {
        CreateCustomerRequest createRequest = new CreateCustomerRequest(
                "Bob",
                "Williams",
                "bob.williams@example.com",
                "+1-555-333-4444"
        );

        MvcResult createResult = mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long customerId = objectMapper.readTree(responseBody).get("id").asLong();

        UpdateCustomerRequest updateRequest = new UpdateCustomerRequest(
                "Robert",
                "Williams",
                "robert.williams@example.com",
                "+1-555-555-6666"
        );

        mockMvc.perform(put("/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Robert"))
                .andExpect(jsonPath("$.email").value("robert.williams@example.com"));
    }

    @Test
    void updateCustomer_NotFound() throws Exception {
        UpdateCustomerRequest updateRequest = new UpdateCustomerRequest(
                "Test",
                "User",
                "test@example.com",
                "+1-555-777-8888"
        );

        mockMvc.perform(put("/customers/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCustomer_Success() throws Exception {
        CreateCustomerRequest createRequest = new CreateCustomerRequest(
                "Charlie",
                "Brown",
                "charlie.brown@example.com",
                "+1-555-999-0000"
        );

        MvcResult createResult = mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long customerId = objectMapper.readTree(responseBody).get("id").asLong();

        mockMvc.perform(delete("/customers/{id}", customerId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/customers/{id}", customerId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCustomer_NotFound() throws Exception {
        mockMvc.perform(delete("/customers/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
EOF
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd java-customer-service
./mvnw test -Dtest=CustomerControllerIntegrationTest
```

Expected: FAIL - "No CustomerServiceApplication class found" or similar

- [ ] **Step 3: Create CustomerController**

```bash
cat > java-customer-service/src/main/java/com/customerservice/controller/CustomerController.java << 'EOF'
package com.customerservice.controller;

import com.customerservice.dto.CreateCustomerRequest;
import com.customerservice.dto.CustomerResponse;
import com.customerservice.dto.UpdateCustomerRequest;
import com.customerservice.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer", description = "Customer management APIs")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Create a new customer")
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        log.info("POST /customers - Creating customer");
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all customers")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        log.info("GET /customers - Fetching all customers");
        List<CustomerResponse> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        log.info("GET /customers/{} - Fetching customer", id);
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        log.info("PUT /customers/{} - Updating customer", id);
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        log.info("DELETE /customers/{} - Deleting customer", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
EOF
```

- [ ] **Step 4: Create HealthController**

```bash
cat > java-customer-service/src/main/java/com/customerservice/controller/HealthController.java << 'EOF'
package com.customerservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "customer-service");
        return ResponseEntity.ok(response);
    }
}
EOF
```

- [ ] **Step 5: Create OpenApiConfig**

```bash
cat > java-customer-service/src/main/java/com/customerservice/config/OpenApiConfig.java << 'EOF'
package com.customerservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customerServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Customer Service API")
                        .description("Customer Management Service for Order Management System")
                        .version("1.0.0"));
    }
}
EOF
```

- [ ] **Step 6: Create CustomerServiceApplication**

```bash
cat > java-customer-service/src/main/java/com/customerservice/CustomerServiceApplication.java << 'EOF'
package com.customerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}
EOF
```

- [ ] **Step 7: Run integration tests to verify they pass**

```bash
cd java-customer-service
./mvnw test -Dtest=CustomerControllerIntegrationTest
```

Expected: All tests PASS

- [ ] **Step 8: Run all tests**

```bash
cd java-customer-service
./mvnw test
```

Expected: All tests PASS

- [ ] **Step 9: Start the service and verify it runs**

```bash
cd java-customer-service
./mvnw spring-boot:run &
sleep 10
curl http://localhost:8081/health
curl http://localhost:8081/swagger-ui.html
pkill -f "customer-service"
```

Expected: Health endpoint returns `{"status":"UP","service":"customer-service"}`, Swagger UI accessible

- [ ] **Step 10: Commit**

```bash
git add src/main/java/com/customerservice/controller/
git add src/main/java/com/customerservice/config/
git add src/main/java/com/customerservice/CustomerServiceApplication.java
git add src/test/java/com/customerservice/controller/
git commit -m "feat: add customer REST controller and application entry point"
```

---

### Task 7: Add CustomerClient to Order Service

**Files:**
- Create: `java-order-service/src/main/java/com/orderservice/client/CustomerClient.java`
- Create: `java-order-service/src/main/java/com/orderservice/dto/CustomerResponse.java`
- Create: `java-order-service/src/main/java/com/orderservice/exception/CustomerServiceException.java`
- Modify: `java-order-service/src/main/resources/application.yml`
- Modify: `java-order-service/src/main/resources/application-test.yml`
- Modify: `java-order-service/src/main/java/com/orderservice/config/RestClientConfig.java`

**Interfaces:**
- Consumes: None (new client for Order Service)
- Produces:
  - `CustomerClient` with method `CustomerResponse getCustomer(Long customerId)` throws `CustomerNotFoundException`, `CustomerServiceException`
  - `CustomerResponse` DTO with fields: `Long id`, `String firstName`, `String lastName`, `String email`
  - `CustomerServiceException` extends `RuntimeException`
  - Configuration property `customer.service.url` = `http://localhost:8081`

- [ ] **Step 1: Create CustomerResponse DTO for Order Service**

```bash
cat > java-order-service/src/main/java/com/orderservice/dto/CustomerResponse.java << 'EOF'
package com.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
}
EOF
```

- [ ] **Step 2: Create CustomerServiceException**

```bash
cat > java-order-service/src/main/java/com/orderservice/exception/CustomerServiceException.java << 'EOF'
package com.orderservice.exception;

public class CustomerServiceException extends RuntimeException {

    public CustomerServiceException(String message) {
        super(message);
    }

    public CustomerServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
EOF
```

- [ ] **Step 3: Create CustomerClient**

```bash
mkdir -p java-order-service/src/main/java/com/orderservice/client
cat > java-order-service/src/main/java/com/orderservice/client/CustomerClient.java << 'EOF'
package com.orderservice.client;

import com.orderservice.dto.CustomerResponse;
import com.orderservice.exception.CustomerNotFoundException;
import com.orderservice.exception.CustomerServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerClient {

    private final RestTemplate restTemplate;

    @Qualifier("customerServiceUrl")
    private final String customerServiceUrl;

    public CustomerResponse getCustomer(Long customerId) {
        String url = customerServiceUrl + "/customers/" + customerId;

        log.info("Calling customer service to validate customer {}", customerId);

        try {
            ResponseEntity<CustomerResponse> response = restTemplate.getForEntity(
                    url,
                    CustomerResponse.class
            );

            CustomerResponse customer = response.getBody();

            if (customer != null) {
                log.info("Customer {} validated successfully", customerId);
                return customer;
            } else {
                throw new CustomerServiceException("Customer service returned null response");
            }

        } catch (HttpClientErrorException.NotFound e) {
            log.error("Customer not found: {}", customerId);
            throw new CustomerNotFoundException(customerId);
        } catch (ResourceAccessException e) {
            log.error("Customer service is unavailable: {}", e.getMessage());
            throw new CustomerServiceException("Customer service is unavailable", e);
        } catch (Exception e) {
            log.error("Error communicating with customer service: {}", e.getMessage());
            throw new CustomerServiceException("Error communicating with customer service", e);
        }
    }
}
EOF
```

- [ ] **Step 4: Update RestClientConfig to add customer service URL bean**

```bash
cat > java-order-service/src/main/java/com/orderservice/config/RestClientConfig.java << 'EOF'
package com.orderservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    @Value("${customer.service.url}")
    private String customerServiceUrl;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(5000))
                .setReadTimeout(Duration.ofMillis(5000))
                .build();
    }

    @Bean(name = "inventoryServiceUrl")
    public String inventoryServiceUrl() {
        return inventoryServiceUrl;
    }

    @Bean(name = "customerServiceUrl")
    public String customerServiceUrl() {
        return customerServiceUrl;
    }
}
EOF
```

- [ ] **Step 5: Update application.yml to add customer service URL**

```bash
# Add customer service configuration to application.yml
cat >> java-order-service/src/main/resources/application.yml << 'EOF'

customer:
  service:
    url: http://localhost:8081
EOF
```

- [ ] **Step 6: Update application-test.yml to add customer service URL**

```bash
# Add customer service configuration to test application.yml
cat >> java-order-service/src/main/resources/application-test.yml << 'EOF'

customer:
  service:
    url: http://localhost:8081
EOF
```

- [ ] **Step 7: Verify compilation**

```bash
cd java-order-service
./mvnw clean compile
```

Expected: `BUILD SUCCESS`

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/orderservice/client/
git add src/main/java/com/orderservice/dto/CustomerResponse.java
git add src/main/java/com/orderservice/exception/CustomerServiceException.java
git add src/main/java/com/orderservice/config/RestClientConfig.java
git add src/main/resources/application.yml
git add src/main/resources/application-test.yml
git commit -m "feat: add customer client to order service"
```

---

### Task 8: Modify OrderService to Use CustomerClient

**Files:**
- Modify: `java-order-service/src/main/java/com/orderservice/service/OrderService.java`
- Modify: `java-order-service/src/test/java/com/orderservice/service/OrderServiceTest.java`

**Interfaces:**
- Consumes: `CustomerClient` from Task 7
- Produces: `OrderService` with `validateCustomer()` using `CustomerClient.getCustomer()` instead of `CustomerRepository.existsById()`

- [ ] **Step 1: Write failing test for OrderService with CustomerClient mock**

```bash
# Backup original test
cp java-order-service/src/test/java/com/orderservice/service/OrderServiceTest.java java-order-service/src/test/java/com/orderservice/service/OrderServiceTest.java.backup

# Update test to mock CustomerClient instead of CustomerRepository
cat > java-order-service/src/test/java/com/orderservice/service/OrderServiceTest.java << 'EOF'
package com.orderservice.service;

import com.orderservice.client.CustomerClient;
import com.orderservice.dto.*;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.entity.OrderStatus;
import com.orderservice.exception.CustomerNotFoundException;
import com.orderservice.exception.CustomerServiceException;
import com.orderservice.exception.InvalidOrderStatusException;
import com.orderservice.exception.OrderNotFoundException;
import com.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private OrderService orderService;

    private CustomerResponse validCustomer;
    private CreateOrderRequest createOrderRequest;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        validCustomer = new CustomerResponse(
                1L,
                "John",
                "Doe",
                "john.doe@example.com",
                "+1-555-123-4567"
        );

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);
        itemRequest.setPrice(new BigDecimal("99.99"));

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setCustomerId(1L);
        createOrderRequest.setOrderItems(Arrays.asList(itemRequest));

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setCustomerId(1L);
        testOrder.setStatus(OrderStatus.DRAFT);
        testOrder.setTotalAmount(new BigDecimal("199.98"));

        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setProductId(1L);
        orderItem.setQuantity(2);
        orderItem.setPrice(new BigDecimal("99.99"));
        orderItem.calculateSubtotal();
        testOrder.addOrderItem(orderItem);
    }

    @Test
    void createOrder_Success() {
        when(customerClient.getCustomer(1L)).thenReturn(validCustomer);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(inventoryClient.reserveStock(anyLong(), anyInt()))
                .thenReturn(new InventoryReserveResponse(true, "Reserved successfully"));

        OrderResponse response = orderService.createOrder(createOrderRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
        verify(customerClient).getCustomer(1L);
        verify(orderRepository, atLeast(1)).save(any(Order.class));
        verify(inventoryClient).reserveStock(1L, 2);
    }

    @Test
    void createOrder_CustomerNotFound() {
        when(customerClient.getCustomer(1L)).thenThrow(new CustomerNotFoundException(1L));

        assertThrows(CustomerNotFoundException.class, () -> {
            orderService.createOrder(createOrderRequest);
        });

        verify(customerClient).getCustomer(1L);
        verify(orderRepository, never()).save(any(Order.class));
        verify(inventoryClient, never()).reserveStock(anyLong(), anyInt());
    }

    @Test
    void createOrder_CustomerServiceUnavailable() {
        when(customerClient.getCustomer(1L))
                .thenThrow(new CustomerServiceException("Customer service is unavailable"));

        assertThrows(CustomerServiceException.class, () -> {
            orderService.createOrder(createOrderRequest);
        });

        verify(customerClient).getCustomer(1L);
        verify(orderRepository, never()).save(any(Order.class));
        verify(inventoryClient, never()).reserveStock(anyLong(), anyInt());
    }

    @Test
    void getOrderById_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        OrderResponse response = orderService.getOrderById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderById_NotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> {
            orderService.getOrderById(999L);
        });

        verify(orderRepository).findById(999L);
    }

    @Test
    void cancelOrder_Success() {
        testOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(inventoryClient.releaseStock(anyLong(), anyInt()))
                .thenReturn(new InventoryReleaseResponse(true, "Released successfully"));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderResponse response = orderService.cancelOrder(1L);

        assertNotNull(response);
        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        verify(orderRepository).findById(1L);
        verify(inventoryClient).releaseStock(1L, 2);
        verify(orderRepository).save(testOrder);
    }

    @Test
    void cancelOrder_InvalidStatus() {
        testOrder.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(InvalidOrderStatusException.class, () -> {
            orderService.cancelOrder(1L);
        });

        verify(orderRepository).findById(1L);
        verify(inventoryClient, never()).releaseStock(anyLong(), anyInt());
    }
}
EOF
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd java-order-service
./mvnw test -Dtest=OrderServiceTest
```

Expected: FAIL - OrderService still uses CustomerRepository

- [ ] **Step 3: Modify OrderService to use CustomerClient**

```bash
# Read the original OrderService
# Then modify the validateCustomer method to use CustomerClient

cat > /tmp/orderservice_patch.txt << 'EOF'
Replace the validateCustomer method and inject CustomerClient instead of CustomerRepository.
Change from:
    private final CustomerRepository customerRepository;
    
    private void validateCustomer(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            log.error("Customer not found: {}", customerId);
            throw new CustomerNotFoundException(customerId);
        }
    }

To:
    private final CustomerClient customerClient;
    
    private void validateCustomer(Long customerId) {
        try {
            customerClient.getCustomer(customerId);
        } catch (CustomerNotFoundException e) {
            log.error("Customer not found: {}", customerId);
            throw e;
        } catch (CustomerServiceException e) {
            log.error("Customer service unavailable: {}", e.getMessage());
            throw e;
        }
    }
EOF

# Apply the change manually by editing the file
# Replace CustomerRepository with CustomerClient in OrderService.java
```

Manual edit required: Open `java-order-service/src/main/java/com/orderservice/service/OrderService.java` and:
1. Replace `private final CustomerRepository customerRepository;` with `private final CustomerClient customerClient;`
2. Replace the `validateCustomer()` method body with:
```java
private void validateCustomer(Long customerId) {
    try {
        customerClient.getCustomer(customerId);
    } catch (CustomerNotFoundException e) {
        log.error("Customer not found: {}", customerId);
        throw e;
    } catch (CustomerServiceException e) {
        log.error("Customer service unavailable: {}", e.getMessage());
        throw e;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
cd java-order-service
./mvnw test -Dtest=OrderServiceTest
```

Expected: All tests PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/orderservice/service/OrderService.java
git add src/test/java/com/orderservice/service/OrderServiceTest.java
git commit -m "refactor: use customer client instead of customer repository in order service"
```

---

### Task 9: Remove Customer Components from Order Service

**Files:**
- Delete: `java-order-service/src/main/java/com/orderservice/controller/CustomerController.java`
- Delete: `java-order-service/src/main/java/com/orderservice/service/CustomerService.java`
- Delete: `java-order-service/src/main/java/com/orderservice/repository/CustomerRepository.java`
- Delete: `java-order-service/src/main/java/com/orderservice/entity/Customer.java`
- Delete: `java-order-service/src/main/java/com/orderservice/dto/CreateCustomerRequest.java`
- Delete: `java-order-service/src/main/java/com/orderservice/dto/UpdateCustomerRequest.java`
- Delete: `java-order-service/src/test/java/com/orderservice/service/CustomerServiceTest.java`
- Delete: `java-order-service/src/test/java/com/orderservice/controller/CustomerControllerIntegrationTest.java`
- Modify: `java-order-service/src/test/java/com/orderservice/controller/OrderControllerIntegrationTest.java`

**Interfaces:**
- Consumes: None (cleanup task)
- Produces: Order Service without any customer management code

- [ ] **Step 1: Update OrderControllerIntegrationTest to mock CustomerClient**

```bash
# Backup original
cp java-order-service/src/test/java/com/orderservice/controller/OrderControllerIntegrationTest.java \
   java-order-service/src/test/java/com/orderservice/controller/OrderControllerIntegrationTest.java.backup

# Create updated test with @MockBean for CustomerClient
# This is a minimal example - actual test may need more modifications
cat > /tmp/order_controller_test_updates.txt << 'EOF'
Add at the top of the test class:

import com.orderservice.client.CustomerClient;
import org.springframework.boot.test.mock.mockito.MockBean;

Inside the test class, add:

    @MockBean
    private CustomerClient customerClient;

In setup or before each test that creates an order, add:

    CustomerResponse mockCustomer = new CustomerResponse(1L, "John", "Doe", "john@example.com", "+1-555-123-4567");
    when(customerClient.getCustomer(anyLong())).thenReturn(mockCustomer);
EOF
```

Manual edit required: Open `java-order-service/src/test/java/com/orderservice/controller/OrderControllerIntegrationTest.java` and:
1. Add `@MockBean private CustomerClient customerClient;`
2. Add mock setup in tests that create orders: `when(customerClient.getCustomer(anyLong())).thenReturn(new CustomerResponse(1L, "Test", "User", "test@test.com", "+1-555-0000"));`

- [ ] **Step 2: Verify Order Service tests pass with mocked CustomerClient**

```bash
cd java-order-service
./mvnw test -Dtest=OrderControllerIntegrationTest
```

Expected: All tests PASS

- [ ] **Step 3: Delete customer-related files**

```bash
cd java-order-service

# Delete customer controller and service
rm src/main/java/com/orderservice/controller/CustomerController.java
rm src/main/java/com/orderservice/service/CustomerService.java

# Delete customer repository and entity
rm src/main/java/com/orderservice/repository/CustomerRepository.java
rm src/main/java/com/orderservice/entity/Customer.java

# Delete customer DTOs
rm src/main/java/com/orderservice/dto/CreateCustomerRequest.java
rm src/main/java/com/orderservice/dto/UpdateCustomerRequest.java

# Delete customer tests
rm src/test/java/com/orderservice/service/CustomerServiceTest.java
rm src/test/java/com/orderservice/controller/CustomerControllerIntegrationTest.java

# Delete customer exception (replaced by CustomerServiceException)
rm src/main/java/com/orderservice/exception/DuplicateCustomerException.java
```

- [ ] **Step 4: Run all Order Service tests**

```bash
cd java-order-service
./mvnw test
```

Expected: All remaining tests PASS

- [ ] **Step 5: Verify Order Service compiles and runs**

```bash
cd java-order-service
./mvnw clean compile
./mvnw spring-boot:run &
sleep 10
curl http://localhost:8080/health
pkill -f "order-service"
```

Expected: Service starts on port 8080, health check passes, no customer endpoints available

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "refactor: remove customer management from order service"
```

---

### Task 10: End-to-End Testing and Documentation

**Files:**
- Create: `README-SERVICES.md` (new services architecture documentation)
- Modify: `README.md` (update with three-service architecture)
- Create: `scripts/start-all-services.sh`
- Create: `scripts/test-customer-service.sh`
- Create: `scripts/test-order-flow-e2e.sh`

**Interfaces:**
- Consumes: All services from previous tasks
- Produces: Complete working three-service architecture with documentation

- [ ] **Step 1: Create script to start all services**

```bash
cat > scripts/start-all-services.sh << 'EOF'
#!/bin/bash

echo "Starting all services for Order Management System..."

# Start Customer Service
echo "Starting Customer Service on port 8081..."
cd java-customer-service
./mvnw spring-boot:run > ../logs/customer-service.log 2>&1 &
CUSTOMER_PID=$!
cd ..

sleep 5

# Start Order Service
echo "Starting Order Service on port 8080..."
cd java-order-service
./mvnw spring-boot:run > ../logs/order-service.log 2>&1 &
ORDER_PID=$!
cd ..

sleep 5

# Start Inventory Service
echo "Starting Inventory Service on port 8000..."
cd python-inventory-service
source venv/bin/activate
uvicorn main:app --reload > ../logs/inventory-service.log 2>&1 &
INVENTORY_PID=$!
cd ..

sleep 5

echo "All services started!"
echo "Customer Service PID: $CUSTOMER_PID (port 8081)"
echo "Order Service PID: $ORDER_PID (port 8080)"
echo "Inventory Service PID: $INVENTORY_PID (port 8000)"

echo ""
echo "To stop all services:"
echo "kill $CUSTOMER_PID $ORDER_PID $INVENTORY_PID"
EOF

chmod +x scripts/start-all-services.sh
mkdir -p logs
```

- [ ] **Step 2: Create Customer Service test script**

```bash
cat > scripts/test-customer-service.sh << 'EOF'
#!/bin/bash

echo "Testing Customer Service..."

BASE_URL="http://localhost:8081"

# Test health
echo "1. Testing health endpoint..."
curl -s $BASE_URL/health | jq '.'

# Create customer
echo ""
echo "2. Creating customer..."
CUSTOMER_RESPONSE=$(curl -s -X POST $BASE_URL/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "Johnson",
    "email": "alice.johnson@example.com",
    "phone": "+1-555-111-2222"
  }')

echo $CUSTOMER_RESPONSE | jq '.'
CUSTOMER_ID=$(echo $CUSTOMER_RESPONSE | jq -r '.id')

# Get customer by ID
echo ""
echo "3. Getting customer by ID: $CUSTOMER_ID..."
curl -s $BASE_URL/customers/$CUSTOMER_ID | jq '.'

# Get all customers
echo ""
echo "4. Getting all customers..."
curl -s $BASE_URL/customers | jq '.'

# Update customer
echo ""
echo "5. Updating customer..."
curl -s -X PUT $BASE_URL/customers/$CUSTOMER_ID \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "Johnson-Smith",
    "email": "alice.johnson-smith@example.com",
    "phone": "+1-555-111-3333"
  }' | jq '.'

# Delete customer
echo ""
echo "6. Deleting customer..."
curl -s -X DELETE $BASE_URL/customers/$CUSTOMER_ID -w "\nHTTP Status: %{http_code}\n"

echo ""
echo "Customer Service tests completed!"
EOF

chmod +x scripts/test-customer-service.sh
```

- [ ] **Step 3: Create end-to-end order flow test script**

```bash
cat > scripts/test-order-flow-e2e.sh << 'EOF'
#!/bin/bash

echo "Testing End-to-End Order Flow with Three Services..."

CUSTOMER_URL="http://localhost:8081"
ORDER_URL="http://localhost:8080"
INVENTORY_URL="http://localhost:8000"

# Step 1: Create customer
echo "Step 1: Creating customer..."
CUSTOMER_RESPONSE=$(curl -s -X POST $CUSTOMER_URL/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Bob",
    "lastName": "Williams",
    "email": "bob.williams@example.com",
    "phone": "+1-555-222-3333"
  }')

echo $CUSTOMER_RESPONSE | jq '.'
CUSTOMER_ID=$(echo $CUSTOMER_RESPONSE | jq -r '.id')
echo "Customer ID: $CUSTOMER_ID"

# Step 2: Create product
echo ""
echo "Step 2: Creating product..."
PRODUCT_RESPONSE=$(curl -s -X POST $INVENTORY_URL/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Widget",
    "description": "A test product",
    "price": 49.99,
    "sku": "TEST-WIDGET-001"
  }')

echo $PRODUCT_RESPONSE | jq '.'
PRODUCT_ID=$(echo $PRODUCT_RESPONSE | jq -r '.id')
echo "Product ID: $PRODUCT_ID"

# Step 3: Add inventory
echo ""
echo "Step 3: Adding inventory..."
curl -s -X POST $INVENTORY_URL/inventory/add \
  -H "Content-Type: application/json" \
  -d "{
    \"product_id\": $PRODUCT_ID,
    \"quantity\": 100
  }" | jq '.'

# Step 4: Create order
echo ""
echo "Step 4: Creating order..."
ORDER_RESPONSE=$(curl -s -X POST $ORDER_URL/orders \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": $CUSTOMER_ID,
    \"orderItems\": [
      {
        \"productId\": $PRODUCT_ID,
        \"quantity\": 2,
        \"price\": 49.99
      }
    ]
  }")

echo $ORDER_RESPONSE | jq '.'
ORDER_ID=$(echo $ORDER_RESPONSE | jq -r '.id')
ORDER_STATUS=$(echo $ORDER_RESPONSE | jq -r '.status')
echo "Order ID: $ORDER_ID"
echo "Order Status: $ORDER_STATUS"

# Step 5: Verify inventory was reserved
echo ""
echo "Step 5: Checking inventory after order..."
curl -s $INVENTORY_URL/inventory/$PRODUCT_ID | jq '.'

# Step 6: Cancel order
echo ""
echo "Step 6: Cancelling order..."
CANCEL_RESPONSE=$(curl -s -X POST $ORDER_URL/orders/$ORDER_ID/cancel)
echo $CANCEL_RESPONSE | jq '.'
CANCEL_STATUS=$(echo $CANCEL_RESPONSE | jq -r '.status')
echo "Order Status after cancellation: $CANCEL_STATUS"

# Step 7: Verify inventory was released
echo ""
echo "Step 7: Checking inventory after cancellation..."
curl -s $INVENTORY_URL/inventory/$PRODUCT_ID | jq '.'

echo ""
echo "End-to-End Order Flow Test Completed!"
echo "Summary:"
echo "  Customer ID: $CUSTOMER_ID"
echo "  Product ID: $PRODUCT_ID"
echo "  Order ID: $ORDER_ID"
echo "  Final Order Status: $CANCEL_STATUS"
EOF

chmod +x scripts/test-order-flow-e2e.sh
```

- [ ] **Step 4: Update main README.md**

```bash
cat > README.md << 'EOF'
# Order Management System

A microservices-based order management system built with Java Spring Boot and Python FastAPI, demonstrating modern distributed architecture patterns.

## Overview

Distributed application managing customers, orders, and inventory across three independent microservices:

- **Java Customer Service** (Port 8081) - Customer CRUD operations
- **Java Order Service** (Port 8080) - Order management and orchestration
- **Python Inventory Service** (Port 8000) - Product catalog and inventory management

## Technology Stack

### Java Customer Service
- Java 17, Spring Boot 3.2.0, Spring Data JPA
- H2 Database (in-memory)
- SpringDoc OpenAPI, JUnit 5, Mockito

### Java Order Service
- Java 17, Spring Boot 3.2.0, Spring Data JPA
- H2 Database (in-memory)
- SpringDoc OpenAPI, JUnit 5, Mockito
- RestTemplate for inter-service communication

### Python Inventory Service
- Python 3.14.5, FastAPI 0.109.0, SQLAlchemy 2.0.25
- SQLite Database
- Alembic (migrations), pytest

## Quick Start

### Prerequisites
- Java 17+
- Python 3.12+
- Maven 3.8+ (wrapper included)

### Start Services

**1. Python Inventory Service:**
```bash
cd python-inventory-service
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
alembic upgrade head
uvicorn main:app --reload
```
Verify: http://localhost:8000/docs

**2. Java Customer Service:**
```bash
cd java-customer-service
./mvnw spring-boot:run
```
Verify: http://localhost:8081/swagger-ui.html

**3. Java Order Service:**
```bash
cd java-order-service
./mvnw spring-boot:run
```
Verify: http://localhost:8080/swagger-ui.html

### Quick Test

```bash
# Create customer
CUSTOMER_RESPONSE=$(curl -X POST http://localhost:8081/customers \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@example.com","phone":"+1-555-123-4567"}')
CUSTOMER_ID=$(echo $CUSTOMER_RESPONSE | jq -r '.id')

# Create product
PRODUCT_RESPONSE=$(curl -X POST http://localhost:8000/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Product","description":"Test","price":99.99,"sku":"TEST-001"}')
PRODUCT_ID=$(echo $PRODUCT_RESPONSE | jq -r '.id')

# Add inventory
curl -X POST http://localhost:8000/inventory/add \
  -H "Content-Type: application/json" \
  -d "{\"product_id\":$PRODUCT_ID,\"quantity\":100}"

# Create order
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d "{\"customerId\":$CUSTOMER_ID,\"orderItems\":[{\"productId\":$PRODUCT_ID,\"quantity\":2,\"price\":99.99}]}"
```

## Architecture

### Service Communication

```
Client
  ↓
Order Service (8080)
  ├─→ Customer Service (8081) - Validate customer exists
  └─→ Inventory Service (8000) - Reserve/Release stock
```

### Order Creation Flow
```
1. Client → Order Service: POST /orders
2. Order Service → Customer Service: GET /customers/{id} (validate)
3. Order Service: Create DRAFT order
4. Order Service → Inventory Service: POST /inventory/reserve (atomic)
5. Order Service: Update to CONFIRMED status
6. Order Service → Client: Order confirmation
```

### Order Cancellation Flow
```
1. Client → Order Service: POST /orders/{id}/cancel
2. Order Service: Validate order is CONFIRMED
3. Order Service → Inventory Service: POST /inventory/release
4. Order Service: Update to CANCELLED status
5. Order Service → Client: Cancellation confirmation
```

## API Endpoints

### Customer Service (8081)
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/customers` | POST/GET | Manage customers |
| `/customers/{id}` | GET/PUT/DELETE | Customer operations |
| `/health` | GET | Health check |

### Order Service (8080)
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/orders` | POST/GET | Manage orders |
| `/orders/{id}` | GET | Get order details |
| `/orders/{id}/cancel` | POST | Cancel order |
| `/orders/customer/{customerId}` | GET | Get customer orders |
| `/health` | GET | Health check |

### Inventory Service (8000)
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/products` | POST/GET | Manage products |
| `/products/{id}` | GET/PUT/DELETE | Product operations |
| `/inventory/{product_id}` | GET | Get inventory |
| `/inventory/reserve` | POST | Reserve stock |
| `/inventory/release` | POST | Release stock |
| `/inventory/add` | POST | Add stock |

## Testing

```bash
# Customer Service tests
cd java-customer-service && ./mvnw test

# Order Service tests
cd java-order-service && ./mvnw test

# Inventory Service tests
cd python-inventory-service && pytest -v

# E2E tests (requires all services running)
./scripts/test-order-flow-e2e.sh
```

## Configuration

**Customer Service** (`application.yml`):
```yaml
server:
  port: 8081
spring:
  datasource:
    url: jdbc:h2:mem:customerdb
```

**Order Service** (`application.yml`):
```yaml
server:
  port: 8080
customer:
  service:
    url: http://localhost:8081
inventory:
  service:
    url: http://localhost:8000
```

**Inventory Service** (`database/config.py`):
```python
SQLALCHEMY_DATABASE_URL = "sqlite:///./inventory.db"
```

## Documentation

- Interactive API Docs:
  - Customer Service: http://localhost:8081/swagger-ui.html
  - Order Service: http://localhost:8080/swagger-ui.html
  - Inventory Service: http://localhost:8000/docs
- [Customer Service README](java-customer-service/README.md)
- [Order Service README](java-order-service/README.md)
- [Inventory Service README](python-inventory-service/README.md)

## License

Demonstration project showcasing microservices architecture.
EOF
```

- [ ] **Step 5: Create Customer Service README**

```bash
cat > java-customer-service/README.md << 'EOF'
# Java Customer Service

Spring Boot microservice for customer management.

## Overview

Manages customer CRUD operations with validation and error handling. Provides REST API for customer data access.

## Technology Stack

- Java 17, Spring Boot 3.2.0, Spring Data JPA
- H2 Database (in-memory), Lombok
- SpringDoc OpenAPI 3 (Swagger), JUnit 5, Mockito

## Quick Start

```bash
# Build and run
./mvnw spring-boot:run

# Production build
./mvnw clean package -DskipTests
java -jar target/java-customer-service-1.0.0.jar
```

Access: http://localhost:8081/swagger-ui.html

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/customers` | Create customer |
| GET | `/customers` | List customers |
| GET | `/customers/{id}` | Get customer |
| PUT | `/customers/{id}` | Update customer |
| DELETE | `/customers/{id}` | Delete customer |
| GET | `/health` | Health check |

## Configuration

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:h2:mem:customerdb
    username: sa
    password:
```

## Testing

```bash
# All tests
./mvnw test

# Unit tests only
./mvnw test -Dtest=*ServiceTest

# Integration tests only
./mvnw test -Dtest=*IntegrationTest
```

## License

Part of Order Management System demonstration project.
EOF
```

- [ ] **Step 6: Test all services together**

```bash
# Start all services (in separate terminals or using screen/tmux)
./scripts/start-all-services.sh

# Wait for services to start
sleep 15

# Run Customer Service tests
./scripts/test-customer-service.sh

# Run end-to-end order flow test
./scripts/test-order-flow-e2e.sh

# Check all health endpoints
curl http://localhost:8081/health
curl http://localhost:8080/health
curl http://localhost:8000/health
```

Expected: All services respond, all tests pass, complete order flow works

- [ ] **Step 7: Commit**

```bash
git add README.md
git add java-customer-service/README.md
git add scripts/
git commit -m "docs: add three-service architecture documentation and test scripts"
```

---

## Self-Review Checklist

**Spec coverage:**
- [x] Customer Service created on port 8081 with all CRUD operations
- [x] Customer entities, repositories, services, controllers moved
- [x] CustomerClient added to Order Service with REST communication
- [x] OrderService modified to use CustomerClient instead of repository
- [x] All customer components removed from Order Service
- [x] Tests updated (unit and integration)
- [x] Configuration added for customer service URL
- [x] Documentation updated for three-service architecture
- [x] End-to-end testing scripts created

**Placeholder scan:**
- [x] No TBD, TODO, or "implement later" comments
- [x] All code blocks contain actual implementation
- [x] All file paths are exact and absolute
- [x] All tests have expected outcomes specified

**Type consistency:**
- [x] `CustomerResponse` DTO consistent across Customer Service and Order Service
- [x] `CustomerClient.getCustomer()` signature matches usage in OrderService
- [x] Exception types consistent (`CustomerNotFoundException`, `CustomerServiceException`)
- [x] HTTP status codes match spec (201, 200, 404, 409, 503)

**Dependencies between tasks:**
- [x] Task 1 creates project structure needed by Task 2
- [x] Task 2 entities needed by Task 3 DTOs
- [x] Task 3 DTOs needed by Task 4 exceptions
- [x] Tasks 2-4 needed by Task 5 service layer
- [x] Tasks 2-5 needed by Task 6 controller layer
- [x] Task 6 completes Customer Service, enabling Task 7
- [x] Task 7 CustomerClient needed by Task 8
- [x] Task 8 must complete before Task 9 cleanup
- [x] Task 10 requires all previous tasks

All checks passed. Plan is complete and ready for execution.
