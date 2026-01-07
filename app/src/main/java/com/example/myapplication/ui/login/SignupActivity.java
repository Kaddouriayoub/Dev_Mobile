package com.example.myapplication.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.model.Client;
import com.example.myapplication.model.Role;
import com.example.myapplication.model.SubscriptionType;
import com.example.myapplication.model.User;
import com.example.myapplication.ui.admin.AdminDashboardActivity;
import com.example.myapplication.ui.client.ClientDashboardActivity;
import com.example.myapplication.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import io.realm.Realm;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPassword;
    private RadioGroup rgRole;
    private Button btnSignup;
    private TextView tvLogin;
    private Realm realm;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        sessionManager = new SessionManager(this);
        etFullName = findViewById(R.id.et_fullname);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        rgRole = findViewById(R.id.rg_role);
        btnSignup = findViewById(R.id.btn_signup);
        tvLogin = findViewById(R.id.tv_login);
        realm = Realm.getDefaultInstance();

        btnSignup.setOnClickListener(v -> registerUser());
        
        tvLogin.setOnClickListener(v -> {
            finish(); // Go back to Login
        });
    }

    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        int selectedRoleId = rgRole.getCheckedRadioButtonId();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if email already exists
        User existingUser = realm.where(User.class).equalTo("email", email).findFirst();
        if (existingUser != null) {
            Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show();
            return;
        }

        final Role role = (selectedRoleId == R.id.rb_admin) ? Role.ADMIN : Role.CLIENT;

        realm.executeTransaction(r -> {
            Number maxId = r.where(User.class).max("id");
            long nextId = (maxId == null) ? 1 : maxId.longValue() + 1;

            User newUser = r.createObject(User.class, nextId);
            newUser.setFullName(fullName);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setRole(role);
            newUser.setCreatedAt(String.valueOf(System.currentTimeMillis())); // Simplified date
            newUser.setEnabled(true);

            // If it's a client, create the Client profile too
            if (role == Role.CLIENT) {
                Client clientProfile = r.createObject(Client.class, nextId); // Use same ID for link
                clientProfile.setSubscriptionType(SubscriptionType.FREE);
                clientProfile.setLoyaltyPoints(0);
                clientProfile.setTotalReservations(0);
            }
        });

        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
        
        // Auto Login
        User newUser = realm.where(User.class).equalTo("email", email).findFirst();
        if (newUser != null) {
            sessionManager.createLoginSession(newUser.getId(), role.name());
            navigateBasedOnRole(role.name());
        }
    }

    private void navigateBasedOnRole(String role) {
        Intent intent;
        if (Role.ADMIN.name().equals(role) || Role.MANAGER.name().equals(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(this, ClientDashboardActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }
}
