package com.smartqueue.model;

/**
 * User.java — Model class representing a registered customer.
 *
 * Fields mirror the 'users' table in the database.
 * This is a standard Java Bean (POJO) used by Servlets and JSPs.
 */
public class User {

    private int    userId;
    private String fullName;
    private String email;
    private String phone;        // Used as kiosk login identifier
    private String passwordHash; // Stored as plain text for academic scope (use BCrypt in production)
    private String role;         // "customer" | "admin"

    // -------------------------------------------------------
    // Constructors
    // -------------------------------------------------------

    public User() {}

    public User(int userId, String fullName, String email, String phone, String passwordHash, String role) {
        this.userId       = userId;
        this.fullName     = fullName;
        this.email        = email;
        this.phone        = phone;
        this.passwordHash = passwordHash;
        this.role         = role;
    }

    // -------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------

    public int getUserId()              { return userId; }
    public void setUserId(int userId)   { this.userId = userId; }

    public String getFullName()                  { return fullName; }
    public void setFullName(String fullName)     { this.fullName = fullName; }

    public String getEmail()                 { return email; }
    public void setEmail(String email)       { this.email = email; }

    public String getPhone()                 { return phone; }
    public void setPhone(String phone)       { this.phone = phone; }

    public String getPasswordHash()                      { return passwordHash; }
    public void setPasswordHash(String passwordHash)     { this.passwordHash = passwordHash; }

    public String getRole()              { return role; }
    public void setRole(String role)     { this.role = role; }

    @Override
    public String toString() {
        return "User{userId=" + userId + ", fullName='" + fullName + "', email='" + email + "', role='" + role + "'}";
    }
}
