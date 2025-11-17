# Spring JMS Orders Demo

This project showcases Spring's modern **JmsClient API** for messaging. This project provides practical examples 
of 7 essential JMS messaging patterns that every Spring developer should know.

## 🎯 What You'll Learn

- How to use Spring's fluent `JmsClient` API for sending and receiving messages
- Quality of Service (QoS) settings: TTL, priority, and delivery delays
- Working with message headers and metadata
- Synchronous and asynchronous messaging patterns
- Request-reply pattern for RPC-style communication
- Performance optimization with reusable operation handles

## 🚀 Quick Start

### Prerequisites

- **Java 25** (or compatible JDK)
- **Docker** and **Docker Compose**
- **Maven 3.8+**

### Get Running in 3 Steps

1. **Start ActiveMQ Artemis**
   ```bash
   docker-compose up -d
   ```
   This starts ActiveMQ Artemis on:
   - JMS port: `61616`
   - Web Console: `http://localhost:8161` (admin/admin)

2. **Run the Spring Boot Application**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Send Your First Message**
   ```bash
   curl -X POST http://localhost:8080/api/orders/simple \
     -H "Content-Type: application/json" \
     -d '{
       "orderId": "ORD-001",
       "customerId": "CUST-123",
       "amount": 299.99,
       "status": "PENDING",
       "timestamp": "2025-01-15T10:30:00"
     }'
   ```

Check the ActiveMQ console to see your message in the `order-queue`!

## 💡 What is JmsClient?

`JmsClient` is Spring's modern, fluent API for JMS messaging introduced in Spring Framework 6.1+. It provides a cleaner, more intuitive way to work with JMS compared to older approaches.

**Key Benefits**:
- **Fluent API**: Chain methods for readable, self-documenting code
- **Type Safety**: Automatic conversion with generic type support
- **Flexible**: Mix and match QoS settings, headers, and destinations
- **Modern**: Built for Spring Boot 3.x+ and Jakarta JMS

**Simple Example**:
```java
jmsClient
    .destination("order-queue")
    .send(order);
```

## 📚 The 7 Messaging Patterns

This project demonstrates seven fundamental JMS patterns through the `OrderMessagingService` class:

### 1️⃣ **Basic Send** - Fire and Forget
The simplest pattern: send a message and move on.

**When to use**: Fire-and-forget notifications, audit logs, background tasks

```java
jmsClient
    .destination("order-queue")
    .send(order);
```

**Try it**: `POST /api/orders/simple`

---

### 2️⃣ **Quality of Service (QoS)** - Control Message Delivery
Configure how messages are delivered with TTL, priority, and delays.

**When to use**: Express orders, time-sensitive data, message expiration

```java
jmsClient
    .destination("order-queue")
    .withTimeToLive(300_000)    // Expire after 5 minutes
    .withPriority(9)             // Highest priority (0-9)
    .withDeliveryDelay(1_000)    // Delay 1 second before available
    .send(order);
```

**Key Concepts**:
- **TTL (Time To Live)**: Message expires if not consumed within time limit
- **Priority**: Higher priority messages are delivered first (0=lowest, 9=highest)
- **Delivery Delay**: Hold message in queue before making it available

**Try it**: `POST /api/orders/priority`

---

### 3️⃣ **Custom Headers** - Add Routing Metadata
Attach metadata to messages for routing, filtering, and tracking without parsing the payload.

**When to use**: Region-based routing, tracking message origin, conditional processing

```java
Message<Order> message = MessageBuilder
    .withPayload(order)
    .setHeader("source", "web-portal")
    .setHeader("region", "US-WEST")
    .setHeader("processedBy", "order-service")
    .build();

jmsClient
    .destination("order-queue")
    .send(message);
```

**Try it**: `POST /api/orders/with-metadata?region=US-EAST`

---

### 4️⃣ **Synchronous Receive** - Pull Messages with Timeout
Block and wait for a message with a timeout, accessing both payload and headers.

**When to use**: Request-driven polling, manual message processing, debugging

```java
Optional<Message<?>> message = jmsClient
    .destination("order-queue")
    .withReceiveTimeout(5_000)  // Wait up to 5 seconds
    .receive();

message.ifPresent(msg -> {
    Order order = (Order) msg.getPayload();
    String region = (String) msg.getHeaders().get("region");
    // Process order...
});
```

**Try it**: `GET /api/orders/receive`

---

### 5️⃣ **Receive and Convert** - Type-Safe Retrieval
Automatically convert messages to the desired type without manual casting.

**When to use**: When you only need the payload and don't care about headers

```java
Optional<Order> order = jmsClient
    .destination("order-queue")
    .withReceiveTimeout(5_000)
    .receive(Order.class);  // Automatic conversion

order.ifPresent(o -> {
    System.out.println("Received: " + o.orderId());
});
```

**Comparison to Pattern 4**: Cleaner API when headers aren't needed

---

### 6️⃣ **Request-Reply** - RPC Over JMS
Send a message and wait for a response (synchronous RPC pattern).

**When to use**: Order confirmation, validation requests, synchronous workflows

```java
Optional<Message<?>> reply = jmsClient
    .destination("order-processor")
    .withReceiveTimeout(10_000)  // Wait up to 10 seconds for reply
    .sendAndReceive(request);

if (reply.isPresent()) {
    String confirmation = (String) reply.get().getPayload();
    System.out.println("Received confirmation: " + confirmation);
}
```

**How it works**:
- Spring automatically creates a temporary reply queue
- Correlation ID is set to match request with response
- Blocks until reply arrives or timeout occurs

**Try it**: `POST /api/orders/process`

---

### 7️⃣ **Reusable Operation Handle** - Performance Optimization
Pre-configure destination and QoS settings once, reuse for multiple sends.

**When to use**: Bulk operations, high-throughput scenarios, repeated sends with same config

```java
// Configure once
var expressHandle = jmsClient
    .destination("express-orders")
    .withTimeToLive(60_000)
    .withPriority(8);

// Reuse many times
expressHandle.send(order1);
expressHandle.send(order2);
expressHandle.send(order3);
```

**Benefit**: Avoid re-configuring destination and QoS for every send operation

**Try it**: `POST /api/orders/bulk-express`

---

## 🏗️ Architecture

```
┌─────────────────┐
│  REST API       │  OrderController
│  (Port 8080)    │  - POST /api/orders/simple
└────────┬────────┘  - POST /api/orders/priority
         │           - GET /api/orders/receive
         │           - etc.
         ▼
┌─────────────────┐
│  Service Layer  │  OrderMessagingService
│                 │  - Injects JmsClient
└────────┬────────┘  - Implements 7 patterns
         │
         ▼
┌─────────────────┐
│   JmsClient     │  Spring's Fluent JMS API
│   (Auto-config) │  - Destination routing
└────────┬────────┘  - QoS configuration
         │           - Message conversion
         ▼
┌─────────────────┐
│  Message        │  JacksonJmsMessageConverter
│  Converter      │  - Object ↔ JSON
└────────┬────────┘  - Type metadata
         │
         ▼
┌─────────────────┐
│  ActiveMQ       │  Apache ActiveMQ Artemis
│  Artemis        │  - Queues: order-queue,
│  (Docker)       │           notification-queue
└─────────────────┘  - Web Console: :8161
```

**Data Model**: `Order` record with fields:
- `orderId`: Unique order identifier
- `customerId`: Customer reference
- `amount`: Order total
- `status`: PENDING | PROCESSING | COMPLETED | FAILED
- `timestamp`: Order creation time

## 🧪 Testing the Application

### Option 1: Using the HTTP Client File

The project includes `store.http` with pre-configured requests for all 7 patterns. Open it in IntelliJ IDEA or VS Code with REST Client extension.

### Option 2: Using cURL

**Send a simple order:**
```bash
curl -X POST http://localhost:8080/api/orders/simple \
  -H "Content-Type: application/json" \
  -d '{"orderId":"ORD-001","customerId":"CUST-123","amount":299.99,"status":"PENDING","timestamp":"2025-01-15T10:30:00"}'
```

**Send a priority order:**
```bash
curl -X POST http://localhost:8080/api/orders/priority \
  -H "Content-Type: application/json" \
  -d '{"orderId":"ORD-002","customerId":"CUST-456","amount":599.99,"status":"PENDING","timestamp":"2025-01-15T10:35:00"}'
```

**Receive a message:**
```bash
curl http://localhost:8080/api/orders/receive
```

**Send with metadata:**
```bash
curl -X POST "http://localhost:8080/api/orders/with-metadata?region=US-WEST" \
  -H "Content-Type: application/json" \
  -d '{"orderId":"ORD-003","customerId":"CUST-789","amount":149.99,"status":"PENDING","timestamp":"2025-01-15T10:40:00"}'
```

### Option 3: ActiveMQ Web Console

Visit `http://localhost:8161` (admin/admin) to:
- View queues and message counts
- Browse messages and their content
- Monitor message flow in real-time
- Manually send/receive test messages

## ⚙️ Configuration Reference

### Application Configuration (`application.yaml`)

```yaml
spring:
  artemis:
    mode: native                           # Use native Artemis protocol
    broker-url: tcp://localhost:61616      # JMS broker connection
    user: admin
    password: admin
  jms:
    template:
      default-destination: demo-queue      # Fallback destination

logging:
  level:
    org.springframework.jms: DEBUG         # Enable JMS debug logging
```

### Docker Compose Configuration

ActiveMQ Artemis runs in Docker with:
- **Ports**: 8161 (web console), 61616 (JMS)
- **Credentials**: admin/admin
- **Persistence**: `./data/activemq` volume for message durability

### Queue Definitions

Defined in `Application.java`:
- **order-queue**: Main queue for order messages
- **notification-queue**: Queue for notifications

## 📖 Understanding Quality of Service

For detailed explanations of QoS concepts, see `Notes.md` in the project root. Key highlights:

**Time To Live (TTL)**:
- Messages expire and are removed from queue after TTL
- Use for time-sensitive data (stock prices, flash sales)
- Default: messages never expire

**Priority (0-9)**:
- Higher priority messages delivered first
- 0 = lowest, 4 = default, 9 = highest
- Use for express shipping, critical alerts

**Delivery Delay**:
- Hold message before making it available to consumers
- Use for scheduled processing, rate limiting
- Measured in milliseconds

## 🔍 Project Structure

```
jms-orders/
├── src/main/java/dev/danvega/store/
│   ├── Application.java                  # Spring Boot entry + queue config
│   └── order/                            # Order domain package
│       ├── Order.java                    # Order data model (record)
│       ├── OrderController.java          # REST endpoints
│       ├── OrderMessagingService.java    # 7 JMS pattern implementations
│       ├── JacksonJmsMessageConverter.java   # JSON message conversion
│       └── package-info.java             # Package null-safety annotations
├── src/main/resources/
│   └── application.yaml                  # Spring Boot configuration
├── src/test/java/dev/danvega/store/
│   └── ApplicationTests.java             # Basic context load test
├── compose.yaml                          # Docker Compose for Artemis
├── store.http                            # HTTP client test requests
├── Notes.md                              # QoS concept explanations
└── pom.xml                               # Maven dependencies
```

## 🎓 Next Steps

### Learn More About JMS
- [Spring JMS Documentation](https://docs.spring.io/spring-framework/reference/integration/jms.html)
- [Apache ActiveMQ Artemis](https://activemq.apache.org/components/artemis/)
- [Jakarta JMS Specification](https://jakarta.ee/specifications/messaging/)
