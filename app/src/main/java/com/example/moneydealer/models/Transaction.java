package com.example.moneydealer.models;

public class Transaction {
    public String id;
    public float amount;
    public String categoryId;
    public String type;        // "expense" или "income"
    public long timestamp;     // время в миллисекундах
    public String comment;

    public Transaction() {}

    public Transaction(String id, float amount, String categoryId, String type, long timestamp, String comment) {
        this.id         = id;
        this.amount     = amount;
        this.categoryId = categoryId;
        this.type       = type;
        this.timestamp  = timestamp;
        this.comment    = comment;
    }
}
