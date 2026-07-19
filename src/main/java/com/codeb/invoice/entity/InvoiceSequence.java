package com.codeb.invoice.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Invoice Sequence Entity - For generating unique invoice numbers
 * Stores the last used invoice number and auto-increments
 */
@Entity
@Table(name = "invoice_sequence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "last_invoice_number")
    @Builder.Default
    private Integer lastInvoiceNumber = 0;

    /**
     * Get next invoice number and increment counter
     * Returns format: INV-0001, INV-0002, etc.
     */
    public synchronized String getNextInvoiceNumber() {
        lastInvoiceNumber++;
        return String.format("INV-%04d", lastInvoiceNumber);
    }
}
