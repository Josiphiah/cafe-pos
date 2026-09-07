package zm.cafe.pos.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.datasource.url=jdbc:h2:mem:auth-test;DB_CLOSE_DELAY=-1", "spring.jpa.hibernate.ddl-auto=create-drop"})
class StaffSecurityTest {
    @Autowired private MockMvc mockMvc;

    @Test @WithMockUser(username = "cashier", roles = "CASHIER")
    void cashierCannotAccessStaffAdministration() throws Exception {
        mockMvc.perform(get("/staff")).andExpect(status().isForbidden());
    }

    @Test @WithMockUser(username = "manager", roles = "MANAGER")
    void managerCanAccessStaffAdministration() throws Exception {
        mockMvc.perform(get("/staff")).andExpect(status().isOk());
    }

    @Test
    void anonymousUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/staff")).andExpect(status().is3xxRedirection());
    }
}
