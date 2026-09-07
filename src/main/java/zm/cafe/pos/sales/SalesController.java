package zm.cafe.pos.sales;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import zm.cafe.pos.customer.CustomerService;
import zm.cafe.pos.domain.Customer;
import zm.cafe.pos.domain.Sale;
import zm.cafe.pos.domain.Staff;
import zm.cafe.pos.repo.CustomerRepository;
import zm.cafe.pos.repo.ProductRepository;
import zm.cafe.pos.repo.SaleRepository;
import zm.cafe.pos.repo.StaffRepository;

import java.util.Optional;

/** The till: build a cart, attach a customer, take payment, show the receipt. */
@Controller
@RequestMapping("/sales")
public class SalesController {

    private final CartService cart;
    private final SaleService saleService;
    private final CustomerService customerService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final StaffRepository staffRepository;

    public SalesController(CartService cart, SaleService saleService,
                           CustomerService customerService,
                           ProductRepository productRepository,
                           CustomerRepository customerRepository,
                           SaleRepository saleRepository,
                           StaffRepository staffRepository) {
        this.cart = cart;
        this.saleService = saleService;
        this.customerService = customerService;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
        this.staffRepository = staffRepository;
    }

    @GetMapping
    public String till(Model model) {
        model.addAttribute("products", productRepository.findByAvailableTrueOrderByNameAsc());
        model.addAttribute("rows", cart.rows());
        model.addAttribute("total", cart.total());
        model.addAttribute("vatRate", Sale.VAT_RATE);
        model.addAttribute("customer", cart.getCustomerId() == null ? null
                : customerRepository.findById(cart.getCustomerId()).orElse(null));
        return "sales/till";
    }

    @PostMapping("/add")
    public String add(@RequestParam Long productId) {
        cart.add(productId);
        return "redirect:/sales";
    }

    @PostMapping("/qty")
    public String qty(@RequestParam Long productId, @RequestParam int quantity) {
        cart.setQuantity(productId, quantity);
        return "redirect:/sales";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam Long productId) {
        cart.remove(productId);
        return "redirect:/sales";
    }

    /**
     * Step 1 of the "name on the cup" flow: look up a phone number. If it is on
     * file, attach that customer. If not, ask the till for a name (step 2).
     */
    @PostMapping("/customer")
    public String attachCustomer(@RequestParam String phone, RedirectAttributes ra) {
        String p = phone == null ? "" : phone.trim();
        if (p.isEmpty()) {
            cart.setCustomerId(null);
            return "redirect:/sales";
        }
        Optional<Customer> found = customerService.findByPhone(p);
        if (found.isPresent()) {
            cart.setCustomerId(found.get().getId());
            ra.addFlashAttribute("customerMsg", "Welcome back, " + found.get().getName() + ".");
        } else {
            ra.addFlashAttribute("newPhone", p);
            ra.addFlashAttribute("customerMsg", p + " is new — add a name to save them.");
        }
        return "redirect:/sales";
    }

    /**
     * Step 2: the number was new, the cashier supplied a name. Create the
     * customer and attach them so the name prints on the receipt.
     */
    @PostMapping("/customer/new")
    public String registerCustomer(@RequestParam String phone, @RequestParam String name,
                                   RedirectAttributes ra) {
        try {
            Customer c = customerService.findOrRegister(phone, name);
            cart.setCustomerId(c.getId());
            ra.addFlashAttribute("customerMsg", "Added " + c.getName() + " to the customer list.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            ra.addFlashAttribute("newPhone", phone == null ? "" : phone.trim());
        }
        return "redirect:/sales";
    }

    @PostMapping("/customer/clear")
    public String clearCustomer() {
        cart.setCustomerId(null);
        return "redirect:/sales";
    }

    @PostMapping("/clear")
    public String clear() {
        cart.clear();
        return "redirect:/sales";
    }

    @PostMapping("/checkout")
    public String checkout(RedirectAttributes ra) {
        if (cart.isEmpty()) {
            ra.addFlashAttribute("error", "Add at least one item before completing the sale.");
            return "redirect:/sales";
        }
        Sale sale = saleService.checkout(cart.getQuantities(), cart.getCustomerId(), currentCashier());
        cart.clear();
        return "redirect:/sales/" + sale.getId() + "/receipt";
    }

    @GetMapping("/{id}/receipt")
    public String receipt(@PathVariable Long id, Model model) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No sale " + id));
        model.addAttribute("sale", sale);
        return "sales/receipt";
    }

    /** The logged-in staff member ringing up the sale (auth slice #1). */
    private Staff currentCashier() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            Optional<Staff> staff = staffRepository.findByUsername(auth.getName());
            if (staff.isPresent()) {
                return staff.get();
            }
        }
        // Fallback for tests / anonymous dev mode.
        return staffRepository.findByUsername("cashier")
                .or(() -> staffRepository.findAll().stream().findFirst())
                .orElseThrow(() -> new IllegalStateException("No staff in the database"));
    }
}
