package zm.cafe.pos.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** One line of a partial {@link Refund}: some quantity of a {@link SaleLine} given back. */
@Entity
@Table(name = "refund_line")
public class RefundLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "refund_id", nullable = false)
    private Refund refund;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "sale_line_id", nullable = false)
    private SaleLine saleLine;

    @Column(nullable = false)
    private int quantity;

    /** VAT-inclusive value returned for this line (unit price × quantity). */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    protected RefundLine() {
    }

    public RefundLine(SaleLine saleLine, int quantity) {
        this.saleLine = saleLine;
        this.quantity = quantity;
        this.amount = saleLine.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() {
        return id;
    }

    public Refund getRefund() {
        return refund;
    }

    public void setRefund(Refund refund) {
        this.refund = refund;
    }

    public SaleLine getSaleLine() {
        return saleLine;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
