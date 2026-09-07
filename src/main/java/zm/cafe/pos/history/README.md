# Kenneth's contribution — issue #4

Kenneth (Presenter) implemented transaction retrieval, the date filter, sale detail,
full and per-line partial refunds, and the refunds audit for issue #4.

## Demo

1. Start the application with Java 21 and `./mvnw spring-boot:run` (Windows: `mvnw.cmd`).
2. Log in as a staff member and open **Transactions**.
3. Set **From** and **To**, then select **Filter transactions**. Both dates include
   the entire day. Either date may be blank; **Clear filters** restores all sales.
4. Select **Refunds audit** to show every recorded refund, newest first, including
   its sale ID, amount in K, reason, staff name/username, and timestamp.

5. Open a sale ID to view its original totals, customer, items, remaining quantities
   and refund history. For a sale made today, select **Process refund**.
6. Enter a return quantity per item and a reason. Returning every remaining unit
   completes a full refund; returning fewer units records a partial refund.
7. Submit and inspect the updated status and audit record. Return the recorded
   amount to the customer; this application records refunds, it does not integrate
   with a payment provider.

Refunds are allowed only on the sale's calendar date, using the application's
server-local clock consistently with sale timestamps. Run the cafe server in its
local trading timezone. Empty pages show explanatory messages.

## Explanation for the presentation

- Spring MVC binds ISO calendar dates from the GET query parameters.
- Spring Data JPA filters stored sale timestamps and sorts newest first.
- Thymeleaf renders the tables and escapes recorded text such as refund reasons.
- The existing Spring Security configuration requires staff login for both pages.
- Refund records keep their amount, reason, processing staff and timestamp; the
  audit page is read-only. Original sale totals stay intact; sale detail shows the
  refunded amount and retained balance separately.
- `RefundService` validates active staff, same-day eligibility, reason length,
  sale-line ownership and remaining quantities. A database write lock serializes
  refunds for each sale; one transaction saves the audit and quantity/status updates.
- Prices come from the original sale-line snapshot. Status moves from `COMPLETED`
  to `PARTIALLY_REFUNDED` or `REFUNDED`, then to `REFUNDED` when nothing remains.
- `HistoryControllerTest` exercises date boundaries, one-sided filters, invalid
  ranges, and rendered audit data against an isolated in-memory H2 database.

`RefundServiceTest` covers full/partial returns, original prices, eligibility and
invalid input; `RefundConcurrencyTest` submits simultaneous full-refund requests
and verifies that only one succeeds. Tests use H2; MySQL has not been exercised.

Run the focused checks with
`./mvnw "-Dtest=HistoryControllerTest,RefundServiceTest,RefundConcurrencyTest" test`.
