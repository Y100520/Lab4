/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import com.mycompany.lab4.DatabaseManager;
import model.Wand;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SalesPanel extends JPanel {
    private final DatabaseManager dbManager;
    private JTable salesTable;
    
    public SalesPanel(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        initializeUI();
        loadSales();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton refreshButton = new JButton("Обновить");
        refreshButton.addActionListener(e -> loadSales());
        
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.NORTH);
        
        salesTable = new JTable();
        salesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(salesTable);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadSales() {
        try {
            List<Wand> soldWands = dbManager.getSoldWandsWithWizards();
            
            String[] columnNames = {
                "ID палочки", "Дата создания", "Цена", 
                "Дата продажи", "Владелец", "Школа"
            };
            
            Object[][] data = new Object[soldWands.size()][6];
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            
            for (int i = 0; i < soldWands.size(); i++) {
                Wand wand = soldWands.get(i);
                data[i][0] = wand.getId();
                data[i][1] = wand.getCreationDate().format(formatter);
                data[i][2] = wand.getPrice();
                data[i][3] = wand.getSaleDate().format(formatter);
                data[i][4] = wand.getOwner().getFirstName() + " " + wand.getOwner().getLastName();
                data[i][5] = wand.getOwner().getSchool();
            }
            
            salesTable.setModel(new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });
            
            salesTable.getColumnModel().getColumn(4).setPreferredWidth(150);
            salesTable.getColumnModel().getColumn(5).setPreferredWidth(100);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Ошибка загрузки данных о продажах: " + e.getMessage(),
                "Ошибка",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
