package zm.cafe.pos.history;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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
    private final RefundService refundService;

    public HistoryController(SaleRepository sales, RefundRepository refunds, RefundService refundService) {
        this.sales = sales;
        this.refunds = refunds;
        this.refundService = refundService;
    }

    @GetMapping("/history")
    public String history(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model) {
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        List<zm.cafe.pos.domain.Sale> results;
        if (from != null && to != null && from.isAfter(to)) {
            model.addAttribute("error", "The start date must be on or before the end date.");
            results = List.of();
        } else if (from == null && to == null) {
            results = sales.findByOrderBySoldAtDesc();
        } else {
            // Inclusive calendar dates; SQL-safe bounds for an omitted endpoint.
            LocalDateTime start = (from == null ? LocalDate.of(1, 1, 1) : from).atStartOfDay();
            LocalDateTime end = (to == null ? LocalDate.of(9999, 12, 31) : to).atTime(LocalTime.MAX);
            results = sales.findBySoldAtBetweenOrderBySoldAtDesc(start, end);
        }
        model.addAttribute("sales", results);
        model.addAttribute("refundableSaleIds", results.stream().filter(refundService::canRefund)
                .map(zm.cafe.pos.domain.Sale::getId).toList());
        return "history/list";
    }

    @GetMapping("/history/refunds")
    public String refunds(Model model) {
        model.addAttribute("refunds", refunds.findByOrderByRefundedAtDesc());
        return "history/refunds-list";
    }

    @GetMapping("/history/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var sale = sales.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale not found"));
        var records = refunds.findBySaleIdOrderByRefundedAtDesc(id);
        var returned = records.stream().map(zm.cafe.pos.domain.Refund::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        model.addAttribute("sale", sale);
        model.addAttribute("refunds", records);
        model.addAttribute("refundedTotal", returned);
        model.addAttribute("remainingTotal", sale.getTotal().subtract(returned));
        model.addAttribute("canRefund", refundService.canRefund(sale));
        return "history/detail";
    }
}
