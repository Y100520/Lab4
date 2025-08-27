/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDate;

public class Wand {
    private int id;
    private LocalDate creationDate;
    private double price;
    private String status; 
    private int woodId;
    private int coreId;
    private int wizardId; 
    private LocalDate saleDate;
    private Wizard owner;
    
    public Wand() {}

    public Wand(LocalDate creationDate, double price, int woodId, int coreId) {
        this.creationDate = creationDate;
        this.price = price;
        this.status = "available";
        this.woodId = woodId;
        this.coreId = coreId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getWoodId() { return woodId; }
    public void setWoodId(int woodId) { this.woodId = woodId; }
    public int getCoreId() { return coreId; }
    public void setCoreId(int coreId) { this.coreId = coreId; }
    public int getWizardId() { return wizardId; }
    public void setWizardId(int wizardId) { this.wizardId = wizardId; }
    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }
    public Wizard getOwner() { return owner; }
    public void setOwner(Wizard owner) { this.owner = owner; }
}
