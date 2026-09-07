package zm.cafe.pos.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Money returned to a customer against a past {@link Sale}. Supports a full
 * whole-transaction refund or a partial refund of selected {@link RefundLine}s.
 * House rule: same trading day only, and a logged-in staff member must process it.
 */
@Entity
@Table(name = "refund")
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Column(nullable = false)
    private LocalDateTime refundedAt = LocalDateTime.now();

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "processed_by", nullable = false)
    private Staff processedBy;

    @Column(nullable = false, length = 200)
    private String reason;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "refund", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RefundLine> lines = new ArrayList<>();

    protected Refund() {
    }

    public Refund(Sale sale, Staff processedBy, String reason) {
        this.sale = sale;
        this.processedBy = processedBy;
        this.reason = reason;
    }

    public void addLine(RefundLine line) {
        line.setRefund(this);
        lines.add(line);
    }

    public void recalculateAmount() {
        this.amount = lines.stream()
                .map(RefundLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() {
        return id;
    }

    public Sale getSale() {
        return sale;
    }

    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }

    public Staff getProcessedBy() {
        return processedBy;
    }

    public String getReason() {
        return reason;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public List<RefundLine> getLines() {
        return lines;
    }
}
