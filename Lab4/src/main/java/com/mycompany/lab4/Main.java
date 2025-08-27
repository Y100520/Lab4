/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.lab4;

import gui.MainFrame;
import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static final String DB_URL = "jdbc:sqlite:ollivanders_junior.db";

    public static void main(String[] args){
        initializeDatabase();

        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }

    public static void initializeDatabase() {
        String[] createTables = {
                "CREATE TABLE IF NOT EXISTS components (id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT, name TEXT NOT NULL, quantity INTEGER DEFAULT 0)",
                "CREATE TABLE IF NOT EXISTS wizards (id INTEGER PRIMARY KEY AUTOINCREMENT, first_name TEXT NOT NULL, last_name TEXT NOT NULL, birth_date TEXT, school TEXT, contact_info TEXT)",
                "CREATE TABLE IF NOT EXISTS wands (id INTEGER PRIMARY KEY AUTOINCREMENT, creation_date TEXT NOT NULL, price REAL, status TEXT, wood_id INTEGER, core_id INTEGER, wizard_id INTEGER, sale_date TEXT)",
                "CREATE TABLE IF NOT EXISTS deliveries (id INTEGER PRIMARY KEY AUTOINCREMENT, delivery_date TEXT NOT NULL, supplier_name TEXT NOT NULL, is_seasonal INTEGER DEFAULT 0)",
                "CREATE TABLE IF NOT EXISTS delivery_items (id INTEGER PRIMARY KEY AUTOINCREMENT, delivery_id INTEGER, component_id INTEGER, quantity INTEGER, unit_price REAL)"
        };

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            for (String sql : createTables) {
                stmt.execute(sql);
            }
            System.out.println("База данных готова к работе!");
        } catch (SQLException e) {
            System.err.println("Ой, не могу создать базу данных! Зовите Дамблдора!");
            e.printStackTrace();
        }
    }
}
