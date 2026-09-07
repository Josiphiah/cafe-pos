# Kenneth's contribution — issue #4

Kenneth (Presenter) implemented the refunds audit page and transaction date filter
reserved in `CONTRIBUTING.md`.

## Demo

1. Start the application with Java 21 and `./mvnw spring-boot:run` (Windows: `mvnw.cmd`).
2. Log in as a staff member and open **Transactions**.
3. Set **From** and **To**, then select **Filter transactions**. Both dates include
   the entire day. Either date may be blank; **Clear filters** restores all sales.
4. Select **Refunds audit** to show every recorded refund, newest first, including
   its sale ID, amount in K, reason, staff name/username, and timestamp.

The audit reads existing refund records. Refund creation and transaction detail
remain part of the coordinator's underlying history/refund implementation.
An empty database shows an explanatory message on each page.

## Explanation for the presentation

- Spring MVC binds ISO calendar dates from the GET query parameters.
- Spring Data JPA filters stored sale timestamps and sorts newest first.
- Thymeleaf renders the tables and escapes recorded text such as refund reasons.
- The existing Spring Security configuration requires staff login for both pages.
- Refund records keep their amount, reason, processing staff and timestamp; the
  audit page is read-only and does not change transaction totals.
- `HistoryControllerTest` exercises date boundaries, one-sided filters, invalid
  ranges, and rendered audit data against an isolated in-memory H2 database.

Run the focused checks with `./mvnw -Dtest=HistoryControllerTest test`.
