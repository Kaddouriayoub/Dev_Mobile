package com.example.myapplication.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Client extends RealmObject {

    @PrimaryKey
    private Long id;
    private double discountRate;
    private int loyaltyPoints;
    private String subscriptionType; // Enum as String
    private int totalReservations;

    public Client() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getDiscountRate() { return discountRate; }
    public void setDiscountRate(double discountRate) { this.discountRate = discountRate; }

    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }

    public SubscriptionType getSubscriptionType() { return subscriptionType != null ? SubscriptionType.valueOf(subscriptionType) : null; }
    public void setSubscriptionType(SubscriptionType subscriptionType) { this.subscriptionType = subscriptionType != null ? subscriptionType.name() : null; }

    public int getTotalReservations() { return totalReservations; }
    public void setTotalReservations(int totalReservations) { this.totalReservations = totalReservations; }
}
