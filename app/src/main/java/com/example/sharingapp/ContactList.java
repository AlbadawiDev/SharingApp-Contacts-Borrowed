package com.example.sharingapp;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ContactList (Java 7 compatible): no lambdas/streams.
 * - load/save via SharedPreferences (JSON)
 * - unique username (case-insensitive)
 * - setContacts overloads
 * - hasContact(contact), getIndex(contact) for Activities
 */
public class ContactList {

    private static final String PREFS = "SharingAppContacts";
    private static final String KEY_JSON = "contacts_json";

    private final ArrayList<Contact> contacts = new ArrayList<Contact>();

    /* ===== Persistence ===== */
    public void loadContacts(Context ctx) {
        contacts.clear();
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = sp.getString(KEY_JSON, "[]");
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                contacts.add(Contact.fromJson(o));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveContacts(Context ctx) {
        JSONArray arr = new JSONArray();
        try {
            for (int i = 0; i < contacts.size(); i++) {
                arr.put(contacts.get(i).toJson());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_JSON, arr.toString()).apply();
    }

    /* ===== CRUD & queries ===== */
    /** Returns a copy so external code can't corrupt internal list. */
    public ArrayList<Contact> getContacts() {
        return new ArrayList<Contact>(contacts);
    }

    public void setContacts(ArrayList<Contact> newContacts) {
        contacts.clear();
        if (newContacts != null) {
            for (int i = 0; i < newContacts.size(); i++) {
                contacts.add(newContacts.get(i));
            }
        }
    }

    public void setContacts(List<Contact> newContacts) {
        contacts.clear();
        if (newContacts != null) {
            for (int i = 0; i < newContacts.size(); i++) {
                contacts.add(newContacts.get(i));
            }
        }
    }

    public int size() { return contacts.size(); }

    public boolean isUsernameAvailable(String username) {
        if (username == null) return false;
        for (int i = 0; i < contacts.size(); i++) {
            Contact c = contacts.get(i);
            if (username.equalsIgnoreCase(c.getUsername())) return false;
        }
        return true;
    }

    public boolean addContact(Contact c) {
        if (c == null || c.getUsername() == null) return false;
        if (!isUsernameAvailable(c.getUsername())) return false;
        contacts.add(c);
        return true;
    }

    public boolean removeContact(Contact c) {
        if (c == null || c.getUsername() == null) return false;
        return removeByUsername(c.getUsername());
    }

    public boolean removeByUsername(String username) {
        if (username == null) return false;
        for (Iterator<Contact> it = contacts.iterator(); it.hasNext(); ) {
            Contact x = it.next();
            String u = x.getUsername();
            if (u != null && u.equalsIgnoreCase(username)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public Contact getContact(String username) {
        if (username == null) return null;
        for (int i = 0; i < contacts.size(); i++) {
            Contact c = contacts.get(i);
            if (username.equalsIgnoreCase(c.getUsername())) return c;
        }
        return null;
    }

    public boolean hasContact(Contact c) {
        if (c == null) return false;
        for (int i = 0; i < contacts.size(); i++) {
            if (c.equals(contacts.get(i))) return true;
        }
        return false;
    }

    /** Returns index of contact in the current list; -1 if not found. */
    public int getIndex(Contact c) {
        if (c == null) return -1;
        for (int i = 0; i < contacts.size(); i++) {
            if (c.equals(contacts.get(i))) return i;
        }
        return -1;
    }

    public boolean updateContactEmail(String username, String newEmail) {
        Contact c = getContact(username);
        if (c == null) return false;
        c.setEmail(newEmail);
        return true;
    }

    public void clear() { contacts.clear(); }
}