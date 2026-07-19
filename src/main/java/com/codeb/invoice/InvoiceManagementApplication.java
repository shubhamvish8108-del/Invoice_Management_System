package com.codeb.invoice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Invoice Management System - Main Application
 *
 * This application handles invoice generation, PDF creation, and email distribution
 * for the Code-B Internal Management System (IMS).
 *
 * @author Code-B Team
 * @version 1.0.0
 */
@SpringBootApplication
public class InvoiceManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvoiceManagementApplication.class, args);
        System.out.println("===========================================");
        System.out.println("  Invoice Management System Started!");
        System.out.println("  Access at: http://localhost:8080");
        System.out.println("===========================================");
    }
}
