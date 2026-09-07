package zm.cafe.pos.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import zm.cafe.pos.domain.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByPhone(String phone);

    List<Customer> findByNameContainingIgnoreCaseOrderByNameAsc(String namePart);
}
