package ui;

import concurrent.KitchenWorkerThread;
import concurrent.NotificationThread;
import repository.MenuRepository;
import repository.OrderRepository;
import service.BillingService;
import service.KitchenService;
import service.OrderService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JLabel notificationBar;
    private javax.swing.Timer notifResetTimer;

    public MainFrame() {
        setTitle("Restaurant Management System");
        setSize(1100, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(25, 25, 35));

        // --- Wire up dependencies ---
        MenuRepository menuRepo   = new MenuRepository();
        OrderRepository orderRepo = new OrderRepository();
        KitchenService kitchenSvc = new KitchenService(orderRepo);
        OrderService orderSvc     = new OrderService(orderRepo);
        BillingService billingSvc = new BillingService(orderRepo);

        // --- Notification thread ---
        NotificationThread notifThread = new NotificationThread();
        notifThread.setListener((message, isReady) ->
                SwingUtilities.invokeLater(() -> showNotification(message, isReady)));

        // --- Kitchen worker thread ---
        KitchenWorkerThread kitchenWorker = new KitchenWorkerThread(kitchenSvc, notifThread);

        Thread kitchenT = new Thread(kitchenWorker, "KitchenWorkerThread");
        kitchenT.setDaemon(true);
        kitchenT.start();

        Thread notifT = new Thread(notifThread, "NotificationThread");
        notifT.setDaemon(true);
        notifT.start();

        // --- Build UI ---
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);

        MenuPanel menuPanel       = new MenuPanel(menuRepo);
        OrderPanel orderPanel     = new OrderPanel(orderSvc, menuRepo);
        BillingPanel billingPanel = new BillingPanel(billingSvc, orderSvc);
        OrderHistoryPanel historyPanel   = new OrderHistoryPanel(orderRepo);
        DashboardPanel    dashboardPanel = new DashboardPanel(orderRepo);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(30, 30, 40));
        tabs.setForeground(Color.WHITE);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));

        tabs.addTab("🍽  Menu",     menuPanel);
        tabs.addTab("📋  Orders",   orderPanel);
        tabs.addTab("💳  Billing",  billingPanel);
        tabs.addTab("📜  History",  historyPanel);
        tabs.addTab("📊  Dashboard", dashboardPanel);

        // Tab colours via change listener
        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            Color[] colors = {
                new Color(255, 200, 60),   // Menu
                new Color(60, 200, 220),   // Orders
                new Color(255, 140, 40),   // Billing
                new Color(99, 179, 237),   // History
                new Color(154, 117, 235)   // Dashboard
            };
            if (idx >= 0 && idx < colors.length) {
                notificationBar.setForeground(colors[idx].darker());
            }
        });

        add(tabs, BorderLayout.CENTER);
        add(buildNotificationBar(), BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(20, 20, 30));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titleLbl = new JLabel("🍴  Restaurant Management System");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLbl.setForeground(new Color(255, 200, 60));

        JLabel subLbl = new JLabel("OOP Project  •  Semester 4  •  Java + Swing");
        subLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subLbl.setForeground(new Color(150, 150, 170));

        header.add(titleLbl, BorderLayout.WEST);
        header.add(subLbl, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 60, 80));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(20, 20, 30));
        wrapper.add(header, BorderLayout.CENTER);
        wrapper.add(sep, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildNotificationBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        panel.setBackground(new Color(20, 20, 30));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60, 60, 80)));

        notificationBar = new JLabel("  System ready. Waiting for orders...");
        notificationBar.setFont(new Font("SansSerif", Font.PLAIN, 12));
        notificationBar.setForeground(new Color(120, 120, 140));
        panel.add(notificationBar);

        // Timer to reset notification after 5 seconds
        notifResetTimer = new javax.swing.Timer(5000, e -> {
            notificationBar.setText("  System ready. Waiting for orders...");
            notificationBar.setForeground(new Color(120, 120, 140));
        });
        notifResetTimer.setRepeats(false);

        return panel;
    }

    public void showNotification(String message, boolean isReady) {
        notificationBar.setText("  🔔  " + message);
        notificationBar.setForeground(isReady ? new Color(60, 220, 100) : new Color(220, 80, 80));
        notifResetTimer.restart();
    }
}
