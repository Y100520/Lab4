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

public class SalesPanel extends JPanel {
    private JTable salesTable;

    public SalesPanel() {
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
        add(new JScrollPane(salesTable), BorderLayout.CENTER);
    }

    private void loadSales() {
        List<Wand> soldWands = new ArrayList<>();
        try {
            try (Connection conn = DriverManager.getConnection(Main.DB_URL);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM wands WHERE status = 'sold'")) {
                while (rs.next()) {
                    Wand wand = new Wand();
                    wand.setId(rs.getInt("id"));
                    wand.setCreationDate(LocalDate.parse(rs.getString("creation_date")));
                    wand.setSaleDate(LocalDate.parse(rs.getString("sale_date")));
                    wand.setPrice(rs.getDouble("price"));
                    wand.setWizardId(rs.getInt("wizard_id"));
                    soldWands.add(wand);
                }
            }

            List<Wizard> allWizards = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection(Main.DB_URL);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM wizards")) {
                while (rs.next()) {
                    Wizard wizard = new Wizard();
                    wizard.setId(rs.getInt("id"));
                    wizard.setFirstName(rs.getString("first_name"));
                    wizard.setLastName(rs.getString("last_name"));
                    wizard.setSchool(rs.getString("school"));
                    allWizards.add(wizard);
                }
            }

            for (Wand wand : soldWands) {
                for (Wizard wizard : allWizards) {
                    if (wand.getWizardId() == wizard.getId()) {
                        wand.setOwner(wizard);
                        break;
                    }
                }
            }

            String[] columnNames = {"ID палочки", "Дата создания", "Цена", "Дата продажи", "Владелец", "Школа"};
            Object[][] data = new Object[soldWands.size()][6];
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            for (int i = 0; i < soldWands.size(); i++) {
                Wand wand = soldWands.get(i);
                data[i][0] = wand.getId();
                data[i][1] = wand.getCreationDate().format(formatter);
                data[i][2] = wand.getPrice();
                data[i][3] = wand.getSaleDate().format(formatter);
                if (wand.getOwner() != null) {
                    data[i][4] = wand.getOwner().getFirstName() + " " + wand.getOwner().getLastName();
                    data[i][5] = wand.getOwner().getSchool();
                } else {
                    data[i][4] = "Неизвестный волшебник";
                    data[i][5] = "-";
                }
            }

            salesTable.setModel(new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
