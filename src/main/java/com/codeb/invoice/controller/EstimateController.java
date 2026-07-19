package com.codeb.invoice.controller;

import com.codeb.invoice.entity.Estimate;
import com.codeb.invoice.service.EstimateService;
import com.codeb.invoice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Controller for Estimate management
 * Estimates can be converted to invoices
 */
@Controller
@RequestMapping("/estimates")
@RequiredArgsConstructor
@Slf4j
public class EstimateController {

    private final EstimateService estimateService;
    private final InvoiceService invoiceService;

    /**
     * List all estimates
     */
    @GetMapping
    public String listEstimates(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer chainId,
            Model model) {

        List<Estimate> estimates;

        if (status != null) {
            estimates = estimateService.getEstimatesByStatus(status);
            model.addAttribute("selectedStatus", status);
        } else {
            estimates = estimateService.getAllEstimates();
        }

        model.addAttribute("estimates", estimates);
        return "estimate/list";
    }

    /**
     * View estimate details
     */
    @GetMapping("/view/{id}")
    public String viewEstimate(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Estimate> estimateOpt = estimateService.getEstimateWithChain(id);

        if (estimateOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Estimate not found");
            return "redirect:/estimates";
        }

        Estimate estimate = estimateOpt.get();
        boolean hasInvoice = invoiceService.hasInvoiceForEstimate(id);

        model.addAttribute("estimate", estimate);
        model.addAttribute("hasInvoice", hasInvoice);

        // Get invoice if exists
        if (hasInvoice) {
            invoiceService.getInvoiceById(id).ifPresent(invoice -> model.addAttribute("invoice", invoice));
        }

        return "estimate/view";
    }

    /**
     * Show create estimate form
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("estimate", new Estimate());
        return "estimate/create";
    }

    /**
     * Create new estimate
     */
    @PostMapping("/save")
    public String createEstimate(
            @RequestParam String estimateNo,
            @RequestParam Integer chainId,
            @RequestParam String serviceDetails,
            @RequestParam Integer qty,
            @RequestParam Double costPerQty,
            RedirectAttributes redirectAttributes) {

        try {
            Estimate estimate = Estimate.builder()
                    .estimateNo(estimateNo)
                    .serviceDetails(serviceDetails)
                    .qty(qty)
                    .costPerQty(new java.math.BigDecimal(costPerQty))
                    .totalAmount(new java.math.BigDecimal(costPerQty).multiply(new java.math.BigDecimal(qty)))
                    .status("PENDING")
                    .build();

            estimateService.createEstimate(estimate);
            redirectAttributes.addFlashAttribute("success", "Estimate created successfully!");
        } catch (Exception e) {
            log.error("Failed to create estimate", e);
            redirectAttributes.addFlashAttribute("error", "Failed to create estimate: " + e.getMessage());
        }

        return "redirect:/estimates";
    }

    /**
     * Update estimate status to approved
     */
    @PostMapping("/approve/{id}")
    public String approveEstimate(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            estimateService.updateStatus(id, "APPROVED");
            redirectAttributes.addFlashAttribute("success", "Estimate approved!");
        } catch (Exception e) {
            log.error("Failed to approve estimate", e);
            redirectAttributes.addFlashAttribute("error", "Failed to approve: " + e.getMessage());
        }

        return "redirect:/estimates/view/" + id;
    }
}
