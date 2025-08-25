/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import com.mycompany.lab4.DatabaseManager;
import com.mycompany.lab4.DeliveryManager;
import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DeliveryPanel extends JPanel {
    private final DatabaseManager dbManager;
    private final DeliveryManager deliveryManager;
    private JTable deliveriesTable;
    
    public DeliveryPanel(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.deliveryManager = new DeliveryManager(dbManager);
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
        add(scrollPane, BorderLayout.CENTER);
        
        deliveriesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
        int selectedRow = deliveriesTable.getSelectedRow();

        if (selectedRow >= 0) {
            int deliveryId = (int) deliveriesTable.getValueAt(selectedRow, 0);
            showDeliveryDetailsDialog(deliveryId);
        }
    }
        });        
        add(scrollPane, BorderLayout.CENTER);
    }


    private void showDeliveryDetailsDialog(int deliveryId) {
    JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Детали поставки", Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setSize(600, 400);
    dialog.setLocationRelativeTo(this);

    JTable itemsTable = new JTable();
    itemsTable.setFillsViewportHeight(true);
    itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    showDeliveryItems(deliveryId, itemsTable);

    dialog.add(new JScrollPane(itemsTable));
    dialog.setVisible(true);
}

    private void createWeeklyDelivery() {
        try {
            boolean deliveryCreated = deliveryManager.processWeeklyDelivery(
                (JFrame)SwingUtilities.getWindowAncestor(this));

            if (deliveryCreated) {
                loadDeliveries();
                JOptionPane.showMessageDialog(this, 
                    "Недельная поставка создана!",
                    "Успех",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Ошибка при создании поставки: " + e.getMessage(),
                "Ошибка",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void createSeasonalDelivery() {
        try {
            if (!deliveryManager.isSummerSeason()) {
                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Сейчас не летний сезон. Вы уверены, что хотите создать сезонную поставку?",
                    "Подтверждение",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            deliveryManager.processSeasonalDelivery();
            loadDeliveries();
            JOptionPane.showMessageDialog(this, "Сезонная поставка создана!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void loadDeliveries() {
        try {
            List<Delivery> deliveries = dbManager.getAllDeliveries();
            
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
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Ошибка загрузки поставок: " + e.getMessage(),
                "Ошибка",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private void showDeliveryItems(int deliveryId, JTable itemsTable) {
        try {
            List<DeliveryItem> items = dbManager.getDeliveryItems(deliveryId);
           

            
            List<ComponentWand> components = dbManager.getAllComponents();
            
            String[] columnNames = {"Компонент", "Тип", "Количество", "Цена за единицу", "Общая стоимость"};
            Object[][] data = new Object[items.size()][5];
            
            for (int i = 0; i < items.size(); i++) {
                DeliveryItem item = items.get(i);
                ComponentWand component = findComponent(components, item.getComponentId());
                
                data[i][0] = component != null ? component.getName() : "Неизвестно";
                data[i][1] = component != null ? component.getType() : "-";
                data[i][2] = item.getQuantity();
                data[i][3] = item.getUnitPrice();
                data[i][4] = item.getQuantity() * item.getUnitPrice();
            }
            
            itemsTable.setModel(new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
                
                @Override
                public Class<?> getColumnClass(int column) {
                    return column == 2 ? Integer.class : 
                           column >= 3 ? Double.class : String.class;
                }
            });
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Ошибка загрузки позиций поставки: " + e.getMessage(),
                "Ошибка",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private ComponentWand findComponent(List<ComponentWand> components, int componentId) {
        for (ComponentWand component : components) {
            if (component.getId() == componentId) {
                return component;
            }
        }
        return null;
    }
    
    
}
