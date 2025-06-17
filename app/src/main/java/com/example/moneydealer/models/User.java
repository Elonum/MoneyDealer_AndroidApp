package com.example.moneydealer.models;

public class User {
    public String name;
    public String surname;
    public String email;
    public String selectedCurrency;

    public User() {}

    public User(String name, String surname, String email) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.selectedCurrency = "";
    }

    public User(String name, String surname, String email, String selectedCurrency) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.selectedCurrency = selectedCurrency;
    }
}
