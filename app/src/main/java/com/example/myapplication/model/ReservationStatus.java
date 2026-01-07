package com.example.myapplication.model;

public enum ReservationStatus {
    PENDING,
    CONFIRMED,
    REFUSED,
    CANCELLED,
    COMPLETED  // Automatically set when reservation time has ended
}
