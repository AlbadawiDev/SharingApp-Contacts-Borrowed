package com.example.sharingapp;

import java.util.HashSet;
import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;

public class ContactRegressionTest {
    @Test public void equalUsernamesHaveEqualHashesInTurkishLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            Contact upper = new Contact("I", "demo@example.test");
            Contact lower = new Contact("i", "demo@example.test");
            Contact dotted = new Contact("\u0130", "demo@example.test");
            assertEquals(upper, lower);
            assertEquals(upper.hashCode(), lower.hashCode());
            assertEquals(dotted, lower);
            assertEquals(dotted.hashCode(), lower.hashCode());
            HashSet<Contact> set = new HashSet<Contact>();
            set.add(upper); set.add(lower); set.add(dotted);
            assertEquals(1, set.size());
        } finally { Locale.setDefault(previous); }
    }

    @Test public void paddedAndBlankUsernamesCannotBypassUniqueness() {
        ContactList contacts = new ContactList();
        assertTrue(contacts.addContact(new Contact(" Alice ", " demo@example.test ")));
        assertFalse(contacts.isUsernameAvailable(" alice "));
        assertFalse(contacts.isUsernameAvailable("  "));
        assertFalse(contacts.addContact(new Contact("ALICE", "other@example.test")));
        assertEquals(1, contacts.size());
    }
}
