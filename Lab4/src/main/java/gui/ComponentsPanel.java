/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import com.mycompany.lab4.Main;
import model.ComponentWand;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComponentsPanel extends JPanel {
    private JTable componentsTable;

    public ComponentsPanel() {
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
        add(new JScrollPane(componentsTable), BorderLayout.CENTER);
    }

    public void loadComponents() {
        List<ComponentWand> components = new ArrayList<>();
        String sql = "SELECT id, type, name, quantity FROM components";

        try (Connection conn = DriverManager.getConnection(Main.DB_URL);
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

            if (components.isEmpty()) {
                createInitialComponents();
                loadComponents();
                return;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка загрузки компонентов: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

        String[] columnNames = {"ID", "Тип", "Название", "Количество"};
        Object[][] data = new Object[components.size()][4];
        for (int i = 0; i < components.size(); i++) {
            ComponentWand c = components.get(i);
            data[i][0] = c.getId();
            data[i][1] = c.getType();
            data[i][2] = c.getName();
            data[i][3] = c.getQuantity();
        }

        componentsTable.setModel(new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });
    }

    private void createInitialComponents() {
        ComponentWand[] initialComponents = {
                new ComponentWand("wood", "Дуб", 0), new ComponentWand("wood", "Ясень", 0),
                new ComponentWand("wood", "Остролист", 0), new ComponentWand("wood", "Кипарис", 0),
                new ComponentWand("wood", "Кедр", 0), new ComponentWand("wood", "Вяз", 0),
                new ComponentWand("wood", "Акация", 0), new ComponentWand("wood", "Бук", 0),
                new ComponentWand("wood", "Орех", 0), new ComponentWand("wood", "Вишня", 0),
                new ComponentWand("wood", "Красное дерево", 0), new ComponentWand("core", "Перо феникса", 0),
                new ComponentWand("core", "Волос единорога", 0), new ComponentWand("core", "Чешуя дракона", 0),
                new ComponentWand("core", "Волос вейлы", 0), new ComponentWand("core", "Хвостовое перо громовой птицы", 0),
                new ComponentWand("core", "Волос русалки", 0), new ComponentWand("core", "Клык василиска", 0)
        };

        String sql = "INSERT INTO components (type, name, quantity) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(Main.DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (ComponentWand component : initialComponents) {
                pstmt.setString(1, component.getType());
                pstmt.setString(2, component.getName());
                pstmt.setInt(3, component.getQuantity());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
