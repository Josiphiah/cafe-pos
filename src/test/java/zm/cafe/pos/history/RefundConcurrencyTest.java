package zm.cafe.pos.history;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import zm.cafe.pos.domain.*;
import zm.cafe.pos.repo.*;
import java.util.Map;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:refundconcurrency;DB_CLOSE_DELAY=-1", "spring.jpa.hibernate.ddl-auto=create-drop"})
class RefundConcurrencyTest {
    @Autowired RefundService service;
    @Autowired SaleRepository sales;
    @Autowired RefundRepository refunds;
    @Autowired StaffRepository staff;
    @Autowired ProductRepository products;
    @Autowired PlatformTransactionManager transactions;

    @Test void concurrentFullRefundsReturnEachUnitOnlyOnce() throws Exception {
        Sale sale = new TransactionTemplate(transactions).execute(status -> {
            Sale created = new Sale(staff.findByUsername("cashier").orElseThrow(), null);
            created.addLine(new SaleLine(products.findAll().getFirst(), 1));
            created.recalculateTotals();
            return sales.saveAndFlush(created);
        });
        CountDownLatch start = new CountDownLatch(1);
        Callable<Boolean> attempt = () -> {
            start.await();
            try {
                service.refund(sale.getId(), Map.of(sale.getLines().getFirst().getId(), 1), "Return", "cashier");
                return true;
            } catch (IllegalArgumentException expected) {
                return false;
            }
        };
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<Boolean> first = pool.submit(attempt);
            Future<Boolean> second = pool.submit(attempt);
            start.countDown();
            assertNotEquals(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
        }
        Sale updated = sales.findById(sale.getId()).orElseThrow();
        assertEquals(SaleStatus.REFUNDED, updated.getStatus());
        assertEquals(1, updated.getLines().getFirst().getRefundedQuantity());
        assertEquals(1, refunds.findBySaleIdOrderByRefundedAtDesc(sale.getId()).size());
    }
}
