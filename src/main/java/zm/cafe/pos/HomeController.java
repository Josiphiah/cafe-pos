package zm.cafe.pos;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import zm.cafe.pos.domain.Sale;
import zm.cafe.pos.repo.CustomerRepository;
import zm.cafe.pos.repo.ProductRepository;
import zm.cafe.pos.repo.SaleRepository;
import zm.cafe.pos.repo.RefundRepository;
import java.util.HashMap;
import java.util.Map;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** SKELETON — shared scaffold on main. Landing dashboard: headline tiles, a 7-day revenue chart and recent sales. */
@Controller
public class HomeController {

    private final SaleRepository saleRepo;
    private final ProductRepository productRepo;
    private final CustomerRepository customerRepo;
    private final RefundRepository refundRepo;

    public HomeController(SaleRepository saleRepo, ProductRepository productRepo, CustomerRepository customerRepo,
                          RefundRepository refundRepo) {
        this.saleRepo = saleRepo;
        this.productRepo = productRepo;
        this.customerRepo = customerRepo;
        this.refundRepo = refundRepo;
    }

    /** One bar of the "sales this week" chart. {@code pct} is 0-100 of the tallest day. */
    public record Bar(String label, BigDecimal amount, int pct) {}

    @GetMapping("/")
    public String index(Model model) {
        List<Sale> all = saleRepo.findAll();
        Map<Long, BigDecimal> refundedBySale = new HashMap<>();
        refundRepo.findAll().forEach(refund -> refundedBySale.merge(
                refund.getSale().getId(), refund.getAmount(), BigDecimal::add));
        // Preserve original receipt totals; dashboard revenue is money retained after refunds.
        Map<Long, BigDecimal> netBySale = new HashMap<>();
        all.forEach(sale -> netBySale.put(sale.getId(), sale.getTotal()
                .subtract(refundedBySale.getOrDefault(sale.getId(), BigDecimal.ZERO))));
        BigDecimal total = netBySale.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("salesCount", all.size());
        model.addAttribute("salesTotal", total);
        model.addAttribute("netBySale", netBySale);
        model.addAttribute("productCount", productRepo.count());
        model.addAttribute("customerCount", customerRepo.count());

        model.addAttribute("recentSales", all.stream()
                .filter(s -> s.getSoldAt() != null)
                .sorted(Comparator.comparing(Sale::getSoldAt).reversed())
                .limit(6)
                .toList());

        LocalDate today = LocalDate.now();
        DateTimeFormatter dow = DateTimeFormatter.ofPattern("EEE");
        List<BigDecimal> amounts = new ArrayList<>();
        BigDecimal max = BigDecimal.ZERO;
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            BigDecimal dayTotal = all.stream()
                    .filter(s -> s.getSoldAt() != null && s.getSoldAt().toLocalDate().equals(day))
                    .map(s -> netBySale.get(s.getId()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            amounts.add(dayTotal);
            if (dayTotal.compareTo(max) > 0) max = dayTotal;
        }

        List<Bar> weekly = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            BigDecimal amt = amounts.get(i);
            int pct = 0;
            if (max.signum() > 0) {
                pct = amt.multiply(BigDecimal.valueOf(100)).divide(max, 0, RoundingMode.HALF_UP).intValue();
            }
            if (pct == 0 && amt.signum() > 0) {
                pct = 6;
            }
            weekly.add(new Bar(today.minusDays(6 - i).format(dow), amt, pct));
        }
        model.addAttribute("weekly", weekly);
        model.addAttribute("weekTotal", amounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add));

        return "index";
    }
}
