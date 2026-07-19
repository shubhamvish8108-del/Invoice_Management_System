package com.codeb.invoice.service;

import com.codeb.invoice.entity.Estimate;
import com.codeb.invoice.repository.EstimateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for Estimate management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EstimateService {

    private final EstimateRepository estimateRepository;

    /**
     * Get all estimates
     */
    @Transactional(readOnly = true)
    public List<Estimate> getAllEstimates() {
        return estimateRepository.findAll();
    }

    /**
     * Get estimate by ID
     */
    @Transactional(readOnly = true)
    public Optional<Estimate> getEstimateById(Integer id) {
        return estimateRepository.findById(id);
    }

    /**
     * Get estimate with chain details
     */
    @Transactional(readOnly = true)
    public Optional<Estimate> getEstimateWithChain(Integer id) {
        return estimateRepository.findByIdWithChain(id);
    }

    /**
     * Get estimate by estimate number
     */
    @Transactional(readOnly = true)
    public Optional<Estimate> getEstimateByNumber(String estimateNo) {
        return estimateRepository.findByEstimateNo(estimateNo);
    }

    /**
     * Get estimates by chain ID
     */
    @Transactional(readOnly = true)
    public List<Estimate> getEstimatesByChain(Integer chainId) {
        return estimateRepository.findByChainId(chainId);
    }

    /**
     * Get approved estimates
     */
    @Transactional(readOnly = true)
    public List<Estimate> getApprovedEstimates() {
        return estimateRepository.findAllApprovedEstimates();
    }

    /**
     * Get estimates by status
     */
    @Transactional(readOnly = true)
    public List<Estimate> getEstimatesByStatus(String status) {
        return estimateRepository.findByStatus(status);
    }

    /**
     * Search estimates by number
     */
    @Transactional(readOnly = true)
    public List<Estimate> searchEstimates(String estimateNo) {
        return estimateRepository.searchByEstimateNo(estimateNo);
    }

    /**
     * Create new estimate
     */
    public Estimate createEstimate(Estimate estimate) {
        log.info("Creating new estimate: {}", estimate.getEstimateNo());
        return estimateRepository.save(estimate);
    }

    /**
     * Update existing estimate
     */
    public Estimate updateEstimate(Integer id, Estimate estimateDetails) {
        Estimate estimate = estimateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estimate not found with id: " + id));

        estimate.setEstimateNo(estimateDetails.getEstimateNo());
        estimate.setChain(estimateDetails.getChain());
        estimate.setServiceDetails(estimateDetails.getServiceDetails());
        estimate.setQty(estimateDetails.getQty());
        estimate.setCostPerQty(estimateDetails.getCostPerQty());
        estimate.setTotalAmount(estimateDetails.getTotalAmount());
        estimate.setStatus(estimateDetails.getStatus());

        log.info("Updated estimate: {}", estimate.getId());
        return estimateRepository.save(estimate);
    }

    /**
     * Update estimate status
     */
    public Estimate updateStatus(Integer id, String status) {
        Estimate estimate = estimateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estimate not found with id: " + id));
        estimate.setStatus(status);
        return estimateRepository.save(estimate);
    }

    /**
     * Delete estimate
     */
    public void deleteEstimate(Integer id) {
        Estimate estimate = estimateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estimate not found with id: " + id));
        estimateRepository.delete(estimate);
        log.info("Deleted estimate: {}", id);
    }

    /**
     * Check if estimate has an invoice
     */
    @Transactional(readOnly = true)
    public boolean hasInvoice(Integer estimateId) {
        return estimateRepository.hasInvoice(estimateId);
    }
}
