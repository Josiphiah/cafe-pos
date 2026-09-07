package zm.cafe.pos.customer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.cafe.pos.domain.Customer;
import zm.cafe.pos.domain.Sale;
import zm.cafe.pos.repo.CustomerRepository;
import zm.cafe.pos.repo.SaleRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Customer lookup, registration and purchase history.
 *
 * <p>The till uses {@link #findOrRegister} for the "name on the cup" flow; the
 * Customers screen uses {@link #register}, {@link #rows} and {@link #profile}.
 */
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;

    public CustomerService(CustomerRepository customerRepository, SaleRepository saleRepository) {
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
    }

    // ---- lookup -------------------------------------------------------------

    public Optional<Customer> findByPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return Optional.empty();
        }
        return customerRepository.findByPhone(phone.trim());
    }

    // ---- registration ----------------------------------------------------------

    /**
     * Register a customer from the Customers screen. Name is required; phone is
     * optional but, when given, must not already be on file.
     *
     * @throws IllegalArgumentException on a blank name or a duplicate phone number
     */
    @Transactional
    public Customer register(String name, String phone) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A customer name is required");
        }
        String p = phone == null || phone.isBlank() ? null : phone.trim();
        if (p != null && customerRepository.findByPhone(p).isPresent()) {
            throw new IllegalArgumentException("A customer with phone " + p + " already exists");
        }
        return customerRepository.save(new Customer(name.trim(), p));
    }

    /**
     * Return the customer on this phone number, creating one with the given name
     * if the number is not yet on file. Used by the till.
     *
     * @throws IllegalArgumentException if a new customer is needed but no name was given
     */
    @Transactional
    public Customer findOrRegister(String phone, String name) {
        String p = phone == null ? "" : phone.trim();
        if (p.isEmpty()) {
            throw new IllegalArgumentException("A phone number is required");
        }
        return customerRepository.findByPhone(p).orElseGet(() -> {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("A name is required to add a new customer");
            }
            return customerRepository.save(new Customer(name.trim(), p));
        });
    }

    // ---- purchase history ----------------------------------------------------

    /** Every sale for a customer, most recent first. */
    public List<Sale> purchaseHistory(Long customerId) {
        return saleRepository.findByCustomerIdOrderBySoldAtDesc(customerId);
    }

    /** Sum of the totals of every sale for a customer (gross, VAT-inclusive). */
    public BigDecimal lifetimeSpend(Long customerId) {
        return purchaseHistory(customerId).stream()
                .map(Sale::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** One row of the Customers list: the customer plus their purchase count and lifetime spend. */
    public List<Row> rows(String nameQuery) {
        List<Customer> customers = (nameQuery == null || nameQuery.isBlank())
                ? customerRepository.findAll().stream()
                    .sorted(Comparator.comparing(Customer::getName, String.CASE_INSENSITIVE_ORDER))
                    .toList()
                : customerRepository.findByNameContainingIgnoreCaseOrderByNameAsc(nameQuery.trim());

        return customers.stream().map(c -> {
            List<Sale> sales = purchaseHistory(c.getId());
            BigDecimal spend = sales.stream().map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            return new Row(c, sales.size(), spend);
        }).toList();
    }

    /** Full profile for one customer, or {@code null} if the id is unknown. */
    public Profile profile(Long customerId) {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) {
            return null;
        }
        List<Sale> sales = purchaseHistory(customerId);
        BigDecimal spend = sales.stream().map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        LocalDateTime lastVisit = sales.isEmpty() ? null : sales.get(0).getSoldAt();
        return new Profile(customer, sales, spend, lastVisit);
    }

    public record Row(Customer customer, int purchases, BigDecimal spend) {
    }

    public record Profile(Customer customer, List<Sale> sales, BigDecimal lifetimeSpend, LocalDateTime lastVisit) {
        public int purchaseCount() {
            return sales.size();
        }
    }
}
