# Spring JMS Orders Demo

This project showcases Spring's modern **JmsClient API** for messaging with practical examples of seven essential 
JMS messaging patterns.

## About JMS

**Java Message Service (JMS)** is a mature messaging standard for Java applications, first released in 1998 
and now maintained as Jakarta Messaging. It remains widely used in enterprise environments—particularly in 
financial services, healthcare, and large corporations—where reliability, transactional integrity, and 
standardization are critical.

**Is JMS still relevant?** Yes, but it depends on your context:

- **Choose JMS when**: You're working with existing Java enterprise systems, need strong transactional guarantees with JTA, or require Jakarta EE compliance
- **Consider modern alternatives when**: Building new cloud-native apps, need event streaming capabilities, or want higher throughput

**Why learn JMS today?** The messaging patterns demonstrated here—request-reply, priority handling, metadata routing, synchronous/asynchronous processing—are fundamental concepts that apply to *any* messaging system.

### JMS-Compatible Products

**Open-Source:**
- **Apache ActiveMQ Artemis** (used in this project) - Modern, high-performance, actively developed
- **Apache ActiveMQ Classic** - Widely deployed legacy version

**Cloud-Managed:**
- **Amazon MQ** - Managed service on AWS (based on ActiveMQ/Artemis)
- **Azure Service Bus Premium** - Full JMS 2.0 support on Azure

**Enterprise:**
- **IBM MQ**, **TIBCO EMS**, **Oracle WebLogic JMS**

## Quick Start

### Prerequisites

- **Java 25** (or compatible JDK)
- **Docker** and **Docker Compose**
- **Maven 3.8+**

### Get Running

1. **Run the application** (Spring Boot auto-starts ActiveMQ Artemis via Docker Compose):
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Send your first message**:
   ```bash
   curl -X POST http://localhost:8080/api/orders/simple \
     -H "Content-Type: application/json" \
     -d '{"orderId":"ORD-001","customerId":"CUST-123","amount":299.99,"status":"PENDING","timestamp":"2025-01-15T10:30:00"}'
   ```

3. **View messages** at the ActiveMQ console: http://localhost:8161 (admin/admin)

## What is JmsClient?

`JmsClient` is Spring's modern, fluent API for JMS messaging (Spring Framework 6.1+). It provides a cleaner, 
more intuitive way to work with JMS:

```java
jmsClient
    .destination("order-queue")
    .send(order);
```

## The 7 Messaging Patterns

All patterns are implemented in `OrderMessagingService.java` with detailed comments explaining each approach.

| Pattern | Description | Endpoint |
|---------|-------------|----------|
| **Basic Send** | Fire-and-forget messaging | `POST /api/orders/simple` |
| **QoS Settings** | TTL, priority, delivery delays | `POST /api/orders/priority` |
| **Custom Headers** | Add routing metadata to messages | `POST /api/orders/with-metadata?region=US-WEST` |
| **Sync Receive** | Pull messages with timeout | `GET /api/orders/receive` |
| **Receive & Convert** | Type-safe retrieval | (see service class) |
| **Request-Reply** | RPC-style synchronous messaging | `POST /api/orders/process` |
| **Reusable Handle** | Pre-configured handle for bulk sends | `POST /api/orders/bulk-express` |

## Architecture

```
┌─────────────────┐
│  REST API       │  OrderController
│  (Port 8080)    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Service Layer  │  OrderMessagingService
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   JmsClient     │  Spring's Fluent JMS API
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  ActiveMQ       │  Queues: order-queue,
│  Artemis        │          notification-queue
│  (Docker)       │  Console: localhost:8161
└─────────────────┘
```

## Testing

### Using the HTTP Client File

The project includes `store.http` with pre-configured requests for all 7 patterns. Open it in IntelliJ IDEA or VS Code with REST Client extension.

### Using cURL

```bash
# Send a simple order
curl -X POST http://localhost:8080/api/orders/simple \
  -H "Content-Type: application/json" \
  -d '{"orderId":"ORD-001","customerId":"CUST-123","amount":299.99,"status":"PENDING","timestamp":"2025-01-15T10:30:00"}'

# Receive a message
curl http://localhost:8080/api/orders/receive
```

## Learn More

- [Spring JMS Documentation](https://docs.spring.io/spring-framework/reference/integration/jms.html)
- [Apache ActiveMQ Artemis](https://activemq.apache.org/components/artemis/)
- [Jakarta JMS Specification](https://jakarta.ee/specifications/messaging/)
