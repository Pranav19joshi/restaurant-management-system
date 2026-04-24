package ui;

import enums.OrderStatus;
import exception.OrderException;
import model.MenuItem;
import model.Order;
import repository.MenuRepository;
import service.OrderService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class OrderPanel extends JPanel {
    private final OrderService orderService;
    private final MenuRepository menuRepository;

    // Left form
    private JTextField tableField, customerField;
    private JTable menuSelectTable;
    private DefaultTableModel menuSelectModel;

    // Right status
    private JTable orderStatusTable;
    private DefaultTableModel orderStatusModel;

    private java.util.Timer refreshTimer;

    public OrderPanel(OrderService orderService, MenuRepository menuRepository) {
        this.orderService = orderService;
        this.menuRepository = menuRepository;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(30, 30, 40));
        buildUI();
        startAutoRefresh();
    }

    private void buildUI() {
        JLabel title = new JLabel("Order Management", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(60, 200, 220));
        add(title, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.45);
        splitPane.setBackground(new Color(30, 30, 40));
        splitPane.setDividerSize(6);

        splitPane.setLeftComponent(buildOrderForm());
        splitPane.setRightComponent(buildOrderStatus());

        add(splitPane, BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildOrderForm() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(new Color(35, 35, 48));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 200, 220)), " New Order ",
                0, 0, new Font("SansSerif", Font.BOLD, 12), new Color(60, 200, 220)));

        // Top: table + customer
        JPanel topForm = new JPanel(new GridLayout(2, 2, 6, 4));
        topForm.setBackground(new Color(35, 35, 48));
        tableField    = new JTextField();
        customerField = new JTextField();
        styleField(tableField); styleField(customerField);
        topForm.add(makeLabel("Table No:"));   topForm.add(tableField);
        topForm.add(makeLabel("Customer:"));   topForm.add(customerField);
        panel.add(topForm, BorderLayout.NORTH);

        // Center: menu item selection
        String[] cols = {"ID", "Name", "Type", "Price (Rs)"};
        menuSelectModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        menuSelectTable = new JTable(menuSelectModel);
        styleTable(menuSelectTable);
        menuSelectTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane sp = new JScrollPane(menuSelectTable);
        sp.getViewport().setBackground(new Color(40, 40, 55));

        JLabel hint = makeLabel(" Ctrl+Click to select multiple items");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 10));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 2));
        centerPanel.setBackground(new Color(35, 35, 48));
        centerPanel.add(makeLabel(" Select Items:"), BorderLayout.NORTH);
        centerPanel.add(sp, BorderLayout.CENTER);
        centerPanel.add(hint, BorderLayout.SOUTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        refreshMenuSelection();
        return panel;
    }

    private JPanel buildOrderStatus() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(new Color(35, 35, 48));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 200, 220)), " Live Order Status ",
                0, 0, new Font("SansSerif", Font.BOLD, 12), new Color(60, 200, 220)));

        String[] cols = {"Order ID", "Table", "Customer", "Status", "Total (Rs)", "Time"};
        orderStatusModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        orderStatusTable = new JTable(orderStatusModel);
        styleTable(orderStatusTable);
        JScrollPane sp = new JScrollPane(orderStatusTable);
        sp.getViewport().setBackground(new Color(40, 40, 55));
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildButtonPanel() {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        btnPanel.setBackground(new Color(30, 30, 40));

        JButton placeBtn  = makeButton("Place Order",   new Color(60, 180, 100));
        JButton serveBtn  = makeButton("Mark Served",   new Color(255, 160, 40));
        JButton cancelBtn = makeButton("Cancel Order",  new Color(200, 70, 70));
        JButton refreshBtn= makeButton("Refresh Now",   new Color(100, 100, 160));

        placeBtn.addActionListener(e -> placeOrder());
        serveBtn.addActionListener(e -> markServed());
        cancelBtn.addActionListener(e -> cancelOrder());
        refreshBtn.addActionListener(e -> refreshOrderStatus());

        btnPanel.add(placeBtn); btnPanel.add(serveBtn);
        btnPanel.add(cancelBtn); btnPanel.add(refreshBtn);
        return btnPanel;
    }

    private void placeOrder() {
        try {
            String tableStr = tableField.getText().trim();
            String customer = customerField.getText().trim();
            if (tableStr.isEmpty()) { showError("Enter table number."); return; }
            int tableNo = Integer.parseInt(tableStr);

            int[] selectedRows = menuSelectTable.getSelectedRows();
            if (selectedRows.length == 0) { showError("Select at least one item."); return; }

            List<MenuItem> available = menuRepository.getAvailableItems();
            List<MenuItem> selected = new ArrayList<>();
            for (int row : selectedRows) {
                String itemId = (String) menuSelectModel.getValueAt(row, 0);
                for (MenuItem item : available) {
                    if (item.getItemId().equals(itemId)) { selected.add(item); break; }
                }
            }

            Order order = orderService.placeOrder(tableNo, customer, selected);
            JOptionPane.showMessageDialog(this,
                    "Order placed!\nID: " + order.getOrderId() + "\nItems: " + selected.size(),
                    "Order Placed", JOptionPane.INFORMATION_MESSAGE);
            tableField.setText(""); customerField.setText("");
            menuSelectTable.clearSelection();
            refreshOrderStatus();
        } catch (NumberFormatException ex) {
            showError("Table number must be a number.");
        } catch (OrderException ex) {
            showError(ex.getMessage());
        }
    }

    private void markServed() {
        int row = orderStatusTable.getSelectedRow();
        if (row < 0) { showError("Select an order to mark served."); return; }
        String orderId = (String) orderStatusModel.getValueAt(row, 0);
        try {
            orderService.markAsServed(orderId);
            refreshOrderStatus();
        } catch (OrderException ex) {
            showError(ex.getMessage());
        }
    }

    private void cancelOrder() {
        int row = orderStatusTable.getSelectedRow();
        if (row < 0) { showError("Select an order to cancel."); return; }
        String orderId = (String) orderStatusModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Cancel order " + orderId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            orderService.cancelOrder(orderId);
            refreshOrderStatus();
        } catch (OrderException ex) {
            showError(ex.getMessage());
        }
    }

    public void refreshMenuSelection() {
        menuSelectModel.setRowCount(0);
        for (MenuItem item : menuRepository.getAvailableItems()) {
            menuSelectModel.addRow(new Object[]{
                    item.getItemId(), item.getName(),
                    item.getItemType(), String.format("%.2f", item.getTaxedPrice())
            });
        }
    }

    public void refreshOrderStatus() {
        SwingUtilities.invokeLater(() -> {
            orderStatusModel.setRowCount(0);
            List<Order> orders = orderService.getAllOrders();
            // sort: newest first approximately by ID descending
            orders.sort((a, b) -> b.getOrderId().compareTo(a.getOrderId()));
            for (Order o : orders) {
                orderStatusModel.addRow(new Object[]{
                        o.getOrderId(), o.getTableNumber(), o.getCustomerName(),
                        o.getStatus().toString(),
                        String.format("%.2f", o.getOrderTotal()),
                        o.getOrderTime()
                });
            }
        });
    }

    private void startAutoRefresh() {
        refreshTimer = new java.util.Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() { refreshOrderStatus(); }
        }, 3000, 3000);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(200, 200, 220));
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return lbl;
    }

    private void styleField(JTextField f) {
        f.setBackground(new Color(50, 50, 65));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 110)));
    }

    private void styleTable(JTable table) {
        table.setBackground(new Color(40, 40, 55));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(60, 60, 80));
        table.getTableHeader().setBackground(new Color(50, 50, 70));
        table.getTableHeader().setForeground(new Color(60, 200, 220));
        table.setRowHeight(22);
        table.setSelectionBackground(new Color(80, 80, 120));
    }

    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
