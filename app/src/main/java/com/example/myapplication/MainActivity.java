package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UserAdapter.OnUserDeleteListener {

    private EditText etName;
    private EditText etDepartment;
    private Button btnSave;
    private Button btnClearAll;
    private TextView tvRecordsHeader;
    private TextView tvEmptyState;
    private RecyclerView rvUsers;

    private FirebaseDatabaseHelper firebaseHelper;
    private ValueEventListener firebaseValueEventListener;
    private UserAdapter userAdapter;
    private final List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseHelper = new FirebaseDatabaseHelper();

        initViews();
        setupRecyclerView();
        setupListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        listenToFirebaseDatabase();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseValueEventListener != null) {
            firebaseHelper.detachUsersListener(firebaseValueEventListener);
        }
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etDepartment = findViewById(R.id.etDepartment);
        btnSave = findViewById(R.id.btnSave);
        btnClearAll = findViewById(R.id.btnClearAll);
        tvRecordsHeader = findViewById(R.id.tvRecordsHeader);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        rvUsers = findViewById(R.id.rvUsers);
    }

    private void setupRecyclerView() {
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(userList, this);
        rvUsers.setAdapter(userAdapter);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> handleSaveUser());
        btnClearAll.setOnClickListener(v -> handleClearAll());
    }

    private void listenToFirebaseDatabase() {
        firebaseValueEventListener = firebaseHelper.attachUsersListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        if (user.getId() == null || user.getId().isEmpty()) {
                            user.setId(dataSnapshot.getKey());
                        }
                        userList.add(0, user); // Add to beginning for newest first
                    }
                }
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.toast_error, error.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSaveUser() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String department = etDepartment.getText() != null ? etDepartment.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(department)) {
            Toast.makeText(this, getString(R.string.toast_fill_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        firebaseHelper.saveUser(name, department, task -> {
            btnSave.setEnabled(true);
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity.this, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
                etName.setText("");
                etDepartment.setText("");
                etName.clearFocus();
                etDepartment.clearFocus();
            } else {
                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(MainActivity.this, getString(R.string.toast_error, errorMsg), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleClearAll() {
        if (userList.isEmpty()) {
            return;
        }
        btnClearAll.setEnabled(false);
        firebaseHelper.clearAllUsers(task -> {
            btnClearAll.setEnabled(true);
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity.this, getString(R.string.toast_all_cleared), Toast.LENGTH_SHORT).show();
            } else {
                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(MainActivity.this, getString(R.string.toast_error, errorMsg), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        userAdapter.updateList(userList);
        int count = userList.size();
        tvRecordsHeader.setText(getString(R.string.records_title, count));

        if (count == 0) {
            rvUsers.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            btnClearAll.setEnabled(false);
        } else {
            rvUsers.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            btnClearAll.setEnabled(true);
        }
    }

    @Override
    public void onDeleteUser(User user) {
        if (user == null || user.getId() == null) {
            return;
        }
        firebaseHelper.deleteUser(user.getId(), task -> {
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity.this, getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show();
            } else {
                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(MainActivity.this, getString(R.string.toast_error, errorMsg), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
