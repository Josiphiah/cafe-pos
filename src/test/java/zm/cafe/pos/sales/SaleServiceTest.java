package zm.cafe.pos.sales;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import zm.cafe.pos.domain.*;
import zm.cafe.pos.repo.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SaleServiceTest {

    @Autowired SaleService saleService;
    @Autowired ProductRepository productRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired StaffRepository staffRepository;

    private Staff cashier() {
        return staffRepository.findByUsername("cashier").orElseGet(() ->
                staffRepository.save(new Staff("cashier", "x", "Front Till", Role.CASHIER)));
    }

    private Product product(String name, String price) {
        Category cat = categoryRepository.findByName("Test")
                .orElseGet(() -> categoryRepository.save(new Category("Test")));
        return productRepository.save(new Product(name, cat, new BigDecimal(price)));
    }

    @Test
    void twoLineSale_computesVatInclusiveTotals() {
        Product espresso = product("T-Espresso", "18.00");
        Product muffin = product("T-Muffin", "22.00");

        Map<Long, Integer> items = new LinkedHashMap<>();
        items.put(espresso.getId(), 2);   // 36.00
        items.put(muffin.getId(), 1);     // 22.00

        Sale sale = saleService.checkout(items, null, cashier());

        assertEquals(0, new BigDecimal("58.00").compareTo(sale.getTotal()), "gross total");
        assertEquals(0, new BigDecimal("50.00").compareTo(sale.getSubtotal()), "net of 16% VAT");
        assertEquals(0, new BigDecimal("8.00").compareTo(sale.getVatAmount()), "VAT portion");
        assertEquals(SaleStatus.COMPLETED, sale.getStatus());
        assertEquals(2, sale.getLines().size());
        assertNull(sale.getCustomer());
    }

    @Test
    void saleLine_snapshotsNameAndPrice() {
        Product p = product("T-Latte", "30.00");
        Sale sale = saleService.checkout(Map.of(p.getId(), 1), null, cashier());

        SaleLine line = sale.getLines().get(0);
        assertEquals("T-Latte", line.getProductName());
        assertEquals(0, new BigDecimal("30.00").compareTo(line.getUnitPrice()));

        // Change the catalogue price afterwards — the sale line must not move.
        p.setPrice(new BigDecimal("99.00"));
        productRepository.save(p);
        assertEquals(0, new BigDecimal("30.00").compareTo(line.getUnitPrice()));
    }

    @Test
    void emptyCart_isRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> saleService.checkout(Map.of(), null, cashier()));
    }
}
