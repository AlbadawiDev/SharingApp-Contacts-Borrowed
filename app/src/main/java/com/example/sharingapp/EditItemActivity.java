package com.example.sharingapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.CompoundButton;

import java.util.ArrayList;

/**
 * Edita un ítem existente:
 * - Cambiar título/desc.
 * - Alternar Available/Borrowed.
 * - Si Borrowed, elegir prestatario desde ContactList (Spinner).
 *
 * Requiere Intent extra: "position" (índice del ítem en ItemList).
 */
public class EditItemActivity extends AppCompatActivity {

    private Context context;

    private EditText title_et;
    private EditText desc_et;
    private Switch available_sw;
    private Spinner borrower_sp;
    private Button save_btn;

    private ItemList item_list = new ItemList();
    private ContactList contact_list = new ContactList();

    private ArrayList<Item> items;
    private ArrayList<Contact> contacts;

    private Item item;          // ítem que estamos editando
    private int position = -1;  // índice del ítem en la lista

    private ArrayAdapter<String> borrowerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        context = getApplicationContext();

        // Cargar datos
        item_list.loadItems(context);
        contact_list.loadContacts(context);

        items = item_list.getItems();
        contacts = contact_list.getContacts();

        // UI
        title_et = (EditText) findViewById(R.id.title);
        desc_et = (EditText) findViewById(R.id.description);
        available_sw = (Switch) findViewById(R.id.available_switch);
        borrower_sp = (Spinner) findViewById(R.id.spinner_borrower);
        save_btn = (Button) findViewById(R.id.btn_save_item);

        // Recuperar posición del ítem a editar
        position = getIntent().getIntExtra("position", -1);
        if (position < 0 || position >= items.size()) {
            Toast.makeText(this, "Invalid item", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        item = items.get(position);

        // Poblamos campos con el ítem actual
        title_et.setText(item.getTitle());
        desc_et.setText(item.getDescription());

        // Disponibilidad via status ("Available" / "Borrowed")
        boolean isAvailable = "Available".equals(item.getStatus());
        available_sw.setChecked(isAvailable);

        setupBorrowerSpinner(); // prepara lista de usernames

        // Mostrar/ocultar spinner según estado actual
        if (isAvailable) {
            borrower_sp.setVisibility(View.GONE);
        } else {
            borrower_sp.setVisibility(View.VISIBLE);
            // Seleccionar en spinner el prestatario actual si existe
            Contact currentBorrower = item.getBorrower(); // <-- Contact
            if (currentBorrower != null) {
                int idx = indexOfUsername(currentBorrower.getUsername());
                if (idx >= 0) borrower_sp.setSelection(idx);
            }
        }

        // Listener del switch
        available_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Available
                    borrower_sp.setVisibility(View.GONE);
                } else {
                    // Borrowed -> necesita un contacto
                    if (contacts == null || contacts.size() == 0) {
                        Toast.makeText(EditItemActivity.this,
                                "No contacts found. Add a contact first.",
                                Toast.LENGTH_LONG).show();
                        // Revertimos a Available
                        available_sw.setOnCheckedChangeListener(null);
                        available_sw.setChecked(true);
                        available_sw.setOnCheckedChangeListener(this);
                        borrower_sp.setVisibility(View.GONE);
                        return;
                    }
                    borrower_sp.setVisibility(View.VISIBLE);
                    // Selección por defecto: primer contacto
                    borrower_sp.setSelection(0);
                }
            }
        });

        // Guardar
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                saveItem();
            }
        });
    }

    private void setupBorrowerSpinner() {
        ArrayList<String> usernames = new ArrayList<String>();
        if (contacts != null) {
            for (int i = 0; i < contacts.size(); i++) {
                usernames.add(contacts.get(i).getUsername());
            }
        }
        borrowerAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, usernames);
        borrower_sp.setAdapter(borrowerAdapter);
    }

    private int indexOfUsername(String username) {
        if (username == null || contacts == null) return -1;
        for (int i = 0; i < contacts.size(); i++) {
            String u = contacts.get(i).getUsername();
            if (u != null && u.equalsIgnoreCase(username)) return i;
        }
        return -1;
    }

    /** Valida y guarda cambios en el ItemList. */
    private void saveItem() {
        // Validación básica de título
        String title = title_et.getText() == null ? "" : title_et.getText().toString().trim();
        if (title.length() == 0) {
            title_et.setError("Empty title!");
            return;
        }

        String desc = desc_et.getText() == null ? "" : desc_et.getText().toString().trim();

        // Actualizamos datos del ítem
        item.setTitle(title);
        item.setDescription(desc);

        boolean available = available_sw.isChecked();
        item.setStatus(available ? "Available" : "Borrowed"); // <-- usa status tipo String

        if (!available) {
            // Borrowed: asignar prestatario Contact desde la lista
            if (contacts == null || contacts.size() == 0) {
                Toast.makeText(this, "No contacts to assign as borrower.", Toast.LENGTH_SHORT).show();
                return;
            }
            String borrowerUsername = (String) borrower_sp.getSelectedItem();
            if (borrowerUsername == null || borrowerUsername.trim().length() == 0) {
                Toast.makeText(this, "Select a borrower.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Buscar el Contact por username y asignarlo
            Contact borrower = contact_list.getContact(borrowerUsername);
            if (borrower == null) {
                Toast.makeText(this, "Borrower not found.", Toast.LENGTH_SHORT).show();
                return;
            }
            item.setBorrower(borrower); // <-- Contact
        } else {
            // Available: sin prestatario
            item.setBorrower(null);
        }

        // Persistimos
        item_list.saveItems(context);

        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
