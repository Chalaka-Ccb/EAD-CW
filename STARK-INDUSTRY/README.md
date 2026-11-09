# STARK INDUSTRY — Coursework setup and demonstration

## Requirements
- Java 17 JDK and Maven (or included Maven wrapper)
- MySQL 8 running locally
- IntelliJ IDEA (or any Java IDE)
- Jaspersoft Studio 6.20.x is optional for editing the included JRXML report designs; the JasperReports 6.20.6 library is installed by Maven.

## Database setup
1. Create the schema and sample records by running `Database/starkdb.sql` in MySQL. **Warning:** the script starts with `DROP DATABASE IF EXISTS stark_industry_db` and will erase an existing database with that name.
2. Set environment variables in the IntelliJ Run Configuration for `com.example.starkindustry.Main`:
   - `STARK_DB_URL=jdbc:mysql://localhost:3306/stark_industry_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
   - `STARK_DB_USER=root` (or a dedicated MySQL account)
   - `STARK_DB_PASSWORD=<your local password>`
3. Do not commit real database passwords.

## Run and package
Open `STARK-INDUSTRY/pom.xml` as a Maven project in IntelliJ, set Project SDK to Java 17, and reload Maven dependencies.

Run `com.example.starkindustry.Main` from IntelliJ, or use:

```sh
./mvnw clean package
java -jar target/STARK-INDUSTRY-1.0.jar
```

On Windows use `mvnw.cmd clean package` instead. The packaged JAR and report generation still need to be verified on a machine with Java 17 and MySQL.

## JasperReports demonstration
The source designs live in `src/main/resources/reports/` and are loaded from the runtime classpath.

- **Restock and Profit Decision Report:** choose a date range in Reports. Joins `parts`, `categories`, `invoice_items`, and `sales_invoices` to show quantity sold, sales revenue, estimated gross profit, stock remaining, and reorder recommendation. Gross profit uses the **current** part cost as an estimate; historical cost changes are not recorded in the schema.
- **Invoice Report:** enter an existing invoice ID. Joins `sales_invoices`, `customers`, `invoice_items`, and `parts` to show printable invoice line details.

Both reports use JasperViewer for preview/printing. The viewer can export reports using JasperReports-supported formats. Test with a valid invoice and a date range containing sample sales.

## Coursework demonstration checklist
- [ ] Login and navigate dashboard
- [ ] Add/edit customer and inventory data; show validation messages
- [ ] Complete a sale and verify stock/database transaction
- [ ] Generate a multi-table decision report for a date range
- [ ] Explain why low-stock parts are recommended for reorder
- [ ] Open and print an invoice
- [ ] Package and launch the application outside IntelliJ

## Known limitations
- Build and MySQL integration tests have not been run by the GitHub connector.
- The sample database contains plaintext demonstration credentials; replace with password hashing and controlled access before real deployment.
- The report's gross profit is an estimate and does not allocate invoice-level discounts.

## Advanced coursework branch: Sales History and Returns

This branch adds a new **Sales History & Returns** navigation page, invoice search, original and refunded totals, item-level partial returns, a return reason and timestamp, and an audit trail of the staff user who processed each return.

**Existing XAMPP database: Do NOT re-import `starkdb.sql`.** It drops and recreates the database, deleting your sales. Instead, open phpMyAdmin > SQL and run **`Database/add_sales_returns.sql` once**. The script creates the new `sales_returns` table without deleting existing records. The advanced application expects that table to exist.

### Returns demonstration
1. Start XAMPP MySQL and launch the application.
2. Open **Sales History & Returns**, search for a saved invoice and select it.
3. Select an invoice item, click **Return Selected Item**, enter a quantity and reason, then confirm.
4. The operation records a refund, increases stock, and records the return in history in **one database transaction**. A second return cannot exceed the original sold quantity.
5. Open Parts and Dashboard again to confirm the changed stock. Refunds are tracked separately from the original invoice totals, which remain as an audit record.
6. The refund is calculated from the original item price with the invoice's overall discount applied proportionally. Actual payment/refund settlement is outside this coursework demo.

### Windows packaging
Run `mvnw.cmd clean package` inside `STARK-INDUSTRY` using Java 17. Copy `target/STARK-INDUSTRY-1.0.jar` and `launch-stark.bat` into the same folder. Set `STARK_DB_URL`, `STARK_DB_USER`, and `STARK_DB_PASSWORD` in the launching Windows environment, then double-click `launch-stark.bat`. The MySQL database must remain accessible.

JasperViewer includes printing and export controls, including PDF. Use its export button after opening a report. This branch adds report summaries and reporting dates, but report rendering and shaded-JAR behavior must be checked locally.

### Regression checklist
- Login and dashboard work
- Add/edit parts and customers; invalid input is rejected
- New invoice saves and deducts stock exactly once
- Sales History lists that invoice
- Partial return adds stock and creates one return record
- Repeated return cannot exceed the sold quantity
- Reopening the invoice still shows the original sale
- Report viewer renders both reports and PDF export works
- Shaded JAR launches outside IntelliJ

### Limitations
- Return amounts are **recorded**; the application does not execute real card/bank refunds.
- Sales history shows original invoice totals and refund amounts separately, rather than rewriting an issued invoice.
- The management report currently reflects gross sales before refunds; consult Sales History for net amounts.
- Database integration, Jasper rendering and package tests require a local XAMPP/MySQL installation and have not been executed through the GitHub connector.
