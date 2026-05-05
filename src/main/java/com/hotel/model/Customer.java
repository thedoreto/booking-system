package com.hotel.model;

import com.hotel.service.result.Result;

public class Customer {
    private static int idCount = 0;
    private int id;
    private String name;
    private String email;
    public Customer(String name, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (email == null || email.isBlank() ||
                !email.matches("@") || !email.matches(".")) {
            throw new IllegalArgumentException("Not valid email");
        }

        this.name = name;
        this.email = email;
        this.id = idCount++ ;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer other = (Customer) o;
        return this.id == other.id;
    }
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
}
