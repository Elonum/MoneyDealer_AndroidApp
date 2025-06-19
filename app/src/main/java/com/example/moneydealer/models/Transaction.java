package com.example.moneydealer.models;

public class Transaction {
    public String id;
    public String accountId;
    public float amount;
    public String categoryId;
    public String type;
    public long timestamp;
    public String comment;

    public Transaction() {}

    public Transaction(String id, String accountId, float amount, String categoryId, String type, long timestamp, String comment) {
        this.id         = id;
        this.accountId  = accountId;
        this.amount     = amount;
        this.categoryId = categoryId;
        this.type       = type;
        this.timestamp  = timestamp;
        this.comment    = comment;
    }
}
