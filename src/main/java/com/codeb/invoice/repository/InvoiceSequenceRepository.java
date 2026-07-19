package com.codeb.invoice.repository;

import com.codeb.invoice.entity.InvoiceSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

/**
 * Repository for Invoice Sequence operations
 * Used to generate unique invoice numbers
 */
@Repository
public interface InvoiceSequenceRepository extends JpaRepository<InvoiceSequence, Integer> {

    /**
     * Get and lock the sequence for update (prevents concurrent number generation)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM InvoiceSequence s WHERE s.id = 1")
    Optional<InvoiceSequence> findAndLockForUpdate();

    /**
     * Get the current sequence
     */
    @Query("SELECT s FROM InvoiceSequence s WHERE s.id = 1")
    Optional<InvoiceSequence> findCurrentSequence();
}
