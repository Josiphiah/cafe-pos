package zm.cafe.pos.history;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.web.server.ResponseStatusException;
import zm.cafe.pos.domain.*;
import zm.cafe.pos.repo.RefundRepository;
import zm.cafe.pos.repo.StaffRepository;

import java.time.LocalDate;
import java.util.Map;

@Service
public class RefundService {
    private final EntityManager entityManager;
    private final RefundRepository refunds;
    private final StaffRepository staff;

    public RefundService(EntityManager entityManager, RefundRepository refunds, StaffRepository staff) {
        this.entityManager = entityManager;
        this.refunds = refunds;
        this.staff = staff;
    }

    public boolean canRefund(Sale sale) {
        return sale.getSoldAt().toLocalDate().equals(LocalDate.now())
                && sale.getStatus() != SaleStatus.REFUNDED
                && sale.getLines().stream().anyMatch(line -> line.getRefundableQuantity() > 0);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Refund refund(Long saleId, Map<Long, Integer> quantities, String reason, String username) {
        Staff processor = staff.findByUsername(username == null ? "" : username)
                .filter(Staff::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Active staff login required"));
        // Serialize refunds for a sale so concurrent requests cannot return the same units twice.
        Sale sale = entityManager.find(Sale.class, saleId, LockModeType.PESSIMISTIC_WRITE);
        if (sale == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale not found");
        if (!canRefund(sale)) throw new IllegalArgumentException("Only sales from today with unrefunded items can be refunded.");
        if (reason == null || reason.isBlank() || reason.trim().length() > 200)
            throw new IllegalArgumentException("Enter a refund reason of 1 to 200 characters.");
        if (quantities == null || quantities.isEmpty())
            throw new IllegalArgumentException("Select at least one item to refund.");

        // Validate every line before changing any persistent quantities.
        for (var entry : quantities.entrySet()) {
            SaleLine line = sale.getLines().stream().filter(l -> l.getId().equals(entry.getKey()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("An item does not belong to this sale."));
            if (entry.getValue() == null || entry.getValue() < 0 || entry.getValue() > line.getRefundableQuantity())
                throw new IllegalArgumentException("Refund quantities must be between zero and the remaining quantity.");
        }
        if (quantities.values().stream().noneMatch(q -> q > 0))
            throw new IllegalArgumentException("Select at least one item to refund.");

        Refund refund = new Refund(sale, processor, reason.trim());
        for (SaleLine line : sale.getLines()) {
            int quantity = quantities.getOrDefault(line.getId(), 0);
            if (quantity > 0) {
                refund.addLine(new RefundLine(line, quantity));
                line.setRefundedQuantity(line.getRefundedQuantity() + quantity);
            }
        }
        refund.recalculateAmount();
        sale.setStatus(sale.getLines().stream().allMatch(l -> l.getRefundableQuantity() == 0)
                ? SaleStatus.REFUNDED : SaleStatus.PARTIALLY_REFUNDED);
        return refunds.save(refund);
    }
}
