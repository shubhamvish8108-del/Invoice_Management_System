package com.codeb.invoice.service;

import com.codeb.invoice.entity.Invoice;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Service for generating PDF invoices using iText library
 */
@Service
@Slf4j
public class PdfGenerationService {

    @Value("${invoice.company.name:Code-B Tech Solutions}")
    private String companyName;

    @Value("${invoice.company.address:123 Tech Park, Mumbai, MH 400001}")
    private String companyAddress;

    @Value("${invoice.pdf.storage.path:./invoices}")
    private String pdfStoragePath;

    // Fonts
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, new BaseColor(51, 51, 51));
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(102, 102, 102));
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, new BaseColor(51, 51, 51));
    private static final Font BOLD_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, new BaseColor(51, 51, 51));
    private static final Font TABLE_HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
    private static final Font TABLE_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(51, 51, 51));

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    /**
     * Generate PDF for an invoice and return as byte array
     */
    public byte[] generateInvoicePdf(Invoice invoice) throws DocumentException, IOException {
        log.info("Generating PDF for invoice: {}", invoice.getInvoiceNo());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Add content
            addHeader(document, invoice);
            addCompanyInfo(document);
            addInvoiceDetails(document, invoice);
            addClientInfo(document, invoice);
            addItemsTable(document, invoice);
            addTotalsTable(document, invoice);
            addFooter(document, invoice);

            document.close();

            log.info("PDF generated successfully for invoice: {}", invoice.getInvoiceNo());
            return baos.toByteArray();

        } catch (DocumentException e) {
            log.error("Error generating PDF for invoice: {}", invoice.getInvoiceNo(), e);
            throw e;
        }
    }

    /**
     * Generate PDF and save to file
     */
    public String generateAndSaveInvoicePdf(Invoice invoice) throws DocumentException, IOException {
        // Ensure directory exists
        Path path = Paths.get(pdfStoragePath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        String fileName = invoice.getInvoiceNo() + ".pdf";
        String filePath = pdfStoragePath + "/" + fileName;

        byte[] pdfBytes = generateInvoicePdf(invoice);

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(pdfBytes);
        }

        log.info("PDF saved to: {}", filePath);
        return filePath;
    }

    private void addHeader(Document document, Invoice invoice) throws DocumentException {
        // Title
        Paragraph title = new Paragraph("INVOICE", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Invoice number and date
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingAfter(30);

        // Invoice number cell
        PdfPCell invoiceCell = new PdfPCell();
        invoiceCell.setBorder(Rectangle.NO_BORDER);
        invoiceCell.addElement(new Paragraph(invoice.getInvoiceNo(), BOLD_FONT));
        invoiceCell.addElement(new Paragraph("Date: " + (invoice.getCreatedAt() != null ?
                invoice.getCreatedAt().format(DATETIME_FORMATTER) : ""), NORMAL_FONT));

        // Status cell
        PdfPCell statusCell = new PdfPCell();
        statusCell.setBorder(Rectangle.NO_BORDER);
        statusCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph statusPara = new Paragraph("STATUS", HEADER_FONT);
        statusPara.setAlignment(Element.ALIGN_RIGHT);

        Font statusFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD,
                invoice.isPartialPayment() ? new BaseColor(255, 153, 0) : new BaseColor(0, 153, 76));
        Paragraph statusValue = new Paragraph(
                invoice.isPartialPayment() ? "PARTIAL PAYMENT" : "FULL PAYMENT", statusFont);

        statusCell.addElement(statusPara);
        statusCell.addElement(statusValue);

        headerTable.addCell(invoiceCell);
        headerTable.addCell(statusCell);
        document.add(headerTable);
    }

    private void addCompanyInfo(Document document) throws DocumentException {
        PdfPTable companyTable = new PdfPTable(1);
        companyTable.setWidthPercentage(50);
        companyTable.setHorizontalAlignment(Element.ALIGN_LEFT);
        companyTable.setSpacingAfter(20);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(new BaseColor(245, 245, 245));
        cell.setPadding(15);

        cell.addElement(new Paragraph("FROM", HEADER_FONT));
        cell.addElement(new Paragraph(companyName, BOLD_FONT));
        cell.addElement(new Paragraph(companyAddress, NORMAL_FONT));

        companyTable.addCell(cell);
        document.add(companyTable);
    }

    private void addInvoiceDetails(Document document, Invoice invoice) throws DocumentException {
        PdfPTable detailsTable = new PdfPTable(2);
        detailsTable.setWidthPercentage(100);
        detailsTable.setSpacingAfter(20);

        // Payment Date
        addDetailRow(detailsTable, "Date of Payment:",
                invoice.getDateOfPayment() != null ?
                        invoice.getDateOfPayment().format(DATETIME_FORMATTER) : "N/A");

        // Service Date
        addDetailRow(detailsTable, "Service Date:",
                invoice.getDateOfService() != null ?
                        invoice.getDateOfService().format(DATE_FORMATTER) : "N/A");

        // Delivery Details
        addDetailRow(detailsTable, "Delivery Details:",
                invoice.getDeliveryDetails() != null ? invoice.getDeliveryDetails() : "N/A");

        document.add(detailsTable);
    }

    private void addClientInfo(Document document, Invoice invoice) throws DocumentException {
        PdfPTable clientTable = new PdfPTable(1);
        clientTable.setWidthPercentage(100);
        clientTable.setSpacingAfter(20);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(new BaseColor(200, 200, 200));
        cell.setPadding(15);

        cell.addElement(new Paragraph("BILL TO", HEADER_FONT));
        cell.addElement(new Paragraph(invoice.getChainName() != null ? invoice.getChainName() : "N/A", BOLD_FONT));

        if (invoice.getGstNumber() != null) {
            cell.addElement(new Paragraph("GST No: " + invoice.getGstNumber(), NORMAL_FONT));
        }

        if (invoice.getChainAddress() != null) {
            cell.addElement(new Paragraph(invoice.getChainAddress(), NORMAL_FONT));
        }

        if (invoice.getChainEmail() != null) {
            cell.addElement(new Paragraph("Email: " + invoice.getChainEmail(), NORMAL_FONT));
        }

        clientTable.addCell(cell);
        document.add(clientTable);
    }

    private void addItemsTable(Document document, Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingAfter(20);

        // Set column widths
        float[] columnWidths = {50, 15, 20, 15};
        table.setWidths(columnWidths);

        // Header row
        BaseColor headerColor = new BaseColor(51, 102, 153);

        addTableHeader(table, "Description", headerColor);
        addTableHeader(table, "Qty", headerColor);
        addTableHeader(table, "Rate (₹)", headerColor);
        addTableHeader(table, "Amount (₹)", headerColor);

        // Data row
        addTableCell(table, invoice.getServiceDetails() != null ? invoice.getServiceDetails() : "Service");
        addTableCell(table, invoice.getQty() != null ? String.valueOf(invoice.getQty()) : "0");
        addTableCell(table, formatCurrency(invoice.getCostPerQty()));
        addTableCell(table, formatCurrency(invoice.getAmountPayable()));

        document.add(table);
    }

    private void addTotalsTable(Document document, Invoice invoice) throws DocumentException {
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(50);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

        // Total Amount
        addTotalRow(totalsTable, "Total Amount:", formatCurrency(invoice.getAmountPayable()), true);

        // Amount Paid (if partial payment)
        if (invoice.isPartialPayment()) {
            addTotalRow(totalsTable, "Amount Paid:",
                    formatCurrency(invoice.getAmountPaid()), false);

            // Balance Due
            Font balanceFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(255, 102, 0));
            addTotalRow(totalsTable, "Balance Due:", formatCurrency(invoice.getBalance()), true, balanceFont);
        }

        document.add(totalsTable);
    }

    private void addFooter(Document document, Invoice invoice) throws DocumentException {
        // Thank you message
        Paragraph thanks = new Paragraph("Thank you for your business!", NORMAL_FONT);
        thanks.setSpacingBefore(40);
        thanks.setAlignment(Element.ALIGN_CENTER);
        document.add(thanks);

        // Terms
        Paragraph terms = new Paragraph(
                "Payment is due within 30 days. Please include invoice number with your payment.",
                new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, new BaseColor(128, 128, 128)));
        terms.setSpacingBefore(10);
        terms.setAlignment(Element.ALIGN_CENTER);
        document.add(terms);
    }

    private void addDetailRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, HEADER_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(5);
        table.addCell(valueCell);
    }

    private void addTableHeader(PdfPTable table, String text, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_HEADER_FONT));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(10);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_FONT));
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String value, boolean isBold) {
        Font font = isBold ? BOLD_FONT : NORMAL_FONT;

        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private void addTotalRow(PdfPTable table, String label, String value, boolean isBold, Font customFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, customFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, customFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        return CURRENCY_FORMAT.format(amount);
    }
}
