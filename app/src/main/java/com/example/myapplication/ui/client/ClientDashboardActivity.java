package com.example.myapplication.ui.client;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
// Corrected imports for your fragments, assuming they are in the 'fragments' sub-package

import com.example.myapplication.ui.client.fragments.ExploreFragment;
import com.example.myapplication.ui.client.fragments.FavouriteFragment;
import com.example.myapplication.ui.client.fragments.ReservationFragment;
import com.example.myapplication.ui.client.fragments.UserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ClientDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Assuming this layout contains R.id.bottom_navigation and R.id.fragment_container

        // 1. Declare and initialize bottomNav only ONCE.
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // 2. Load the default fragment when the activity is first created.
        // Assuming ExploreFragment is the intended default.
        loadFragment(new ExploreFragment());

        // 3. Set ONE listener to handle all item clicks.
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_reservations) { // Assuming you have a nav_home ID
                replaceFragment(new ReservationFragment());
                return true;
            } else if (itemId == R.id.nav_explore) {
                replaceFragment(new ExploreFragment());
                return true;
            } else if (itemId == R.id.nav_profile) { // Assuming you have a nav_profile ID
                replaceFragment(new UserFragment());
                return true;
            } else if (itemId == R.id.nav_favorites) { // Assuming you have a nav_favorites ID
                replaceFragment(new FavouriteFragment());
                return true;
            }
            return false; // Return false if the item is not handled
        });
    }
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
    // You only need one method to replace fragments.
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
