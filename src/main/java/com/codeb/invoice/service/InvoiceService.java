package com.codeb.invoice.service;

import com.codeb.invoice.entity.Chain;
import com.codeb.invoice.entity.Estimate;
import com.codeb.invoice.entity.Invoice;
import com.codeb.invoice.entity.InvoiceSequence;
import com.codeb.invoice.repository.EstimateRepository;
import com.codeb.invoice.repository.InvoiceRepository;
import com.codeb.invoice.repository.InvoiceSequenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for Invoice management operations
 * Handles invoice creation, updates, deletion, and search
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceSequenceRepository sequenceRepository;
    private final EstimateRepository estimateRepository;
    private final ChainService chainService;

    /**
     * Get all invoices
     */
    @Transactional(readOnly = true)
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAllWithChain();
    }

    /**
     * Get invoice by ID
     */
    @Transactional(readOnly = true)
    public Optional<Invoice> getInvoiceById(Integer id) {
        return invoiceRepository.findByIdWithChain(id);
    }

    /**
     * Get invoice by invoice number
     */
    @Transactional(readOnly = true)
    public Optional<Invoice> getInvoiceByNumber(String invoiceNo) {
        return invoiceRepository.findByInvoiceNo(invoiceNo);
    }

    /**
     * Get invoices by chain ID
     */
    @Transactional(readOnly = true)
    public List<Invoice> getInvoicesByChain(Integer chainId) {
        return invoiceRepository.findByChainId(chainId);
    }

    /**
     * Get invoices by status
     */
    @Transactional(readOnly = true)
    public List<Invoice> getInvoicesByStatus(String status) {
        return invoiceRepository.findByStatus(status);
    }

    /**
     * Search invoices with multiple criteria
     */
    @Transactional(readOnly = true)
    public List<Invoice> searchInvoices(String invoiceNo, Integer estimatedId,
                                         Integer chainId, String companyName) {
        return invoiceRepository.searchInvoices(invoiceNo, estimatedId, chainId, companyName);
    }

    /**
     * Generate invoice from estimate
     * This is the main function called when user clicks "Generate Invoice"
     */
    public Invoice generateInvoiceFromEstimate(Integer estimateId, String emailId,
                                               LocalDate dateOfService, String deliveryDetails) {
        log.info("Generating invoice for estimate ID: {}", estimateId);

        // Get estimate
        Estimate estimate = estimateRepository.findById(estimateId)
                .orElseThrow(() -> new RuntimeException("Estimate not found with id: " + estimateId));

        // Get chain for GST info
        Chain chain = estimate.getChain();
        if (chain == null) {
            throw new RuntimeException("Chain not found for estimate");
        }

        // Check if invoice already exists for this estimate
        Optional<Invoice> existingInvoice = invoiceRepository.findFirstByEstimatedId(estimateId);
        if (existingInvoice.isPresent()) {
            throw new RuntimeException("Invoice already exists for this estimate: " + existingInvoice.get().getInvoiceNo());
        }

        // Generate unique invoice number
        String invoiceNo = generateInvoiceNumber();

        // Create invoice
        Invoice invoice = Invoice.builder()
                .invoiceNo(invoiceNo)
                .estimatedId(estimateId)
                .chainId(chain.getId())
                .serviceDetails(estimate.getServiceDetails())
                .qty(estimate.getQty())
                .costPerQty(estimate.getCostPerQty())
                .amountPayable(estimate.getTotalAmount())
                .balance(BigDecimal.ZERO) // Full payment
                .dateOfPayment(LocalDateTime.now())
                .dateOfService(dateOfService != null ? dateOfService : LocalDate.now().plusDays(7))
                .deliveryDetails(deliveryDetails)
                .emailId(emailId)
                .status("GENERATED")
                .chainName(chain.getChainName())
                .gstNumber(chain.getGstNumber())
                .chainAddress(chain.getAddress())
                .chainEmail(chain.getContactEmail())
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);
        log.info("Invoice generated successfully: {}", invoiceNo);

        return savedInvoice;
    }

    /**
     * Generate unique invoice number (INV-0001, INV-0002, etc.)
     */
    private synchronized String generateInvoiceNumber() {
        InvoiceSequence sequence = sequenceRepository.findAndLockForUpdate()
                .orElseGet(() -> {
                    InvoiceSequence newSeq = InvoiceSequence.builder()
                            .lastInvoiceNumber(0)
                            .build();
                    return sequenceRepository.save(newSeq);
                });

        String invoiceNo = sequence.getNextInvoiceNumber();
        sequenceRepository.save(sequence);

        // Ensure uniqueness (unlikely to fail but safe)
        while (invoiceRepository.existsByInvoiceNo(invoiceNo)) {
            sequence.setLastInvoiceNumber(sequence.getLastInvoiceNumber() + 1);
            invoiceNo = sequence.getNextInvoiceNumber();
            sequenceRepository.save(sequence);
        }

        return invoiceNo;
    }

    /**
     * Create invoice with partial payment
     */
    public Invoice createPartialPaymentInvoice(Integer estimateId, String emailId,
                                               BigDecimal amountPaid, LocalDate dateOfService,
                                               String deliveryDetails) {
        log.info("Creating partial payment invoice for estimate ID: {}", estimateId);

        Estimate estimate = estimateRepository.findById(estimateId)
                .orElseThrow(() -> new RuntimeException("Estimate not found with id: " + estimateId));

        Chain chain = estimate.getChain();
        String invoiceNo = generateInvoiceNumber();

        BigDecimal totalAmount = estimate.getTotalAmount();
        BigDecimal balance = totalAmount.subtract(amountPaid);

        Invoice invoice = Invoice.builder()
                .invoiceNo(invoiceNo)
                .estimatedId(estimateId)
                .chainId(chain.getId())
                .serviceDetails(estimate.getServiceDetails())
                .qty(estimate.getQty())
                .costPerQty(estimate.getCostPerQty())
                .amountPayable(totalAmount)
                .balance(balance)
                .dateOfPayment(LocalDateTime.now())
                .dateOfService(dateOfService)
                .deliveryDetails(deliveryDetails)
                .emailId(emailId)
                .status("GENERATED")
                .chainName(chain.getChainName())
                .gstNumber(chain.getGstNumber())
                .chainAddress(chain.getAddress())
                .chainEmail(chain.getContactEmail())
                .build();

        return invoiceRepository.save(invoice);
    }

    /**
     * Update invoice email (only editable field)
     */
    public Invoice updateInvoiceEmail(Integer id, String newEmail) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));

        invoice.setEmailId(newEmail);
        log.info("Updated invoice {} email to {}", invoice.getInvoiceNo(), newEmail);

        return invoiceRepository.save(invoice);
    }

    /**
     * Update invoice
     */
    public Invoice updateInvoice(Integer id, Invoice invoiceDetails) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));

        // Only allow updating email (as per requirements)
        invoice.setEmailId(invoiceDetails.getEmailId());
        invoice.setDeliveryDetails(invoiceDetails.getDeliveryDetails());
        invoice.setDateOfService(invoiceDetails.getDateOfService());

        return invoiceRepository.save(invoice);
    }

    /**
     * Delete invoice
     */
    public void deleteInvoice(Integer id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));

        invoiceRepository.delete(invoice);
        log.info("Deleted invoice: {}", invoice.getInvoiceNo());
    }

    /**
     * Get invoice count
     */
    @Transactional(readOnly = true)
    public long getInvoiceCount() {
        return invoiceRepository.count();
    }

    /**
     * Get invoice count by status
     */
    @Transactional(readOnly = true)
    public long getInvoiceCountByStatus(String status) {
        return invoiceRepository.countByStatus(status);
    }

    /**
     * Get total amount by chain
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByChain(Integer chainId) {
        return invoiceRepository.getTotalAmountByChainId(chainId);
    }

    /**
     * Check if estimate has invoice
     */
    @Transactional(readOnly = true)
    public boolean hasInvoiceForEstimate(Integer estimateId) {
        return invoiceRepository.findFirstByEstimatedId(estimateId).isPresent();
    }
}
