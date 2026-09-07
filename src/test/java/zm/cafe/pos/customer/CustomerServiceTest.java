package zm.cafe.pos.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import zm.cafe.pos.domain.*;
import zm.cafe.pos.repo.CategoryRepository;
import zm.cafe.pos.repo.CustomerRepository;
import zm.cafe.pos.repo.ProductRepository;
import zm.cafe.pos.repo.SaleRepository;
import zm.cafe.pos.repo.StaffRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:customer-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"})
class CustomerServiceTest {

    @Autowired CustomerService customerService;
    @Autowired CustomerRepository customerRepository;
    @Autowired SaleRepository saleRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired StaffRepository staffRepository;

    private Sale saleFor(Customer c, String price) {
        Category cat = categoryRepository.findByName("CT").orElseGet(() -> categoryRepository.save(new Category("CT")));
        Product p = productRepository.save(new Product("P-" + price, cat, new BigDecimal(price)));
        Staff cashier = staffRepository.findByUsername("cashier").orElseThrow();
        Sale sale = new Sale(cashier, c);
        sale.addLine(new SaleLine(p, 1));
        sale.recalculateTotals();
        return saleRepository.saveAndFlush(sale);
    }

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

    // ---- Customers-screen registration (issue #5) --------------------------

    @Test
    void register_createsCustomer_phoneOptional() {
        Customer withPhone = customerService.register("Grace Phiri", "0966001122");
        Customer noPhone = customerService.register("Walk-in Regular", "  ");

        assertNotNull(withPhone.getId());
        assertEquals("0966001122", withPhone.getPhone());
        assertNotNull(noPhone.getId());
        assertNull(noPhone.getPhone());
    }

    @Test
    void register_rejectsBlankNameAndDuplicatePhone() {
        customerService.register("First Owner", "0966333444");

        assertThrows(IllegalArgumentException.class, () -> customerService.register("  ", "0966999888"));
        assertThrows(IllegalArgumentException.class, () -> customerService.register("Second Owner", "0966333444"));
    }

    // ---- purchase history (issue #5) -------------------------------------------

    @Test
    void purchaseHistory_returnsOnlyThatCustomersSales_mostRecentFirst() {
        Customer a = customerRepository.save(new Customer("Customer A", "0955010101"));
        Customer b = customerRepository.save(new Customer("Customer B", "0955020202"));
        saleFor(a, "10.00");
        saleFor(a, "20.00");
        saleFor(b, "99.00");

        var historyA = customerService.purchaseHistory(a.getId());
        assertEquals(2, historyA.size());
        assertTrue(historyA.stream().allMatch(s -> s.getCustomer().getId().equals(a.getId())));
        assertTrue(!historyA.get(0).getSoldAt().isBefore(historyA.get(1).getSoldAt()), "most recent first");
    }

    @Test
    void lifetimeSpend_sumsTheCustomersSaleTotals() {
        Customer c = customerRepository.save(new Customer("Big Spender", "0955030303"));
        saleFor(c, "12.50");
        saleFor(c, "37.50");

        assertEquals(0, new BigDecimal("50.00").compareTo(customerService.lifetimeSpend(c.getId())));
    }

    @Test
    void profile_reportsCountSpendAndLastVisit() {
        Customer c = customerRepository.save(new Customer("Profile Test", "0955040404"));
        saleFor(c, "15.00");
        Sale latest = saleFor(c, "25.00");

        var profile = customerService.profile(c.getId());
        assertNotNull(profile);
        assertEquals(2, profile.purchaseCount());
        assertEquals(0, new BigDecimal("40.00").compareTo(profile.lifetimeSpend()));
        assertEquals(latest.getSoldAt(), profile.lastVisit());
        assertNull(customerService.profile(Long.MAX_VALUE), "unknown id -> null");
    }
}
