package com.codeb.invoice.repository;

import com.codeb.invoice.entity.Chain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Chain entity operations
 */
@Repository
public interface ChainRepository extends JpaRepository<Chain, Integer> {

    /**
     * Find chain by name
     */
    Optional<Chain> findByChainName(String chainName);

    /**
     * Find chains by GST number
     */
    Optional<Chain> findByGstNumber(String gstNumber);

    /**
     * Search chains by name (partial match)
     */
    @Query("SELECT c FROM Chain c WHERE LOWER(c.chainName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Chain> searchByName(@Param("name") String name);

    /**
     * Get all chains with their invoices
     */
    @Query("SELECT DISTINCT c FROM Chain c LEFT JOIN FETCH c.invoices")
    List<Chain> findAllWithInvoices();

    /**
     * Check if chain exists by name
     */
    boolean existsByChainName(String chainName);
}
