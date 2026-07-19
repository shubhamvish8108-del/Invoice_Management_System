package com.codeb.invoice.service;

import com.codeb.invoice.entity.Chain;
import com.codeb.invoice.repository.ChainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for Chain management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChainService {

    private final ChainRepository chainRepository;

    /**
     * Get all chains
     */
    @Transactional(readOnly = true)
    public List<Chain> getAllChains() {
        return chainRepository.findAll();
    }

    /**
     * Get chain by ID
     */
    @Transactional(readOnly = true)
    public Optional<Chain> getChainById(Integer id) {
        return chainRepository.findById(id);
    }

    /**
     * Get chain by name
     */
    @Transactional(readOnly = true)
    public Optional<Chain> getChainByName(String name) {
        return chainRepository.findByChainName(name);
    }

    /**
     * Search chains by name
     */
    @Transactional(readOnly = true)
    public List<Chain> searchChains(String name) {
        return chainRepository.searchByName(name);
    }

    /**
     * Create new chain
     */
    public Chain createChain(Chain chain) {
        log.info("Creating new chain: {}", chain.getChainName());
        return chainRepository.save(chain);
    }

    /**
     * Update existing chain
     */
    public Chain updateChain(Integer id, Chain chainDetails) {
        Chain chain = chainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chain not found with id: " + id));

        chain.setChainName(chainDetails.getChainName());
        chain.setGstNumber(chainDetails.getGstNumber());
        chain.setAddress(chainDetails.getAddress());
        chain.setContactEmail(chainDetails.getContactEmail());
        chain.setContactPhone(chainDetails.getContactPhone());

        log.info("Updated chain: {}", chain.getId());
        return chainRepository.save(chain);
    }

    /**
     * Delete chain
     */
    public void deleteChain(Integer id) {
        Chain chain = chainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chain not found with id: " + id));
        chainRepository.delete(chain);
        log.info("Deleted chain: {}", id);
    }

    /**
     * Get chains with invoices
     */
    @Transactional(readOnly = true)
    public List<Chain> getChainsWithInvoices() {
        return chainRepository.findAllWithInvoices();
    }

    /**
     * Check if chain exists
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return chainRepository.existsByChainName(name);
    }
}
