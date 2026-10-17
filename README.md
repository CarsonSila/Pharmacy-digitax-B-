# PharmaCore POS Backend (Tax-Compliant via DigiTax)

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=flat&logo=openjdk)](https://openjdk.org/)
[![Spring Boot 3.3](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL 8](https://img.shields.io/badge/MySQL-8.0+-4479A1?style=flat&logo=mysql)](https://www.mysql.com/)
[![DigiTax](https://img.shields.io/badge/KRA%20eTIMS-DigiTax%20API-0052CC)](https://digitax.tech)

A mission-critical, cloud-hosted **Retail Pharmacy Point-of-Sale (POS) & Tax Compliance Backend** designed to eliminate manual ETR double-entry and guarantee real-time **KRA eTIMS digital fiscalization** for every sale.

---

## 🌟 Key Architecture & Features

### 1. Automated KRA eTIMS Tax Engine (DigiTax)
* **Bifurcated Tax Classes**:
  * **Exempt (0.00%)**: Human prescription medicines (POM) and API medicaments under the VAT Act First Schedule.
  * **Zero-Rated (0.00%)**: Specified medical supplies and surgical inputs under the Second Schedule.
  * **Standard Rate (16.00%)**: Cosmetics, toiletries, nutritional supplements, and OTC personal care items.
* **KRA HS Code Mapping**: Pre-configured HS codes per SKU (e.g. `3004.10.00` for penicillins).
* **Exact BigDecimal Arithmetic**: Strict rounding (`RoundingMode.HALF_UP`) prevents 1-cent discrepancy rejections from tax authority gateways.

### 2. FEFO Batch Inventory & Expiry Gates
* **First-Expired, First-Out**: The system automatically allocates and deducts stock from the batch with the earliest valid expiration date.
* **Safety Lockouts**: Automatically excludes quarantined or expired batches from cashier selection.

### 3. Parent-Child Unit Conversions
* Supports selling medications by **Box (100 tablets)**, **Blister/Strip (10 tablets)**, or **Single Tablet/Capsule**, while atomically decrementing inventory in base units from the batch.

### 4. Resilient Offline-First Fiscal Queue
* **Fast-Path ($<1.2\text{s}$)**: Real-time synchronous signing with DigiTax; prints thermal receipt with official KRA QR verification link.
* **Offline Fallback**: In the event of network drops or tax gateway downtime, the invoice is marked as `QUEUED`, and an asynchronous scheduled worker (`FiscalSyncWorker`) retries automatically every 30 seconds with exponential backoff.

### 5. Thermal Receipt Generation (ESC/POS 80mm)
* Pre-formats clean 80mm thermal receipts with pharmacy header, itemized batch & expiry, tax breakdown (`[A]`, `[E]`, `[B]`), and scannable KRA eTIMS QR code string.

---

## 🗄️ Database Architecture

The schema is organized in `db/`:
* `db/setup_pharmacore_db.sql`: Complete DDL for `roles`, `users`, `categories`, `units_of_measure`, `tax_classifications`, `products`, `product_batches`, `product_unit_conversions`, `orders`, `order_items`, and `fiscal_invoices`.
* `db/seed_pharmacore_data.sql`: Seed data with realistic pharmaceuticals, batches, and unit conversions.

---

## 🚀 Getting Started

### Prerequisites
* **Java 21 LTS**
* **MySQL 8.0+** (running on port `3307` or `3306`)

### 1. Database Setup
```bash
mysql -P 3307 -u root -p < db/setup_pharmacore_db.sql
mysql -P 3307 -u root -p < db/seed_pharmacore_data.sql
```

### 2. Configure Environment (`src/main/resources/application.yml`)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/pharmacore_db?useSSL=false&serverTimezone=UTC
    username: root
    password: YOUR_PASSWORD

digitax:
  base-url: https://api.digitax.tech
  api-key: ${DIGITAX_API_KEY:your_sandbox_api_key}
  mock-mode: true # Set to false when using live DigiTax credentials
```

### 3. Run the Application
```bash
./gradlew bootRun
```

---

## 📡 Core API Endpoints

### 1. POS Checkout & Fiscalization
`POST /api/v1/pos/checkout`
```json
{
  "cashierId": 1,
  "buyerName": "Walk-in Customer",
  "buyerPin": "P051234567Z",
  "paymentMethod": "MPESA",
  "paymentReference": "SHK89201JD",
  "items": [
    {
      "productId": 1,
      "unitId": 2,
      "quantity": 2
    },
    {
      "productId": 3,
      "quantity": 1
    }
  ]
}
```

### 2. Product & Barcode Lookup
`GET /api/v1/pos/products?q=Amoxil`

### 3. FEFO Batches for Product
`GET /api/v1/pos/products/{productId}/batches`

### 4. Audit & Fiscal Invoices
`GET /api/v1/fiscal/invoices?status=COMPLETED`
