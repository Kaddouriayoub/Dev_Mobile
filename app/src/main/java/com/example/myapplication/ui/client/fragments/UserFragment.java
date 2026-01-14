package com.example.myapplication.ui.client.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.model.Client;
import com.example.myapplication.model.SubscriptionType;
import com.example.myapplication.model.User;
import com.example.myapplication.ui.admin.EditProfileFragment;
import com.example.myapplication.ui.client.UpgradeActivity; // Assurez-vous de créer cette classe
import com.example.myapplication.ui.login.LoginActivity;
import com.example.myapplication.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;

public class UserFragment extends Fragment {

    private TextView tvFullName, tvRole, tvEmail, tvCreatedAt, tvLoyaltyPoints, tvSubscription;
    private Button btnLogout, btnEditProfile, btnUpgrade;
    private Realm realm;
    private SessionManager sessionManager;

    public UserFragment() {
        // Constructeur public requis
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        // Initialisation des vues
        initViews(view);

        realm = Realm.getDefaultInstance();
        if (getContext() != null) {
            sessionManager = new SessionManager(getContext());
        }

        loadProfileData();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        tvFullName = view.findViewById(R.id.tv_full_name);
        tvRole = view.findViewById(R.id.tv_role);
        tvEmail = view.findViewById(R.id.tv_email);
        tvCreatedAt = view.findViewById(R.id.tv_created_at);
        tvLoyaltyPoints = view.findViewById(R.id.tv_loyalty_points);
        tvSubscription = view.findViewById(R.id.tv_subscription);

        btnLogout = view.findViewById(R.id.btn_logout);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnUpgrade = view.findViewById(R.id.btn_upgrade);
    }

    private void setupListeners() {
        // Bouton Edit Profile
        btnEditProfile.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EditProfileFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Bouton Upgrade (Vers une nouvelle Activité)
        btnUpgrade.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UpgradeActivity.class);
            startActivity(intent);
        });

        // Bouton Logout
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
    }

    private void loadProfileData() {
        if (sessionManager == null) return;

        Long userId = sessionManager.getUserId();
        if (userId == -1) return;

        // 1. Charger les données User (Nom, Email, etc.)
        User user = realm.where(User.class).equalTo("id", userId).findFirst();
        if (user != null) {
            tvFullName.setText(user.getFullName());
            tvRole.setText(user.getRole() != null ? user.getRole().name() : "N/A");
            tvEmail.setText(user.getEmail());

            // Format the date properly
            String createdAtValue = user.getCreatedAt();
            if (!TextUtils.isEmpty(createdAtValue)) {
                tvCreatedAt.setText(formatDate(createdAtValue));
            } else {
                tvCreatedAt.setText("N/A");
            }
        }

        // 2. Charger les données Client et gérer la visibilité du bouton Upgrade
        Client client = realm.where(Client.class).equalTo("id", userId).findFirst();
        if (client != null) {
            tvLoyaltyPoints.setText("Loyalty Points: " + client.getLoyaltyPoints());

            SubscriptionType type = client.getSubscriptionType();
            tvSubscription.setText("Subscription: " + (type != null ? type.name() : "FREE"));

            // CONDITION : Masquer le bouton si déjà PREMIUM
            if (type != null && type == SubscriptionType.PREMIUM) {
                btnUpgrade.setVisibility(View.GONE); // Cache le bouton complètement
            } else {
                btnUpgrade.setVisibility(View.VISIBLE); // Affiche le bouton pour les FREE
            }
        }
    }

    private String formatDate(String dateValue) {
        try {
            // Try to parse as a timestamp (milliseconds since epoch)
            long timestamp = Long.parseLong(dateValue);
            Date date = new Date(timestamp);
            SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
            return formatter.format(date);
        } catch (NumberFormatException e) {
            // If it's not a timestamp, try to parse it as a string date
            try {
                // Try common date formats
                SimpleDateFormat[] formats = {
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH),
                    new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH),
                    new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
                    new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
                };

                Date date = null;
                for (SimpleDateFormat format : formats) {
                    try {
                        date = format.parse(dateValue);
                        break;
                    } catch (Exception ignored) {
                        // Try next format
                    }
                }

                if (date != null) {
                    SimpleDateFormat outputFormatter = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
                    return outputFormatter.format(date);
                }
            } catch (Exception ignored) {
                // If all parsing fails, return the original value
            }
            return dateValue;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}