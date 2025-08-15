# Test Suite Documentation

This document provides comprehensive information about the test suite for the Cupid backend application.

## Overview

The test suite is designed to provide comprehensive coverage of the application's functionality, including unit tests, integration tests, and end-to-end testing scenarios. The tests follow best practices for Spring Boot applications and use modern testing frameworks.

## Test Structure

### Test Types

1. **Unit Tests** - Test individual components in isolation with mocked dependencies
2. **Integration Tests** - Test component interactions with real database and Spring context
3. **Controller Tests** - Test REST API endpoints with MockMvc
4. **Service Tests** - Test business logic with mocked repositories
5. **Repository Tests** - Test data access layer with real database

### Test Organization

```
src/test/java/com/example/cupid/
├── controller/
│   ├── PropertyControllerTest.java          # Unit tests for PropertyController
│   ├── PropertyControllerIntegrationTest.java # Integration tests for PropertyController
│   └── CacheControllerTest.java             # Unit tests for CacheController
├── service/
│   ├── CupidFetchServiceTest.java           # Unit tests for CupidFetchService
│   ├── PropertyServiceTest.java             # Unit tests for PropertyService
│   ├── PropertyServiceIntegrationTest.java  # Integration tests for PropertyService
│   └── CacheServiceTest.java                # Unit tests for CacheService
├── client/
│   └── CupidApiClientTest.java              # Unit tests for CupidApiClient
└── utils/
    └── TestDataBuilder.java                 # Test data builder utilities
```

## Test Configuration

### Test Profile
- **Profile**: `test`
- **Database**: H2 in-memory database
- **Cache**: Simple in-memory cache
- **Flyway**: Disabled for tests

### Test Dependencies
- Spring Boot Test Starter
- H2 Database (test scope)
- AssertJ for assertions
- Reactor Test for reactive testing
- Mockito for mocking

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=PropertyControllerTest
```

### Run Tests with Specific Profile
```bash
mvn test -Dspring.profiles.active=test
```

### Run Integration Tests Only
```bash
mvn test -Dtest="*IntegrationTest"
```

### Run Unit Tests Only
```bash
mvn test -Dtest="*Test" -Dtest="!*IntegrationTest"
```

## Test Coverage

### Unit Tests

#### CupidFetchServiceTest
- **Purpose**: Test the main service responsible for fetching and saving hotel data
- **Coverage**: 
  - Successful property fetch and save
  - Error handling for API failures
  - Translation and review fetching
  - Complex data mapping scenarios
  - Transaction rollback on errors

#### PropertyServiceTest
- **Purpose**: Test the property service business logic
- **Coverage**:
  - Hotel retrieval with pagination
  - Search functionality (by name, city)
  - Caching behavior
  - Error scenarios

#### CacheServiceTest
- **Purpose**: Test cache management functionality
- **Coverage**:
  - Cache hit/miss scenarios
  - Cache eviction
  - Cache loading with fallback
  - Error handling

#### CupidApiClientTest
- **Purpose**: Test the external API client
- **Coverage**:
  - API connection testing
  - Property data fetching
  - Translation fetching
  - Review fetching
  - Error handling and retry logic

#### PropertyControllerTest
- **Purpose**: Test REST API endpoints
- **Coverage**:
  - Property import endpoint
  - Property retrieval endpoint
  - Property refresh endpoint
  - Error handling and validation

#### CacheControllerTest
- **Purpose**: Test cache management endpoints
- **Coverage**:
  - Cache status endpoint
  - Cache clearing endpoints
  - Cache statistics endpoint

### Integration Tests

#### PropertyControllerIntegrationTest
- **Purpose**: Test full API stack with real database
- **Coverage**:
  - End-to-end API testing
  - Database integration
  - Request/response validation
  - Error scenarios

#### PropertyServiceIntegrationTest
- **Purpose**: Test service layer with real database and caching
- **Coverage**:
  - Database transaction testing
  - Caching integration
  - Complex query scenarios
  - Data persistence verification

## Test Data Management

### TestDataBuilder
A utility class that provides factory methods for creating consistent test data:

```java
// Create a test property
Property property = TestDataBuilder.createTestProperty(
    1270324L, "Test Hotel", "Paris", 4, BigDecimal.valueOf(8.5), 100
);

// Create mock JSON responses
JsonNode propertyData = TestDataBuilder.createMockPropertyJsonNode(1270324L, "Test Hotel");
JsonNode reviewsData = TestDataBuilder.createMockReviewsJsonNode();
```

### Test Data Strategy
- **Isolation**: Each test creates its own data
- **Consistency**: TestDataBuilder ensures consistent test data
- **Cleanup**: @Transactional ensures test data cleanup
- **Realism**: Test data mimics real-world scenarios

## Testing Best Practices

### 1. Test Naming Convention
```java
@Test
void methodName_Scenario_ExpectedResult() {
    // test implementation
}
```

### 2. Arrange-Act-Assert Pattern
```java
@Test
void getProperty_Success() {
    // Arrange
    when(repository.findById(1270324L)).thenReturn(Optional.of(property));
    
    // Act
    ResponseEntity<Property> response = controller.getProperty(1270324L);
    
    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
}
```

### 3. Mocking Strategy
- Mock external dependencies (APIs, databases)
- Use real implementations for internal components
- Verify interactions with mocks

### 4. Assertion Strategy
- Use AssertJ for readable assertions
- Test both positive and negative scenarios
- Verify side effects and interactions

## Performance Testing

### Test Execution Time
- Unit tests: < 1 second each
- Integration tests: < 5 seconds each
- Full test suite: < 2 minutes

### Memory Usage
- H2 in-memory database: ~50MB
- Test data: ~10MB
- Total test memory: ~100MB

## Continuous Integration

### GitHub Actions Integration
The test suite is designed to run in CI/CD pipelines:

```yaml
- name: Run Tests
  run: mvn test
```

### Test Reports
- JUnit 5 test reports
- Coverage reports (if configured)
- Test execution time tracking

## Troubleshooting

### Common Issues

1. **H2 Database Connection Issues**
   - Ensure H2 dependency is in test scope
   - Check test profile configuration

2. **Mock Issues**
   - Verify mock setup in @BeforeEach
   - Check mock verification calls

3. **Transaction Issues**
   - Ensure @Transactional on test classes
   - Check test isolation

4. **Cache Issues**
   - Clear cache before tests
   - Verify cache configuration

### Debug Mode
Run tests with debug logging:
```bash
mvn test -Dlogging.level.com.example.cupid=DEBUG
```

## Future Enhancements

### Planned Improvements
1. **Performance Tests**: Add load testing scenarios
2. **Contract Tests**: Add API contract testing
3. **Security Tests**: Add authentication/authorization tests
4. **Database Tests**: Add specific database constraint tests
5. **Cache Tests**: Add cache performance tests

### Test Coverage Goals
- **Line Coverage**: > 90%
- **Branch Coverage**: > 85%
- **Integration Coverage**: > 95%

## Conclusion

This comprehensive test suite ensures the reliability and maintainability of the Cupid backend application. The tests cover all major functionality and provide confidence in the application's behavior across different scenarios.

For questions or issues with the test suite, please refer to the troubleshooting section or contact the development team.
