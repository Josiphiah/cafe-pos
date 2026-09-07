package zm.cafe.pos.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import zm.cafe.pos.domain.Category;
import zm.cafe.pos.domain.Product;
import zm.cafe.pos.repo.CategoryRepository;
import zm.cafe.pos.repo.ProductRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CatalogServiceTest {

    @Autowired CatalogService catalogService;
    @Autowired ProductRepository productRepository;
    @Autowired CategoryRepository categoryRepository;

    private Category category() {
        return categoryRepository.findByName("Test")
                .orElseGet(() -> categoryRepository.save(new Category("Test")));
    }

    private Product item(String name, String price) {
        return productRepository.save(new Product(name, category(), new BigDecimal(price)));
    }

    // --- price must be positive -------------------------------------------------

    @Test
    void create_rejectsZeroOrNegativePrice() {
        Long cat = category().getId();

        assertThrows(IllegalArgumentException.class,
                () -> catalogService.create("Flat White", cat, new BigDecimal("0.00")));
        assertThrows(IllegalArgumentException.class,
                () -> catalogService.create("Flat White", cat, new BigDecimal("-5.00")));
        assertThrows(IllegalArgumentException.class,
                () -> catalogService.create("Flat White", cat, null));
    }

    @Test
    void updatePrice_rejectsZeroOrNegativePrice() {
        Product p = item("C-Mocha", "32.00");

        assertThrows(IllegalArgumentException.class,
                () -> catalogService.updatePrice(p.getId(), new BigDecimal("0.00")));
        assertThrows(IllegalArgumentException.class,
                () -> catalogService.updatePrice(p.getId(), new BigDecimal("-1.00")));

        // rejected write must not have moved the stored price
        assertEquals(0, new BigDecimal("32.00").compareTo(
                productRepository.findById(p.getId()).orElseThrow().getPrice()));
    }

    @Test
    void create_persistsItemWithGivenPriceAndCategory() {
        Product saved = catalogService.create("C-Cortado", category().getId(), new BigDecimal("26.00"));

        Product reloaded = productRepository.findById(saved.getId()).orElseThrow();
        assertEquals("C-Cortado", reloaded.getName());
        assertEquals(0, new BigDecimal("26.00").compareTo(reloaded.getPrice()));
        assertEquals("Test", reloaded.getCategory().getName());
        assertTrue(reloaded.isAvailable(), "a new item is on the menu by default");
    }

    // --- retire hides the item -----------------------------------------------

    @Test
    void retire_removesItemFromAvailableListing() {
        Product p = item("C-Retire-Me", "40.00");
        assertTrue(catalogService.listAvailable().stream().anyMatch(x -> x.getId().equals(p.getId())));

        catalogService.retire(p.getId());

        assertFalse(catalogService.listAvailable().stream().anyMatch(x -> x.getId().equals(p.getId())),
                "retired item must drop off the till listing");
        assertFalse(productRepository.findById(p.getId()).orElseThrow().isAvailable());
        assertTrue(productRepository.findById(p.getId()).isPresent(),
                "retire is a soft-delete — the row still exists for sale history");
    }

    @Test
    void restore_bringsARetiredItemBack() {
        Product p = item("C-Seasonal", "38.00");
        catalogService.retire(p.getId());

        catalogService.restore(p.getId());

        assertTrue(catalogService.listAvailable().stream().anyMatch(x -> x.getId().equals(p.getId())));
    }

    // --- edit updates the price ---------------------------------------------

    @Test
    void edit_updatesStoredPrice() {
        Product p = item("C-Latte", "30.00");

        catalogService.edit(p.getId(), "C-Latte", p.getCategory().getId(), new BigDecimal("34.00"));

        assertEquals(0, new BigDecimal("34.00").compareTo(
                productRepository.findById(p.getId()).orElseThrow().getPrice()));
    }
}
