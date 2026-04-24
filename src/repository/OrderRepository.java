package repository;

import enums.OrderStatus;
import model.MenuItem;
import model.Order;
import model.FoodItem;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class OrderRepository {
    private static final String FILE_PATH = "data/orders.csv";
    private Map<String, Order> orders = new HashMap<>();

    public OrderRepository() {
        new File("data").mkdirs();
        ensureHeaderExists();
        loadFromFile();
    }

    private void ensureHeaderExists() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("orderId,tableNumber,customerName,status,total,orderTime,items");
                writer.newLine();
            } catch (IOException e) {
                System.err.println("Error creating orders file: " + e.getMessage());
            }
        }
    }

    public void save(Order order) {
        orders.put(order.getOrderId(), order);
        appendToCsv(order);
    }

    private void appendToCsv(Order order) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(order.toCsvString());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error appending order: " + e.getMessage());
        }
    }

    public Order findById(String orderId) {
        return orders.get(orderId);
    }

    public List<Order> getAll() {
        return new ArrayList<>(orders.values());
    }

    public void updateStatus(String orderId, OrderStatus status) {
        Order order = orders.get(orderId);
        if (order != null) {
            order.setStatus(status);
            updateStatusInFile(orderId, status);
        }
    }

    public List<Order> getByStatus(OrderStatus status) {
        return orders.values().stream()
                .filter(o -> o.getStatus() == status)
                .collect(Collectors.toList());
    }

    // Unit III – FileReader: load all past orders from CSV into memory
    public void loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;
        orders.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                // CSV format: orderId,tableNumber,customerName,status,total,orderTime,items
                String[] parts = line.split(",", 7);
                if (parts.length < 6) continue;
                String orderId      = parts[0].trim();
                int tableNumber     = Integer.parseInt(parts[1].trim());
                String customerName = parts[2].trim();
                String statusStr    = parts[3].trim();
                String orderTime    = parts[5].trim();
                String itemsStr     = parts.length > 6 ? parts[6].trim() : "";

                // Rebuild a lightweight list of menu items from stored names
                List<MenuItem> items = new ArrayList<>();
                if (!itemsStr.isEmpty()) {
                    for (String itemName : itemsStr.split("\\|")) {
                        String n = itemName.trim();
                        if (!n.isEmpty()) {
                            // Placeholder item — name only; price already captured in total
                            items.add(new FoodItem("?", n, "Unknown", 0.0, true, false, 0));
                        }
                    }
                }

                Order order = new Order(orderId, tableNumber, customerName, items);
                order.setOrderTime(orderTime);
                try {
                    order.setStatus(OrderStatus.valueOf(statusStr));
                } catch (IllegalArgumentException ex) {
                    order.setStatus(OrderStatus.PENDING);
                }
                orders.put(orderId, order);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading orders: " + e.getMessage());
        }
    }

    // Rewrites the entire CSV whenever a status changes so changes survive restart
    public void updateStatusInFile(String orderId, OrderStatus newStatus) {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = br.readLine();
            if (header != null) lines.add(header);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", 7);
                if (parts.length >= 4 && parts[0].trim().equals(orderId)) {
                    parts[3] = newStatus.name();
                    lines.add(String.join(",", parts));
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading orders for update: " + e.getMessage());
            return;
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing updated orders: " + e.getMessage());
        }
    }
}
