package ui;

import model.DrinkItem;
import model.FoodItem;
import model.MenuItem;
import repository.MenuRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MenuPanel extends JPanel {
    private final MenuRepository menuRepository;
    private DefaultTableModel tableModel;
    private JTable menuTable;

    // Form fields
    private JComboBox<String> typeCombo;
    private JTextField idField, nameField, priceField, categoryField, extra1Field, extra2Field;
    private JCheckBox extra1Check;

    public MenuPanel(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(30, 30, 40));
        buildUI();
        refreshTable();
    }

    private void buildUI() {
        // Title
        JLabel title = new JLabel("Menu Management", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(255, 200, 60));
        add(title, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Name", "Base Price", "Category", "Type", "Taxed Price", "Available"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        menuTable = new JTable(tableModel);
        menuTable.setBackground(new Color(40, 40, 55));
        menuTable.setForeground(Color.WHITE);
        menuTable.setGridColor(new Color(60, 60, 80));
        menuTable.getTableHeader().setBackground(new Color(50, 50, 70));
        menuTable.getTableHeader().setForeground(new Color(255, 200, 60));
        menuTable.setRowHeight(22);
        menuTable.setSelectionBackground(new Color(80, 80, 120));
        JScrollPane scrollPane = new JScrollPane(menuTable);
        scrollPane.getViewport().setBackground(new Color(40, 40, 55));
        add(scrollPane, BorderLayout.CENTER);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(35, 35, 48));
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 200, 60)), " Add New Item ",
                0, 0, new Font("SansSerif", Font.BOLD, 12), new Color(255, 200, 60)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        typeCombo    = new JComboBox<>(new String[]{"Food", "Drink"});
        idField      = new JTextField(8);
        nameField    = new JTextField(14);
        priceField   = new JTextField(8);
        categoryField= new JTextField(12);
        extra1Field  = new JTextField(8);
        extra2Field  = new JTextField(8);
        extra1Check  = new JCheckBox();
        extra1Check.setBackground(new Color(35, 35, 48));

        styleField(idField); styleField(nameField); styleField(priceField);
        styleField(categoryField); styleField(extra1Field); styleField(extra2Field);
        styleCombo(typeCombo);

        typeCombo.addActionListener(e -> updateFormLabels(formPanel, gbc));

        // Row 0
        addFormRow(formPanel, gbc, 0, "Type:", typeCombo, "ID:", idField);
        addFormRow(formPanel, gbc, 1, "Name:", nameField, "Price (Rs):", priceField);
        addFormRow(formPanel, gbc, 2, "Category:", categoryField, "Veg/Alcoholic:", extra1Check);
        addFormRow(formPanel, gbc, 3, "Prep Time/Volume:", extra1Field, "(mins or ml)", extra2Field);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        btnPanel.setBackground(new Color(35, 35, 48));

        JButton addBtn    = makeButton("Add Item",           new Color(60, 180, 100));
        JButton removeBtn = makeButton("Remove Selected",    new Color(200, 70, 70));
        JButton toggleBtn = makeButton("Toggle Availability",new Color(80, 130, 200));
        JButton refreshBtn= makeButton("Refresh",            new Color(100, 100, 160));

        addBtn.addActionListener(e -> addItem());
        removeBtn.addActionListener(e -> removeSelected());
        toggleBtn.addActionListener(e -> toggleAvailability());
        refreshBtn.addActionListener(e -> refreshTable());

        btnPanel.add(addBtn); btnPanel.add(removeBtn);
        btnPanel.add(toggleBtn); btnPanel.add(refreshBtn);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(new Color(35, 35, 48));
        southPanel.add(formPanel, BorderLayout.CENTER);
        southPanel.add(btnPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row,
                             String lbl1, Component c1, String lbl2, Component c2) {
        gbc.gridy = row;
        gbc.gridx = 0; panel.add(makeLabel(lbl1), gbc);
        gbc.gridx = 1; panel.add(c1, gbc);
        gbc.gridx = 2; panel.add(makeLabel(lbl2), gbc);
        gbc.gridx = 3; panel.add(c2, gbc);
    }

    private void updateFormLabels(JPanel panel, GridBagConstraints gbc) { /* labels static — simpler */ }

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

    private void styleCombo(JComboBox<?> cb) {
        cb.setBackground(new Color(50, 50, 65));
        cb.setForeground(Color.WHITE);
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

    private void addItem() {
        try {
            String type     = (String) typeCombo.getSelectedItem();
            String id       = idField.getText().trim();
            String name     = nameField.getText().trim();
            String priceStr = priceField.getText().trim();
            String category = categoryField.getText().trim();

            if (id.isEmpty() || name.isEmpty() || priceStr.isEmpty() || category.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double price = Double.parseDouble(priceStr);

            MenuItem item;
            if ("Food".equals(type)) {
                boolean isVeg = extra1Check.isSelected();
                int prep = extra1Field.getText().trim().isEmpty() ? 10 : Integer.parseInt(extra1Field.getText().trim());
                item = new FoodItem(id, name, category, price, true, isVeg, prep);
            } else {
                boolean isAlcoholic = extra1Check.isSelected();
                String volume = extra2Field.getText().trim().isEmpty() ? "300ml" : extra2Field.getText().trim();
                item = new DrinkItem(id, name, category, price, true, isAlcoholic, volume);
            }
            menuRepository.add(item);
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Item added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid price or prep time.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeSelected() {
        int row = menuTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Remove item " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            menuRepository.remove(id);
            refreshTable();
        }
    }

    private void toggleAvailability() {
        int row = menuTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        MenuItem item = menuRepository.findById(id);
        if (item != null) {
            item.setAvailable(!item.isAvailable());
            menuRepository.saveToFile();
            refreshTable();
        }
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<MenuItem> items = menuRepository.getAll();
        for (MenuItem item : items) {
            tableModel.addRow(new Object[]{
                    item.getItemId(),
                    item.getName(),
                    String.format("%.2f", item.getBasePrice()),
                    item.getCategory(),
                    item.getItemType(),
                    String.format("%.2f", item.getTaxedPrice()),
                    item.isAvailable() ? "Yes" : "No"
            });
        }
    }

    private void clearForm() {
        idField.setText(""); nameField.setText(""); priceField.setText("");
        categoryField.setText(""); extra1Field.setText(""); extra2Field.setText("");
        extra1Check.setSelected(false);
    }
}
