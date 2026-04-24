package repository;

import interfaces.Manageable;
import model.DrinkItem;
import model.FoodItem;
import model.MenuItem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MenuRepository implements Manageable<MenuItem> {
    private static final String FILE_PATH = "data/menu.csv";
    private List<MenuItem> menuItems = new ArrayList<>();

    public MenuRepository() {
        loadFromFile();
        if (menuItems.isEmpty()) {
            seedDefaultItems();
            saveToFile();
        }
    }

    private void seedDefaultItems() {
        // 8 Food items
        menuItems.add(new FoodItem("F001", "Butter Chicken",   "Main Course", 280.0, true,  false, 20));
        menuItems.add(new FoodItem("F002", "Paneer Tikka",     "Starters",    180.0, true,  true,  15));
        menuItems.add(new FoodItem("F003", "Dal Makhani",      "Main Course", 160.0, true,  true,  18));
        menuItems.add(new FoodItem("F004", "Chicken Biryani",  "Rice",        250.0, true,  false, 25));
        menuItems.add(new FoodItem("F005", "Veg Biryani",      "Rice",        200.0, true,  true,  22));
        menuItems.add(new FoodItem("F006", "Garlic Naan",      "Breads",       50.0, true,  true,   8));
        menuItems.add(new FoodItem("F007", "Samosa",           "Starters",     40.0, true,  true,  10));
        menuItems.add(new FoodItem("F008", "Gulab Jamun",      "Desserts",     80.0, true,  true,   5));
        // 4 Drink items
        menuItems.add(new DrinkItem("D001", "Mango Lassi",     "Beverages",    90.0, true,  false, "300ml"));
        menuItems.add(new DrinkItem("D002", "Masala Chai",     "Beverages",    40.0, true,  false, "200ml"));
        menuItems.add(new DrinkItem("D003", "Cold Coffee",     "Beverages",   110.0, true,  false, "350ml"));
        menuItems.add(new DrinkItem("D004", "Fresh Lime Soda", "Beverages",    60.0, true,  false, "300ml"));
    }

    @Override
    public void add(MenuItem item) {
        menuItems.add(item);
        saveToFile();
    }

    @Override
    public void remove(String id) {
        menuItems.removeIf(item -> item.getItemId().equals(id));
        saveToFile();
    }

    @Override
    public MenuItem findById(String id) {
        return menuItems.stream()
                .filter(item -> item.getItemId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<MenuItem> getAll() {
        return new ArrayList<>(menuItems);
    }

    public List<MenuItem> getAvailableItems() {
        return menuItems.stream()
                .filter(MenuItem::isAvailable)
                .collect(Collectors.toList());
    }

    public void loadFromFile() {
        new File("data").mkdirs();
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length < 8) continue;
                String itemId    = parts[0].trim();
                String name      = parts[1].trim();
                double basePrice = Double.parseDouble(parts[2].trim());
                String category  = parts[3].trim();
                String itemType  = parts[4].trim();
                boolean available = Boolean.parseBoolean(parts[5].trim());
                String extra1    = parts[6].trim();
                String extra2    = parts[7].trim();

                if ("Food".equalsIgnoreCase(itemType)) {
                    boolean isVeg = Boolean.parseBoolean(extra1);
                    int prep = Integer.parseInt(extra2);
                    menuItems.add(new FoodItem(itemId, name, category, basePrice, available, isVeg, prep));
                } else {
                    boolean isAlc = Boolean.parseBoolean(extra1);
                    menuItems.add(new DrinkItem(itemId, name, category, basePrice, available, isAlc, extra2));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading menu: " + e.getMessage());
        }
    }

    public void saveToFile() {
        new File("data").mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write("itemId,name,basePrice,category,itemType,available,extra1,extra2");
            writer.newLine();
            for (MenuItem item : menuItems) {
                String extra1, extra2;
                if (item instanceof FoodItem) {
                    FoodItem f = (FoodItem) item;
                    extra1 = String.valueOf(f.isVegetarian());
                    extra2 = String.valueOf(f.getPrepTimeMinutes());
                } else {
                    DrinkItem d = (DrinkItem) item;
                    extra1 = String.valueOf(d.isAlcoholic());
                    extra2 = d.getVolume();
                }
                writer.write(String.format("%s,%s,%.1f,%s,%s,%s,%s,%s",
                        item.getItemId(), item.getName(), item.getBasePrice(),
                        item.getCategory(), item.getItemType(),
                        item.isAvailable(), extra1, extra2));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving menu: " + e.getMessage());
        }
    }
}
