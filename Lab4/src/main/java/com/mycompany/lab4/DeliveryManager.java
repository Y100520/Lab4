/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.lab4;


import java.sql.SQLException;
import model.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class DeliveryManager {
    private final DatabaseManager dbManager;
    
    public DeliveryManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    public boolean processWeeklyDelivery(JFrame parentFrame) throws SQLException {
    List<ComponentWand> lowStockComponents = dbManager.getComponentsLowStock(10);

    if (lowStockComponents.isEmpty()) {
        JOptionPane.showMessageDialog(parentFrame,
            "Нет компонентов с количеством меньше 10. Все компоненты в достатке.",
            "Нет компонентов для заказа",
            JOptionPane.INFORMATION_MESSAGE);
        return false;
    }
    
    int itemsCount = Math.min(15, lowStockComponents.size()); 
    itemsCount = Math.max(itemsCount, 1); 
    
    List<DeliveryItem> items = new ArrayList<>();
    for (int i = 0; i < itemsCount; i++) {
        ComponentWand component = lowStockComponents.get(i);
        int quantity = calculateOrderQuantity(component, false);
        items.add(new DeliveryItem(
            component.getId(),
            quantity,
            dbManager.getComponentPrice(component.getId())
        ));
    }
    
    Delivery delivery = new Delivery(
        LocalDate.now().plusDays(2),
        "Основной поставщик",
        false
    );
    
    if (!items.isEmpty()) {
            delivery.setItems(items);
            dbManager.addDelivery(delivery);
    }
    return true;
}
    
    public void processSeasonalDelivery() throws SQLException {
        List<ComponentWand> popularWoods = dbManager.getPopularComponents("wood", 5);
       Delivery delivery = new Delivery(
            LocalDate.now().plusDays(3),
            "Сезонный поставщик",
            true
        );
        
        List<DeliveryItem> items = new ArrayList<>();
        for (ComponentWand wood : popularWoods) {
            DeliveryItem item = new DeliveryItem(
                wood.getId(),
                calculateOrderQuantity(wood, true),
                dbManager.getComponentPrice(wood.getId())
            );
            items.add(item);
        }
        
        delivery.setItems(items);
        dbManager.addDelivery(delivery);
    }
            
 
    private int calculateOrderQuantity(ComponentWand component, boolean isSeasonal) {
        int baseQuantity = isSeasonal ? 50 : 20;
    return Math.max(baseQuantity - component.getQuantity(), 10);
    }

    public boolean isSummerSeason() {
        Month currentMonth = LocalDate.now().getMonth();
        return currentMonth == Month.JUNE || 
               currentMonth == Month.JULY || 
               currentMonth == Month.AUGUST;
    }
    
}
