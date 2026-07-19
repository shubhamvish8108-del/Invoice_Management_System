package com.codeb.invoice.repository;

import com.codeb.invoice.entity.Estimate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Estimate entity operations
 */
@Repository
public interface EstimateRepository extends JpaRepository<Estimate, Integer> {

    /**
     * Find estimate by estimate number
     */
    Optional<Estimate> findByEstimateNo(String estimateNo);

    /**
     * Find estimates by chain ID
     */
    List<Estimate> findByChainId(Integer chainId);

    /**
     * Find estimates by status
     */
    List<Estimate> findByStatus(String status);

    /**
     * Search estimates by estimate number (partial match)
     */
    @Query("SELECT e FROM Estimate e WHERE LOWER(e.estimateNo) LIKE LOWER(CONCAT('%', :estimateNo, '%'))")
    List<Estimate> searchByEstimateNo(@Param("estimateNo") String estimateNo);

    /**
     * Get estimates with chain details
     */
    @Query("SELECT e FROM Estimate e JOIN FETCH e.chain WHERE e.id = :id")
    Optional<Estimate> findByIdWithChain(@Param("id") Integer id);

    /**
     * Get all approved estimates that can have invoices generated
     */
    @Query("SELECT e FROM Estimate e WHERE e.status = 'APPROVED'")
    List<Estimate> findAllApprovedEstimates();

    /**
     * Check if estimate already has an invoice
     */
    @Query("SELECT COUNT(i) > 0 FROM Invoice i WHERE i.estimatedId = :estimateId")
    boolean hasInvoice(@Param("estimateId") Integer estimateId);
}
