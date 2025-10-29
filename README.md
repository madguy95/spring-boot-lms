# spring-boot-jwt-redis-mysql

## Project Overview
This project is a Spring Boot application that demonstrates secure authentication, real-time messaging, scheduled tasks, event streaming, and caching using JWT, Redis, MySQL, Kafka, and WebSocket (SockJS/STOMP). It is designed for scalable, production-ready systems requiring user authentication, scheduled jobs, messaging, event streaming, and caching.

## Technologies Used
- **Spring Boot 3**: Rapid application development framework.
- **JWT (JSON Web Token)**: Stateless authentication.
- **Redis**: In-memory data store for caching, session management, and pub/sub.
- **MySQL**: Relational database for persistent storage.
- **Quartz**: Job scheduling for background tasks.
- **SockJS/STOMP**: WebSocket messaging for real-time communication.
- **Kafka**: Distributed event streaming and messaging.

## Project Structure & Component Explanations
The source code is organized under `src/main/java/com/springjwt/`:

### 1. Auth Module
Handles user authentication, registration, JWT token generation/validation, and role-based access control.
- **auth/**: Controllers and services for login, registration, and token management.
- **security/**: JWT filter, security config, and user details service.
- **How it works**: Users register/login, receive JWT. JWT is validated for protected endpoints. Roles control access. You can enable/disable authentication by modifying security config in `security/` and `application.yml` (e.g., permit all endpoints for testing).

### 2. Quartz Module
Manages scheduled jobs and background tasks using Quartz Scheduler.
- **quartz/**: Job definitions, scheduling configuration, and job listeners.
- **How it works**: Jobs are scheduled via cron expressions in config or code. You can enable/disable jobs by commenting out job beans or adjusting cron expressions in `application.yml`.

### 3. Message Module
Implements real-time messaging via WebSocket (SockJS/STOMP).
- **message/**: Controllers and services for sending/receiving messages.
- **websocket/**: WebSocket configuration and event handlers.
- **How it works**: Clients connect to `/ws`, subscribe to topics, send/receive messages instantly. You can enable/disable messaging by toggling WebSocket config in `application.yml` or disabling related beans.

### 4. Redis Integration
Used for caching, session management, and pub/sub messaging.
- **redis/**: Redis configuration, cache manager, and utility classes.
- **How it works**: Stores session/token data, caches queries, supports pub/sub for notifications. You can enable/disable Redis by adjusting `spring.data.redis.*` properties in `application.yml`.

### 5. Kafka Integration
Distributed event streaming and messaging.
- **kafka/**: Kafka producer/consumer config, event handlers.
- **How it works**: Publishes and consumes events for scalable, decoupled communication between services. You can enable/disable Kafka by setting `spring.kafka.enabled` in `application.yml` or commenting out Kafka beans.

### 6. Other Notable Components
- **user/**: User management (CRUD, roles).
- **audit/**: Auditing actions and events.
- **core/**: Common utilities, logging, and health checks.
- **common/**: Shared constants, enums, and annotations.

## Setup & Configuration

### 1. Database & Service Configuration
Edit `src/main/resources/application.properties` or `application.yml`:
- **MySQL**
  ```
  spring.datasource.url=jdbc:mysql://localhost:3306/testdb?useSSL=false
  spring.datasource.username=root
  spring.datasource.password=123456
  ```
- **Redis**
  ```
  spring.data.redis.database=0
  spring.data.redis.host=localhost
  spring.data.redis.port=16379
  spring.data.redis.password=mypass
  ```
- **Kafka**
  ```
  spring.kafka.bootstrap-servers=localhost:9092
  spring.kafka.consumer.group-id=my-group
  spring.kafka.enabled=true # Set to false to disable Kafka
  ```
- **Quartz**
  ```
  spring.quartz.job-store-type=memory # Use 'jdbc' for persistent jobs
  spring.quartz.enabled=true # Set to false to disable Quartz
  ```
- **WebSocket**
  ```
  spring.websocket.enabled=true # Set to false to disable WebSocket messaging
  ```

### 2. Running the Application
- With Gradle:
  ```
  ./gradlew bootRun
  ```
- With Maven:
  ```
  mvn spring-boot:run
  ```

### 3. SQL Initialization
Run the following SQL to initialize roles:
```
INSERT INTO roles(name) VALUES('ROLE_USER');
INSERT INTO roles(name) VALUES('ROLE_MODERATOR');
INSERT INTO roles(name) VALUES('ROLE_ADMIN');
```

### 4. Docker Support
Use `docker-compose.yml` to start MySQL, Redis, and other dependencies:
```
docker-compose up -d
```
For Kafka, use:
```
docker-compose -f docker-compose-kafka.yml up -d
```

## Usage Guide

### 1. Authentication Flow
- Register a new user via `/api/auth/signup`.
- Login via `/api/auth/signin` to receive a JWT token.
- Use the JWT token in the `Authorization` header for protected endpoints.

### 2. WebSocket Messaging
- Connect to `/ws` endpoint using SockJS/STOMP.
- Subscribe to topics and send messages using the provided API.

### 3. Scheduled Tasks
- Quartz jobs are configured in `quartz/` and run automatically based on cron expressions.

### 4. Kafka Events
- Publish/consume events via Kafka topics for inter-service communication.

## Component Enable/Disable Flags

This project supports dynamic enabling/disabling of core modules (Redis, Kafka, Quartz) using configuration flags in `application.yml` under the `app` section:

```yaml
app:
  redis:
    enabled: ${REDIS_ENABLED:true}   # Enable Redis integration (true/false)
  kafka:
    enabled: ${KAFKA_ENABLED:true}   # Enable Kafka integration (true/false)
  quartz:
    enabled: ${QUARTZ_ENABLED:true}  # Enable Quartz scheduler (true/false)
```

**How it works:**
- Each flag controls whether the corresponding module is initialized and used by the application.
- The default value is `true` (enabled). Set to `false` to disable the module.
- You can override these values using environment variables (e.g., `REDIS_ENABLED=false`).
- Disabling a module will prevent its beans/configuration from loading, allowing you to run the app without that dependency.

**Typical use cases:**
- **Development:** Disable modules you don't need (e.g., Kafka, Redis) for faster startup and easier debugging.
- **Production:** Enable all required modules and ensure external services are running.
- **Troubleshooting:** Temporarily disable a module to isolate issues or run the app in a minimal mode.

**Example (disable Redis and Kafka):**
```shell
set REDIS_ENABLED=false
set KAFKA_ENABLED=false
./gradlew bootRun
```

**Note:** Always restart the application after changing these flags or environment variables.

## Important Notes & Best Practices
- Change default passwords and secrets before deploying to production.
- Use environment variables or secret management for sensitive configs.
- Monitor logs in the `logs/` directory for troubleshooting.
- Use health checks and audit logs for monitoring and compliance.
- Review and update scheduled jobs and message topics as needed for your use case.

## References
- [Spring Boot](https://spring.io/projects/spring-boot)
- [JWT Auth](https://bezkoder.com/angular-spring-boot-jwt-auth/)
- [Redis](https://redis.io/)
- [MySQL](https://dev.mysql.com/doc/)
- [Quartz](https://www.quartz-scheduler.org/)
- [SockJS/STOMP](https://spring.io/guides/gs/messaging-stomp-websocket/)
- [Kafka](https://kafka.apache.org/documentation/)
