package com.example.shixun;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shixun.adapter.ContactAdapter;
import com.example.shixun.db.DatabaseHelper;
import com.example.shixun.model.Contact;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ContactAdapter.OnContactClickListener {
    private static final String TAG = "MainActivity";
    private DatabaseHelper dbHelper;
    private ContactAdapter adapter;
    private long userId;
    private List<Contact> contacts = new ArrayList<>();
    private List<Contact> allContacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            userId = getIntent().getLongExtra("user_id", -1);
            if (userId == -1) {
                finish();
                return;
            }

            dbHelper = new DatabaseHelper(this);
            
            RecyclerView recyclerView = findViewById(R.id.contacts_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ContactAdapter(contacts, this);
            recyclerView.setAdapter(adapter);

            FloatingActionButton fab = findViewById(R.id.fab_add);
            fab.setOnClickListener(v -> showAddContactDialog());

            loadContacts();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            Toast.makeText(this, "初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterContacts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterContacts(newText);
                return true;
            }
        });
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            new AlertDialog.Builder(this)
                .setTitle("退出登录")
                .setMessage("确定要退出登录吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("取消", null)
                .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void filterContacts(String query) {
        contacts.clear();
        if (query.isEmpty()) {
            contacts.addAll(allContacts);
        } else {
            for (Contact contact : allContacts) {
                if (contact.getName().toLowerCase().contains(query.toLowerCase()) ||
                    contact.getPhone().contains(query)) {
                    contacts.add(contact);
                }
            }
        }
        adapter.updateContacts(contacts);
    }

    private void loadContacts() {
        allContacts.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CONTACTS,
                null,
                DatabaseHelper.COLUMN_USER_REF + "=?",
                new String[]{String.valueOf(userId)},
                null, null, DatabaseHelper.COLUMN_NAME);

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACT_ID));
            String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
            String phone = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE));
            String email = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL));
            
            allContacts.add(new Contact(id, name, phone, email, userId));
        }
        cursor.close();
        contacts.clear();
        contacts.addAll(allContacts);
        adapter.updateContacts(contacts);
    }

    private void showAddContactDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_contact, null);
        EditText nameInput = view.findViewById(R.id.contact_name_input);
        EditText phoneInput = view.findViewById(R.id.contact_phone_input);
        EditText emailInput = view.findViewById(R.id.contact_email_input);

        new AlertDialog.Builder(this)
                .setTitle("添加联系人")
                .setView(view)
                .setPositiveButton("添加", (dialog, which) -> {
                    String name = nameInput.getText().toString();
                    String phone = phoneInput.getText().toString();
                    String email = emailInput.getText().toString();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "姓名不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    addContact(name, phone, email);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void addContact(String name, String phone, String email) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_PHONE, phone);
        values.put(DatabaseHelper.COLUMN_EMAIL, email);
        values.put(DatabaseHelper.COLUMN_USER_REF, userId);

        db.insert(DatabaseHelper.TABLE_CONTACTS, null, values);
        loadContacts();
    }

    @Override
    public void onContactClick(Contact contact) {
        showEditContactDialog(contact);
    }

    @Override
    public void onContactLongClick(Contact contact) {
        new AlertDialog.Builder(this)
                .setTitle("删除联系人")
                .setMessage("确定要删除这个联系人吗？")
                .setPositiveButton("删除", (dialog, which) -> deleteContact(contact))
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteContact(Contact contact) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_CONTACTS,
                DatabaseHelper.COLUMN_CONTACT_ID + "=?",
                new String[]{String.valueOf(contact.getId())});
        loadContacts();
    }

    private void showEditContactDialog(Contact contact) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_contact, null);
        EditText nameInput = view.findViewById(R.id.contact_name_input);
        EditText phoneInput = view.findViewById(R.id.contact_phone_input);
        EditText emailInput = view.findViewById(R.id.contact_email_input);

        nameInput.setText(contact.getName());
        phoneInput.setText(contact.getPhone());
        emailInput.setText(contact.getEmail());

        new AlertDialog.Builder(this)
                .setTitle("编辑联系人")
                .setView(view)
                .setPositiveButton("保存", (dialog, which) -> {
                    String name = nameInput.getText().toString();
                    String phone = phoneInput.getText().toString();
                    String email = emailInput.getText().toString();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "姓名不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateContact(contact.getId(), name, phone, email);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateContact(long contactId, String name, String phone, String email) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_PHONE, phone);
        values.put(DatabaseHelper.COLUMN_EMAIL, email);

        db.update(DatabaseHelper.TABLE_CONTACTS,
                values,
                DatabaseHelper.COLUMN_CONTACT_ID + "=?",
                new String[]{String.valueOf(contactId)});
        loadContacts();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}