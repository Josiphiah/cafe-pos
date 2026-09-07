package zm.cafe.pos.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The menu screen is manager-only; a cashier must be refused. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.datasource.url=jdbc:h2:mem:catalog-sec-test;DB_CLOSE_DELAY=-1", "spring.jpa.hibernate.ddl-auto=create-drop"})
class CatalogSecurityTest {

    @Autowired private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "cashier", roles = "CASHIER")
    void cashierCannotAccessMenuManagement() throws Exception {
        mockMvc.perform(get("/catalog")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void managerCanAccessMenuManagement() throws Exception {
        mockMvc.perform(get("/catalog")).andExpect(status().isOk());
    }

    @Test
    void anonymousUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/catalog")).andExpect(status().is3xxRedirection());
    }
}
