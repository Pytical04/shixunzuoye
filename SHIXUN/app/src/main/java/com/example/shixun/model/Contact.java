package com.example.shixun.model;

public class Contact {
    private long id;
    private String name;
    private String phone;
    private String email;
    private long userId;

    public Contact(long id, String name, String phone, String email, long userId) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.userId = userId;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
} 