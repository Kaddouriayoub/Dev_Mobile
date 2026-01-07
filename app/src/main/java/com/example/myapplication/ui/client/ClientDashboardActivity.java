package com.example.myapplication.ui.client;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.ui.client.ExploreFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ClientDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Default fragment
        loadFragment(new ExploreFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_explore) {
                loadFragment(new ExploreFragment());
                return true;
            }
            // Later:
            // if (item.getItemId() == R.id.nav_favorites)
            // if (item.getItemId() == R.id.nav_profile)
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
