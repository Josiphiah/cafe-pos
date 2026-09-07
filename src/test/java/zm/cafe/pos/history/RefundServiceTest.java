package zm.cafe.pos.history;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import zm.cafe.pos.domain.*;
import zm.cafe.pos.repo.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:refundtests;DB_CLOSE_DELAY=-1", "spring.jpa.hibernate.ddl-auto=create-drop"})
@Transactional
class RefundServiceTest {
    @Autowired RefundService service;
    @Autowired SaleRepository sales;
    @Autowired RefundRepository refunds;
    @Autowired StaffRepository staff;
    @Autowired ProductRepository products;
    @Autowired CategoryRepository categories;

    private Sale sale() {
        Category category = categories.save(new Category("Refund test"));
        Product product = products.save(new Product("Refund item", category, new BigDecimal("11.60")));
        Sale sale = new Sale(staff.findByUsername("cashier").orElseThrow(), null);
        sale.addLine(new SaleLine(product, 3));
        sale.recalculateTotals();
        return sales.saveAndFlush(sale);
    }

    @Test void partialThenRemainingRefundUpdatesQuantitiesStatusAndAudit() {
        Sale sale = sale();
        SaleLine line = sale.getLines().getFirst();
        Refund first = service.refund(sale.getId(), Map.of(line.getId(), 1), " Damaged item ", "cashier");
        assertEquals(new BigDecimal("11.60"), first.getAmount());
        assertEquals("Damaged item", first.getReason());
        assertEquals("cashier", first.getProcessedBy().getUsername());
        assertNotNull(first.getRefundedAt());
        assertEquals(1, line.getRefundedQuantity());
        assertEquals(SaleStatus.PARTIALLY_REFUNDED, sale.getStatus());
        Refund second = service.refund(sale.getId(), Map.of(line.getId(), 2), "Return remaining", "cashier");
        assertEquals(new BigDecimal("23.20"), second.getAmount());
        assertEquals(0, line.getRefundableQuantity());
        assertEquals(SaleStatus.REFUNDED, sale.getStatus());
        assertEquals(new BigDecimal("34.80"), sale.getTotal());
        assertEquals(2, refunds.findBySaleIdOrderByRefundedAtDesc(sale.getId()).size());
        assertThrows(IllegalArgumentException.class, () -> service.refund(sale.getId(), Map.of(line.getId(), 1), "Duplicate", "cashier"));
    }

    @Test void fullRefundUsesStoredPriceEvenIfCatalogChanges() {
        Sale sale = sale();
        SaleLine line = sale.getLines().getFirst();
        line.getProduct().setPrice(new BigDecimal("99.00"));
        Refund refund = service.refund(sale.getId(), Map.of(line.getId(), 3), "Full return", "cashier");
        assertEquals(sale.getTotal(), refund.getAmount());
        assertEquals(SaleStatus.REFUNDED, sale.getStatus());
    }

    @Test void selectedLineRefundLeavesOtherLinesUntouched() {
        Sale sale = sale();
        SaleLine first = sale.getLines().getFirst();
        sale.addLine(new SaleLine(first.getProduct(), 2));
        sale.recalculateTotals();
        sales.saveAndFlush(sale);
        SaleLine other = sale.getLines().getLast();
        service.refund(sale.getId(), Map.of(first.getId(), 3), "Return first line", "cashier");
        assertEquals(3, first.getRefundedQuantity());
        assertEquals(0, other.getRefundedQuantity());
        assertEquals(SaleStatus.PARTIALLY_REFUNDED, sale.getStatus());
        assertThrows(IllegalArgumentException.class, () -> service.refund(sale.getId(),
                Map.of(first.getId(), 1, other.getId(), 1), "Invalid mixed return", "cashier"));
        assertEquals(0, other.getRefundedQuantity());
        assertEquals(1, refunds.count());
    }

    @Test void rejectsPastAndFutureSales() {
        Sale sale = sale();
        for (int days : new int[]{-1, 1}) {
            sale.setSoldAt(LocalDateTime.now().plusDays(days));
            assertThrows(IllegalArgumentException.class, () -> service.refund(sale.getId(), Map.of(sale.getLines().getFirst().getId(), 1), "Return", "cashier"));
        }
        assertEquals(0, refunds.count());
    }

    @Test void rejectsInvalidQuantitiesReasonsAndForeignLinesWithoutChanges() {
        Sale sale = sale();
        Long line = sale.getLines().getFirst().getId();
        for (Map<Long, Integer> quantities : java.util.List.of(Map.<Long,Integer>of(), Map.of(line, 0), Map.of(line, -1), Map.of(line, 4), Map.of(Long.MAX_VALUE, 1))) {
            assertThrows(IllegalArgumentException.class, () -> service.refund(sale.getId(), quantities, "Return", "cashier"));
        }
        for (String reason : new String[]{null, " ", "x".repeat(201)}) {
            assertThrows(IllegalArgumentException.class, () -> service.refund(sale.getId(), Map.of(line, 1), reason, "cashier"));
        }
        assertEquals(0, refunds.count());
        assertEquals(0, sale.getLines().getFirst().getRefundedQuantity());
        assertEquals(SaleStatus.COMPLETED, sale.getStatus());
    }

    @Test void requiresActiveStaffAndExistingSale() {
        Sale sale = sale();
        Map<Long,Integer> quantities = Map.of(sale.getLines().getFirst().getId(), 1);
        assertThrows(ResponseStatusException.class, () -> service.refund(sale.getId(), quantities, "Return", null));
        assertThrows(ResponseStatusException.class, () -> service.refund(Long.MAX_VALUE, quantities, "Return", "cashier"));
        staff.findByUsername("cashier").orElseThrow().setActive(false);
        assertThrows(ResponseStatusException.class, () -> service.refund(sale.getId(), quantities, "Return", "cashier"));
    }
}
