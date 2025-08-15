# Caching and Optimization Guide

This document outlines the caching and optimization strategies implemented in the Cupid application to improve API response times and database query performance.

## 🚀 Performance Optimizations Implemented

### 1. Redis Caching Layer
- **Cache Manager**: Configured with Redis for distributed caching
- **Cache Names**: 
  - `properties`: Hotel details (TTL: 60 minutes)
  - `hotels`: Hotel listings (TTL: 60 minutes)
  - `reviews`: Hotel reviews (TTL: 15 minutes)
  - `translations`: Hotel translations (TTL: 45 minutes)

### 2. Database Query Optimizations
- **JOIN FETCH**: Optimized queries to fetch related entities in single queries
- **Custom Queries**: Implemented specific queries for common operations
- **Pagination**: Added pagination support for large datasets
- **Batch Operations**: Configured Hibernate batch processing

### 3. Service Layer Caching
- **Method-level Caching**: Annotated service methods with `@Cacheable`
- **Cache Eviction**: Automatic cache invalidation on data updates
- **Read-only Transactions**: Optimized for read operations

## 📊 Cache Configuration

### Redis Settings
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
```

### Cache TTL Settings
- **Default**: 30 minutes
- **Properties**: 60 minutes
- **Reviews**: 15 minutes
- **Translations**: 45 minutes

## 🔧 API Endpoints

### Hotel Operations
- `GET /api/v1/hotels` - Get paginated hotel list (cached)
- `GET /api/v1/hotels/{hotelId}` - Get hotel details (cached)
- `GET /api/v1/hotels/search` - Search hotels by name/city (cached)

### Review Operations
- `GET /api/v1/hotels/{hotelId}/reviews` - Get all reviews (cached)
- `GET /api/v1/hotels/{hotelId}/reviews/top` - Get top reviews (cached)
- `GET /api/v1/hotels/{hotelId}/reviews/recent` - Get recent reviews (cached)

### Translation Operations
- `GET /api/v1/hotels/{hotelId}/translations` - Get all translations (cached)
- `GET /api/v1/hotels/{hotelId}/translations/{lang}` - Get specific language (cached)

### Cache Management
- `GET /api/cache/status` - View cache status
- `GET /api/cache/stats` - View cache statistics
- `POST /api/cache/clear/{cacheName}` - Clear specific cache
- `POST /api/cache/clear/all` - Clear all caches
- `POST /api/v1/cache/clear` - Clear all API caches

## 🗄️ Database Optimizations

### Hibernate Configuration
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc.batch_size: 20
        order_inserts: true
        order_updates: true
        batch_versioned_data: true
```

### Query Optimizations
- **Eager Loading**: Used `LEFT JOIN FETCH` to avoid N+1 queries
- **Indexed Queries**: Optimized queries for common search patterns
- **Pagination**: Implemented efficient pagination for large datasets

## 📈 Performance Benefits

### Before Optimization
- Multiple database queries per request
- No caching layer
- N+1 query problems
- Slower response times

### After Optimization
- Single optimized queries with JOIN FETCH
- Redis caching for frequent requests
- Eliminated N+1 query issues
- Significantly improved response times
- Reduced database load

## 🚀 Getting Started

### Prerequisites
1. Redis server running on localhost:6379
2. PostgreSQL database configured
3. Java 17+

### Running the Application
1. Start Redis server
2. Start PostgreSQL database
3. Run the Spring Boot application
4. Access API endpoints at `http://localhost:8080/api/v1/`

### Monitoring Cache Performance
- Check cache hit/miss rates in logs
- Monitor Redis memory usage
- Use cache management endpoints for insights

## 🔍 Cache Keys

### Property Cache Keys
- `{id}` - Individual property by ID
- `hotel_{hotelId}` - Property by hotel ID
- `search_{name}_{city}` - Search results

### Hotel List Cache Keys
- `page_{pageNumber}_{pageSize}` - Paginated hotel lists

### Review Cache Keys
- `hotel_{hotelId}` - All reviews for a hotel
- `hotel_{hotelId}_page_{page}_{size}` - Paginated reviews
- `hotel_{hotelId}_top_{limit}` - Top reviews
- `hotel_{hotelId}_recent_{limit}` - Recent reviews

### Translation Cache Keys
- `hotel_{hotelId}` - All translations for a hotel
- `hotel_{hotelId}_lang_{lang}` - Specific language translation
- `hotel_{hotelId}_recent` - Recent translations

## 🛠️ Troubleshooting

### Common Issues
1. **Redis Connection Failed**: Ensure Redis server is running
2. **Cache Not Working**: Check cache annotations and configuration
3. **Memory Issues**: Monitor Redis memory usage and adjust TTL settings

### Debug Mode
Enable debug logging for cache operations:
```yaml
logging:
  level:
    org.springframework.cache: DEBUG
```

## 📚 Best Practices

1. **Cache Strategy**: Use appropriate TTL based on data volatility
2. **Cache Keys**: Design meaningful and unique cache keys
3. **Cache Eviction**: Implement proper cache invalidation strategies
4. **Monitoring**: Regularly monitor cache performance and hit rates
5. **Memory Management**: Configure Redis memory limits appropriately

## 🔄 Future Enhancements

- **Cache Warming**: Pre-populate caches with frequently accessed data
- **Distributed Caching**: Implement Redis cluster for high availability
- **Cache Analytics**: Add detailed cache performance metrics
- **Smart Eviction**: Implement LRU or LFU eviction policies
- **Cache Compression**: Add compression for large cached objects

