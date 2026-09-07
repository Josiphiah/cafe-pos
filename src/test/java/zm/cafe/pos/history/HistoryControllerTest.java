package zm.cafe.pos.history;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import zm.cafe.pos.domain.*;
import zm.cafe.pos.repo.*;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.security.test.context.support.WithAnonymousUser;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:historytests;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "cashier", roles = "CASHIER")
class HistoryControllerTest {
    @Autowired MockMvc mvc;
    @Autowired SaleRepository sales;
    @Autowired StaffRepository staff;
    @Autowired RefundRepository refunds;
    @Autowired ProductRepository products;

    private Sale saleAt(String date) {
        Sale sale = new Sale(staff.findByUsername("cashier").orElseThrow(), null);
        sale.setSoldAt(LocalDateTime.parse(date));
        return sales.saveAndFlush(sale);
    }

    @Test
    void dateFilterIncludesBothWholeDaysAndExcludesAdjacentDates() throws Exception {
        saleAt("2026-08-31T23:59:59");
        Sale first = saleAt("2026-09-01T00:00:00");
        Sale last = saleAt("2026-09-02T23:59:59.999999");
        saleAt("2026-09-03T00:00:00");
        mvc.perform(get("/history").param("from", "2026-09-01").param("to", "2026-09-02"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("sales", contains(last, first)))
                .andExpect(content().string(containsString("2026-09-02 23:59")));
    }

    @Test
    void supportsOpenEndedAndUnfilteredHistory() throws Exception {
        Sale first = saleAt("2026-09-01T12:00:00");
        Sale last = saleAt("2026-09-03T12:00:00");
        mvc.perform(get("/history").param("from", "2026-09-02"))
                .andExpect(model().attribute("sales", contains(last)));
        mvc.perform(get("/history").param("to", "2026-09-02"))
                .andExpect(model().attribute("sales", contains(first)));
        mvc.perform(get("/history"))
                .andExpect(model().attribute("sales", contains(last, first)));
    }

    @Test
    void reversedDatesShowHelpfulErrorAndMalformedDatesReturnBadRequest() throws Exception {
        mvc.perform(get("/history").param("from", "2026-09-03").param("to", "2026-09-01"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("sales", empty()));
        mvc.perform(get("/history").param("from", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void auditRendersRefundDetailsAndEscapesReason() throws Exception {
        Sale sale = saleAt("2026-09-01T12:00:00");
        Refund refund = refunds.saveAndFlush(new Refund(sale, sale.getCashier(), "<script>reason</script>"));
        mvc.perform(get("/history/refunds"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("refunds", contains(refund)))
                .andExpect(content().string(containsString("&lt;script&gt;reason&lt;/script&gt;")))
                .andExpect(content().string(containsString(sale.getCashier().getFullName())));
    }

    @Test
    void emptyAuditExplainsThereAreNoRefunds() throws Exception {
        mvc.perform(get("/history/refunds"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No refunds have been recorded.")));
    }

    @Test
    void saleDetailRefundFormAndSubmissionWorkTogether() throws Exception {
        Sale sale = saleAt(java.time.LocalDateTime.now().toString());
        sale.addLine(new SaleLine(products.findAll().getFirst(), 2));
        sale.recalculateTotals();
        sales.saveAndFlush(sale);
        String detail = "/history/" + sale.getId();
        String form = detail + "/refund";
        mvc.perform(get(detail)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Process refund")));
        mvc.perform(get(form)).andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        mvc.perform(post(form).param("reason", "Return").param("quantity_" + sale.getLines().getFirst().getId(), "1"))
                .andExpect(status().isForbidden());
        mvc.perform(post(form).with(csrf()).param("reason", "Return").param("quantity_" + sale.getLines().getFirst().getId(), "1"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl(detail));
        mvc.perform(get(detail)).andExpect(status().isOk())
                .andExpect(content().string(containsString("PARTIALLY_REFUNDED")))
                .andExpect(content().string(containsString("Return")));
        mvc.perform(post(form).with(csrf()).param("reason", "Return").param("quantity_" + sale.getLines().getFirst().getId(), "oops"))
                .andExpect(redirectedUrl(form)).andExpect(flash().attributeExists("error"));
    }

    @Test
    void missingSaleReturnsNotFound() throws Exception {
        mvc.perform(get("/history/9223372036854775807")).andExpect(status().isNotFound());
        mvc.perform(get("/history/9223372036854775807/refund")).andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void anonymousVisitorsMustLogIn() throws Exception {
        mvc.perform(get("/history/1")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(post("/history/1/refund").with(csrf())).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
