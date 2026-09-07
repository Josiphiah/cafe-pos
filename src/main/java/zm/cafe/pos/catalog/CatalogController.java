package zm.cafe.pos.catalog;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * Menu &amp; product management screen (manager-only — see {@code SecurityConfig}).
 * Lists every item, adds new ones, re-prices in place and retires / restores.
 */
@Controller
@RequestMapping("/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public String menu(Model model) {
        if (!model.containsAttribute("productForm")) {
            model.addAttribute("productForm", new ProductForm());
        }
        addPageData(model);
        return "catalog/list";
    }

    @PostMapping
    public String add(@Valid @ModelAttribute ProductForm productForm, BindingResult bindingResult,
                      Model model, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            addPageData(model);
            return "catalog/list";
        }
        try {
            catalogService.create(productForm.getName(), productForm.getCategoryId(), productForm.getPrice());
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("catalog.create.failed", ex.getMessage());
            addPageData(model);
            return "catalog/list";
        }
        ra.addFlashAttribute("success", "Added " + productForm.getName() + " to the menu");
        return "redirect:/catalog";
    }

    @PostMapping("/{id}/price")
    public String reprice(@PathVariable Long id, @RequestParam BigDecimal price, RedirectAttributes ra) {
        try {
            catalogService.updatePrice(id, price);
            ra.addFlashAttribute("success", "Price updated");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/catalog";
    }

    @PostMapping("/{id}/retire")
    public String retire(@PathVariable Long id, RedirectAttributes ra) {
        catalogService.retire(id);
        ra.addFlashAttribute("success", "Item taken off the menu");
        return "redirect:/catalog";
    }

    @PostMapping("/{id}/restore")
    public String restore(@PathVariable Long id, RedirectAttributes ra) {
        catalogService.restore(id);
        ra.addFlashAttribute("success", "Item put back on the menu");
        return "redirect:/catalog";
    }

    private void addPageData(Model model) {
        model.addAttribute("products", catalogService.listAll());
        model.addAttribute("categories", catalogService.categories());
    }
}
