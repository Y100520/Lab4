/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import com.mycompany.lab4.Main;
import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MainFrame extends JFrame {

    public MainFrame() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Магазин волшебных палочек Олливандеры (Junior Edition)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Палочки", new WandsPanel());
        tabbedPane.addTab("Покупатели", new WizardsPanel());
        tabbedPane.addTab("Компоненты", new ComponentsPanel());
        tabbedPane.addTab("Продажи", new SalesPanel());
        tabbedPane.addTab("Поставки", new DeliveryPanel());

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
                "Вы уверены, что хотите стереть всю магию из базы данных?",
                "Подтверждение заклинания забвения",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection(Main.DB_URL);
                 Statement stmt = conn.createStatement()) {

                stmt.execute("DROP TABLE IF EXISTS wands");
                stmt.execute("DROP TABLE IF EXISTS wizards");
                stmt.execute("DROP TABLE IF EXISTS components");
                stmt.execute("DROP TABLE IF EXISTS deliveries");
                stmt.execute("DROP TABLE IF EXISTS delivery_items");

                Main.initializeDatabase();

                JOptionPane.showMessageDialog(
                        this,
                        "Все данные стерты! Можно начинать с чистого листа.",
                        "Успех",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "Ошибка заклинания! " + e.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}
