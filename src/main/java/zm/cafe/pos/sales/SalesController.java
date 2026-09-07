package zm.cafe.pos.sales;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final StaffRepository staffRepository;

    public SalesController(CartService cart, SaleService saleService,
                           ProductRepository productRepository,
                           CustomerRepository customerRepository,
                           SaleRepository saleRepository,
                           StaffRepository staffRepository) {
        this.cart = cart;
        this.saleService = saleService;
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

    @PostMapping("/customer")
    public String attachCustomer(@RequestParam String phone, RedirectAttributes ra) {
        Optional<Customer> found = customerRepository.findByPhone(phone.trim());
        if (found.isPresent()) {
            cart.setCustomerId(found.get().getId());
        } else {
            ra.addFlashAttribute("error", "No customer with phone " + phone
                    + ". Register them on the Customers page first.");
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

    /**
     * TODO(auth slice #1): replace with {@code CurrentStaff.get()} once Salifyanji's
     * authentication is on main. Until then every sale is attributed to the seeded
     * {@code cashier} account.
     */
    private Staff currentCashier() {
        return staffRepository.findByUsername("cashier")
                .or(() -> staffRepository.findAll().stream().findFirst())
                .orElseThrow(() -> new IllegalStateException("No staff in the database"));
    }
}
