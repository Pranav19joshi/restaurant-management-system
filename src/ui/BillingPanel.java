package ui;

import enums.PaymentMethod;
import exception.PaymentException;
import model.Bill;
import model.Order;
import service.BillingService;
import service.OrderService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BillingPanel extends JPanel {
    private final BillingService billingService;
    private final OrderService orderService;

    private JComboBox<String> orderCombo;
    private JTextField discountField, amountPaidField;
    private JComboBox<PaymentMethod> paymentCombo;
    private JTextArea receiptArea;
    private JLabel billIdLabel, totalLabel;

    private String currentBillId = null;

    public BillingPanel(BillingService billingService, OrderService orderService) {
        this.billingService = billingService;
        this.orderService = orderService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(30, 30, 40));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("Billing & Payment", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(255, 140, 40));
        add(title, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.4);
        splitPane.setDividerSize(6);
        splitPane.setBackground(new Color(30, 30, 40));
        splitPane.setLeftComponent(buildControlPanel());
        splitPane.setRightComponent(buildReceiptPanel());
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel buildControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(35, 35, 48));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 140, 40)), " Billing Controls ",
                0, 0, new Font("SansSerif", Font.BOLD, 12), new Color(255, 140, 40)));

        // Select Order
        panel.add(Box.createVerticalStrut(8));
        panel.add(makeLabel("  Select Order:"));
        orderCombo = new JComboBox<>();
        orderCombo.setBackground(new Color(50, 50, 65));
        orderCombo.setForeground(Color.WHITE);
        orderCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        loadOrders();
        panel.add(orderCombo);

        JButton generateBtn = makeButton("Generate Bill", new Color(255, 140, 40));
        generateBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        generateBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        generateBtn.addActionListener(e -> generateBill());
        panel.add(Box.createVerticalStrut(6));
        panel.add(generateBtn);

        panel.add(Box.createVerticalStrut(12));
        panel.add(makeSeparator());

        // Bill info
        billIdLabel = makeLabel("  Bill ID: ---");
        totalLabel  = makeLabel("  Total:   Rs. ---");
        panel.add(Box.createVerticalStrut(6));
        panel.add(billIdLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(totalLabel);

        panel.add(Box.createVerticalStrut(12));
        panel.add(makeSeparator());

        // Discount
        panel.add(Box.createVerticalStrut(8));
        panel.add(makeLabel("  Discount Amount (Rs):"));
        discountField = new JTextField("0");
        styleField(discountField);
        discountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panel.add(discountField);

        JButton discountBtn = makeButton("Apply Discount", new Color(100, 160, 80));
        discountBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        discountBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        discountBtn.addActionListener(e -> applyDiscount());
        panel.add(Box.createVerticalStrut(4));
        panel.add(discountBtn);

        panel.add(Box.createVerticalStrut(12));
        panel.add(makeSeparator());

        // Payment
        panel.add(Box.createVerticalStrut(8));
        panel.add(makeLabel("  Payment Method:"));
        paymentCombo = new JComboBox<>(PaymentMethod.values());
        paymentCombo.setBackground(new Color(50, 50, 65));
        paymentCombo.setForeground(Color.WHITE);
        paymentCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panel.add(paymentCombo);

        panel.add(Box.createVerticalStrut(6));
        panel.add(makeLabel("  Amount Paid (Rs):"));
        amountPaidField = new JTextField("0");
        styleField(amountPaidField);
        amountPaidField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panel.add(amountPaidField);

        JButton payBtn = makeButton("Process Payment", new Color(60, 180, 100));
        payBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        payBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        payBtn.addActionListener(e -> processPayment());
        panel.add(Box.createVerticalStrut(6));
        panel.add(payBtn);

        panel.add(Box.createVerticalStrut(10));

        JButton refreshOrdersBtn = makeButton("Refresh Orders", new Color(100, 100, 160));
        refreshOrdersBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        refreshOrdersBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        refreshOrdersBtn.addActionListener(e -> loadOrders());
        panel.add(refreshOrdersBtn);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel buildReceiptPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(35, 35, 48));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 140, 40)), " Receipt ",
                0, 0, new Font("SansSerif", Font.BOLD, 12), new Color(255, 140, 40)));

        receiptArea = new JTextArea();
        receiptArea.setFont(new Font("Courier New", Font.PLAIN, 13));
        receiptArea.setBackground(new Color(20, 20, 30));
        receiptArea.setForeground(new Color(180, 255, 180));
        receiptArea.setEditable(false);
        receiptArea.setText("\n  Generate a bill to see the receipt here.");
        panel.add(new JScrollPane(receiptArea), BorderLayout.CENTER);
        return panel;
    }

    private void loadOrders() {
        orderCombo.removeAllItems();
        List<Order> orders = orderService.getAllOrders();
        orders.sort((a, b) -> b.getOrderId().compareTo(a.getOrderId()));
        for (Order o : orders) {
            orderCombo.addItem(o.getOrderId() + " | Table " + o.getTableNumber() + " | " + o.getCustomerName() + " [" + o.getStatus() + "]");
        }
    }

    private String getSelectedOrderId() {
        String selected = (String) orderCombo.getSelectedItem();
        if (selected == null) return null;
        return selected.split(" ")[0];  // first token is order ID
    }

    private void generateBill() {
        String orderId = getSelectedOrderId();
        if (orderId == null) { showError("Select an order."); return; }
        try {
            Bill bill = billingService.generateBill(orderId);
            currentBillId = bill.getBillId();
            billIdLabel.setText("  Bill ID: " + bill.getBillId());
            totalLabel.setText(String.format("  Total:   Rs. %.2f", bill.getTotal()));
            receiptArea.setText(bill.generateReceiptText());
            amountPaidField.setText(String.format("%.2f", bill.getTotal()));
        } catch (PaymentException ex) {
            showError(ex.getMessage());
        }
    }

    private void applyDiscount() {
        if (currentBillId == null) { showError("Generate a bill first."); return; }
        try {
            double discount = Double.parseDouble(discountField.getText().trim());
            billingService.applyDiscount(currentBillId, discount);
            Bill bill = billingService.getBill(currentBillId);
            totalLabel.setText(String.format("  Total:   Rs. %.2f", bill.getTotal()));
            receiptArea.setText(bill.generateReceiptText());
            amountPaidField.setText(String.format("%.2f", bill.getTotal()));
        } catch (NumberFormatException ex) {
            showError("Enter a valid discount amount.");
        } catch (PaymentException ex) {
            showError(ex.getMessage());
        }
    }

    private void processPayment() {
        if (currentBillId == null) { showError("Generate a bill first."); return; }
        try {
            PaymentMethod method = (PaymentMethod) paymentCombo.getSelectedItem();
            double paid = Double.parseDouble(amountPaidField.getText().trim());
            double change = billingService.processPayment(currentBillId, method, paid);
            Bill bill = billingService.getBill(currentBillId);
            receiptArea.setText(bill.generateReceiptText());
            JOptionPane.showMessageDialog(this,
                    String.format("Payment successful!\nChange returned: Rs. %.2f", change),
                    "Payment Done", JOptionPane.INFORMATION_MESSAGE);
            currentBillId = null;
            billIdLabel.setText("  Bill ID: ---");
            totalLabel.setText("  Total:   Rs. ---");
        } catch (NumberFormatException ex) {
            showError("Enter a valid amount paid.");
        } catch (PaymentException ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(200, 200, 220));
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JSeparator makeSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(80, 80, 100));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private void styleField(JTextField f) {
        f.setBackground(new Color(50, 50, 65));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 110)));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }
}
