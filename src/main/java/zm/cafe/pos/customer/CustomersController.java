package zm.cafe.pos.customer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import zm.cafe.pos.domain.Customer;
import zm.cafe.pos.repo.CustomerRepository;
import zm.cafe.pos.repo.SaleRepository;

import java.util.Comparator;
import java.util.List;

/**
 * Customers list — name, phone and when they were added — with a name search
 * and the ability to remove a customer who has no recorded sales.
 * The profile / purchase-history page still belongs to issue #5.
 */
@Controller
public class CustomersController {

    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;

    public CustomersController(CustomerRepository customerRepository, SaleRepository saleRepository) {
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
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

    /**
     * Delete a customer. Blocked when they have recorded sales — those sales must
     * keep pointing at a real customer, so remove is only for mistaken or unused
     * entries.
     */
    @PostMapping("/customers/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null) {
            ra.addFlashAttribute("error", "That customer no longer exists.");
            return "redirect:/customers";
        }
        int sales = saleRepository.findByCustomerIdOrderBySoldAtDesc(id).size();
        if (sales > 0) {
            ra.addFlashAttribute("error", "Cannot delete " + customer.getName()
                    + " — they have " + sales + " recorded sale(s).");
            return "redirect:/customers";
        }
        customerRepository.delete(customer);
        ra.addFlashAttribute("success", "Removed " + customer.getName() + " from the customer list.");
        return "redirect:/customers";
    }
}
