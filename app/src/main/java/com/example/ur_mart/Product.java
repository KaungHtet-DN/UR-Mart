package com.example.ur_mart;

public class Product {
    private String productId, productName, description, price, image;

    public String getproductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }

    public Product(String productId, String productName, String description, String price, String image) {
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.image = image;
    }

    public Product(){

    }
}
