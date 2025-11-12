package com.finance.models;

import java.io.Serializable;

public class Budget implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String budgetId;
    private String userId;
    private String category;
    private double budgetAmount;
    private double spentAmount;
    private String month;
    private String year;

    public Budget() {
    }

    public Budget(String budgetId, String userId, String category, double budgetAmount, 
                 double spentAmount, String month, String year) {
        this.budgetId = budgetId;
        this.userId = userId;
        this.category = category;
        this.budgetAmount = budgetAmount;
        this.spentAmount = spentAmount;
        this.month = month;
        this.year = year;
    }

    // Getters and Setters
    public String getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(String budgetId) {
        this.budgetId = budgetId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public double getRemainingAmount() {
        return budgetAmount - spentAmount;
    }

    public double getPercentageUsed() {
        if (budgetAmount == 0) return 0;
        return (spentAmount / budgetAmount) * 100;
    }

    @Override
    public String toString() {
        return "Budget{" +
                "budgetId='" + budgetId + '\'' +
                ", userId='" + userId + '\'' +
                ", category='" + category + '\'' +
                ", budgetAmount=" + budgetAmount +
                ", spentAmount=" + spentAmount +
                ", month='" + month + '\'' +
                ", year='" + year + '\'' +
                '}';
    }
}
