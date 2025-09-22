package com.example.sharingapp;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;

/**
 * Contact model: unique username + email (+ optional id for legacy compatibility).
 * Java 7 compatible (no streams/lambdas).
 */
public class Contact implements Serializable {

    private String username;
    private String email;
    private String id; // optional

    public Contact() { }

    public Contact(String username, String email) {
        this(username, email, null);
    }

    public Contact(String username, String email, String id) {
        setUsername(username);
        setEmail(email);
        this.id = id;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("username required");
        }
        this.username = username.trim();
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("email required");
        }
        this.email = email.trim();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @Override
    public String toString() { return username; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact)) return false;
        Contact other = (Contact) o;
        if (this.username == null || other.username == null) return false;
        return this.username.equalsIgnoreCase(other.username);
    }

    @Override
    public int hashCode() {
        return username == null ? 0 : username.toLowerCase().hashCode();
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("username", username);
        o.put("email", email);
        o.put("id", id);
        return o;
    }

    public static Contact fromJson(JSONObject o) throws JSONException {
        return new Contact(
                o.optString("username", null),
                o.optString("email", null),
                o.optString("id", null)
        );
    }
}