package com.acme.inventory.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a stock adjustment transaction.
 */
public class StockMovement implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum MovementType { RECEIPT, ISSUE, ADJUSTMENT, RETURN }

    private Long         id;
    private Long         productId;
    private MovementType type;
    private int          quantity;
    private String       referenceNumber;
    private String       notes;
    private String       createdBy;
    private Date         createdAt;

    public StockMovement() {
        this.createdAt = new Date();
    }

    public Long getId()                           { return id; }
    public void setId(Long id)                    { this.id = id; }

    public Long getProductId()                    { return productId; }
    public void setProductId(Long productId)      { this.productId = productId; }

    public MovementType getType()                 { return type; }
    public void setType(MovementType type)        { this.type = type; }

    public int getQuantity()                      { return quantity; }
    public void setQuantity(int quantity)         { this.quantity = quantity; }

    public String getReferenceNumber()            { return referenceNumber; }
    public void setReferenceNumber(String r)      { this.referenceNumber = r; }

    public String getNotes()                      { return notes; }
    public void setNotes(String notes)            { this.notes = notes; }

    public String getCreatedBy()                  { return createdBy; }
    public void setCreatedBy(String createdBy)    { this.createdBy = createdBy; }

    public Date getCreatedAt()                    { return createdAt; }
    public void setCreatedAt(Date createdAt)      { this.createdAt = createdAt; }
}
