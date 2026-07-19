# Invoice Management System

A Spring Boot based web application for managing estimates and generating professional GST-compliant invoices. The system allows users to approve estimates, generate invoices, download them as PDF files, and send invoices directly through email.

This project was developed as part of the **Code-B Java Internship** to demonstrate backend development using Spring Boot and MySQL.

---

## Features

- Create and manage customer estimates
- Approve estimates before invoice generation
- Generate invoices from approved estimates
- GST-ready invoice details
- Download invoices as PDF
- Preview invoices before downloading
- Send invoices via email
- Track full and partial payments
- Search invoices using invoice number, company name, or chain
- Edit invoice details
- Clean dashboard with invoice statistics

---

## Tech Stack

| Technology | Version |
|------------|---------|
| Java | 17 |
| Spring Boot | 3.2.0 |
| Spring Data JPA | 3.2.0 |
| Thymeleaf | 3.1 |
| MySQL | 8.x |
| Maven | 3.x |
| iText PDF | 5.5.13 |
| Lombok | 1.18.30 |

---

## Project Structure

```
InvoiceManagementSystem
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.codeb.invoice
│   │   │       ├── controller
│   │   │       ├── entity
│   │   │       ├── repository
│   │   │       ├── service
│   │   │       ├── config
│   │   │       └── util
│   │   │
│   │   └── resources
│   │       ├── templates
│   │       │   ├── invoice
│   │       │   ├── estimate
│   │       │   └── index.html
│   │       ├── static
│   │       ├── db
│   │       └── application.properties
│   │
│   └── test
│
├── pom.xml
└── README.md
```

---

## Prerequisites

Before running the project, make sure you have:

- Java 17 or above
- MySQL Server
- Maven
- IntelliJ IDEA (recommended)

---

## Database Setup

Create a database in MySQL.

```sql
CREATE DATABASE invoice_db;
```

Update your `application.properties` file.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/invoice_db
spring.datasource.username=root
spring.datasource.password=your_password
```

Run the SQL script available in:

```
src/main/resources/db/schema.sql
```

---

## Running the Project

Clone the repository.

```bash
git clone https://github.com/your-username/invoice-management-system.git
```

Move into the project directory.

```bash
cd invoice-management-system
```

Build the project.

```bash
mvn clean install
```

Run the application.

```bash
mvn spring-boot:run
```

Or simply run

```
InvoiceManagementApplication.java
```

from IntelliJ IDEA.

---

## Application URLs

| Page | URL |
|------|-----|
| Home | http://localhost:8080 |
| Invoice Dashboard | http://localhost:8080/invoices |
| Estimate Dashboard | http://localhost:8080/estimates |

---

## Workflow

```
Create Estimate
        │
        ▼
Approve Estimate
        │
        ▼
Generate Invoice
        │
        ▼
View Invoice
        │
 ┌──────┴────────┐
 ▼               ▼
Download PDF   Send Email
```

---

## Main Modules

### Estimate Management

- View all estimates
- Approve pending estimates
- Convert approved estimates into invoices

### Invoice Management

- Generate invoices
- View invoice details
- Edit invoice information
- Download invoice as PDF
- Email invoices to customers

### Dashboard

- Total invoices
- Generated invoices
- Paid invoices
- Partial payment invoices

---

## Search Functionality

Invoices can be searched using:

- Invoice Number
- Company Name
- Chain
- Estimate ID

---

## PDF Generation

Invoices can be downloaded in PDF format using the integrated iText library.

Each PDF contains:

- Company Information
- Customer Details
- GST Number
- Invoice Number
- Service Details
- Payment Summary

---

## Email Support

The application can send invoices directly to customers through Gmail SMTP.

Update the following properties before using email:

```properties
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
```

---

## Screenshots

You can add screenshots here.

```
Home Dashboard

Estimate List

Invoice Details

Invoice PDF
```

---

## Future Improvements

- User Authentication
- Role-Based Access Control
- Customer Management
- Payment Gateway Integration
- Invoice Analytics Dashboard
- Export to Excel
- REST API Documentation using Swagger

---


---

## License

This project was developed as part of the **Code-B Java Internship** for learning and demonstration purposes.
