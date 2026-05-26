package com.hotel.booking.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String name;
    private String password;
    private String role; // "USER" / "ADMIN"

    public User() {
    }

    public User(String name, String email, String password) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (email == null || email.isBlank() ||
                !email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Not valid email");
        }
        if (password == null || password.isBlank() || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        this.password = password;
        this.name = name;
        this.email = email;
        this.role = "USER";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
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
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
