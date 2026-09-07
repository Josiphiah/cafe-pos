package zm.cafe.pos.customer;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import zm.cafe.pos.customer.CustomerService.Profile;
import zm.cafe.pos.domain.Customer;
import zm.cafe.pos.repo.CustomerRepository;

import java.util.Optional;

/**
 * Customers screen (issue #5): a searchable list showing purchases and spend,
 * a registration form, a per-customer profile with full purchase history, a
 * phone lookup used by the till, and removal of an unused customer.
 */
@Controller
@RequestMapping("/customers")
public class CustomersController {

    private final CustomerService customerService;
    private final CustomerRepository customerRepository;

    public CustomersController(CustomerService customerService, CustomerRepository customerRepository) {
        this.customerService = customerService;
        this.customerRepository = customerRepository;
    }

    /** List everyone (or a name search), with purchase count and lifetime spend per row. */
    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("rows", customerService.rows(q));
        model.addAttribute("customerCount", customerRepository.count());
        model.addAttribute("q", q);
        return "customers/list";
    }

    /** Registration form. A phone can be pre-filled (e.g. from the till lookup miss). */
    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) String phone, Model model) {
        if (!model.containsAttribute("customerForm")) {
            CustomerForm form = new CustomerForm();
            form.setPhone(phone);
            model.addAttribute("customerForm", form);
        }
        return "customers/form";
    }

    /** Create a customer from the form. */
    @PostMapping
    public String create(@Valid @ModelAttribute CustomerForm customerForm, BindingResult bindingResult,
                         Model model, RedirectAttributes ra) {
        if (!bindingResult.hasErrors()) {
            try {
                Customer saved = customerService.register(customerForm.getName(), customerForm.getPhone());
                ra.addFlashAttribute("success", "Registered " + saved.getName());
                return "redirect:/customers/" + saved.getId();
            } catch (IllegalArgumentException ex) {
                bindingResult.reject("customer.create.failed", ex.getMessage());
            }
        }
        return "customers/form";
    }

    /** Profile: details plus every past sale, lifetime spend and last-visit date. */
    @GetMapping("/{id}")
    public String profile(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Profile profile = customerService.profile(id);
        if (profile == null) {
            ra.addFlashAttribute("error", "No such customer.");
            return "redirect:/customers";
        }
        model.addAttribute("profile", profile);
        return "customers/detail";
    }

    /**
     * Phone lookup. Redirects to the customer's profile when the number is on
     * file, otherwise to the registration form with the number pre-filled.
     */
    @GetMapping("/lookup")
    public String lookup(@RequestParam String phone, RedirectAttributes ra) {
        Optional<Customer> found = customerService.findByPhone(phone);
        if (found.isPresent()) {
            return "redirect:/customers/" + found.get().getId();
        }
        ra.addFlashAttribute("error", "No customer with phone " + phone + " — register them below.");
        return "redirect:/customers/new?phone=" + phone.trim();
    }

    /** Delete a customer who has no recorded sales. */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null) {
            ra.addFlashAttribute("error", "That customer no longer exists.");
            return "redirect:/customers";
        }
        int sales = customerService.purchaseHistory(id).size();
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
