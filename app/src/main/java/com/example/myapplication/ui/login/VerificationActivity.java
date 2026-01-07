package com.example.myapplication.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

public class VerificationActivity extends AppCompatActivity {

    private TextInputEditText etCode;
    private Button btnVerify;
    private TextView tvInstruction;
    private Realm realm;
    private SessionManager sessionManager;

    // Data passed from Signup
    private String generatedCode;
    private String fullName, email, password;
    private String roleName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification.xml);

        etCode = findViewById(R.id.et_code);
        btnVerify = findViewById(R.id.btn_verify);
        tvInstruction = findViewById(R.id.tv_instruction);
        realm = Realm.getDefaultInstance();
        sessionManager = new SessionManager(this);

        // Retrieve data
        if (getIntent() != null) {
            generatedCode = getIntent().getStringExtra("CODE");
            fullName = getIntent().getStringExtra("FULL_NAME");
            email = getIntent().getStringExtra("EMAIL");
            password = getIntent().getStringExtra("PASSWORD");
            roleName = getIntent().getStringExtra("ROLE");
            
            tvInstruction.setText("We sent a code to " + email);
        }

        btnVerify.setOnClickListener(v -> verifyCode());
    }

    private void verifyCode() {
        String inputCode = etCode.getText().toString().trim();

        if (inputCode.isEmpty()) {
            Toast.makeText(this, "Please enter the code", Toast.LENGTH_SHORT).show();
            return;
        }

        if (inputCode.equals(generatedCode)) {
            // Success! Create User in Realm
            createAccount();
        } else {
            Toast.makeText(this, "Incorrect code. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void createAccount() {
        final Role role = Role.valueOf(roleName);

        realm.executeTransaction(r -> {
            Number maxId = r.where(User.class).max("id");
            long nextId = (maxId == null) ? 1 : maxId.longValue() + 1;

            User newUser = r.createObject(User.class, nextId);
            newUser.setFullName(fullName);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setRole(role);
            newUser.setCreatedAt(String.valueOf(System.currentTimeMillis()));
            newUser.setEnabled(true);

            if (role == Role.CLIENT) {
                Client clientProfile = r.createObject(Client.class, nextId);
                clientProfile.setSubscriptionType(SubscriptionType.FREE);
                clientProfile.setLoyaltyPoints(0);
                clientProfile.setTotalReservations(0);
            }
        });

        // Auto Login
        User newUser = realm.where(User.class).equalTo("email", email).findFirst();
        if (newUser != null) {
            sessionManager.createLoginSession(newUser.getId(), roleName);
            navigateBasedOnRole(roleName);
        }
    }

    private void navigateBasedOnRole(String role) {
        Intent intent;
        if (Role.ADMIN.name().equals(role)) {
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
