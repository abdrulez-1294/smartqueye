package com.smartqueue.model;

/**
 * CartItem.java — Represents a single line item inside a shopping cart.
 *
 * Stored as a list inside the HttpSession under the key "cart".
 * Each item holds a reference to a Product plus quantity.
 */
public class CartItem {

    private Product product;  // The product being purchased
    private int     quantity; // How many units of this product

    // -------------------------------------------------------
    // Constructors
    // -------------------------------------------------------

    public CartItem() {}

    public CartItem(Product product, int quantity) {
        this.product  = product;
        this.quantity = quantity;
    }

    // -------------------------------------------------------
    // Derived Calculations (no DB needed)
    // -------------------------------------------------------

    /**
     * Total price for this line item (unit price × quantity).
     */
    public double getLineTotal() {
        return product.getPrice() * quantity;
    }

    /**
     * Total expected weight for this line item (grams × quantity).
     * Used by kiosk weight verification.
     */
    public int getLineTotalWeightGrams() {
        return product.getExpectedWeightGrams() * quantity;
    }

    // -------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------

    public Product getProduct()                  { return product; }
    public void setProduct(Product product)      { this.product = product; }

    public int getQuantity()               { return quantity; }
    public void setQuantity(int quantity)  { this.quantity = quantity; }
}
