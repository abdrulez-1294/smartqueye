package com.smartqueue.model;

/**
 * Product.java — Model class representing a retail product in SmartQueue.
 *
 * Fields mirror the 'products' table.
 * 'expectedWeightGrams' is used by the Kiosk Weight Verification logic.
 */
public class Product {

    private int    productId;
    private String name;
    private String barcode;           // Unique barcode string (simulated by text input)
    private double price;             // Price in local currency (e.g., ₹ or $)
    private int    expectedWeightGrams; // Expected weight in grams for verification
    private int    stockQty;          // Current stock quantity
    private String category;          // e.g., "Grocery", "Electronics", "Dairy"
    private String imageUrl;          // Optional product image path

    // -------------------------------------------------------
    // Constructors
    // -------------------------------------------------------

    public Product() {}

    public Product(int productId, String name, String barcode, double price,
                   int expectedWeightGrams, int stockQty, String category, String imageUrl) {
        this.productId           = productId;
        this.name                = name;
        this.barcode             = barcode;
        this.price               = price;
        this.expectedWeightGrams = expectedWeightGrams;
        this.stockQty            = stockQty;
        this.category            = category;
        this.imageUrl            = imageUrl;
    }

    // -------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------

    public int getProductId()                  { return productId; }
    public void setProductId(int productId)    { this.productId = productId; }

    public String getName()              { return name; }
    public void setName(String name)     { this.name = name; }

    public String getBarcode()                 { return barcode; }
    public void setBarcode(String barcode)     { this.barcode = barcode; }

    public double getPrice()               { return price; }
    public void setPrice(double price)     { this.price = price; }

    public int getExpectedWeightGrams()                          { return expectedWeightGrams; }
    public void setExpectedWeightGrams(int expectedWeightGrams)  { this.expectedWeightGrams = expectedWeightGrams; }

    public int getStockQty()                   { return stockQty; }
    public void setStockQty(int stockQty)      { this.stockQty = stockQty; }

    public String getCategory()                    { return category; }
    public void setCategory(String category)       { this.category = category; }

    public String getImageUrl()                    { return imageUrl; }
    public void setImageUrl(String imageUrl)       { this.imageUrl = imageUrl; }

    @Override
    public String toString() {
        return "Product{id=" + productId + ", name='" + name + "', barcode='" + barcode
               + "', price=" + price + ", weight=" + expectedWeightGrams + "g}";
    }
}
