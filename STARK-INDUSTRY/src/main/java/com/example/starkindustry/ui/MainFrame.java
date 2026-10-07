//package com.example.starkindustry.ui;
//
//import com.example.starkindustry.model.User;
//
//import javax.swing.*;
//import java.awt.*;
//
//public class MainFrame extends JFrame {
//
//    private final User loggedUser;
//    private final CardLayout cardLayout = new CardLayout();
//    private final JPanel content = new JPanel(cardLayout);
//
//    private final DashboardPanel dashboardPanel = new DashboardPanel();
//    private final InvoicePanel invoicePanel;
//    private final PartPanel partPanel = new PartPanel();
//    private final CustomerPanel customerPanel = new CustomerPanel();
//
//    public MainFrame(User loggedUser) {
//        this.loggedUser = loggedUser;
//        this.invoicePanel = new InvoicePanel(loggedUser);
//
//        setTitle("Spare Part Shop - Sales System");
//        setDefaultCloseOperation(EXIT_ON_CLOSE);
//        setSize(1200, 720);
//        setLocationRelativeTo(null);
//
//        add(buildHeader(), BorderLayout.NORTH);
//        add(buildSidebar(), BorderLayout.WEST);
//        add(buildContent(), BorderLayout.CENTER);
//
//        showScreen("DASHBOARD");
//    }
//
//    // ---------- header ----------
//    private JPanel buildHeader() {
//        JPanel header = new JPanel(new BorderLayout());
//        header.setBackground(new Color(30, 40, 55));
//        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
//
//        JLabel name = new JLabel("SPARE PART SHOP");
//        name.setForeground(Color.WHITE);
//        name.setFont(name.getFont().deriveFont(Font.BOLD, 18f));
//
//        JLabel user = new JLabel("Logged in: " + loggedUser.getFullName());
//        user.setForeground(new Color(200, 210, 220));
//
//        header.add(name, BorderLayout.WEST);
//        header.add(user, BorderLayout.EAST);
//        return header;
//    }
//
//    // ---------- left menu ----------
//    private JPanel buildSidebar() {
//        JPanel side = new JPanel();
//        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
//        side.setBackground(new Color(44, 57, 75));
//        side.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
//        side.setPreferredSize(new Dimension(190, 0));
//
//        side.add(menuButton("Dashboard", "DASHBOARD"));
//        side.add(menuButton("New Invoice", "INVOICE"));
//        side.add(menuButton("Parts", "PARTS"));
//        side.add(menuButton("Customers", "CUSTOMERS"));
//        side.add(menuButton("Reports", "REPORTS"));
//        side.add(Box.createVerticalGlue());
//
//        JButton logout = menuButton("Logout", null);
//        logout.addActionListener(e -> logout());
//        side.add(logout);
//        return side;
//    }
//
//    private JButton menuButton(String text, String screenName) {
//        JButton b = new JButton(text);
//        b.setAlignmentX(Component.LEFT_ALIGNMENT);
//        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
//        b.setHorizontalAlignment(SwingConstants.LEFT);
//        b.setFocusPainted(false);
//        b.setForeground(Color.WHITE);
//        b.setBackground(new Color(44, 57, 75));
//        b.putClientProperty("JButton.buttonType", "borderless");
//        if (screenName != null) {
//            b.addActionListener(e -> showScreen(screenName));
//        }
//        return b;
//    }
//
//    // ---------- centre (screens) ----------
//    private JPanel buildContent() {
//        content.add(dashboardPanel, "DASHBOARD");
//        content.add(invoicePanel, "INVOICE");
//        content.add(partPanel, "PARTS");
//        content.add(customerPanel, "CUSTOMERS");
//        content.add(placeholder("Reports (later)"), "REPORTS");
//        return content;
//    }
//
//    private JPanel placeholder(String text) {
//        JPanel p = new JPanel(new BorderLayout());
//        p.add(new JLabel(text, SwingConstants.CENTER), BorderLayout.CENTER);
//        return p;
//    }
//
//    private void showScreen(String name) {
//        // reload fresh data each time a screen is shown
//        switch (name) {
//            case "DASHBOARD":
//                dashboardPanel.refresh();
//                break;
//            case "INVOICE":
//                invoicePanel.refresh();
//                break;
//            case "PARTS":
//                partPanel.refresh();
//                break;
//            default:
//                break;
//        }
//        cardLayout.show(content, name);
//    }
//
//    private void logout() {
//        int choice = JOptionPane.showConfirmDialog(this, "Log out now?", "Logout", JOptionPane.YES_NO_OPTION);
//        if (choice == JOptionPane.YES_OPTION) {
//            dispose();
//            new LoginFrame().setVisible(true);
//        }
//    }
//
//    public User getLoggedUser() {
//        return loggedUser;
//    }
//}


package com.example.starkindustry.ui;

import com.example.starkindustry.model.User;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final User loggedUser;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);

    private final DashboardPanel dashboardPanel = new DashboardPanel();
    private final InvoicePanel invoicePanel;
    private final PartPanel partPanel = new PartPanel();
    private final CustomerPanel customerPanel = new CustomerPanel();

    public MainFrame(User loggedUser) {
        this.loggedUser = loggedUser;
        this.invoicePanel = new InvoicePanel(loggedUser);

        setTitle("Spare Part Shop - Sales System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 720);
        setLocationRelativeTo(null);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);

        showScreen("DASHBOARD");
    }

    // ---------- header ----------
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 40, 55));
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel name = new JLabel("SPARE PART SHOP");
        name.setForeground(Color.WHITE);
        name.setFont(name.getFont().deriveFont(Font.BOLD, 18f));

        JLabel user = new JLabel("Logged in: " + loggedUser.getFullName());
        user.setForeground(new Color(200, 210, 220));

        header.add(name, BorderLayout.WEST);
        header.add(user, BorderLayout.EAST);
        return header;
    }

    // ---------- left menu ----------
    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(new Color(44, 57, 75));
        side.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        side.setPreferredSize(new Dimension(190, 0));

        side.add(menuButton("Dashboard", "DASHBOARD"));
        side.add(menuButton("New Invoice", "INVOICE"));
        side.add(menuButton("Parts", "PARTS"));
        side.add(menuButton("Customers", "CUSTOMERS"));
        side.add(menuButton("Reports", "REPORTS"));
        side.add(Box.createVerticalGlue());

        JButton logout = menuButton("Logout", null);
        logout.addActionListener(e -> logout());
        side.add(logout);
        return side;
    }

    private JButton menuButton(String text, String screenName) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false);
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(44, 57, 75));
        b.putClientProperty("JButton.buttonType", "borderless");
        if (screenName != null) {
            b.addActionListener(e -> showScreen(screenName));
        }
        return b;
    }

    // ---------- centre (screens) ----------
    private JPanel buildContent() {
        content.add(dashboardPanel, "DASHBOARD");
        content.add(invoicePanel, "INVOICE");
        content.add(partPanel, "PARTS");
        content.add(customerPanel, "CUSTOMERS");
        content.add(new ReportsPanel(), "REPORTS");
        return content;
    }

    private JPanel placeholder(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(text, SwingConstants.CENTER), BorderLayout.CENTER);
        return p;
    }

    private void showScreen(String name) {
        // reload fresh data each time a screen is shown
        switch (name) {
            case "DASHBOARD":
                dashboardPanel.refresh();
                break;
            case "INVOICE":
                invoicePanel.refresh();
                break;
            case "PARTS":
                partPanel.refresh();
                break;
            default:
                break;
        }
        cardLayout.show(content, name);
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this, "Log out now?", "Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    public User getLoggedUser() {
        return loggedUser;
    }
}