# Café POS

Advanced Software Engineering Project — Café Point of Sale.

Stack: Java 17 · Spring Boot 3.3 · Spring Data JPA · Spring Security · Thymeleaf · H2 (default) / MySQL.

## Run

```
mvn spring-boot:run
```

Opens on http://localhost:8080 . Uses a file-based H2 database at `./data/cafepos.mv.db` — no setup needed.

To use MySQL instead:

```
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```
