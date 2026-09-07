package zm.cafe.pos.sales;

import org.springframework.web.context.annotation.SessionScope;
import org.springframework.stereotype.Component;
import zm.cafe.pos.domain.Product;
import zm.cafe.pos.repo.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The in-progress sale for one till session: product id -> quantity, plus an
 * optional customer. Lives in the HTTP session, cleared once the sale is completed.
 */
@Component
@SessionScope
public class CartService {

    private final ProductRepository productRepository;
    private final Map<Long, Integer> quantities = new LinkedHashMap<>();
    private Long customerId;

    public CartService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void add(Long productId) {
        quantities.merge(productId, 1, Integer::sum);
    }

    public void setQuantity(Long productId, int qty) {
        if (qty <= 0) {
            quantities.remove(productId);
        } else {
            quantities.put(productId, qty);
        }
    }

    public void remove(Long productId) {
        quantities.remove(productId);
    }

    public void clear() {
        quantities.clear();
        customerId = null;
    }

    public boolean isEmpty() {
        return quantities.isEmpty();
    }

    public Map<Long, Integer> getQuantities() {
        return new LinkedHashMap<>(quantities);
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    /** Display rows for the till, resolved against the current menu. */
    public List<CartRow> rows() {
        List<CartRow> rows = new ArrayList<>();
        for (Map.Entry<Long, Integer> e : quantities.entrySet()) {
            Product p = productRepository.findById(e.getKey()).orElse(null);
            if (p != null) {
                rows.add(new CartRow(p, e.getValue()));
            }
        }
        return rows;
    }

    public BigDecimal total() {
        return rows().stream()
                .map(CartRow::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** One line of the cart as shown on the till. */
    public record CartRow(Product product, int quantity) {
        public BigDecimal lineTotal() {
            return product.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
    }
}
