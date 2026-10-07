package com.example.starkindustry.ui;

import com.example.starkindustry.dao.DashboardDAO;
import com.example.starkindustry.model.Part;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

/** Dashboard: key numbers, top selling parts chart and the low stock list. */
public class DashboardPanel extends JPanel {

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    private final JLabel lblTodaySales = new JLabel("-");
    private final JLabel lblMonthSales = new JLabel("-");
    private final JLabel lblLowStock = new JLabel("-");
    private final JLabel lblCustomers = new JLabel("-");

    private final JPanel chartArea = new JPanel(new BorderLayout());
    private final DefaultTableModel lowStockModel = new DefaultTableModel(
            new String[]{"Part No", "Name", "Stock", "Reorder"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Dashboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        add(title, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 15, 0));
        cards.add(makeCard("Today's Sales (Rs.)", lblTodaySales, new Color(46, 125, 50)));
        cards.add(makeCard("This Month (Rs.)", lblMonthSales, new Color(21, 101, 192)));
        cards.add(makeCard("Low Stock Parts", lblLowStock, new Color(198, 40, 40)));
        cards.add(makeCard("Customers", lblCustomers, new Color(106, 27, 154)));

        chartArea.setBorder(BorderFactory.createTitledBorder("Top Selling Parts (units sold)"));

        JTable lowStockTable = new JTable(lowStockModel);
        lowStockTable.setRowHeight(24);
        JScrollPane lowStockScroll = new JScrollPane(lowStockTable);
        lowStockScroll.setBorder(BorderFactory.createTitledBorder("Needs Reordering"));
        lowStockScroll.setPreferredSize(new Dimension(380, 0));

        JPanel lower = new JPanel(new BorderLayout(15, 0));
        lower.add(chartArea, BorderLayout.CENTER);
        lower.add(lowStockScroll, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.add(cards, BorderLayout.NORTH);
        center.add(lower, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private JPanel makeCard(String caption, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        Border line = BorderFactory.createMatteBorder(0, 6, 0, 0, accent);
        Border pad = BorderFactory.createEmptyBorder(18, 16, 18, 16);
        card.setBorder(BorderFactory.createCompoundBorder(line, pad));
        card.setBackground(new Color(245, 247, 250));

        JLabel cap = new JLabel(caption);
        cap.setForeground(Color.GRAY);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 28f));
        valueLabel.setForeground(accent);

        card.add(cap, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    /** Called every time the dashboard is shown: reloads all numbers, the chart and the list. */
    public void refresh() {
        try {
            lblTodaySales.setText(String.format("%,.2f", dashboardDAO.getTodaySales()));
            lblMonthSales.setText(String.format("%,.2f", dashboardDAO.getMonthSales()));
            lblLowStock.setText(String.valueOf(dashboardDAO.getLowStockCount()));
            lblCustomers.setText(String.valueOf(dashboardDAO.getCustomerCount()));

            showChart(dashboardDAO.getTopSellingParts(5));

            lowStockModel.setRowCount(0);
            for (Part p : dashboardDAO.getLowStockParts()) {
                lowStockModel.addRow(new Object[]{p.getPartNo(), p.getName(), p.getStockQty(), p.getReorderLevel()});
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showChart(Map<String, Integer> topParts) {
        chartArea.removeAll();
        if (topParts.isEmpty()) {
            chartArea.add(new JLabel("No sales yet", SwingConstants.CENTER), BorderLayout.CENTER);
        } else {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Map.Entry<String, Integer> e : topParts.entrySet()) {
                dataset.addValue(e.getValue(), "Units sold", e.getKey());
            }
            JFreeChart chart = ChartFactory.createBarChart(
                    null, null, "Units sold", dataset, PlotOrientation.VERTICAL, false, true, false);

            chart.setBackgroundPaint(Color.WHITE);
            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setRangeGridlinePaint(new Color(210, 210, 210));
            ((NumberAxis) plot.getRangeAxis()).setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, new Color(21, 101, 192));
            renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
            renderer.setMaximumBarWidth(0.12);

            chartArea.add(new ChartPanel(chart), BorderLayout.CENTER);
        }
        chartArea.revalidate();
        chartArea.repaint();
    }
}