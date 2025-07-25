package com.example.moneydealer.models;

import java.util.ArrayList;
import java.util.List;

public class Account {
    public String id;
    public String name;
    public long createdAt;
    public double initialBalance;
    public List<Transaction> incomes;
    public List<Transaction> expenses;
    public double balance;

    public Account() {
        incomes = new ArrayList<>();
        expenses = new ArrayList<>();
    }

    public Account(String id, String name, long createdAt, double initialBalance) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.initialBalance = initialBalance;
        this.incomes = new ArrayList<>();
        this.expenses = new ArrayList<>();
    }

    public double getTotalIncome() {
        double total = 0;
        for (Transaction income : incomes) {
            total += income.amount;
        }
        return total;
    }

    public double getTotalExpense() {
        double total = 0;
        for (Transaction expense : expenses) {
            total += expense.amount;
        }
        return total;
    }

    public double getBalance() {
        return initialBalance + getTotalIncome() - getTotalExpense();
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> all = new ArrayList<>();
        all.addAll(incomes);
        all.addAll(expenses);
        all.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
        return all;
    }
} 