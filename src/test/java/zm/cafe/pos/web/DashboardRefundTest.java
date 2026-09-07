package zm.cafe.pos.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import zm.cafe.pos.HomeController;
import zm.cafe.pos.domain.*;
import zm.cafe.pos.history.RefundService;
import zm.cafe.pos.repo.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:dashboardrefund;DB_CLOSE_DELAY=-1", "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "cashier", roles = "CASHIER")
class DashboardRefundTest {
    @Autowired MockMvc mvc;
    @Autowired SaleRepository sales;
    @Autowired StaffRepository staff;
    @Autowired ProductRepository products;
    @Autowired CategoryRepository categories;
    @Autowired RefundService refunds;

    private Sale sale(int quantity, int daysAgo) {
        Category category = categories.findByName("Dashboard test").orElseGet(() -> categories.save(new Category("Dashboard test")));
        Product product = products.save(new Product("Dashboard item", category, new BigDecimal("10.00")));
        Sale sale = new Sale(staff.findByUsername("cashier").orElseThrow(), null);
        sale.setSoldAt(LocalDateTime.now().minusDays(daysAgo));
        sale.addLine(new SaleLine(product, quantity));
        sale.recalculateTotals();
        return sales.saveAndFlush(sale);
    }

    @SuppressWarnings("unchecked")
    private void assertDashboard(String expectedTotal, String expectedWeek, Sale sale, String expectedNet) throws Exception {
        var model = mvc.perform(get("/")).andExpect(status().isOk()).andReturn().getModelAndView().getModel();
        assertEquals(0, new BigDecimal(expectedTotal).compareTo((BigDecimal) model.get("salesTotal")));
        assertEquals(0, new BigDecimal(expectedWeek).compareTo((BigDecimal) model.get("weekTotal")));
        var weekly = (List<HomeController.Bar>) model.get("weekly");
        assertEquals(0, new BigDecimal(expectedWeek).compareTo(weekly.getLast().amount()));
        assertEquals(2, model.get("salesCount"));
        var net = (Map<Long, BigDecimal>) model.get("netBySale");
        assertEquals(0, new BigDecimal(expectedNet).compareTo(net.get(sale.getId())));
    }

    @Test void dashboardDeductsSuccessivePartialAndFullRefundsWithoutChangingReceipts() throws Exception {
        sale(2, 8); // Outside weekly chart, still included in lifetime total.
        Sale current = sale(3, 0);
        Long lineId = current.getLines().getFirst().getId();
        assertDashboard("50.00", "30.00", current, "30.00");
        refunds.refund(current.getId(), Map.of(lineId, 1), "Partial return", "cashier");
        assertDashboard("40.00", "20.00", current, "20.00");
        refunds.refund(current.getId(), Map.of(lineId, 2), "Remaining return", "cashier");
        assertDashboard("20.00", "0.00", current, "0.00");
        assertEquals(new BigDecimal("30.00"), current.getTotal());
        assertEquals(SaleStatus.REFUNDED, current.getStatus());
    }
}
