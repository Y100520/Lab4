/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import com.mycompany.lab4.Main;
import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DeliveryPanel extends JPanel {
    private JTable deliveriesTable;

    public DeliveryPanel() {
        initializeUI();
        loadDeliveries();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton weeklyDeliveryBtn = new JButton("Создать недельную поставку");
        weeklyDeliveryBtn.addActionListener(e -> createWeeklyDelivery());

        JButton seasonalDeliveryBtn = new JButton("Создать сезонную поставку");
        seasonalDeliveryBtn.addActionListener(e -> createSeasonalDelivery());

        JButton refreshButton = new JButton("Обновить");
        refreshButton.addActionListener(e -> loadDeliveries());

        buttonPanel.add(weeklyDeliveryBtn);
        buttonPanel.add(seasonalDeliveryBtn);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.NORTH);

        deliveriesTable = new JTable();
        deliveriesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(deliveriesTable);

        deliveriesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && deliveriesTable.getSelectedRow() >= 0) {
                int deliveryId = (int) deliveriesTable.getValueAt(deliveriesTable.getSelectedRow(), 0);
                showDeliveryDetailsDialog(deliveryId);
            }
        });
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadDeliveries() {
        List<Delivery> deliveries = getAllDeliveries();

        String[] columnNames = {"ID", "Дата поставки", "Поставщик", "Тип", "Кол-во позиций"};
        Object[][] data = new Object[deliveries.size()][5];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (int i = 0; i < deliveries.size(); i++) {
            Delivery delivery = deliveries.get(i);
            data[i][0] = delivery.getId();
            data[i][1] = delivery.getDeliveryDate().format(formatter);
            data[i][2] = delivery.getSupplierName();
            data[i][3] = delivery.isSeasonal() ? "Сезонная" : "Обычная";
            data[i][4] = delivery.getItems().size();
        }

        deliveriesTable.setModel(new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });
    }

    private void createWeeklyDelivery() {
        List<ComponentWand> lowStockComponents = getComponentsLowStock(10);

        if (lowStockComponents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Нет компонентов с количеством меньше 10. Все в достатке.", "Нет компонентов для заказа", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<DeliveryItem> items = new ArrayList<>();
        for (ComponentWand component : lowStockComponents) {
            int quantity = 20 - component.getQuantity();
            items.add(new DeliveryItem(component.getId(), quantity, 10.0));
        }

        addDelivery(LocalDate.now().plusDays(2), "Основной поставщик", false, items);
        loadDeliveries();
        JOptionPane.showMessageDialog(this, "Недельная поставка создана!", "Успех", JOptionPane.INFORMATION_MESSAGE);
    }

    private void createSeasonalDelivery() {
        Month currentMonth = LocalDate.now().getMonth();
        boolean isSummer = currentMonth == Month.JUNE || currentMonth == Month.JULY || currentMonth == Month.AUGUST;

        if (!isSummer) {
            int confirm = JOptionPane.showConfirmDialog(this, "Сейчас не летний сезон. Вы уверены?", "Подтверждение", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        List<ComponentWand> popularWoods = getPopularComponents("wood", 5);
        List<DeliveryItem> items = new ArrayList<>();
        for (ComponentWand wood : popularWoods) {
            items.add(new DeliveryItem(wood.getId(), 50, 12.5));
        }

        addDelivery(LocalDate.now().plusDays(3), "Сезонный поставщик", true, items);
        loadDeliveries();
        JOptionPane.showMessageDialog(this, "Сезонная поставка создана!");
    }

    private void showDeliveryDetailsDialog(int deliveryId) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Детали поставки", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        JTable itemsTable = new JTable();

        List<DeliveryItem> items = getDeliveryItems(deliveryId);
        List<ComponentWand> allComponents = getAllComponents();

        String[] columnNames = {"Компонент", "Тип", "Количество", "Цена за единицу", "Общая стоимость"};
        Object[][] data = new Object[items.size()][5];
        for (int i = 0; i < items.size(); i++) {
            DeliveryItem item = items.get(i);
            ComponentWand component = allComponents.stream()
                    .filter(c -> c.getId() == item.getComponentId())
                    .findFirst()
                    .orElse(null);

            data[i][0] = component != null ? component.getName() : "Неизвестно";
            data[i][1] = component != null ? component.getType() : "-";
            data[i][2] = item.getQuantity();
            data[i][3] = item.getUnitPrice();
            data[i][4] = item.getQuantity() * item.getUnitPrice();
        }

        itemsTable.setModel(new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });

        dialog.add(new JScrollPane(itemsTable));
        dialog.setVisible(true);
    }

    private void addDelivery(LocalDate date, String supplier, boolean isSeasonal, List<DeliveryItem> items) {
        String insertDeliverySQL = "INSERT INTO deliveries (delivery_date, supplier_name, is_seasonal) VALUES (?, ?, ?)";
        String insertItemSQL = "INSERT INTO delivery_items (delivery_id, component_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        String updateComponentSQL = "UPDATE components SET quantity = quantity + ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(Main.DB_URL)) {
            conn.setAutoCommit(false);

            try (PreparedStatement deliveryStmt = conn.prepareStatement(insertDeliverySQL, Statement.RETURN_GENERATED_KEYS)) {
                deliveryStmt.setString(1, date.toString());
                deliveryStmt.setString(2, supplier);
                deliveryStmt.setInt(3, isSeasonal ? 1 : 0);
                deliveryStmt.executeUpdate();

                int deliveryId;
                try (ResultSet generatedKeys = deliveryStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        deliveryId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating delivery failed, no ID obtained.");
                    }
                }

                try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSQL);
                     PreparedStatement componentStmt = conn.prepareStatement(updateComponentSQL)) {

                    for (DeliveryItem item : items) {
                        itemStmt.setInt(1, deliveryId);
                        itemStmt.setInt(2, item.getComponentId());
                        itemStmt.setInt(3, item.getQuantity());
                        itemStmt.setDouble(4, item.getUnitPrice());
                        itemStmt.addBatch();

                        componentStmt.setInt(1, item.getQuantity());
                        componentStmt.setInt(2, item.getComponentId());
                        componentStmt.addBatch();
                    }
                    itemStmt.executeBatch();
                    componentStmt.executeBatch();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Delivery> getAllDeliveries() {
        List<Delivery> deliveries = new ArrayList<>();
        String sql = "SELECT * FROM deliveries ORDER BY delivery_date DESC";
        try (Connection conn = DriverManager.getConnection(Main.DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Delivery delivery = new Delivery();
                delivery.setId(rs.getInt("id"));
                delivery.setDeliveryDate(LocalDate.parse(rs.getString("delivery_date")));
                delivery.setSupplierName(rs.getString("supplier_name"));
                delivery.setSeasonal(rs.getInt("is_seasonal") == 1);
                delivery.setItems(getDeliveryItems(delivery.getId()));
                deliveries.add(delivery);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deliveries;
    }

    private List<DeliveryItem> getDeliveryItems(int deliveryId) {
        List<DeliveryItem> items = new ArrayList<>();
        String sql = "SELECT * FROM delivery_items WHERE delivery_id = " + deliveryId;
        try (Connection conn = DriverManager.getConnection(Main.DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                DeliveryItem item = new DeliveryItem();
                item.setId(rs.getInt("id"));
                item.setDeliveryId(rs.getInt("delivery_id"));
                item.setComponentId(rs.getInt("component_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getDouble("unit_price"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    private List<ComponentWand> getComponentsLowStock(int threshold) {
        List<ComponentWand> components = new ArrayList<>();
        String sql = "SELECT * FROM components WHERE quantity < " + threshold;
        try (Connection conn = DriverManager.getConnection(Main.DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ComponentWand c = new ComponentWand();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setQuantity(rs.getInt("quantity"));
                components.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return components;
    }

    private List<ComponentWand> getPopularComponents(String type, int limit) {
        List<ComponentWand> components = new ArrayList<>();
        String sql = "SELECT c.id, c.name, c.type, COUNT(w.id) as usage_count " +
                "FROM components c " +
                "LEFT JOIN wands w ON (c.id = w.wood_id OR c.id = w.core_id) " +
                "WHERE c.type = '" + type + "' " +
                "GROUP BY c.id, c.name, c.type " +
                "ORDER BY usage_count DESC " +
                "LIMIT " + limit;
        try (Connection conn = DriverManager.getConnection(Main.DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ComponentWand c = new ComponentWand();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                components.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return components;
    }

    private List<ComponentWand> getAllComponents() {
        List<ComponentWand> components = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(Main.DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM components")) {
            while(rs.next()) {
                ComponentWand c = new ComponentWand();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setType(rs.getString("type"));
                components.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return components;
    }
}
