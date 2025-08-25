/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;


public class DeliveryItem {
    private int id;
    private int deliveryId;
    private int componentId;
    private int quantity;
    private double unitPrice;
    
    public DeliveryItem() {}

    public DeliveryItem(int componentId, int quantity, double unitPrice) {
        this.componentId = componentId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getId() {
        return id;
    }

    public int getDeliveryId() {
        return deliveryId;
    }

    public int getComponentId() {
        return componentId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDeliveryId(int deliveryId) {
        this.deliveryId = deliveryId;
    }

    public void setComponentId(int componentId) {
        this.componentId = componentId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
    
}
