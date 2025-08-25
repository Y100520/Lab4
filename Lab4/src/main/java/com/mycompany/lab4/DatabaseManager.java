/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.lab4;

import model.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:ollivanders.db";
    
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
    
    public void initializeDatabase() {
        String[] createTables = {
            "CREATE TABLE IF NOT EXISTS components (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "type TEXT CHECK(type IN ('wood', 'core'))," +
            "name TEXT NOT NULL," +
            "quantity INTEGER NOT NULL DEFAULT 0)",
            
            "CREATE TABLE IF NOT EXISTS wizards (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "first_name TEXT NOT NULL," +
            "last_name TEXT NOT NULL," +
            "birth_date DATE," +
            "school TEXT," +
            "contact_info TEXT)",
            
            "CREATE TABLE IF NOT EXISTS wands (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "creation_date DATE NOT NULL," +
            "price REAL NOT NULL CHECK(price > 0)," +
            "status TEXT DEFAULT 'available' CHECK(status IN ('available', 'sold'))," +
            "wood_id INTEGER NOT NULL," +
            "core_id INTEGER NOT NULL," +
            "wizard_id INTEGER," +
            "sale_date DATE," +
            "FOREIGN KEY(wood_id) REFERENCES components(id)," +
            "FOREIGN KEY(core_id) REFERENCES components(id)," +
            "FOREIGN KEY(wizard_id) REFERENCES wizards(id))",
                
            "CREATE TABLE IF NOT EXISTS deliveries (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "delivery_date DATE NOT NULL," +
            "supplier_name TEXT NOT NULL," +
            "is_seasonal BOOLEAN NOT NULL DEFAULT FALSE)",

            "CREATE TABLE IF NOT EXISTS delivery_items (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "delivery_id INTEGER NOT NULL," +
            "component_id INTEGER NOT NULL," +
            "quantity INTEGER NOT NULL," +
            "unit_price REAL NOT NULL," +
            "FOREIGN KEY(delivery_id) REFERENCES deliveries(id)," +
            "FOREIGN KEY(component_id) REFERENCES components(id))"
        };
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
                for (String sql : createTables) {
                    stmt.execute(sql);
                }
            } catch (SQLException e) {
                System.err.println("Ошибка при инициализации базы данных: " + e.getMessage());
            }
        }
    
        public void addComponent(ComponentWand component) throws SQLException {
            String sql = "INSERT INTO components (type, name, quantity) VALUES (?, ?, ?)";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, component.getType());
                stmt.setString(2, component.getName());
                stmt.setInt(3, component.getQuantity());

                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        component.setId(rs.getInt(1));
                    }
                }
            }
        }
    
        public List<ComponentWand> getAllComponents() throws SQLException {
            List<ComponentWand> components = new ArrayList<>();
            String sql = "SELECT * FROM components";

            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    ComponentWand component = new ComponentWand();
                    component.setId(rs.getInt("id"));
                    component.setType(rs.getString("type"));
                    component.setName(rs.getString("name"));
                    component.setQuantity(rs.getInt("quantity"));

                    components.add(component);
                }
            }
            return components;
        }
    
        public void addWizard(Wizard wizard) throws SQLException {
            String sql = "INSERT INTO wizards (first_name, last_name, birth_date, school, contact_info) " +
                         "VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, wizard.getFirstName());
                stmt.setString(2, wizard.getLastName());
                stmt.setDate(3, Date.valueOf(wizard.getBirthDate()));
                stmt.setString(4, wizard.getSchool());
                stmt.setString(5, wizard.getContactInfo());

                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        wizard.setId(rs.getInt(1));
                    }
                }
            }
        }
    
        public List<Wizard> getAllWizards() throws SQLException {
            List<Wizard> wizards = new ArrayList<>();
            String sql = "SELECT * FROM wizards";

            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    Wizard wizard = new Wizard();
                    wizard.setId(rs.getInt("id"));
                    wizard.setFirstName(rs.getString("first_name"));
                    wizard.setLastName(rs.getString("last_name"));
                    wizard.setBirthDate(rs.getDate("birth_date").toLocalDate());
                    wizard.setSchool(rs.getString("school"));
                    wizard.setContactInfo(rs.getString("contact_info"));

                    wizards.add(wizard);
                }
            }
            return wizards;
        }
    
        public void addWand(Wand wand) throws SQLException {
            Connection conn = null;
            try{
                conn = getConnection();
                conn.setAutoCommit(false);
            
            if (!areComponentsAvailable(wand.getWoodId(), wand.getCoreId())) {
                throw new SQLException("Недостаточно компонентов для создания палочки");
            }
            String sql = "INSERT INTO wands (creation_date, price, status, wood_id, core_id) " +
                         "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setDate(1, Date.valueOf(wand.getCreationDate()));
                stmt.setDouble(2, wand.getPrice());
                stmt.setString(3, wand.getStatus());
                stmt.setInt(4, wand.getWoodId());
                stmt.setInt(5, wand.getCoreId());

                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        wand.setId(rs.getInt(1));
                    }
                }
            }
            
            updateComponentQuantity(conn, wand.getWoodId(), -1);
            updateComponentQuantity(conn, wand.getCoreId(), -1);
            } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
            } finally {
                if (conn != null) conn.setAutoCommit(true);
            }
        }
    
        public List<Wand> getAvailableWands() throws SQLException {
            List<Wand> wands = new ArrayList<>();
            String sql = "SELECT * FROM wands WHERE status = 'available'";

            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    Wand wand = new Wand();
                    wand.setId(rs.getInt("id"));
                    wand.setCreationDate(rs.getDate("creation_date").toLocalDate());
                    wand.setPrice(rs.getDouble("price"));
                    wand.setStatus(rs.getString("status"));
                    wand.setWoodId(rs.getInt("wood_id"));
                    wand.setCoreId(rs.getInt("core_id"));

                    wands.add(wand);
                }
            }
            return wands;
        }
    
        public void sellWand(int wandId, int wizardId) throws SQLException {
            String sql = "UPDATE wands SET status = 'sold', wizard_id = ?, sale_date = ? " +
                         "WHERE id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, wizardId);
                stmt.setDate(2, Date.valueOf(LocalDate.now()));
                stmt.setInt(3, wandId);

                stmt.executeUpdate();
            }
        }
    
        public void clearAllData() throws SQLException {
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

                stmt.execute("PRAGMA foreign_keys = OFF");
                stmt.execute("DROP TABLE IF EXISTS wands");
                stmt.execute("DROP TABLE IF EXISTS wizards");
                stmt.execute("DROP TABLE IF EXISTS components");
                stmt.execute("DROP TABLE IF EXISTS deliveries");
                stmt.execute("DROP TABLE IF EXISTS delivery_items");
                stmt.execute("PRAGMA foreign_keys = ON");
                initializeDatabase();
            }
        }
    
        public List<Wand> getSoldWandsWithWizards() throws SQLException {
            List<Wand> wands = new ArrayList<>();
            String sql = "SELECT w.*, wz.first_name, wz.last_name, wz.school " +
                         "FROM wands w " +
                         "JOIN wizards wz ON w.wizard_id = wz.id " +
                         "WHERE w.status = 'sold'";

            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    Wand wand = new Wand();
                    wand.setId(rs.getInt("id"));
                    wand.setCreationDate(rs.getDate("creation_date").toLocalDate());
                    wand.setPrice(rs.getDouble("price"));
                    wand.setStatus(rs.getString("status"));
                    wand.setWoodId(rs.getInt("wood_id"));
                    wand.setCoreId(rs.getInt("core_id"));
                    wand.setWizardId(rs.getInt("wizard_id"));
                    wand.setSaleDate(rs.getDate("sale_date").toLocalDate());

                    Wizard wizard = new Wizard();
                    wizard.setId(rs.getInt("wizard_id"));
                    wizard.setFirstName(rs.getString("first_name"));
                    wizard.setLastName(rs.getString("last_name"));
                    wizard.setSchool(rs.getString("school"));

                    wand.setOwner(wizard);

                    wands.add(wand);
                }
            }
            return wands;
        }

        public void addDelivery(Delivery delivery) throws SQLException {
            String sql = "INSERT INTO deliveries (delivery_date, supplier_name, is_seasonal) VALUES (?, ?, ?)";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setDate(1, Date.valueOf(delivery.getDeliveryDate()));
                stmt.setString(2, delivery.getSupplierName());
                stmt.setBoolean(3, delivery.isSeasonal());

                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        delivery.setId(rs.getInt(1));
                    }
                }

                addDeliveryItems(delivery.getId(), delivery.getItems());
            }
        }

        private void addDeliveryItems(int deliveryId, List<DeliveryItem> items) throws SQLException {
            String sql = "INSERT INTO delivery_items (delivery_id, component_id, quantity, unit_price) VALUES (?, ?, ?, ?)";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                for (DeliveryItem item : items) {
                    stmt.setInt(1, deliveryId);
                    stmt.setInt(2, item.getComponentId());
                    stmt.setInt(3, item.getQuantity());
                    stmt.setDouble(4, item.getUnitPrice());
                    stmt.addBatch();

                    updateComponentQuantity(getConnection(), item.getComponentId(), item.getQuantity());
                }

                stmt.executeBatch();
            }
        }
    
    
        public List<DeliveryItem> getDeliveryItems(int deliveryId) throws SQLException {
            List<DeliveryItem> items = new ArrayList<>();
            String sql = "SELECT * FROM delivery_items WHERE delivery_id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, deliveryId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        DeliveryItem item = new DeliveryItem();
                        item.setId(rs.getInt("id"));
                        item.setDeliveryId(rs.getInt("delivery_id"));
                        item.setComponentId(rs.getInt("component_id"));
                        item.setQuantity(rs.getInt("quantity"));
                        item.setUnitPrice(rs.getDouble("unit_price"));

                        items.add(item);
                    }
                }
            }
            return items;
        }
    
        public boolean hasDeliveryThisWeek() throws SQLException {
           String sql = "SELECT COUNT(*) FROM deliveries WHERE delivery_date BETWEEN ? AND ?";
           LocalDate today = LocalDate.now();
           LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);

           try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

               stmt.setDate(1, Date.valueOf(startOfWeek));
               stmt.setDate(2, Date.valueOf(today));

               try (ResultSet rs = stmt.executeQuery()) {
                   return rs.next() && rs.getInt(1) > 0;
               }
           }
       }
     
        public List<ComponentWand> getComponentsLowStock(int threshold) throws SQLException {
            List<ComponentWand> components = new ArrayList<>();
            String sql = "SELECT * FROM components WHERE quantity < ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, threshold);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ComponentWand component = new ComponentWand();
                        component.setId(rs.getInt("id"));
                        component.setType(rs.getString("type"));
                        component.setName(rs.getString("name"));
                        component.setQuantity(rs.getInt("quantity"));

                        components.add(component);
                    }
                }
            }
            return components;
        }
    
        public List<ComponentWand> getPopularComponents(String type, int limit) throws SQLException {
            List<ComponentWand> components = new ArrayList<>();
            String sql = "SELECT c.* FROM components c " +
                         "JOIN (SELECT wood_id AS comp_id, COUNT(*) AS cnt FROM wands GROUP BY wood_id " +
                         "      UNION ALL " +
                         "      SELECT core_id AS comp_id, COUNT(*) AS cnt FROM wands GROUP BY core_id) stats " +
                         "ON c.id = stats.comp_id " +
                         "WHERE c.type = ? " +
                         "ORDER BY stats.cnt DESC LIMIT ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, type);
                stmt.setInt(2, limit);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ComponentWand component = new ComponentWand();
                        component.setId(rs.getInt("id"));
                        component.setType(rs.getString("type"));
                        component.setName(rs.getString("name"));
                        component.setQuantity(rs.getInt("quantity"));

                        components.add(component);
                    }
                }
            }
            return components;
        }

        public double getComponentPrice(int componentId) throws SQLException {
            String sql = "SELECT unit_price FROM delivery_items " +
                         "WHERE component_id = ? ORDER BY delivery_id DESC LIMIT 1";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, componentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? rs.getDouble(1) : 10.0; 
                }
            }
        }

        public List<Delivery> getAllDeliveries() throws SQLException {
                List<Delivery> deliveries = new ArrayList<>();
                String sql = "SELECT * FROM deliveries ORDER BY delivery_date DESC";

                try (Connection conn = getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {

                    while (rs.next()) {
                        Delivery delivery = new Delivery();
                        delivery.setId(rs.getInt("id"));
                        delivery.setDeliveryDate(rs.getDate("delivery_date").toLocalDate());
                        delivery.setSupplierName(rs.getString("supplier_name"));
                        delivery.setSeasonal(rs.getBoolean("is_seasonal"));

                        delivery.setItems(getDeliveryItems(delivery.getId()));

                        deliveries.add(delivery);
                    }
                }
                return deliveries;
            }
        
            public void updateComponentQuantity(Connection conn, int componentId, int delta) throws SQLException {
                String sql = "UPDATE components SET quantity = quantity + ? WHERE id = ?";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                    stmt.setInt(1, delta);
                    stmt.setInt(2, componentId);
                    stmt.executeUpdate();
                }
            }
            
        public boolean areComponentsAvailable(int woodId, int coreId) throws SQLException {
            String sql = "SELECT COUNT(*) FROM components WHERE id IN (?, ?) AND quantity > 0";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, woodId);
                stmt.setInt(2, coreId);

                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() && rs.getInt(1) == 2;
                }
            }
        }
    
        public List<ComponentWand> getAvailableComponents(String type) throws SQLException {
            String sql = "SELECT * FROM components WHERE type = ? AND quantity > 0";

            List<ComponentWand> components = new ArrayList<>();
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, type);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ComponentWand component = new ComponentWand();
                        component.setId(rs.getInt("id"));
                        component.setType(rs.getString("type"));
                        component.setName(rs.getString("name"));
                        component.setQuantity(rs.getInt("quantity"));
                        components.add(component);
                    }
                }
            }
            return components;
        }
}
