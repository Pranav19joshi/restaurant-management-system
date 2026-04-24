package model;

import enums.OrderStatus;
import interfaces.Orderable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Order implements Orderable {
    private String orderId;
    private int tableNumber;
    private String customerName;
    private String assignedWaiter;
    private OrderStatus status;
    private String orderTime;
    private List<MenuItem> items;

    public Order(String orderId, int tableNumber, String customerName, List<MenuItem> items) {
        this.orderId = orderId;
        this.tableNumber = tableNumber;
        this.customerName = customerName;
        this.items = new ArrayList<>(items);
        this.status = OrderStatus.PENDING;
        this.orderTime = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
        this.assignedWaiter = "";
    }

    @Override
    public String getOrderId() { return orderId; }

    @Override
    public OrderStatus getStatus() { return status; }

    @Override
    public void setStatus(OrderStatus status) { this.status = status; }

    @Override
    public double getOrderTotal() {
        return items.stream().mapToDouble(MenuItem::getTaxedPrice).sum();
    }

    @Override
    public List<String> getItemNames() {
        return items.stream().map(MenuItem::getName).collect(Collectors.toList());
    }

    public int getTableNumber() { return tableNumber; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getAssignedWaiter() { return assignedWaiter; }
    public void setAssignedWaiter(String assignedWaiter) { this.assignedWaiter = assignedWaiter; }

    public String getOrderTime() { return orderTime; }
    public void setOrderTime(String orderTime) { this.orderTime = orderTime; }

    public List<MenuItem> getItems() { return items; }

    public String toCsvString() {
        String itemNames = getItemNames().stream().collect(Collectors.joining("|"));
        return String.format("%s,%d,%s,%s,%.2f,%s,%s",
                orderId, tableNumber, customerName, status.name(),
                getOrderTotal(), orderTime, itemNames);
    }

    @Override
    public String toString() {
        return orderId + " | Table " + tableNumber + " | " + customerName + " | " + status;
    }
}
