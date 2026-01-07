package com.example.myapplication.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.model.Role;
import com.example.myapplication.model.User;
import com.example.myapplication.ui.admin.AdminDashboardActivity;
import com.example.myapplication.ui.client.ClientDashboardActivity;
import com.example.myapplication.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import io.realm.Realm;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignup;
    private Realm realm;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            navigateBasedOnRole(sessionManager.getUserRole());
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvSignup = findViewById(R.id.tv_signup);
        realm = Realm.getDefaultInstance();

        btnLogin.setOnClickListener(v -> loginUser());
        
        tvSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = realm.where(User.class).equalTo("email", email).findFirst();

        if (user != null && user.getPassword() != null && user.getPassword().equals(password)) {
            String role = user.getRole() != null ? user.getRole().name() : Role.CLIENT.name();
            sessionManager.createLoginSession(user.getId(), role);
            navigateBasedOnRole(role);
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateBasedOnRole(String role) {
        Intent intent;
        if (Role.ADMIN.name().equals(role) || Role.MANAGER.name().equals(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(this, ClientDashboardActivity.class);
        }
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
