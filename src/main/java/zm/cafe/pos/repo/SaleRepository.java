package zm.cafe.pos.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import zm.cafe.pos.domain.Sale;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByOrderBySoldAtDesc();

    List<Sale> findByCustomerIdOrderBySoldAtDesc(Long customerId);

    List<Sale> findBySoldAtBetweenOrderBySoldAtDesc(LocalDateTime from, LocalDateTime to);
}
