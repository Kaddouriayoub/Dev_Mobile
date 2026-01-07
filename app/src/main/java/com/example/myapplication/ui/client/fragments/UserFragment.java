package com.example.myapplication.ui.client.fragments;

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
import com.example.myapplication.R;
import com.example.myapplication.model.Role;
import com.example.myapplication.model.User;
import com.example.myapplication.ui.login.LoginActivity;
import com.example.myapplication.utils.SessionManager;
import io.realm.Realm;

public class UserFragment extends Fragment {

    private TextView tvFullName, tvRole, tvEmail, tvCreatedAt;
    private Button btnLogout, btnEditProfile;
    private Realm realm;
    private SessionManager sessionManager;

    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        tvFullName = view.findViewById(R.id.tv_full_name);
        tvRole = view.findViewById(R.id.tv_role);
        tvEmail = view.findViewById(R.id.tv_email);
        tvCreatedAt = view.findViewById(R.id.tv_created_at);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);

        realm = Realm.getDefaultInstance();
        if (getContext() != null) {
            sessionManager = new SessionManager(getContext());
        }

        loadProfileData();

        btnLogout.setOnClickListener(v -> {
            if (sessionManager != null) {
                sessionManager.logoutUser();
            }
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
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
