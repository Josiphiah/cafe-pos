package zm.cafe.pos.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import zm.cafe.pos.domain.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByAvailableTrueOrderByNameAsc();

    List<Product> findByCategoryIdOrderByNameAsc(Long categoryId);
}
