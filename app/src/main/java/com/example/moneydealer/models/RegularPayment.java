package com.example.moneydealer.models;

public class RegularPayment {
    public String id;
    public String accountId;
    public float amount;
    public String categoryId;
    public String type;
    public String comment;
    public long startTimestamp;
    public String period;
    public long lastAppliedTimestamp;

    public RegularPayment() {}

    public RegularPayment(String id, String accountId, float amount, String categoryId, String type, String comment, long startTimestamp, String period, long lastAppliedTimestamp) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.categoryId = categoryId;
        this.type = type;
        this.comment = comment;
        this.startTimestamp = startTimestamp;
        this.period = period;
        this.lastAppliedTimestamp = lastAppliedTimestamp;
    }
} 