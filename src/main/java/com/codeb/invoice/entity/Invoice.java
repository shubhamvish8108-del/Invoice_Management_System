package com.codeb.invoice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Invoice Entity - Main entity for invoice generation
 * Stores all invoice details including GST info from Chain, service details, payment info
 *
 * KEY FIELDS:
 * - invoiceNo: Auto-generated unique 4-digit number
 * - estimatedId: Links to estimate for service details
 * - chainId: Links to chain for GST and company info
 * - balance: Tracks partial payment status
 * - emailId: Where to send the invoice
 */
@Entity
@Table(name = "invoice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "invoice_no", unique = true, nullable = false, length = 10)
    private String invoiceNo;

    @Column(name = "estimated_id", nullable = false)
    private Integer estimatedId;

    @Column(name = "chain_id", nullable = false)
    private Integer chainId;

    @Column(name = "service_details", length = 500)
    private String serviceDetails;

    @Column(name = "qty")
    private Integer qty;

    @Column(name = "cost_per_qty", precision = 10, scale = 2)
    private BigDecimal costPerQty;

    @Column(name = "amount_payable", precision = 10, scale = 2)
    private BigDecimal amountPayable;

    @Column(name = "balance", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "date_of_payment")
    private LocalDateTime dateOfPayment;

    @Column(name = "date_of_service")
    private LocalDate dateOfService;

    @Column(name = "delivery_details", length = 200)
    private String deliveryDetails;

    @Column(name = "email_id", length = 100)
    private String emailId;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "GENERATED";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Transient fields for enriched data (not stored in DB)
    @Transient
    private String chainName;

    @Transient
    private String gstNumber;

    @Transient
    private String chainAddress;

    @Transient
    private String chainEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chain_id", insertable = false, updatable = false)
    private Chain chain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estimated_id", insertable = false, updatable = false)
    private Estimate estimate;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (dateOfPayment == null) {
            dateOfPayment = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if this is a partial payment invoice
     */
    public boolean isPartialPayment() {
        return balance != null && balance.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Get amount paid (total - balance)
     */
    public BigDecimal getAmountPaid() {
        if (amountPayable == null) return BigDecimal.ZERO;
        if (balance == null) return amountPayable;
        return amountPayable.subtract(balance);
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", invoiceNo='" + invoiceNo + '\'' +
                ", amountPayable=" + amountPayable +
                ", status='" + status + '\'' +
                '}';
    }
}
