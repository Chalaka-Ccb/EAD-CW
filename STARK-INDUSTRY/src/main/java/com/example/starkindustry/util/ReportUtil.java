package com.example.starkindustry.util;

import com.example.starkindustry.config.DBConnection;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/** Runs the Jasper reports (the .jrxml files in src/main/resources/reports) and shows them in a viewer. */
public class ReportUtil {

    /** BIG report: what sold, what profit it made and what must be reordered in a date range. */
    public static void showRestockReport(LocalDate from, LocalDate to) {
        Map<String, Object> params = new HashMap<>();
        params.put("FROM_DATE", java.sql.Date.valueOf(from));
        params.put("TO_DATE", java.sql.Date.valueOf(to));
        show("/reports/RestockProfitReport.jrxml", params, "Restock and Profit Decision Report");
    }

    /** SMALL report: one printable invoice. */
    public static void showInvoice(int invoiceId) {
        Map<String, Object> params = new HashMap<>();
        params.put("INVOICE_ID", invoiceId);
        show("/reports/InvoiceReport.jrxml", params, "Invoice #" + invoiceId);
    }

    private static void show(String reportFile, Map<String, Object> params, String windowTitle) {
        try (InputStream in = ReportUtil.class.getResourceAsStream(reportFile)) {
            if (in == null) {
                throw new RuntimeException("Report file not found: " + reportFile);
            }
            JasperReport report = JasperCompileManager.compileReport(in);          // .jrxml -> compiled report
            JasperPrint print = JasperFillManager.fillReport(                      // run the SQL and fill it
                    report, params, DBConnection.getInstance().getConnection());

            if (print.getPages().isEmpty()) {
                throw new RuntimeException("No data found for this report.");
            }
            JasperViewer viewer = new JasperViewer(print, false);   // false = don't exit the app on close
            viewer.setTitle(windowTitle);
            viewer.setVisible(true);
        } catch (JRException | IOException e) {
            throw new RuntimeException("Could not generate the report: " + e.getMessage(), e);
        }
    }
}