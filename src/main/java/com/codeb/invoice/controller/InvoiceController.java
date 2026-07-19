package com.codeb.invoice.controller;

import com.codeb.invoice.entity.Estimate;
import com.codeb.invoice.entity.Invoice;
import com.codeb.invoice.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Invoice management operations
 * Handles invoice dashboard, generation, viewing, updating, and deletion
 */
@Controller
@RequestMapping("/invoices")
@RequiredArgsConstructor
@Slf4j
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final EstimateService estimateService;
    private final ChainService chainService;
    private final PdfGenerationService pdfGenerationService;
    private final EmailService emailService;

    /**
     * Dashboard - List all invoices
     */
    @GetMapping
    public String listInvoices(
            @RequestParam(required = false) String invoiceNo,
            @RequestParam(required = false) Integer estimatedId,
            @RequestParam(required = false) Integer chainId,
            @RequestParam(required = false) String companyName,
            Model model) {

        List<Invoice> invoices;

        // If search parameters provided, search
        if (invoiceNo != null || estimatedId != null || chainId != null || companyName != null) {
            invoices = invoiceService.searchInvoices(invoiceNo, estimatedId, chainId, companyName);
            model.addAttribute("searchPerformed", true);
        } else {
            invoices = invoiceService.getAllInvoices();
        }

        model.addAttribute("invoices", invoices);
        model.addAttribute("chains", chainService.getAllChains());

        model.addAttribute("totalInvoices", invoices.size());

        model.addAttribute("generatedCount",
                invoices.stream()
                        .filter(i -> "GENERATED".equals(i.getStatus()))
                        .count());

        model.addAttribute("paidCount",
                invoices.stream()
                        .filter(i -> "PAID".equals(i.getStatus()))
                        .count());

        model.addAttribute("partialCount",
                invoices.stream()
                        .filter(i -> i.getBalance() != null
                                && i.getBalance().compareTo(BigDecimal.ZERO) > 0)
                        .count());

        return "invoice/list";
    }

    /**
     * Show create invoice form (pre-filled from estimate)
     */
    @GetMapping("/create/{estimateId}")
    public String showCreateForm(@PathVariable Integer estimateId, Model model, RedirectAttributes redirectAttributes) {
        log.info("Opening create invoice form for estimate: {}", estimateId);

        // Get estimate with chain details
        Optional<Estimate> estimateOpt = estimateService.getEstimateWithChain(estimateId);

        if (estimateOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Estimate not found");
            return "redirect:/estimates";
        }

        Estimate estimate = estimateOpt.get();

        // Check if invoice already exists
        if (invoiceService.hasInvoiceForEstimate(estimateId)) {
            redirectAttributes.addFlashAttribute("error",
                    "Invoice already exists for this estimate. Please view the existing invoice.");
            return "redirect:/invoices";
        }

        model.addAttribute("estimate", estimate);
        model.addAttribute("chain", estimate.getChain());

        // Pre-fill form data
        Invoice invoiceForm = new Invoice();
        invoiceForm.setEstimatedId(estimateId);
        invoiceForm.setChainId(estimate.getChain().getId());
        invoiceForm.setServiceDetails(estimate.getServiceDetails());
        invoiceForm.setQty(estimate.getQty());
        invoiceForm.setCostPerQty(estimate.getCostPerQty());
        invoiceForm.setAmountPayable(estimate.getTotalAmount());
        invoiceForm.setDateOfService(LocalDate.now().plusDays(7));
        invoiceForm.setChainName(estimate.getChain().getChainName());
        invoiceForm.setGstNumber(estimate.getChain().getGstNumber());

        model.addAttribute("invoice", invoiceForm);

        return "invoice/create";
    }

    /**
     * Generate and save invoice
     */
    @PostMapping("/generate")
    public String generateInvoice(
            @RequestParam Integer estimateId,
            @RequestParam String emailId,
            @RequestParam(required = false) LocalDate dateOfService,
            @RequestParam(required = false) String deliveryDetails,
            @RequestParam(required = false) String paymentType,
            @RequestParam(required = false) BigDecimal amountPaid,
            RedirectAttributes redirectAttributes) {

        log.info("Generating invoice for estimate: {}, email: {}", estimateId, emailId);

        try {
            Invoice invoice;

            if ("partial".equals(paymentType) && amountPaid != null && amountPaid.compareTo(BigDecimal.ZERO) > 0) {
                // Partial payment invoice
                invoice = invoiceService.createPartialPaymentInvoice(
                        estimateId, emailId, amountPaid, dateOfService, deliveryDetails);
            } else {
                // Full payment invoice
                invoice = invoiceService.generateInvoiceFromEstimate(
                        estimateId, emailId, dateOfService, deliveryDetails);
            }

            redirectAttributes.addFlashAttribute("success",
                    "Invoice " + invoice.getInvoiceNo() + " generated successfully!");

            return "redirect:/invoices/view/" + invoice.getId();

        } catch (Exception e) {
            log.error("Failed to generate invoice", e);
            redirectAttributes.addFlashAttribute("error", "Failed to generate invoice: " + e.getMessage());
            return "redirect:/estimates";
        }
    }

    /**
     * View invoice details
     */
    @GetMapping("/view/{id}")
    public String viewInvoice(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Invoice> invoiceOpt = invoiceService.getInvoiceById(id);

        if (invoiceOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invoice not found");
            return "redirect:/invoices";
        }

        model.addAttribute("invoice", invoiceOpt.get());
        return "invoice/view";
    }

    /**
     * Show edit invoice form (only email is editable)
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Invoice> invoiceOpt = invoiceService.getInvoiceById(id);

        if (invoiceOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invoice not found");
            return "redirect:/invoices";
        }

        model.addAttribute("invoice", invoiceOpt.get());
        return "invoice/edit";
    }

    /**
     * Update invoice
     */
    @PostMapping("/update/{id}")
    public String updateInvoice(
            @PathVariable Integer id,
            @RequestParam String emailId,
            @RequestParam(required = false) String deliveryDetails,
            @RequestParam(required = false) LocalDate dateOfService,
            RedirectAttributes redirectAttributes) {

        try {
            invoiceService.updateInvoice(id, Invoice.builder()
                    .emailId(emailId)
                    .deliveryDetails(deliveryDetails)
                    .dateOfService(dateOfService)
                    .build());

            redirectAttributes.addFlashAttribute("success", "Invoice updated successfully!");
        } catch (Exception e) {
            log.error("Failed to update invoice", e);
            redirectAttributes.addFlashAttribute("error", "Failed to update invoice: " + e.getMessage());
        }

        return "redirect:/invoices/view/" + id;
    }

    /**
     * Delete invoice
     */
    @PostMapping("/delete/{id}")
    public String deleteInvoice(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            invoiceService.deleteInvoice(id);
            redirectAttributes.addFlashAttribute("success", "Invoice deleted successfully!");
        } catch (Exception e) {
            log.error("Failed to delete invoice", e);
            redirectAttributes.addFlashAttribute("error", "Failed to delete invoice: " + e.getMessage());
        }

        return "redirect:/invoices";
    }

    /**
     * Download invoice as PDF
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Integer id) {
        Optional<Invoice> invoiceOpt = invoiceService.getInvoiceById(id);

        if (invoiceOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Invoice invoice = invoiceOpt.get();
            byte[] pdfBytes = pdfGenerationService.generateInvoicePdf(invoice);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", invoice.getInvoiceNo() + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Failed to generate PDF", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Preview invoice as PDF (inline)
     */
    @GetMapping("/preview/{id}")
    public ResponseEntity<byte[]> previewInvoicePdf(@PathVariable Integer id) {
        Optional<Invoice> invoiceOpt = invoiceService.getInvoiceById(id);

        if (invoiceOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Invoice invoice = invoiceOpt.get();
            byte[] pdfBytes = pdfGenerationService.generateInvoicePdf(invoice);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Failed to generate PDF", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Send invoice via email
     */
    @PostMapping("/send-email/{id}")
    public String sendInvoiceEmail(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Optional<Invoice> invoiceOpt = invoiceService.getInvoiceById(id);

        if (invoiceOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invoice not found");
            return "redirect:/invoices";
        }

        try {
            emailService.sendInvoiceEmail(invoiceOpt.get());
            redirectAttributes.addFlashAttribute("success", "Invoice sent to " + invoiceOpt.get().getEmailId());
        } catch (Exception e) {
            log.error("Failed to send email", e);
            redirectAttributes.addFlashAttribute("error", "Failed to send email: " + e.getMessage());
        }

        return "redirect:/invoices/view/" + id;
    }

    /**
     * Get dashboard statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getStats() {
        DashboardStats stats = new DashboardStats(
                invoiceService.getInvoiceCount(),
                invoiceService.getInvoiceCountByStatus("GENERATED"),
                invoiceService.getInvoiceCountByStatus("PAID"),
                invoiceService.getInvoiceCountByStatus("CANCELLED")
        );
        return ResponseEntity.ok(stats);
    }

    // Stats record for JSON response
    public record DashboardStats(
            long totalInvoices,
            long generatedCount,
            long paidCount,
            long cancelledCount
    ) {}
}
