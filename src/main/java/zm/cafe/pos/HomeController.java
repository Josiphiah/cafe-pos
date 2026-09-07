package zm.cafe.pos;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import zm.cafe.pos.domain.Sale;
import zm.cafe.pos.repo.CustomerRepository;
import zm.cafe.pos.repo.ProductRepository;
import zm.cafe.pos.repo.SaleRepository;

import java.math.BigDecimal;

/** SKELETON — shared scaffold on main. Landing dashboard with headline counts. */
@Controller
public class HomeController {

    private final SaleRepository saleRepo;
    private final ProductRepository productRepo;
    private final CustomerRepository customerRepo;

    public HomeController(SaleRepository saleRepo, ProductRepository productRepo, CustomerRepository customerRepo) {
        this.saleRepo = saleRepo;
        this.productRepo = productRepo;
        this.customerRepo = customerRepo;
    }

    @GetMapping("/")
    public String index(Model model) {
        BigDecimal total = saleRepo.findAll().stream()
                .map(Sale::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("salesCount", saleRepo.count());
        model.addAttribute("salesTotal", total);
        model.addAttribute("productCount", productRepo.count());
        model.addAttribute("customerCount", customerRepo.count());
        return "index";
    }
}
