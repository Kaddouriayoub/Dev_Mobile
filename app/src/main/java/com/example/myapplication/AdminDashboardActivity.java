package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class AdminDashboardActivity extends AppCompatActivity {

    private LinearLayout navWorkspaces, navOrders, navProfile;
    private ImageView imgWorkspaces, imgOrders, imgProfile;
    private TextView txtWorkspaces, txtOrders, txtProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize Views
        navWorkspaces = findViewById(R.id.nav_workspaces);
        navOrders = findViewById(R.id.nav_orders);
        navProfile = findViewById(R.id.nav_profile);

        imgWorkspaces = findViewById(R.id.img_workspaces);
        imgOrders = findViewById(R.id.img_orders);
        imgProfile = findViewById(R.id.img_profile);

        txtWorkspaces = findViewById(R.id.txt_workspaces);
        txtOrders = findViewById(R.id.txt_orders);
        txtProfile = findViewById(R.id.txt_profile);

        // Set Click Listeners
        navWorkspaces.setOnClickListener(v -> loadFragment(new AdminWorkspacesFragment(), 1));
        navOrders.setOnClickListener(v -> loadFragment(new AdminOrdersFragment(), 2));
        navProfile.setOnClickListener(v -> loadFragment(new AdminProfileFragment(), 3));

        // Load Default Fragment
        loadFragment(new AdminWorkspacesFragment(), 1);
    }

    private void loadFragment(Fragment fragment, int tabIndex) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        updateNavUI(tabIndex);
    }

    private void updateNavUI(int selectedTab) {
        int colorSelected = ContextCompat.getColor(this, R.color.colorPrimary);
        int colorUnselected = ContextCompat.getColor(this, R.color.colorGrayText);

        // Reset all
        imgWorkspaces.setColorFilter(colorUnselected);
        txtWorkspaces.setTextColor(colorUnselected);
        imgOrders.setColorFilter(colorUnselected);
        txtOrders.setTextColor(colorUnselected);
        imgProfile.setColorFilter(colorUnselected);
        txtProfile.setTextColor(colorUnselected);

        // Highlight selected
        switch (selectedTab) {
            case 1:
                imgWorkspaces.setColorFilter(colorSelected);
                txtWorkspaces.setTextColor(colorSelected);
                break;
            case 2:
                imgOrders.setColorFilter(colorSelected);
                txtOrders.setTextColor(colorSelected);
                break;
            case 3:
                imgProfile.setColorFilter(colorSelected);
                txtProfile.setTextColor(colorSelected);
                break;
        }
    }
}
