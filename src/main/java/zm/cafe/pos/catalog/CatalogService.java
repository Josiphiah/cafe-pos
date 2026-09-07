package zm.cafe.pos.catalog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.cafe.pos.domain.Category;
import zm.cafe.pos.domain.Product;
import zm.cafe.pos.repo.CategoryRepository;
import zm.cafe.pos.repo.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Menu &amp; product management for managers: add items, change their price or
 * category, and retire items that are no longer sold. Retiring is a soft-delete
 * ({@link Product#isAvailable()} flag) so historical sale lines keep pointing at
 * a real product.
 */
@Service
public class CatalogService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public CatalogService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /** Items currently on the menu, name order — what the till shows. */
    public List<Product> listAvailable() {
        return productRepository.findByAvailableTrueOrderByNameAsc();
    }

    /** Every item including retired ones — what the management screen shows. */
    public List<Product> listAll() {
        return productRepository.findAll();
    }

    public List<Category> categories() {
        return categoryRepository.findAll();
    }

    /** Add a new category. Name must be non-blank and not already in use. */
    @Transactional
    public Category createCategory(String name) {
        String cleanName = requireName(name);
        categoryRepository.findByName(cleanName).ifPresent(c -> {
            throw new IllegalArgumentException("There is already a category called " + cleanName);
        });
        return categoryRepository.save(new Category(cleanName));
    }

    /** Add a new menu item. Price must be greater than zero. */
    @Transactional
    public Product create(String name, Long categoryId, BigDecimal price) {
        String cleanName = requireName(name);
        Category category = requireCategory(categoryId);
        requirePositivePrice(price);
        return productRepository.save(new Product(cleanName, category, price));
    }

    /** Change an existing item's name, category and price in one edit. */
    @Transactional
    public Product edit(Long productId, String name, Long categoryId, BigDecimal price) {
        Product product = require(productId);
        product.setName(requireName(name));
        product.setCategory(requireCategory(categoryId));
        requirePositivePrice(price);
        product.setPrice(price);
        return productRepository.save(product);
    }

    /** Convenience for the "just re-price this item" case. */
    @Transactional
    public Product updatePrice(Long productId, BigDecimal price) {
        Product product = require(productId);
        requirePositivePrice(price);
        product.setPrice(price);
        return productRepository.save(product);
    }

    /** Take an item off the menu without deleting its history. */
    @Transactional
    public Product retire(Long productId) {
        Product product = require(productId);
        product.setAvailable(false);
        return productRepository.save(product);
    }

    /** Put a retired item back on the menu. */
    @Transactional
    public Product restore(Long productId) {
        Product product = require(productId);
        product.setAvailable(true);
        return productRepository.save(product);
    }

    private Product require(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown product " + productId));
    }

    private Category requireCategory(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("A product must belong to a category");
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown category " + categoryId));
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A product must have a name");
        }
        return name.trim();
    }

    private static void requirePositivePrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
    }
}
