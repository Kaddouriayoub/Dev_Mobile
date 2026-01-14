package com.example.myapplication.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Review extends RealmObject {

    @PrimaryKey
    private Long id;
    private int rating;
    private String comment;
    private String createdAt;
    private Long clientId;
    private Client client; // Reference to Client object
    private Long workspaceId;
    private Workspace workspace; // Reference to Workspace object
    private long reservationId;
    private Reservation reservation;

    public Review() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Long getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(Long workspaceId) { this.workspaceId = workspaceId; }

    public Workspace getWorkspace() { return workspace; }
    public void setWorkspace(Workspace workspace) { this.workspace = workspace; }

    public long getReservationId() {return reservationId;}

    public void setReservationId(long reservationId) {this.reservationId = reservationId;}

    public Reservation getReservation() {return reservation;}

    public void setReservation(Reservation reservation) {this.reservation = reservation;}
}
