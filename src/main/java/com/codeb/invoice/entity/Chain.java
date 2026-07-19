package com.codeb.invoice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Chain Entity - Represents client companies with GST information
 * Invoices are linked to chains to retrieve GST and contact details
 */
@Entity
@Table(name = "chain")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "chain_name", nullable = false, length = 100)
    private String chainName;

    @Column(name = "gst_number", length = 20)
    private String gstNumber;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "chain", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Estimate> estimates = new ArrayList<>();

    @OneToMany(mappedBy = "chain", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Invoice> invoices = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Chain{" +
                "id=" + id +
                ", chainName='" + chainName + '\'' +
                ", gstNumber='" + gstNumber + '\'' +
                '}';
    }
}
