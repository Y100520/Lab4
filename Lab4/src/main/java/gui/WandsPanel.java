/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import com.mycompany.lab4.Main;
import model.Wand;
import model.Wizard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import model.ComponentWand;

public class WandsPanel extends JPanel {
    private JTable wandsTable;

    public WandsPanel() {
        initializeUI();
        loadWands();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addButton = new JButton("Добавить палочку");
        addButton.addActionListener(e -> showAddWandDialog());

        JButton sellButton = new JButton("Продать палочку");
        sellButton.addActionListener(e -> showSellWandDialog());

        JButton refreshButton = new JButton("Обновить");
        refreshButton.addActionListener(e -> loadWands());

        buttonPanel.add(addButton);
        buttonPanel.add(sellButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.NORTH);

        wandsTable = new JTable();
        wandsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(wandsTable);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadWands() {
        List<Wand> wands = new ArrayList<>();
        String sql = "SELECT * FROM wands WHERE status = 'available'";

        try (Connection conn = DriverManager.getConnection(Main.DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Wand wand = new Wand();
                wand.setId(rs.getInt("id"));
                wand.setCreationDate(LocalDate.parse(rs.getString("creation_date")));
                wand.setPrice(rs.getDouble("price"));
                wand.setStatus(rs.getString("status"));
                wand.setWoodId(rs.getInt("wood_id"));
                wand.setCoreId(rs.getInt("core_id"));
                wands.add(wand);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка загрузки палочек: " + e.getMessage());
        }

        String[] columnNames = {"ID", "Дата создания", "Цена", "Статус", "ID древесины", "ID сердцевины"};
        Object[][] data = new Object[wands.size()][6];

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (int i = 0; i < wands.size(); i++) {
            Wand wand = wands.get(i);
            data[i][0] = wand.getId();
            data[i][1] = wand.getCreationDate().format(formatter);
            data[i][2] = wand.getPrice();
            data[i][3] = wand.getStatus();
            data[i][4] = wand.getWoodId();
            data[i][5] = wand.getCoreId();
        }

        wandsTable.setModel(new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }

    private void showAddWandDialog() {
        List<ComponentWand> availableWoods = getAvailableComponents("wood");
        List<ComponentWand> availableCores = getAvailableComponents("core");

        if (availableWoods.isEmpty() || availableCores.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Нет доступных компонентов для создания палочки!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Добавить палочку", true);
        dialog.setLayout(new GridLayout(0, 2, 5, 5));

        JComboBox<ComponentWand> woodCombo = createComponentCombo(availableWoods);
        JComboBox<ComponentWand> coreCombo = createComponentCombo(availableCores);
        JTextField priceField = new JTextField();

        dialog.add(new JLabel("Дата создания:"));
        dialog.add(new JLabel(LocalDate.now().toString()));
        dialog.add(new JLabel("Цена:"));
        dialog.add(priceField);
        dialog.add(new JLabel("Древесина:"));
        dialog.add(woodCombo);
        dialog.add(new JLabel("Сердцевина:"));
        dialog.add(coreCombo);

        JButton saveButton = new JButton("Сохранить");
        saveButton.addActionListener(e -> {
            try {
                ComponentWand selectedWood = (ComponentWand) woodCombo.getSelectedItem();
                ComponentWand selectedCore = (ComponentWand) coreCombo.getSelectedItem();
                double price = Double.parseDouble(priceField.getText());

                if (price <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Цена должна быть больше нуля!");
                    return;
                }

                String insertWandSql = "INSERT INTO wands (creation_date, price, status, wood_id, core_id) VALUES ('" +
                        LocalDate.now().toString() + "', " + price + ", 'available', " +
                        selectedWood.getId() + ", " + selectedCore.getId() + ")";

                String updateWoodSql = "UPDATE components SET quantity = quantity - 1 WHERE id = " + selectedWood.getId();
                String updateCoreSql = "UPDATE components SET quantity = quantity - 1 WHERE id = " + selectedCore.getId();

                try (Connection conn = DriverManager.getConnection(Main.DB_URL);
                     Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(insertWandSql);
                    stmt.executeUpdate(updateWoodSql);
                    stmt.executeUpdate(updateCoreSql);
                }

                loadWands();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Палочка успешно создана!");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Введите корректную цену", "Ошибка", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Ошибка при создании палочки: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(new JLabel());
        dialog.add(saveButton);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showSellWandDialog() {
        List<Wand> availableWands = getAvailableWands();
        List<Wizard> allWizards = getAllWizards();

        if (availableWands.isEmpty() || allWizards.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Нет доступных палочек или покупателей!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Продать палочку", true);
        dialog.setLayout(new GridLayout(0, 2, 5, 5));

        JComboBox<Wand> wandCombo = new JComboBox<>(availableWands.toArray(new Wand[0]));
        JComboBox<Wizard> wizardCombo = new JComboBox<>(allWizards.toArray(new Wizard[0]));

        wandCombo.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new DefaultListCellRenderer().getListCellRendererComponent(list, "Палочка ID: " + value.getId(), index, isSelected, cellHasFocus)
        );
        wizardCombo.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new DefaultListCellRenderer().getListCellRendererComponent(list, value.getFirstName() + " " + value.getLastName(), index, isSelected, cellHasFocus)
        );

        dialog.add(new JLabel("Палочка:"));
        dialog.add(wandCombo);
        dialog.add(new JLabel("Покупатель:"));
        dialog.add(wizardCombo);

        JButton sellButton = new JButton("Продать");
        sellButton.addActionListener(e -> {
            try {
                Wand selectedWand = (Wand) wandCombo.getSelectedItem();
                Wizard selectedWizard = (Wizard) wizardCombo.getSelectedItem();

                String sql = "UPDATE wands SET status = 'sold', wizard_id = " + selectedWizard.getId() +
                        ", sale_date = '" + LocalDate.now().toString() + "' WHERE id = " + selectedWand.getId();

                try (Connection conn = DriverManager.getConnection(Main.DB_URL);
                     Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(sql);
                }

                loadWands();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Палочка успешно продана!");

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(new JLabel());
        dialog.add(sellButton);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private List<ComponentWand> getAvailableComponents(String type) {
        List<ComponentWand> components = new ArrayList<>();
        String sql = "SELECT * FROM components WHERE type = '" + type + "' AND quantity > 0";
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

    private List<Wizard> getAllWizards() {
        List<Wizard> wizards = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(Main.DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM wizards")) {
            while (rs.next()) {
                Wizard w = new Wizard();
                w.setId(rs.getInt("id"));
                w.setFirstName(rs.getString("first_name"));
                w.setLastName(rs.getString("last_name"));
                wizards.add(w);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wizards;
    }

    private List<Wand> getAvailableWands() {
        List<Wand> wands = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(Main.DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM wands WHERE status = 'available'")) {
            while (rs.next()) {
                Wand w = new Wand();
                w.setId(rs.getInt("id"));
                wands.add(w);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wands;
    }

    private JComboBox<ComponentWand> createComponentCombo(List<ComponentWand> components) {
        JComboBox<ComponentWand> combo = new JComboBox<>(components.toArray(new ComponentWand[0]));
        combo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = (JLabel) new DefaultListCellRenderer().getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                label.setText(value.getName());
            }
            return label;
        });
        return combo;
    }
}
