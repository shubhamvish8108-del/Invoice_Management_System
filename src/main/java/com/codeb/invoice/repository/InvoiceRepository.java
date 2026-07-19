package com.codeb.invoice.repository;

import com.codeb.invoice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Invoice entity operations
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    /**
     * Find invoice by invoice number
     */
    Optional<Invoice> findByInvoiceNo(String invoiceNo);

    /**
     * Find invoices by chain ID
     */
    List<Invoice> findByChainId(Integer chainId);

    /**
     * Find invoices by estimated ID
     */
    List<Invoice> findByEstimatedId(Integer estimatedId);

    /**
     * Find invoices by status
     */
    List<Invoice> findByStatus(String status);

    /**
     * Search invoices by invoice number (partial match)
     */
    @Query("SELECT i FROM Invoice i WHERE LOWER(i.invoiceNo) LIKE LOWER(CONCAT('%', :invoiceNo, '%'))")
    List<Invoice> searchByInvoiceNo(@Param("invoiceNo") String invoiceNo);

    /**
     * Search invoices by company name (via chain)
     */
    @Query("SELECT i FROM Invoice i JOIN i.chain c WHERE LOWER(c.chainName) LIKE LOWER(CONCAT('%', :companyName, '%'))")
    List<Invoice> searchByCompanyName(@Param("companyName") String companyName);

    /**
     * Advanced search - by invoiceNo, estimatedId, chainId, or companyName
     */
    @Query("SELECT DISTINCT i FROM Invoice i " +
            "LEFT JOIN FETCH i.chain " +
            "WHERE (:invoiceNo IS NULL OR LOWER(i.invoiceNo) LIKE LOWER(CONCAT('%', :invoiceNo, '%'))) " +
            "AND (:estimatedId IS NULL OR i.estimatedId = :estimatedId) " +
            "AND (:chainId IS NULL OR i.chainId = :chainId) " +
            "AND (:companyName IS NULL OR LOWER(i.chain.chainName) LIKE LOWER(CONCAT('%', :companyName, '%')))")
    List<Invoice> searchInvoices(
            @Param("invoiceNo") String invoiceNo,
            @Param("estimatedId") Integer estimatedId,
            @Param("chainId") Integer chainId,
            @Param("companyName") String companyName
    );

    /**
     * Get invoice with chain details
     */
    @Query("SELECT i FROM Invoice i JOIN FETCH i.chain WHERE i.id = :id")
    Optional<Invoice> findByIdWithChain(@Param("id") Integer id);

    /**
     * Get all invoices with chain details
     */
    @Query("SELECT i FROM Invoice i JOIN FETCH i.chain ORDER BY i.createdAt DESC")
    List<Invoice> findAllWithChain();

    /**
     * Get invoice by estimated ID (should be only one)
     */
    Optional<Invoice> findFirstByEstimatedId(Integer estimatedId);

    /**
     * Check if invoice exists by invoice number
     */
    boolean existsByInvoiceNo(String invoiceNo);

    /**
     * Get count of invoices by status
     */
    long countByStatus(String status);

    /**
     * Get total amount payable for a chain
     */
    @Query("SELECT COALESCE(SUM(i.amountPayable), 0) FROM Invoice i WHERE i.chainId = :chainId")
    java.math.BigDecimal getTotalAmountByChainId(@Param("chainId") Integer chainId);
}
