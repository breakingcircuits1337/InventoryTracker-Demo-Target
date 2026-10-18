package com.acme.inventory.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a single inventory item.
 */
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long   id;
    private String sku;
    private String name;
    private String description;
    private String category;
    private double unitPrice;
    private int    quantityOnHand;
    private int    reorderPoint;
    private String supplierCode;
    private Date   lastUpdated;
    private boolean active;

    public Product() {
    }

    public Product(Long id, String sku, String name, String category,
                   double unitPrice, int quantityOnHand) {
        this.id             = id;
        this.sku            = sku;
        this.name           = name;
        this.category       = category;
        this.unitPrice      = unitPrice;
        this.quantityOnHand = quantityOnHand;
        this.active         = true;
        this.lastUpdated    = new Date();
    }

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public String getSku()                     { return sku; }
    public void setSku(String sku)             { this.sku = sku; }

    public String getName()                    { return name; }
    public void setName(String name)           { this.name = name; }

    public String getDescription()             { return description; }
    public void setDescription(String d)       { this.description = d; }

    public String getCategory()                { return category; }
    public void setCategory(String category)   { this.category = category; }

    public double getUnitPrice()               { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public int getQuantityOnHand()             { return quantityOnHand; }
    public void setQuantityOnHand(int q)       { this.quantityOnHand = q; }

    public int getReorderPoint()               { return reorderPoint; }
    public void setReorderPoint(int r)         { this.reorderPoint = r; }

    public String getSupplierCode()            { return supplierCode; }
    public void setSupplierCode(String s)      { this.supplierCode = s; }

    public Date getLastUpdated()               { return lastUpdated; }
    public void setLastUpdated(Date d)         { this.lastUpdated = d; }

    public boolean isActive()                  { return active; }
    public void setActive(boolean active)      { this.active = active; }

    public boolean isLowStock() {
        return quantityOnHand <= reorderPoint;
    }

    @Override
    public String toString() {
        return "Product{id=" + id + ", sku='" + sku + "', name='" + name + "'}";
    }
}
