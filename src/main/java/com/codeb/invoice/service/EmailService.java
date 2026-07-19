package com.codeb.invoice.service;

import com.codeb.invoice.entity.Invoice;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Service for sending invoice emails
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final PdfGenerationService pdfGenerationService;

    @Value("${invoice.email.from:noreply@codebtech.com}")
    private String fromEmail;

    @Value("${invoice.company.name:Code-B Tech Solutions}")
    private String companyName;

    /**
     * Send invoice email with PDF attachment
     */
    public void sendInvoiceEmail(Invoice invoice) throws MessagingException {
        log.info("Sending invoice email for: {} to {}", invoice.getInvoiceNo(), invoice.getEmailId());

        if (invoice.getEmailId() == null || invoice.getEmailId().isBlank()) {
            throw new IllegalArgumentException("Email address is required");
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        try {
            helper.setFrom(fromEmail);
            helper.setTo(invoice.getEmailId());
            helper.setSubject("Invoice " + invoice.getInvoiceNo() + " from " + companyName);

            // Create email body
            String emailBody = createEmailBody(invoice);
            helper.setText(emailBody, true); // true for HTML

            // Generate and attach PDF
            byte[] pdfBytes = pdfGenerationService.generateInvoicePdf(invoice);
            ByteArrayResource pdfResource = new ByteArrayResource(pdfBytes);
            helper.addAttachment(invoice.getInvoiceNo() + ".pdf", pdfResource);

            // Send email
            mailSender.send(message);
            log.info("Invoice email sent successfully to {}", invoice.getEmailId());

        } catch (Exception e) {
            log.error("Failed to send invoice email for: {}", invoice.getInvoiceNo(), e);
            throw new MessagingException("Failed to send email", e);
        }
    }

    /**
     * Send invoice email without attachment (for preview)
     */
    public void sendInvoiceEmailPreview(Invoice invoice) throws MessagingException {
        log.info("Sending invoice email preview for: {} to {}", invoice.getInvoiceNo(), invoice.getEmailId());

        if (invoice.getEmailId() == null || invoice.getEmailId().isBlank()) {
            throw new IllegalArgumentException("Email address is required");
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(invoice.getEmailId());
        helper.setSubject("Invoice " + invoice.getInvoiceNo() + " from " + companyName + " [PREVIEW]");

        String emailBody = createEmailBody(invoice);
        helper.setText(emailBody, true);

        mailSender.send(message);
        log.info("Preview email sent successfully to {}", invoice.getEmailId());
    }

    /**
     * Create HTML email body
     */
    private String createEmailBody(Invoice invoice) {
        String paymentStatus = invoice.isPartialPayment() ? "PARTIAL PAYMENT" : "FULL PAYMENT";
        String statusColor = invoice.isPartialPayment() ? "#FF9900" : "#00994C";

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #336699; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .invoice-box { background: white; padding: 20px; border: 1px solid #ddd; }
                    .status { display: inline-block; padding: 5px 15px; border-radius: 3px; color: white; font-weight: bold; }
                    table { width: 100%%; border-collapse: collapse; margin: 20px 0; }
                    th { background-color: #336699; color: white; padding: 10px; text-align: left; }
                    td { padding: 10px; border-bottom: 1px solid #ddd; }
                    .total { font-weight: bold; font-size: 18px; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>INVOICE</h1>
                        <p>%s</p>
                    </div>

                    <div class="content">
                        <div class="invoice-box">
                            <p><strong>Invoice Number:</strong> %s</p>
                            <p><strong>Status:</strong> <span class="status" style="background-color: %s">%s</span></p>
                            <p><strong>Date:</strong> %s</p>

                            <hr>

                            <p><strong>Bill To:</strong></p>
                            <p>%s</p>
                            %s

                            <hr>

                            <table>
                                <tr>
                                    <th>Description</th>
                                    <th>Qty</th>
                                    <th>Rate</th>
                                    <th>Amount</th>
                                </tr>
                                <tr>
                                    <td>%s</td>
                                    <td>%d</td>
                                    <td>₹%.2f</td>
                                    <td>₹%.2f</td>
                                </tr>
                            </table>

                            <p class="total">Total Amount: ₹%.2f</p>
                            %s
                        </div>

                        <p style="margin-top: 20px;">
                            Please find the attached PDF invoice for your records.
                            Payment is due within 30 days.
                        </p>
                    </div>

                    <div class="footer">
                        <p>Thank you for your business!</p>
                        <p>%s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                invoice.getInvoiceNo(),
                invoice.getInvoiceNo(),
                statusColor, paymentStatus,
                invoice.getCreatedAt() != null ? invoice.getCreatedAt().toLocalDate().toString() : "",
                invoice.getChainName() != null ? invoice.getChainName() : "",
                invoice.getGstNumber() != null ? "<p>GST No: " + invoice.getGstNumber() + "</p>" : "",
                invoice.getServiceDetails() != null ? invoice.getServiceDetails() : "",
                invoice.getQty() != null ? invoice.getQty() : 0,
                invoice.getCostPerQty() != null ? invoice.getCostPerQty().doubleValue() : 0.0,
                invoice.getAmountPayable() != null ? invoice.getAmountPayable().doubleValue() : 0.0,
                invoice.getAmountPayable() != null ? invoice.getAmountPayable().doubleValue() : 0.0,
                invoice.isPartialPayment() ?
                    "<p style='color: #FF6600;'><strong>Balance Due:</strong> ₹" +
                        (invoice.getBalance() != null ? invoice.getBalance().doubleValue() : 0.0) + "</p>" : "",
                companyName
        );
    }
}
