package zm.cafe.pos.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * One product line on a {@link Sale}. Product name and unit price are copied in
 * as a snapshot so the receipt still reads correctly if the menu changes later.
 */
@Entity
@Table(name = "sale_line")
public class SaleLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 80)
    private String productName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    /** How many units of this line have already been refunded. */
    @Column(nullable = false)
    private int refundedQuantity = 0;

    protected SaleLine() {
    }

    public SaleLine(Product product, int quantity) {
        this.product = product;
        this.productName = product.getName();
        this.unitPrice = product.getPrice();
        this.quantity = quantity;
    }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public int getRefundableQuantity() {
        return quantity - refundedQuantity;
    }

    public Long getId() {
        return id;
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public Product getProduct() {
        return product;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getRefundedQuantity() {
        return refundedQuantity;
    }

    public void setRefundedQuantity(int refundedQuantity) {
        this.refundedQuantity = refundedQuantity;
    }
}
