package com.example.sharingapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Edita o elimina un contacto existente.
 * - El username NO se edita (se usa como clave única).
 * - Se puede editar el email.
 * - No permite eliminar si el contacto es prestatario activo.
 *
 * Requiere que la Intent traiga "position" (índice en ContactList),
 * tal como haces en ContactsActivity.
 */
public class EditContactActivity extends AppCompatActivity {

    private ContactList contact_list = new ContactList();
    private ItemList item_list = new ItemList();

    private Context context;

    private EditText username;
    private EditText email;

    private Contact original_contact;
    private int meta_pos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);

        context = getApplicationContext();

        // Cargar datos persistidos
        contact_list.loadContacts(context);
        item_list.loadItems(context);

        // Referencias UI
        username = (EditText) findViewById(R.id.username);
        email    = (EditText) findViewById(R.id.email);

        // Recuperar la posición enviada desde ContactsActivity
        meta_pos = getIntent().getIntExtra("position", -1);
        if (meta_pos < 0 || meta_pos >= contact_list.size()) {
            Toast.makeText(this, "Invalid contact", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Obtener el contacto a editar (de la copia devuelta por getContacts)
        original_contact = contact_list.getContacts().get(meta_pos);

        // Mostrar datos; username bloqueado (clave)
        username.setText(original_contact.getUsername());
        username.setEnabled(false);
        email.setText(original_contact.getEmail());
    }

    /** Guarda cambios del email. Llamado por el botón con android:onClick="saveContact" */
    public void saveContact(View view) {
        String email_str = email.getText() == null ? "" : email.getText().toString().trim();

        if (email_str.length() == 0) {
            email.setError("Empty field!");
            return;
        }
        if (email_str.indexOf('@') == -1) {
            email.setError("Must be an email address!");
            return;
        }

        boolean ok = contact_list.updateContactEmail(original_contact.getUsername(), email_str);
        if (ok) {
            contact_list.saveContacts(context);
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            finish(); // volver a la lista
        } else {
            Toast.makeText(this, "Unable to save", Toast.LENGTH_SHORT).show();
        }
    }

    /** Elimina el contacto (si no es prestatario activo). Llamado por android:onClick="deleteContact" */
    public void deleteContact(View view) {
        // Seguridad extra: bloquear si es prestatario activo
        ArrayList<Contact> active_borrowers = item_list.getActiveBorrowers();
        boolean isActive = false;

        if (active_borrowers != null) {
            for (int i = 0; i < active_borrowers.size(); i++) {
                if (original_contact.equals(active_borrowers.get(i))) {
                    isActive = true;
                    break;
                }
            }
        }

        if (isActive) {
            Toast.makeText(this, "Cannot delete active borrower!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean removed = contact_list.removeByUsername(original_contact.getUsername());
        if (removed) {
            contact_list.saveContacts(context);
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Unable to delete", Toast.LENGTH_SHORT).show();
        }
    }
}
