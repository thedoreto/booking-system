package com.hotel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document(collection = "customers")
public class Customer {

    @Id
    private String id;

    private String name;
    private String email;

    public Customer(String name, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (email == null || email.isBlank() ||
                !email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Not valid email");
        }

        this.name = name;
        this.email = email;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer other = (Customer) o;
        return Objects.equals(this.id, other.id);
    }
    @Override
    public int hashCode() {
        return Integer.parseInt(id);
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
}
