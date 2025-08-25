/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import com.mycompany.lab4.DatabaseManager;
import model.Wand;
import model.Wizard;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import model.ComponentWand;

public class WandsPanel extends JPanel {
    private final DatabaseManager dbManager;
    private JTable wandsTable;
    
    public WandsPanel(DatabaseManager dbManager) {
        this.dbManager = dbManager;
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
    private JComboBox<ComponentWand> createComponentCombo(List<ComponentWand> components) {
    JComboBox<ComponentWand> combo = new JComboBox<>(components.toArray(new ComponentWand[0]));
    combo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
        JLabel label = (JLabel) new DefaultListCellRenderer().getListCellRendererComponent(
            list, value, index, isSelected, cellHasFocus);
        if (value != null) {
            label.setText(((ComponentWand) value).getName()); // Показываем название
        }
        return label;
    });
    return combo;
}
    private void loadWands() {
        try {
            List<Wand> wands = dbManager.getAvailableWands();
            
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
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Ошибка загрузки данных: " + e.getMessage(),
                "Ошибка",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private void showAddWandDialog() {
    try {
        List<ComponentWand> availableWoods = dbManager.getAvailableComponents("wood");
        List<ComponentWand> availableCores = dbManager.getAvailableComponents("core");
        
        if (availableWoods.isEmpty() || availableCores.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Нет доступных компонентов для создания палочки",
                "Ошибка",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Добавить палочку", true);
        dialog.setLayout(new GridLayout(0, 2, 5, 5));
        
        JComboBox<ComponentWand> woodCombo = createComponentCombo(availableWoods);
        JComboBox<ComponentWand> coreCombo = createComponentCombo(availableCores);
        JTextField priceField = new JTextField();
        
        JLabel dateLabel = new JLabel(LocalDate.now().toString());
        
        dialog.add(new JLabel("Дата создания:"));
        dialog.add(dateLabel);
        dialog.add(new JLabel("Цена:"));
        dialog.add(priceField);
        dialog.add(new JLabel("Древесина:"));
        dialog.add(woodCombo);
        dialog.add(new JLabel("Сердцевина:"));
        dialog.add(coreCombo);
        
        JButton saveButton = new JButton("Сохранить");
        saveButton.addActionListener(e -> {
            
            try {
                
                ComponentWand selectedWood = (ComponentWand)woodCombo.getSelectedItem();
                ComponentWand selectedCore = (ComponentWand)coreCombo.getSelectedItem();
                
                double price = Double.parseDouble(priceField.getText());
                if (price <= 0) {
                    JOptionPane.showMessageDialog(
                    dialog,
                    "Цена должна быть положительной!",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
                }
                Wand wand = new Wand(
                    LocalDate.now(),
                    Double.parseDouble(priceField.getText()),
                    selectedWood.getId(),
                    selectedCore.getId()
                );
                
                dbManager.addWand(wand);
                loadWands();
                dialog.dispose();
                
                JOptionPane.showMessageDialog(
                    this,
                    "Палочка успешно создана!",
                    "Успех",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                    dialog,
                    "Введите корректную цену",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
                );
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                    dialog,
                    "Ошибка при создании палочки: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
        
        dialog.add(saveButton);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Ошибка загрузки компонентов: " + e.getMessage(),
            "Ошибка",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
    
    private void showSellWandDialog() {
        try {
            List<Wand> wands = dbManager.getAvailableWands();
            List<Wizard> wizards = dbManager.getAllWizards();
            
            if (wands.isEmpty() || wizards.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Нет доступных палочек или покупателей",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Продать палочку", true);
            dialog.setLayout(new GridLayout(0, 2, 5, 5));
            
            JComboBox<Wand> wandCombo = new JComboBox<>(wands.toArray(new Wand[0]));
            JComboBox<Wizard> wizardCombo = new JComboBox<>(wizards.toArray(new Wizard[0]));
            
            dialog.add(new JLabel("Палочка:"));
            dialog.add(wandCombo);
            dialog.add(new JLabel("Покупатель:"));
            dialog.add(wizardCombo);
            
            JButton sellButton = new JButton("Продать");
            sellButton.addActionListener(e -> {
                try {
                    Wand selectedWand = (Wand)wandCombo.getSelectedItem();
                    Wizard selectedWizard = (Wizard)wizardCombo.getSelectedItem();
                    
                    dbManager.sellWand(selectedWand.getId(), selectedWizard.getId());
                    loadWands();
                    dialog.dispose();
                    
                    JOptionPane.showMessageDialog(
                        this,
                        "Палочка успешно продана!",
                        "Успех",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(
                        dialog,
                        "Ошибка: " + ex.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            });
            
            dialog.add(sellButton);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Ошибка загрузки данных: " + e.getMessage(),
                "Ошибка",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
