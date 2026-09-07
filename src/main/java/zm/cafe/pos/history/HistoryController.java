package zm.cafe.pos.history;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import zm.cafe.pos.repo.RefundRepository;
import zm.cafe.pos.repo.SaleRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
public class HistoryController {
    private final SaleRepository sales;
    private final RefundRepository refunds;

    public HistoryController(SaleRepository sales, RefundRepository refunds) {
        this.sales = sales;
        this.refunds = refunds;
    }

    @GetMapping("/history")
    public String history(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model) {
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        if (from != null && to != null && from.isAfter(to)) {
            model.addAttribute("error", "The start date must be on or before the end date.");
            model.addAttribute("sales", List.of());
        } else if (from == null && to == null) {
            model.addAttribute("sales", sales.findByOrderBySoldAtDesc());
        } else {
            // Inclusive calendar dates; SQL-safe bounds for an omitted endpoint.
            LocalDateTime start = (from == null ? LocalDate.of(1, 1, 1) : from).atStartOfDay();
            LocalDateTime end = (to == null ? LocalDate.of(9999, 12, 31) : to).atTime(LocalTime.MAX);
            model.addAttribute("sales", sales.findBySoldAtBetweenOrderBySoldAtDesc(start, end));
        }
        return "history/list";
    }

    @GetMapping("/history/refunds")
    public String refunds(Model model) {
        model.addAttribute("refunds", refunds.findByOrderByRefundedAtDesc());
        return "history/refunds-list";
    }
}
