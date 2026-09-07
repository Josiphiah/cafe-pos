package zm.cafe.pos.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A completed transaction at the till: one or more {@link SaleLine}s, a cashier,
 * an optional {@link Customer}, and stored money totals (16% VAT, inclusive).
 */
@Entity
@Table(name = "sale")
public class Sale {

    /** Zambian standard-rated VAT. */
    public static final BigDecimal VAT_RATE = new BigDecimal("0.16");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime soldAt = LocalDateTime.now();

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "cashier_id", nullable = false)
    private Staff cashier;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SaleLine> lines = new ArrayList<>();

    /** Net of VAT. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal vatAmount = BigDecimal.ZERO;

    /** What the customer paid (subtotal + VAT). */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SaleStatus status = SaleStatus.COMPLETED;

    protected Sale() {
    }

    public Sale(Staff cashier, Customer customer) {
        this.cashier = cashier;
        this.customer = customer;
    }

    public void addLine(SaleLine line) {
        line.setSale(this);
        lines.add(line);
    }

    /** Recompute stored totals from the current lines. Prices are VAT-inclusive. */
    public void recalculateTotals() {
        BigDecimal gross = lines.stream()
                .map(SaleLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.total = gross.setScale(2, RoundingMode.HALF_UP);
        this.subtotal = total.divide(BigDecimal.ONE.add(VAT_RATE), 2, RoundingMode.HALF_UP);
        this.vatAmount = total.subtract(subtotal);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getSoldAt() {
        return soldAt;
    }

    public void setSoldAt(LocalDateTime soldAt) {
        this.soldAt = soldAt;
    }

    public Staff getCashier() {
        return cashier;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<SaleLine> getLines() {
        return lines;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public SaleStatus getStatus() {
        return status;
    }

    public void setStatus(SaleStatus status) {
        this.status = status;
    }
}
