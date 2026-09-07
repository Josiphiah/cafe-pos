package zm.cafe.pos.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import zm.cafe.pos.domain.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);
}
