package com.example.moneydealer.models;

public class Category {
    public String id;
    public String name;
    public int color;
    public String type;

    public Category() {}

    public Category(String id, String name, int color, String type) {
        this.id    = id;
        this.name  = name;
        this.color = color;
        this.type  = type;
    }
}
