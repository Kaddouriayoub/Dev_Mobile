package com.example.myapplication.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myapplication.R;
import com.example.myapplication.model.Role;
import com.example.myapplication.model.User;
import com.example.myapplication.ui.login.LoginActivity;
import com.example.myapplication.utils.SessionManager;
import io.realm.Realm;

public class AdminProfileFragment extends Fragment {

    private TextView tvFullName, tvRole, tvEmail, tvCreatedAt;
    private Button btnLogout, btnEditProfile;
    private Realm realm;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_profile, container, false);
        
        tvFullName = view.findViewById(R.id.tv_full_name);
        tvRole = view.findViewById(R.id.tv_role);
        tvEmail = view.findViewById(R.id.tv_email);
        tvCreatedAt = view.findViewById(R.id.tv_created_at);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);

        realm = Realm.getDefaultInstance();
        // Use getContext() or getActivity() safely
        if (getContext() != null) {
            sessionManager = new SessionManager(getContext());
        }

        loadProfileData();

        btnLogout.setOnClickListener(v -> {
            if (sessionManager != null) {
                sessionManager.logoutUser();
            }
            // Navigate back to LoginActivity
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                // Clear the back stack so user cannot go back to admin dashboard
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish(); // Finish current activity (AdminDashboardActivity)
            } else {
                Toast.makeText(getContext(), "Error logging out", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadProfileData() {
        if (sessionManager == null) return;
        
        Long userId = sessionManager.getUserId();
        User user = null;

        if (userId != -1) {
             user = realm.where(User.class).equalTo("id", userId).findFirst();
        }
        
        // Fallback or Dummy only if login skipped (which shouldn't happen now)
        if (user == null) {
            user = realm.where(User.class).equalTo("role", Role.ADMIN.name()).findFirst();
        }

        if (user != null) {
            tvFullName.setText(user.getFullName());
            if (user.getRole() != null) {
                tvRole.setText(user.getRole().name());
            }
            tvEmail.setText(user.getEmail());
            tvCreatedAt.setText(user.getCreatedAt());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }
}
