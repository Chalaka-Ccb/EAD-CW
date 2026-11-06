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
