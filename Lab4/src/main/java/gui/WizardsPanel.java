/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import com.mycompany.lab4.Main;
import model.Wizard;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WizardsPanel extends JPanel {
    private JTable wizardsTable;

    public WizardsPanel() {
        initializeUI();
        loadWizards();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addButton = new JButton("Добавить покупателя");
        addButton.addActionListener(e -> showAddWizardDialog());

        JButton refreshButton = new JButton("Обновить");
        refreshButton.addActionListener(e -> loadWizards());

        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.NORTH);

        wizardsTable = new JTable();
        wizardsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(wizardsTable);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadWizards() {
        List<Wizard> wizards = new ArrayList<>();
        String sql = "SELECT * FROM wizards";

        try (Connection conn = DriverManager.getConnection(Main.DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Wizard wizard = new Wizard();
                wizard.setId(rs.getInt("id"));
                wizard.setFirstName(rs.getString("first_name"));
                wizard.setLastName(rs.getString("last_name"));
                wizard.setBirthDate(LocalDate.parse(rs.getString("birth_date")));
                wizard.setSchool(rs.getString("school"));
                wizard.setContactInfo(rs.getString("contact_info"));
                wizards.add(wizard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String[] columnNames = {"ID", "Имя", "Фамилия", "Дата рождения", "Школа", "Контакты"};
        Object[][] data = new Object[wizards.size()][6];

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (int i = 0; i < wizards.size(); i++) {
            Wizard wizard = wizards.get(i);
            data[i][0] = wizard.getId();
            data[i][1] = wizard.getFirstName();
            data[i][2] = wizard.getLastName();
            data[i][3] = wizard.getBirthDate().format(formatter);
            data[i][4] = wizard.getSchool();
            data[i][5] = wizard.getContactInfo();
        }

        wizardsTable.setModel(new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }

    private void showAddWizardDialog() {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Добавить покупателя", true);
        dialog.setLayout(new GridLayout(0, 2, 5, 5));

        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField birthDateField = new JTextField();
        JTextField schoolField = new JTextField();
        JTextField contactField = new JTextField();

        dialog.add(new JLabel("Имя:"));
        dialog.add(firstNameField);
        dialog.add(new JLabel("Фамилия:"));
        dialog.add(lastNameField);
        dialog.add(new JLabel("Дата рождения (гггг-мм-дд):"));
        dialog.add(birthDateField);
        dialog.add(new JLabel("Школа:"));
        dialog.add(schoolField);
        dialog.add(new JLabel("Контакты:"));
        dialog.add(contactField);

        JButton saveButton = new JButton("Сохранить");
        saveButton.addActionListener(e -> {
            try {
                String sql = "INSERT INTO wizards (first_name, last_name, birth_date, school, contact_info) VALUES ('" +
                        firstNameField.getText() + "', '" +
                        lastNameField.getText() + "', '" +
                        birthDateField.getText() + "', '" +
                        schoolField.getText() + "', '" +
                        contactField.getText() + "')";

                try (Connection conn = DriverManager.getConnection(Main.DB_URL);
                     Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(sql);
                }

                loadWizards();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Новый волшебник добавлен!");

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        dialog,
                        "Ошибка! Проверьте формат даты (гггг-мм-дд). " + ex.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        dialog.add(new JLabel());
        dialog.add(saveButton);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
