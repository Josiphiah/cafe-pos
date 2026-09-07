package zm.cafe.pos.history;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import zm.cafe.pos.domain.Role;
import zm.cafe.pos.domain.Sale;
import zm.cafe.pos.repo.SaleRepository;
import zm.cafe.pos.repo.StaffRepository;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/history/{id}/refund")
public class RefundController {
    private final SaleRepository sales;
    private final RefundService service;
    private final StaffRepository staff;
    private final PasswordEncoder passwordEncoder;

    public RefundController(SaleRepository sales, RefundService service,
                            StaffRepository staff, PasswordEncoder passwordEncoder) {
        this.sales = sales;
        this.service = service;
        this.staff = staff;
        this.passwordEncoder = passwordEncoder;
    }

    /** A refund must be authorised with the password of an active MANAGER account. */
    private boolean managerAuthorised(String password) {
        if (password == null || password.isBlank()) {
            return false;
        }
        return staff.findAll().stream()
                .filter(s -> s.isActive() && s.getRole() == Role.MANAGER)
                .anyMatch(s -> passwordEncoder.matches(password, s.getPasswordHash()));
    }

    @GetMapping
    public String form(@PathVariable Long id, Model model) {
        Sale sale = sales.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale not found"));
        model.addAttribute("sale", sale);
        model.addAttribute("canRefund", service.canRefund(sale));
        return "history/refund";
    }

    @PostMapping
    public String refund(@PathVariable Long id, @RequestParam Map<String, String> parameters,
                         Principal principal, RedirectAttributes redirect) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (!managerAuthorised(parameters.get("managerPassword"))) {
            redirect.addFlashAttribute("error", "Enter a manager password to authorise this refund.");
            return "redirect:/history/" + id + "/refund";
        }
        try {
            Map<Long, Integer> quantities = new LinkedHashMap<>();
            for (var entry : parameters.entrySet()) {
                if (entry.getKey().startsWith("quantity_")) {
                    quantities.put(Long.valueOf(entry.getKey().substring(9)), Integer.valueOf(entry.getValue()));
                }
            }
            service.refund(id, quantities, parameters.get("reason"), principal.getName());
            redirect.addFlashAttribute("success", "Refund recorded successfully.");
            return "redirect:/history/" + id;
        } catch (NumberFormatException ex) {
            redirect.addFlashAttribute("error", "Enter whole-number quantities for the selected items.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/history/" + id + "/refund";
    }
}
