/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import com.mycompany.lab4.DatabaseManager;
import javax.swing.*;
import java.sql.SQLException;

public class MainFrame extends JFrame {
    private final DatabaseManager dbManager;
    private JTabbedPane tabbedPane;
    private ComponentsPanel componentsPanel;
    private DeliveryPanel deliveryPanel; 
    
    public MainFrame(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Магазин волшебных палочек Олливандеры");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        tabbedPane = new JTabbedPane();
        componentsPanel = new ComponentsPanel(dbManager);
        deliveryPanel = new DeliveryPanel(dbManager);
        JTabbedPane tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("Палочки", new WandsPanel(dbManager));
        tabbedPane.addTab("Покупатели", new WizardsPanel(dbManager));
        tabbedPane.addTab("Компоненты", new ComponentsPanel(dbManager));
        tabbedPane.addTab("Продажи", new SalesPanel(dbManager));
        tabbedPane.addTab("Поставки", new DeliveryPanel(dbManager));
        
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Файл");
        
        JMenuItem clearDataItem = new JMenuItem("Очистить все данные");
        clearDataItem.addActionListener(e -> clearAllData());
        
        fileMenu.add(clearDataItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        
        add(tabbedPane);
    }
    
    private void clearAllData() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Вы уверены, что хотите очистить все данные?",
            "Подтверждение",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dbManager.clearAllData();
                componentsPanel.loadComponents();
                deliveryPanel.loadDeliveries();
                JOptionPane.showMessageDialog(
                    this,
                    "Все данные успешно очищены",
                    "Успех",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Ошибка при очистке данных: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}
