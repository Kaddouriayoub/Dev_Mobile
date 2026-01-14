package com.example.myapplication.model;

public class RevenueData {
    private Long workspaceId;
    private String workspaceName;
    private double totalRevenue;
    private int confirmedOrderCount;
    private int completedOrderCount;
    private int totalOrderCount;

    public RevenueData(Long workspaceId, String workspaceName) {
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;
        this.totalRevenue = 0.0;
        this.confirmedOrderCount = 0;
        this.completedOrderCount = 0;
        this.totalOrderCount = 0;
    }

    // Getters
    public Long getWorkspaceId() {
        return workspaceId;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public int getConfirmedOrderCount() {
        return confirmedOrderCount;
    }

    public int getCompletedOrderCount() {
        return completedOrderCount;
    }

    public int getTotalOrderCount() {
        return totalOrderCount;
    }

    // Setters
    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public void setConfirmedOrderCount(int confirmedOrderCount) {
        this.confirmedOrderCount = confirmedOrderCount;
    }

    public void setCompletedOrderCount(int completedOrderCount) {
        this.completedOrderCount = completedOrderCount;
    }

    public void setTotalOrderCount(int totalOrderCount) {
        this.totalOrderCount = totalOrderCount;
    }

    // Helper methods
    public void addRevenue(double amount) {
        this.totalRevenue += amount;
        this.totalOrderCount++;
    }

    public void incrementConfirmed() {
        this.confirmedOrderCount++;
    }

    public void incrementCompleted() {
        this.completedOrderCount++;
    }
}
