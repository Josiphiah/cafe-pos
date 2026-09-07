<!--
  COORDINATOR TODO (Josiphiah): fill in each section below, then delete this comment.
  This is the repo front page. Domain-model prose was moved to docs/03-domain-model.md.
-->

# Café POS

<!-- One short paragraph: what the system is and who it is for. -->
_TODO: overview._

## Tech stack

| Layer | Choice |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3 (Web, Data JPA, Security, Thymeleaf, Validation) |
| Database | H2 file-based by default (zero setup); MySQL 8 via the `mysql` profile |
| Build | Maven (wrapper included — `./mvnw`) |
| Tests | JUnit 5 + Spring Security Test |

## Running it

```
./mvnw clean spring-boot:run
```

On Windows PowerShell: `.\mvnw.cmd clean spring-boot:run`.
Then open <http://localhost:8080>. A file-based H2 database is created at
`./data/cafepos.mv.db` on first run and seeded with a demo menu and two logins:

| Username | Password | Role |
|---|---|---|
| `manager` | `manager123` | MANAGER |
| `cashier` | `cashier123` | CASHIER |

MySQL instead: create a database `cafepos`, then
`./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql`.

## Project structure

```
src/main/java/zm/cafe/pos/
  domain/     JPA entities (Staff, Category, Product, Customer, Sale, SaleLine, Refund, RefundLine)
  repo/       Spring Data repositories
  config/     security configuration + demo data seeding
  auth/       staff login & staff administration          (Issue #1 — Salifyanji)
  sales/      till, cart, checkout, receipts               (Issue #3 — Josiphiah)
  catalog/    menu & product management                    (Issue #2 — Goodson)
  history/    transaction history & refunds                (Issue #4 — Kenneth)
  customer/   customer profiles & purchase history         (Issue #5 — Vanessa)
docs/         project documentation                        (Issue #6 — Vanessa)
```

## Team

| Member | Role | GitHub | Issue |
|---|---|---|---|
| Josiphiah | Coordinator | @Josiphiah | #3 |
| Kenneth | Presenter | @KennethKM | #4 |
| Goodson | Quality Assurance | @Goodson-Jr | #2 |
| Salifyanji | Technical Lead | @salimupashi20-ai | #1 |
| Vanessa Banda | Documentation Lead | @vanessa200321 | #5, #6 |

## Documentation

See [`docs/`](docs/) — requirements, use cases, domain model, design class
diagram, system sequence diagrams, database design (ERD + 3NF), test plan,
user manual, and the final report.
