package zm.cafe.pos.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The dashboard and the Customers list render inside the shared sidebar shell. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:shell-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"})
class ShellPagesTest {

    @Autowired private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void dashboardRendersWithChartData() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("weekly", "recentSales", "salesTotal"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sales this week")));
    }

    @Test
    @WithMockUser(username = "cashier", roles = "CASHIER")
    void customersListIsReachableByAnyStaff() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("rows", "customerCount"));
    }

    @Test
    void anonymousIsSentToLogin() throws Exception {
        mockMvc.perform(get("/customers")).andExpect(status().is3xxRedirection());
    }
}
