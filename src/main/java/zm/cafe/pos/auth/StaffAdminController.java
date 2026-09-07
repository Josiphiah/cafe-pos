package zm.cafe.pos.auth;

import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import zm.cafe.pos.domain.Role;
import zm.cafe.pos.domain.Staff;
import zm.cafe.pos.repo.StaffRepository;

@Controller
public class StaffAdminController {
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffAdminController(StaffRepository staffRepository, PasswordEncoder passwordEncoder) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/staff")
    public String staff(Model model) {
        if (!model.containsAttribute("staffForm")) model.addAttribute("staffForm", new StaffForm());
        addPageData(model);
        return "staff/list";
    }

    @PostMapping("/staff")
    public String addStaff(@Valid @ModelAttribute StaffForm staffForm, BindingResult bindingResult,
                           Model model, RedirectAttributes redirectAttributes) {
        if (staffRepository.existsByUsername(staffForm.getUsername())) {
            bindingResult.rejectValue("username", "duplicate", "That username is already in use");
        }
        if (bindingResult.hasErrors()) {
            addPageData(model);
            return "staff/list";
        }
        Staff staff = new Staff(staffForm.getUsername(), passwordEncoder.encode(staffForm.getPassword()),
                staffForm.getFullName(), staffForm.getRole());
        staffRepository.save(staff);
        redirectAttributes.addFlashAttribute("success", "Staff account created for " + staff.getFullName());
        return "redirect:/staff";
    }

    private void addPageData(Model model) {
        model.addAttribute("staffMembers", staffRepository.findAll());
        model.addAttribute("roles", Role.values());
    }
}
