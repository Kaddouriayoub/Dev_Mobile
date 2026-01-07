package com.example.myapplication.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Reservation extends RealmObject {

    @PrimaryKey
    private Long id;
    private String reservationDate;
    private String startTime;
    private String endTime;
    private String status; // Enum as String
    private double totalPrice;
    private Long workspaceId;
    private Workspace workspace; // Reference to Workspace object
    private Long clientId;
    private Client client; // Reference to Client object

    public Reservation() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReservationDate() { return reservationDate; }
    public void setReservationDate(String reservationDate) { this.reservationDate = reservationDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public ReservationStatus getStatus() { return status != null ? ReservationStatus.valueOf(status) : null; }
    public void setStatus(ReservationStatus status) { this.status = status != null ? status.name() : null; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public Long getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(Long workspaceId) { this.workspaceId = workspaceId; }

    public Workspace getWorkspace() { return workspace; }
    public void setWorkspace(Workspace workspace) { this.workspace = workspace; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
}
