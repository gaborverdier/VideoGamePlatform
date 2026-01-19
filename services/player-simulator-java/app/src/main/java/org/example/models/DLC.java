package org.example. models;

public class DLC {
    private String name;
    private double price;
    private boolean installed;
    
    public DLC(String name, double price) {
        this.name = name;
        this. price = price;
        this. installed = false;
    }
    
    public String getName() { return name; }
    public double getPrice() { return price; }
    public boolean isInstalled() { return installed; }
    public void setInstalled(boolean installed) { this.installed = installed; }
    
    public String getFormattedPrice() {
        return String.format("%.2f€", price);
    }
}