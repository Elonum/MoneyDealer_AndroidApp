package com.example.moneydealer.utils;

public class CurrencyUtils {
    public static String getCurrencySymbol(String currencyCode) {
        if (currencyCode == null) return "";
        switch (currencyCode) {
            case "RUB": return "₽";
            case "USD": return "$";
            case "EUR": return "€";
            case "BYN": return "Br";
            case "KZT": return "₸";
            case "UAH": return "₴";
            case "GBP": return "£";
            case "JPY": return "¥";
            case "CNY": return "¥";
            case "KRW": return "₩";
            case "INR": return "₹";
            case "BRL": return "R$";
            case "TRY": return "₺";
            case "CHF": return "₣";
            case "CAD": return "$";
            case "AUD": return "$";
            case "MXN": return "$";
            default: return currencyCode;
        }
    }
} 