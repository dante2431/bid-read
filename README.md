# 🚀 High-Performance Real-Time Market Data Processor

A production-grade Spring Boot application that processes real-time cryptocurrency market data from Binance WebSocket, featuring high-throughput multithreaded architecture and in-memory database storage.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Quick Start](#quick-start)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Performance](#performance)
- [Monitoring](#monitoring)
- [Development](#development)

## 🎯 Overview

This application demonstrates enterprise-grade Java development with focus on:
- **Real-time Processing**: Live cryptocurrency trade data from Binance
- **High Throughput**: Multi-threaded event processing with queue-based architecture
- **In-Memory Storage**: Fast H2 database for bid data
- **Production Ready**: Comprehensive error handling, monitoring, and configuration

## ✨ Features

### 🔄 Real-Time Data Processing
- **WebSocket Connection**: Connects to Binance real-time trade stream (BTC/USDT)
- **Event Queue**: 10,000 capacity queue for handling high-frequency data
- **Worker Threads**: 4 concurrent threads processing events
- **Auto-Reconnection**: Automatic WebSocket recovery on connection loss

### 💾 Data Management
- **H2 Database**: In-memory storage with high-performance inserts
- **JPA Repository**: Clean data access layer with custom queries
- **Data Model**: Precise financial data handling with BigDecimal
- **Console Access**: Web-based H2 console for data inspection

### 🌐 REST API
- **Market Data Endpoints**: Retrieve bid data with filtering options
- **Statistics API**: Real-time performance metrics
- **Health Checks**: Application status monitoring
- **CORS Support**: Cross-origin resource sharing enabled

### ⚙️ Enterprise Features
- **Spring Boot**: Modern framework with auto-configuration
- **Dependency Injection**: Clean component architecture
- **Configuration Management**: YAML-based external configuration
- **Structured Logging**: Comprehensive logging with SLF4J
- **Actuator**: Production-ready monitoring endpoints

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Binance       │    │   Event Queue   │    │  Worker Threads │    │   H2 Database   │
│   WebSocket     │───▶│ (10,000 capacity)│───▶│  (4 threads)    │───▶│  (In-Memory)    │
│                 │    │                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │                       │
         │                       │                       │                       │
         ▼                       ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ WebSocketClient │    │ EventProcessor  │    │   BidRepository │    │   Bid Entity    │
│                 │    │                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Component Responsibilities

- **BinanceWebSocketClient**: Manages WebSocket connection and message parsing
- **EventProcessor**: Coordinates thread pool and queue management
- **BidRepository**: Handles database operations with JPA
- **MarketDataController**: Provides REST API endpoints
- **ApplicationConfig**: Manages application startup and initialization

## 🛠 Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| **Framework** | Spring Boot | 3.2.0 |
| **Language** | Java | 17 |
| **Build Tool** | Gradle | 8.10 |
| **Database** | H2 | In-Memory |
| **WebSocket** | Tyrus | 2.1.4 |
| **JSON** | Jackson | Spring Boot Managed |
| **JPA** | Hibernate | 6.3.1 |
| **Testing** | JUnit | 5.10.0 |

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Gradle 8.0 or higher
- Internet connection (for Binance WebSocket)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd bid-read
   ```

2. **Build the application**
   ```bash
   ./gradlew build
   ```

3. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

4. **Verify the application**
   ```bash
   curl http://localhost:8080/api/market/stats
   ```

### Startup Sequence

The application will automatically:
1. Initialize H2 in-memory database
2. Start 4 worker threads
3. Connect to Binance WebSocket
4. Begin processing real-time trade data

## 📚 API Documentation

### Market Data Endpoints

#### Get Recent Bids
```http
GET /api/market/bids/{symbol}
```
**Example**: `GET /api/market/bids/BTCUSDT`
**Response**: Array of recent bid objects ordered by event time (descending)

#### Get Bids Since Timestamp
```http
GET /api/market/bids/{symbol}/since?since={timestamp}
```
**Example**: `GET /api/market/bids/BTCUSDT/since?since=2026-03-01T01:00:00`
**Response**: Array of bids since specified timestamp

#### Get Performance Statistics
```http
GET /api/market/stats
```
**Response**:
```json
{
  "queueSize": 0,
  "isQueueFull": false,
  "btcTradesLastHour": 15025,
  "totalBids": 15025
}
```

### Health Check

#### Application Health
```http
GET /actuator/health
```
**Response**:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

### Data Model

#### Bid Object
```json
{
  "id": 12345,
  "symbol": "BTCUSDT",
  "price": "66528.43000000",
  "quantity": "0.00857000",
  "eventTime": "2026-03-01T01:09:46.626",
  "createdAt": "2026-03-01T01:09:46.285761"
}
```

## ⚙️ Configuration

### Application Configuration

The application uses YAML configuration in `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:marketdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password: password
  
  h2:
    console:
      enabled: true
      path: /h2-console
      settings:
        web-allow-others: true
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    dialect: org.hibernate.dialect.H2Dialect

market:
  data:
    binance:
      websocket:
        url: wss://stream.binance.com:9443/ws/btcusdt@trade
        reconnect-delay: 5000
    processor:
      thread-pool-size: 4
      queue-capacity: 10000
```

### Environment Variables

You can override configuration using environment variables:
- `SERVER_PORT`: Change application port
- `MARKET_DATA_BINANCE_WEBSOCKET_URL`: Custom WebSocket URL
- `MARKET_DATA_PROCESSOR_THREAD_POOL_SIZE`: Adjust worker threads
- `MARKET_DATA_PROCESSOR_QUEUE_CAPACITY`: Change queue size

## 📊 Performance

### Throughput Metrics

| Metric | Typical Value | Description |
|--------|---------------|-------------|
| **Events/Second** | 1,000+ | Real-time trade processing |
| **Database Inserts** | Concurrent | Multi-threaded inserts |
| **Memory Usage** | ~100MB | In-memory database storage |
| **CPU Usage** | Low-Moderate | Efficient thread management |

### Performance Features

- **Fixed Thread Pool**: Prevents thread creation overhead
- **Bounded Queue**: Memory protection with backpressure
- **Non-blocking I/O**: WebSocket communication
- **Batch Processing**: Efficient database operations

### Scaling Considerations

- **Horizontal Scaling**: Deploy multiple instances with load balancing
- **Database Scaling**: Switch to persistent database for historical data
- **Queue Scaling**: Adjust queue capacity based on traffic patterns
- **Thread Scaling**: Modify thread pool size for CPU optimization

## 🔍 Monitoring

### Application Monitoring

#### Health Endpoints
- **Actuator Health**: `/actuator/health`
- **Application Stats**: `/api/market/stats`
- **Custom Metrics**: Queue size, processing rates

#### Database Console
- **URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:marketdb`
- **Username**: `sa`
- **Password**: `password`

#### Logging Levels
```yaml
logging:
  level:
    com.market.bid: INFO
    org.springframework.web: WARN
    org.hibernate.SQL: WARN
```

### Performance Monitoring

The application provides real-time metrics:
- **Queue Size**: Current event queue length
- **Processing Rate**: Events processed per second
- **Database Operations**: Insert performance
- **WebSocket Status**: Connection health

## 🛠 Development

### Project Structure

```
src/main/java/com/market/bid/
├── BidApplication.java          # Main application class
├── config/
│   └── ApplicationConfig.java   # Startup configuration
├── controller/
│   └── MarketDataController.java # REST API endpoints
├── dto/
│   └── BidEvent.java           # Data transfer object
├── entity/
│   └── Bid.java                # JPA entity
├── repository/
│   └── BidRepository.java      # Data access layer
└── service/
    ├── BinanceWebSocketClient.java # WebSocket handling
    └── EventProcessor.java     # Event processing logic
```

### Code Quality

- **Clean Architecture**: Separated concerns with clear boundaries
- **Dependency Injection**: Spring IoC container management
- **Error Handling**: Comprehensive exception management
- **Logging**: Structured logging with appropriate levels
- **Testing**: JUnit 5 with Spring Boot Test

### Build and Test

```bash
# Build application
./gradlew build

# Run tests
./gradlew test

# Run application
./gradlew bootRun

# Generate documentation
./gradlew javadoc
```

### IDE Integration

The project is optimized for IntelliJ IDEA with:
- **Code Style**: Consistent Java formatting
- **Live Templates**: Quick code generation
- **Debug Configuration**: Breakpoint-ready debugging
- **Spring Boot Dashboard**: Application management

## 🔧 Troubleshooting

### Common Issues

#### WebSocket Connection Failed
**Symptoms**: Application starts but no data processing
**Solution**: Check internet connection and Binance service status

#### Database Connection Error
**Symptoms**: H2 console shows "database not found"
**Solution**: Ensure application is fully started before accessing console

#### High Memory Usage
**Symptoms**: Memory warnings in logs
**Solution**: Reduce queue capacity or implement data cleanup

#### Performance Degradation
**Symptoms**: Slow processing or queue overflow
**Solution**: Increase thread pool size or optimize database operations

### Debug Mode

Enable debug logging:
```yaml
logging:
  level:
    com.market.bid: DEBUG
    org.hibernate.SQL: DEBUG
```

### Production Considerations

- **Database**: Switch to PostgreSQL/MySQL for persistence
- **Security**: Add authentication and authorization
- **Monitoring**: Integrate with APM tools
- **Backup**: Implement data backup strategies
- **Scaling**: Configure horizontal scaling

## 📄 License

This project is provided as-is for educational and development purposes.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📞 Support

For issues and questions:
- Check the troubleshooting section
- Review application logs
- Verify configuration settings
- Test API endpoints individually

---

**Built with ❤️ using Spring Boot and Java 17**
