package com.example.sharingapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * Add a new contact
 */
public class AddContactActivity extends AppCompatActivity {

    private ContactList contact_list = new ContactList();
    private Context context;

    private EditText username;
    private EditText email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        username = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.email);

        context = getApplicationContext();
        contact_list.loadContacts(context);
    }

    public void saveContact(View view) {

        String username_str = username.getText().toString().trim();
        String email_str = email.getText().toString().trim();

        if (username_str.equals("")) {
            username.setError("Empty field!");
            return;
        }

        if (email_str.equals("")) {
            email.setError("Empty field!");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email_str).matches()){
            email.setError("Must be an email address!");
            return;
        }

        if (!contact_list.isUsernameAvailable(username_str)){
            username.setError("Username already taken!");
            return;
        }

        Contact contact = new Contact(username_str, email_str, null);

        if (!contact_list.addContact(contact)) {
            username.setError("Username already taken!");
            return;
        }
        contact_list.saveContacts(context);

        // End AddContactActivity
        finish();
    }
}

