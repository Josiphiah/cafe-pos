package zm.cafe.pos.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import zm.cafe.pos.domain.Customer;
import zm.cafe.pos.repo.CustomerRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:customer-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"})
class CustomerServiceTest {

    @Autowired CustomerService customerService;
    @Autowired CustomerRepository customerRepository;

    @Test
    void knownPhone_reusesExistingCustomer_ignoringTypedName() {
        Customer existing = customerRepository.save(new Customer("Chembe Zulu", "0955000111"));

        Customer result = customerService.findOrRegister("0955000111", "Someone Else");

        assertEquals(existing.getId(), result.getId());
        assertEquals("Chembe Zulu", result.getName());
    }

    @Test
    void newPhoneWithName_createsCustomer() {
        long before = customerRepository.count();

        Customer created = customerService.findOrRegister("0955222333", "Mercy Banda");

        assertNotNull(created.getId());
        assertEquals("Mercy Banda", created.getName());
        assertEquals("0955222333", created.getPhone());
        assertEquals(before + 1, customerRepository.count());
    }

    @Test
    void newPhoneWithoutName_isRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> customerService.findOrRegister("0955444555", "  "));
        assertThrows(IllegalArgumentException.class,
                () -> customerService.findOrRegister("0955444555", null));
    }

    @Test
    void findByPhone_trimsAndHandlesBlank() {
        customerRepository.save(new Customer("Trim Test", "0955666777"));

        assertTrue(customerService.findByPhone("  0955666777 ").isPresent());
        assertTrue(customerService.findByPhone("   ").isEmpty());
        assertTrue(customerService.findByPhone(null).isEmpty());
    }
}
