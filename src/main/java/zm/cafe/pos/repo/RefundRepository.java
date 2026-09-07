package zm.cafe.pos.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import zm.cafe.pos.domain.Refund;

import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByOrderByRefundedAtDesc();

    List<Refund> findBySaleIdOrderByRefundedAtDesc(Long saleId);
}
