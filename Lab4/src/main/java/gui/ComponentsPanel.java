/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import com.mycompany.lab4.DatabaseManager;
import model.ComponentWand;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ComponentsPanel extends JPanel {
    private final DatabaseManager dbManager;
    private JTable componentsTable;
    
    public ComponentsPanel(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        initializeUI();
        loadComponents();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton refreshButton = new JButton("Обновить");
        refreshButton.addActionListener(e -> loadComponents());
        
        buttonPanel.add(refreshButton);
        
        add(buttonPanel, BorderLayout.NORTH);
        
        componentsTable = new JTable();
        componentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(componentsTable);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    public void loadComponents() {
        try {
                if (dbManager.getAllComponents().isEmpty()) {
                    createInitialComponentsWithZeroQuantity();
                }
            List<ComponentWand> components = dbManager.getAllComponents();
            
            String[] columnNames = {"ID", "Тип", "Название", "Количество"};
            Object[][] data = new Object[components.size()][5];
            
            for (int i = 0; i < components.size(); i++) {
                ComponentWand component = components.get(i);
                data[i][0] = component.getId();
                data[i][1] = component.getType();
                data[i][2] = component.getName();
                data[i][3] = component.getQuantity();
            }
            
            componentsTable.setModel(new DefaultTableModel(data, columnNames) {
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
    
    private void createInitialComponentsWithZeroQuantity() throws SQLException {
    ComponentWand[] initialComponents = {
        new ComponentWand("wood", "Дуб", 0),
        new ComponentWand("wood", "Ясень", 0),
        new ComponentWand("wood", "Остролист", 0),
        new ComponentWand("wood", "Кипарис", 0),
        new ComponentWand("wood", "Кедр", 0),
        new ComponentWand("wood", "Вяз", 0),
        new ComponentWand("wood", "Акация", 0),
        new ComponentWand("wood", "Бук", 0),
        new ComponentWand("wood", "Орех", 0),
        new ComponentWand("wood", "Вишня", 0),
        new ComponentWand("wood", "Красное дерево", 0),
        new ComponentWand("core", "Перо феникса", 0),
        new ComponentWand("core", "Волос единорога", 0),
        new ComponentWand("core", "Чешуя дракона", 0),
        new ComponentWand("core", "Волос вейлы", 0),
        new ComponentWand("core", "Хвостовое перо громовой птицы", 0),
        new ComponentWand("core", "Волос русалки", 0),
        new ComponentWand("core", "Клык василиска", 0),
        new ComponentWand("core", "Сердечная жила", 0),
        new ComponentWand("core", "Нерв тролля", 0),
        new ComponentWand("core", "Перо пеликана", 0)
    };

    for (ComponentWand component : initialComponents) {
        dbManager.addComponent(component);
    }
}
}
