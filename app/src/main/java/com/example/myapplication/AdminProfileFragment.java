package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.realm.Realm;

public class AdminProfileFragment extends Fragment {

    private TextView tvFullName, tvRole, tvEmail, tvCreatedAt;
    private Button btnLogout, btnEditProfile;
    private Realm realm;

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

        loadProfileData();

        btnLogout.setOnClickListener(v -> {
            // Logout logic
            // TODO: Clear user session preference
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        });

        return view;
    }

    private void loadProfileData() {
        // For demonstration, let's pick the first user with ADMIN role or create a dummy one
        User user = realm.where(User.class).equalTo("role", Role.ADMIN.name()).findFirst();

        if (user == null) {
            // Create a dummy admin if none exists
            realm.executeTransaction(r -> {
                User newUser = r.createObject(User.class, 1L); // ID 1
                newUser.setFullName("Mourad Admin");
                newUser.setEmail("mourad@admin.com");
                newUser.setRole(Role.ADMIN);
                newUser.setCreatedAt("01/01/2024");
                newUser.setEnabled(true);
            });
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
