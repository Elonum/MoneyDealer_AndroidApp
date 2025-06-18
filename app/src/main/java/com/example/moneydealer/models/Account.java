package com.example.moneydealer.models;

import java.util.ArrayList;
import java.util.List;

public class Account {
    public String id;
    public String name;
    public String currency;
    public List<Transaction> incomes;
    public List<Transaction> expenses;

    public Account() {
        incomes = new ArrayList<>();
        expenses = new ArrayList<>();
    }

    public Account(String id, String name, String currency) {
        this.id = id;
        this.name = name;
        this.currency = currency;
        this.incomes = new ArrayList<>();
        this.expenses = new ArrayList<>();
    }

    public double getBalance() {
        double totalIncome = 0;
        double totalExpense = 0;
        for (Transaction income : incomes) {
            totalIncome += income.amount;
        }
        for (Transaction expense : expenses) {
            totalExpense += expense.amount;
        }
        return totalIncome - totalExpense;
    }
} 