package com.example.myapplication;

import java.util.List;

public class Workspace {

    private Long id;
    private String name;
    private String description;
    private WorkspaceType type;
    private int capacity;
    private double pricePerHour;
    private String city;
    private String address;
    private List<String> images;
    private String status;

    public Workspace() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public WorkspaceType getType() { return type; }
    public void setType(WorkspaceType type) { this.type = type; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public double getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(double pricePerHour) { this.pricePerHour = pricePerHour; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

