/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDate;
import java.util.List;

public class Delivery {
    private int id;
    private LocalDate deliveryDate;
    private String supplierName;
    private boolean isSeasonal;
    private List<DeliveryItem> items;

    public Delivery() {}

    public Delivery(LocalDate deliveryDate, String supplierName, boolean isSeasonal) {
        this.deliveryDate = deliveryDate;
        this.supplierName = supplierName;
        this.isSeasonal = isSeasonal;
    }

    public int getId() {
        return id;
    }
    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }
    public String getSupplierName() {
        return supplierName;
    }
    public boolean isSeasonal() {
        return isSeasonal;
    }
    public List<DeliveryItem> getItems() {
        return items;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }
    public void setSeasonal(boolean isSeasonal) {
        this.isSeasonal = isSeasonal;
    }
    public void setItems(List<DeliveryItem> items) {
        this.items = items;
    }
}
