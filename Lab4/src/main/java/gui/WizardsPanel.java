/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import com.mycompany.lab4.DatabaseManager;
import model.Wizard;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WizardsPanel extends JPanel {
    private final DatabaseManager dbManager;
    private JTable wizardsTable;
    
    public WizardsPanel(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        initializeUI();
        loadWizards();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton addButton = new JButton("Добавить покупателя");
        addButton.addActionListener(e -> showAddWizardDialog());
        
        JButton refreshButton = new JButton("Обновить");
        refreshButton.addActionListener(e -> loadWizards());
        
        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);
        
        add(buttonPanel, BorderLayout.NORTH);
        
        // Таблица с покупателями
        wizardsTable = new JTable();
        wizardsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(wizardsTable);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadWizards() {
        try {
            List<Wizard> wizards = dbManager.getAllWizards();
            
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
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Ошибка загрузки данных: " + e.getMessage(),
                "Ошибка",
                JOptionPane.ERROR_MESSAGE
            );
        }
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
                Wizard wizard = new Wizard(
                    firstNameField.getText(),
                    lastNameField.getText(),
                    LocalDate.parse(birthDateField.getText())
                );
                wizard.setSchool(schoolField.getText());
                wizard.setContactInfo(contactField.getText());
                
                dbManager.addWizard(wizard);
                loadWizards();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    dialog,
                    "Ошибка: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
        
        dialog.add(saveButton);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}