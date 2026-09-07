package zm.cafe.pos.customer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import zm.cafe.pos.domain.Customer;
import zm.cafe.pos.repo.CustomerRepository;

import java.util.Comparator;
import java.util.List;

/**
 * Minimal Customers list — name, phone and when they were added — with a name
 * search. A starting point for the customer slice (issue #5); the profile /
 * purchase-history page still belongs there.
 */
@Controller
public class CustomersController {

    private final CustomerRepository customerRepository;

    public CustomersController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/customers")
    public String list(@RequestParam(required = false) String q, Model model) {
        List<Customer> customers = (q == null || q.isBlank())
                ? customerRepository.findAll().stream()
                    .sorted(Comparator.comparing(Customer::getName, String.CASE_INSENSITIVE_ORDER))
                    .toList()
                : customerRepository.findByNameContainingIgnoreCaseOrderByNameAsc(q.trim());

        model.addAttribute("customers", customers);
        model.addAttribute("customerCount", customerRepository.count());
        model.addAttribute("q", q);
        return "customers/list";
    }
}
