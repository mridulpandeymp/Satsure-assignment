# In-Memory Cache System (Spring Boot)

A production-grade, thread-safe, in-memory caching solution built using **Spring Boot**.  
Supports custom TTL (Time-To-Live), LRU (Least Recently Used) eviction policy, scheduled cleanup of expired entries, and RESTful APIs to interact with the cache.


## Dependencies

| Dependency              | Purpose                              |
|-------------------------|--------------------------------------|
| Spring Boot Starter Web | REST API support                     |
| Spring Boot Starter     | Core Spring Boot features            |
| Spring Boot Starter Test| JUnit-based testing                  |
| Jakarta Annotations     | Lifecycle hooks (`@PostConstruct`)  |

### `pom.xml` Snippet
```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```


## How to Run

### Step 1: Clone the Repository
```bash
git clone https://github.com/your-username/in-memory-cache-springboot.git
cd in-memory-cache-springboot
```

### Step 2: Build the Project
```bash
mvn clean install
```

### Step 3: Run the Application
```bash
mvn spring-boot:run
```
Service will start at: http://localhost:8080/cache


## Configuration 

### `application.yml`
```yaml
spring:
  application:
    name: cache-service
  main:
    allow-bean-definition-overriding: true

cache:
  max-size: 1000
  default-ttl: 300
```


## Design Decisions

* Custom cache with HashMap + Doubly Linked List

* LRU Eviction: Removes least recently used entry on overflow

* TTL Expiry: Entry-specific or default expiration

* Thread-safety using ReentrantReadWriteLock

* Periodic cleanup via Spring scheduler

* REST API for external access and control

  
## Concurrency Model

* Thread-safe using ReentrantReadWriteLock

* readLock() for read-only access

* writeLock() for put/delete operations

* Prevents race conditions in high-concurrency environments

## Eviction Logic (LRU)

* If cache.size() > maxSize, remove the tail node from the doubly linked list

* Most recently accessed entries are moved to the head

* LRU logic ensures optimal memory usage under pressure


## Expiry Logic

Each entry tracks an expiryTime (createdAt + ttl)
Entries are considered invalid if:
```Java
System.currentTimeMillis() > expiryTime
```
Spring @Scheduled job runs every 10 seconds to clean expired entries


## Sample Stats Output (`GET /cache/stats`)

```JSON
{
  "hits": 12,
  "misses": 4,
  "hit_rate": 0.75,
  "total_requests": 16,
  "current_size": 5,
  "evictions": 3,
  "expired_removals": 2
}
```


## Performance Considerations

| Concern           | Strategy                                    |
| ----------------- | ------------------------------------------- |
| High Read Volume  | `readLock()` allows concurrent safe reads   |
| High Write Volume | Exclusive `writeLock()` ensures consistency |
| Large Cache Size  | Efficient doubly-linked pointer operations  |
| Expired Entries   | Periodic cleanup + lazy eviction on get()   |



## REST API Endpoints

| Method | Endpoint       | Description                 |
| ------ | -------------- | --------------------------- |
| PUT    | `/cache`       | Add/update a cache entry    |
| GET    | `/cache/{key}` | Fetch a value by key        |
| DELETE | `/cache/{key}` | Remove a specific cache key |
| DELETE | `/cache`       | Clear the entire cache      |
| GET    | `/cache/stats` | View cache statistics       |


## Test Scenarios

### 1. Basic Operations
```Java
cache.put("config:db_host", "localhost:5432");
cache.put("config:api_key", "abc123", 60);
```
### 2. Eviction Test
```Java
for (int i = 0; i < 1200; i++) {
    cache.put("data:" + i, "value_" + i);
}
```
### 3. Expiration Test
```Java
cache.put("temp_data", "expires_soon", 2);
Thread.sleep(3000);
assert cache.get("temp_data") == null;
```
### 4. Concurrent Access Test
```Java
Runnable task = () -> {
    for (int i = 0; i < 100; i++) {
        cache.put("thread:" + Thread.currentThread().getId() + ":item:" + i, "value");
        cache.get("thread:" + Thread.currentThread().getId() + ":item:" + (i / 2));
    }
};
```


## Running Tests
```bash
mvn test
```
### Tests cover:

* TTL expiration

* LRU eviction

* Thread-safe operations

* Stats accuracy


## Sample Logs
```text
[INFO] Cleanup job running every 10s...
[INFO] Removed expired key: temp_data
[INFO] LRU eviction: key=data:0
[INFO] Cache Stats => Hits: 8, Misses: 2, Hit Rate: 0.80
```
To enable debug logs:
```yaml
logging:
  level:
    com:
      example:
        cache: DEBUG
```
