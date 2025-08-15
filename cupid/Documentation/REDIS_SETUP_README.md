# Redis Setup and Cache Configuration Guide

This guide explains how to set up Redis for caching and how to configure the application to work with or without Redis.

## 🚀 **Quick Start Options**

### **Option 1: Use In-Memory Cache (Default)**
The application will work immediately with in-memory caching:
```bash
# No additional setup required - just run the application
./mvnw spring-boot:run
```

### **Option 2: Use Redis for Distributed Caching**
Set up Redis and activate the Redis profile for better performance and distributed caching.

## 🔧 **Redis Setup Instructions**

### **Windows Setup**

#### **Using Docker (Recommended)**
```bash
# Install Docker Desktop from https://www.docker.com/products/docker-desktop
# Then run:
docker run -d --name redis -p 6379:6379 redis:latest
```

#### **Using WSL2**
```bash
# Install WSL2 and Ubuntu
wsl --install -d Ubuntu

# In WSL2 Ubuntu:
sudo apt update
sudo apt install redis-server
sudo systemctl start redis-server
```

#### **Native Windows Redis**
1. Download Redis for Windows from: https://github.com/microsoftarchive/redis/releases
2. Extract and run `redis-server.exe`

### **macOS Setup**

#### **Using Homebrew**
```bash
brew install redis
brew services start redis
```

#### **Using Docker**
```bash
docker run -d --name redis -p 6379:6379 redis:latest
```

### **Linux Setup**

#### **Ubuntu/Debian**
```bash
sudo apt update
sudo apt install redis-server
sudo systemctl start redis-server
sudo systemctl enable redis-server
```

#### **CentOS/RHEL**
```bash
sudo yum install redis
sudo systemctl start redis
sudo systemctl enable redis
```

#### **Using Docker**
```bash
docker run -d --name redis -p 6379:6379 redis:latest
```

## 📊 **Configuration Profiles**

### **Profile 1: In-Memory Cache (Default)**
```bash
# No profile needed - uses in-memory caching
./mvnw spring-boot:run
```

**Features:**
- ✅ Fast startup
- ✅ No external dependencies
- ✅ Good for development/testing
- ❌ Cache lost on application restart
- ❌ No distributed caching

### **Profile 2: Redis Cache**
```bash
# Activate Redis profile
./mvnw spring-boot:run --spring.profiles.active=redis
```

**Features:**
- ✅ Persistent caching
- ✅ Distributed caching support
- ✅ Better performance for production
- ❌ Requires Redis server
- ❌ Slightly more complex setup

### **Profile 3: Custom Redis Configuration**
```bash
# Use custom Redis host/port
./mvnw spring-boot:run --spring.profiles.active=redis --spring.data.redis.host=192.168.1.100 --spring.data.redis.port=6380
```

## 🔍 **Verifying Redis Connection**

### **Test Redis Connection**
```bash
# Connect to Redis CLI
redis-cli

# Test basic commands
127.0.0.1:6379> ping
PONG

127.0.0.1:6379> info
# Server information will be displayed

127.0.0.1:6379> exit
```

### **Check Application Health**
```bash
# When Redis is running, check health endpoint
curl http://localhost:8080/actuator/health

# Should show Redis status:
{
  "status": "UP",
  "components": {
    "redis": {
      "status": "UP",
      "details": {
        "redis": "Redis is running",
        "ping": "PONG"
      }
    }
  }
}
```

## 🛠️ **Troubleshooting**

### **Common Issues**

#### **1. Redis Connection Failed**
```bash
# Error: Unable to connect to Redis
# Solution: Start Redis server or use in-memory profile
./mvnw spring-boot:run --spring.profiles.active=inmemory
```

#### **2. Port Already in Use**
```bash
# Error: Port 6379 already in use
# Solution: Use different port or stop existing Redis
docker run -d --name redis -p 6380:6379 redis:latest
# Then update application-redis.yml with port 6380
```

#### **3. Redis Memory Issues**
```bash
# Check Redis memory usage
redis-cli info memory

# Set memory limits in redis.conf
maxmemory 256mb
maxmemory-policy allkeys-lru
```

### **Debug Commands**
```bash
# Check Redis logs
docker logs redis

# Monitor Redis commands in real-time
redis-cli monitor

# Check Redis keys
redis-cli keys "*"

# Clear all Redis data
redis-cli flushall
```

## 📈 **Performance Comparison**

| Cache Type | Startup Time | Memory Usage | Persistence | Distributed |
|------------|--------------|--------------|-------------|-------------|
| In-Memory  | Fast         | Low          | No          | No          |
| Redis      | Medium       | Medium       | Yes         | Yes         |

## 🔄 **Switching Between Cache Types**

### **Development Environment**
```bash
# Quick development with in-memory cache
./mvnw spring-boot:run

# When Redis is needed
./mvnw spring-boot:run --spring.profiles.active=redis
```

### **Production Environment**
```bash
# Always use Redis in production
./mvnw spring-boot:run --spring.profiles.active=redis
```

## 📚 **Additional Resources**

- [Redis Documentation](https://redis.io/documentation)
- [Spring Boot Caching](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-caching)
- [Docker Redis](https://hub.docker.com/_/redis)

## 🎯 **Recommendations**

- **Development**: Use in-memory cache for quick iteration
- **Testing**: Use in-memory cache for CI/CD pipelines
- **Production**: Use Redis for scalability and persistence
- **Docker**: Use Docker Redis for consistent environments

