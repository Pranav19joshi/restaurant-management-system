package ui;

import enums.OrderStatus;
import model.MenuItem;
import model.Order;
import repository.OrderRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * DashboardPanel — Unit V: Map, Collections.sort, List
 *                  Unit IV: Lambda / Stream expressions
 * Shows live summary stats, a revenue bar chart, top-5 items, and status breakdown.
 */
public class DashboardPanel extends JPanel {

    private final OrderRepository orderRepo;

    // Stat card labels (updated on refresh)
    private JLabel revenueVal, ordersVal, avgBillVal;

    // Top-5 items list model
    private DefaultListModel<String> top5Model;

    // Status breakdown table
    private DefaultTableModel statusTableModel;

    // Embedded bar chart
    private BarChartPanel barChart;

    private static final Color BG      = new Color(25, 25, 35);
    private static final Color CARD_BG = new Color(32, 32, 50);
    private static final Color ACCENT  = new Color(99, 179, 237);
    private static final Color FG      = new Color(220, 220, 235);

    public DashboardPanel(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
        setLayout(new BorderLayout(0, 10));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        add(buildTopRow(),      BorderLayout.NORTH);
        add(buildCenterArea(),  BorderLayout.CENTER);

        refresh();
    }

    // ── Top row: stat cards + Refresh button ─────────────────────────────────
    private JPanel buildTopRow() {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(BG);

        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setBackground(BG);

        revenueVal = new JLabel("Rs. 0.00");
        ordersVal  = new JLabel("0");
        avgBillVal = new JLabel("Rs. 0.00");

        cards.add(buildCard("💰 Total Revenue", revenueVal, new Color(72, 207, 173)));
        cards.add(buildCard("📦 Total Orders",  ordersVal,  new Color(154, 117, 235)));
        cards.add(buildCard("📊 Avg. Bill",     avgBillVal, new Color(252, 182, 72)));

        JButton refreshBtn = new JButton("⟳  Refresh");
        refreshBtn.setBackground(new Color(60, 140, 200));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refresh());

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrap.setBackground(BG);
        btnWrap.add(refreshBtn);

        row.add(cards, BorderLayout.CENTER);
        row.add(btnWrap, BorderLayout.EAST);
        return row;
    }

    private JPanel buildCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 4));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent.darker(), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setForeground(new Color(160, 160, 180));
        titleLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));

        valueLabel.setForeground(accent);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        card.add(titleLbl);
        card.add(valueLabel);
        return card;
    }

    // ── Center: bar chart left, top-5 + status table right ────────────────
    private JPanel buildCenterArea() {
        JPanel center = new JPanel(new GridLayout(1, 2, 14, 0));
        center.setBackground(BG);

        // Left: bar chart card
        JPanel chartCard = buildSectionCard("📈 Revenue by Category");
        barChart = new BarChartPanel();
        chartCard.add(barChart, BorderLayout.CENTER);
        center.add(chartCard);

        // Right: top-5 + status breakdown stacked
        JPanel rightCol = new JPanel(new GridLayout(2, 1, 0, 14));
        rightCol.setBackground(BG);
        rightCol.add(buildTop5Panel());
        rightCol.add(buildStatusTablePanel());
        center.add(rightCol);

        return center;
    }

    private JPanel buildSectionCard(String heading) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 55, 80), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel hdr = new JLabel(heading);
        hdr.setForeground(ACCENT);
        hdr.setFont(new Font("SansSerif", Font.BOLD, 14));
        card.add(hdr, BorderLayout.NORTH);
        return card;
    }

    private JPanel buildTop5Panel() {
        JPanel card = buildSectionCard("🏆 Top 5 Most Ordered Items");

        top5Model = new DefaultListModel<>();
        JList<String> list = new JList<>(top5Model);
        list.setBackground(new Color(28, 28, 44));
        list.setForeground(FG);
        list.setFont(new Font("SansSerif", Font.PLAIN, 13));
        list.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        list.setFixedCellHeight(26);

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(28, 28, 44));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildStatusTablePanel() {
        JPanel card = buildSectionCard("📋 Orders by Status");

        String[] cols = {"Status", "Count"};
        statusTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable statusTable = new JTable(statusTableModel);
        statusTable.setBackground(new Color(28, 28, 44));
        statusTable.setForeground(FG);
        statusTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusTable.setRowHeight(26);
        statusTable.setShowGrid(false);
        statusTable.getTableHeader().setBackground(new Color(40, 40, 60));
        statusTable.getTableHeader().setForeground(ACCENT);
        statusTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        JScrollPane scroll = new JScrollPane(statusTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(28, 28, 44));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ── Refresh: recalculate everything from repository ───────────────────
    public void refresh() {
        orderRepo.loadFromFile();
        List<Order> orders = orderRepo.getAll();

        // ── Stat cards ────────────────────────────────────────────────────
        double totalRevenue = orders.stream()
                .mapToDouble(Order::getOrderTotal).sum();
        int totalOrders = orders.size();
        double avgBill  = totalOrders == 0 ? 0 : totalRevenue / totalOrders;

        revenueVal.setText(String.format("Rs. %.2f", totalRevenue));
        ordersVal.setText(String.valueOf(totalOrders));
        avgBillVal.setText(String.format("Rs. %.2f", avgBill));

        // ── Unit V – Map + Unit IV – Lambda: revenue by category ─────────
        Map<String, Double> revenueByCategory = new HashMap<>();
        for (Order o : orders) {
            for (MenuItem item : o.getItems()) {
                revenueByCategory.merge(
                        item.getCategory(),
                        item.getTaxedPrice(),
                        Double::sum   // lambda: accumulate price
                );
            }
        }
        barChart.setData(revenueByCategory);

        // ── Unit V – Map + List: top-5 most ordered items ─────────────────
        Map<String, Integer> itemCount = new HashMap<>();
        orders.forEach(o -> o.getItems().forEach(item ->
                itemCount.merge(item.getName(), 1, Integer::sum)
        ));

        // Unit V – Collections.sort with lambda comparator (descending)
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(itemCount.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());
        List<Map.Entry<String, Integer>> top5 = sorted.subList(0, Math.min(5, sorted.size()));

        top5Model.clear();
        int rank = 1;
        for (Map.Entry<String, Integer> e : top5) {
            top5Model.addElement(String.format("  %d.  %s  —  %d orders", rank++, e.getKey(), e.getValue()));
        }
        if (top5Model.isEmpty()) top5Model.addElement("  (no data yet)");

        // ── Status breakdown table ─────────────────────────────────────────
        statusTableModel.setRowCount(0);
        for (OrderStatus status : OrderStatus.values()) {
            long count = orders.stream().filter(o -> o.getStatus() == status).count();
            if (count > 0) {
                statusTableModel.addRow(new Object[]{status.getDisplayName(), count});
            }
        }
        if (statusTableModel.getRowCount() == 0) {
            statusTableModel.addRow(new Object[]{"(no orders)", 0});
        }
    }
}
