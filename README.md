# Café POS

Café POS is a web-based point-of-sale system for a single café outlet, built for the Advanced Software Engineering group project. A member of staff logs in, rings up a sale from the menu, and issues a receipt showing the 16% VAT breakdown and the change due. Returning customers are matched by phone number and registered on the spot at the till, so their name prints on the receipt and their past purchases can be looked up later. Managers additionally maintain the menu and prices, manage staff accounts, and authorise refunds — full or per-item — against same-day transactions. The application runs on an embedded H2 database with no setup, and can switch to MySQL through a Spring profile.

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
