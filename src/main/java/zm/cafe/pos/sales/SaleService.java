package zm.cafe.pos.sales;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.cafe.pos.domain.*;
import zm.cafe.pos.repo.CustomerRepository;
import zm.cafe.pos.repo.ProductRepository;
import zm.cafe.pos.repo.SaleRepository;

import java.math.BigDecimal;
import java.util.Map;

/** Turns a cart into a persisted {@link Sale} with snapshotted lines and stored totals. */
@Service
public class SaleService {

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;

    public SaleService(ProductRepository productRepository,
                       CustomerRepository customerRepository,
                       SaleRepository saleRepository) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
    }

    /**
     * @param items          product id -> quantity (quantity &gt; 0)
     * @param customerId      optional customer to attach, may be {@code null}
     * @param cashier         the logged-in staff member ringing up the sale
     * @param amountReceived  cash tendered by the customer, or {@code null} if not recorded;
     *                        must be at least the sale total when given
     */
    @Transactional
    public Sale checkout(Map<Long, Integer> items, Long customerId, Staff cashier, BigDecimal amountReceived) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Cannot complete a sale with an empty cart");
        }

        Customer customer = customerId == null ? null
                : customerRepository.findById(customerId).orElse(null);

        Sale sale = new Sale(cashier, customer);
        items.forEach((productId, qty) -> {
            if (qty == null || qty <= 0) {
                return;
            }
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown product " + productId));
            sale.addLine(new SaleLine(product, qty));
        });

        if (sale.getLines().isEmpty()) {
            throw new IllegalArgumentException("Cannot complete a sale with no valid lines");
        }

        sale.recalculateTotals();
        sale.pay(amountReceived);
        return saleRepository.save(sale);
    }
}
