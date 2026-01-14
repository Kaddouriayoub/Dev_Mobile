package com.example.myapplication.ui.client;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
// Corrected imports for your fragments, assuming they are in the 'fragments' sub-package

import com.example.myapplication.ui.client.fragments.ExploreFragment;
import com.example.myapplication.ui.client.fragments.FavouriteFragment;
import com.example.myapplication.ui.client.fragments.ReservationFragment;
import com.example.myapplication.ui.client.fragments.UserFragment;

public class ClientDashboardActivity extends AppCompatActivity {

    private LinearLayout navExplore, navFavorites, navReservations, navProfile;
    private ImageView imgExplore, imgFavorites, imgReservations, imgProfile;
    private TextView txtExplore, txtFavorites, txtReservations, txtProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        navExplore = findViewById(R.id.nav_explore);
        navFavorites = findViewById(R.id.nav_favorites);
        navReservations = findViewById(R.id.nav_reservations);
        navProfile = findViewById(R.id.nav_profile);

        imgExplore = findViewById(R.id.img_explore);
        imgFavorites = findViewById(R.id.img_favorites);
        imgReservations = findViewById(R.id.img_reservations);
        imgProfile = findViewById(R.id.img_profile);

        txtExplore = findViewById(R.id.txt_explore);
        txtFavorites = findViewById(R.id.txt_favorites);
        txtReservations = findViewById(R.id.txt_reservations);
        txtProfile = findViewById(R.id.txt_profile);

        // Set Click Listeners
        navExplore.setOnClickListener(v -> {
            replaceFragment(new ExploreFragment());
            updateNavUI(1);
        });
        navFavorites.setOnClickListener(v -> {
            replaceFragment(new FavouriteFragment());
            updateNavUI(2);
        });
        navReservations.setOnClickListener(v -> {
            replaceFragment(new ReservationFragment());
            updateNavUI(3);
        });
        navProfile.setOnClickListener(v -> {
            replaceFragment(new UserFragment());
            updateNavUI(4);
        });

        // Load Default Fragment and update UI
        replaceFragment(new ExploreFragment());
        updateNavUI(1);
    }

    private void updateNavUI(int selectedTab) {
        int colorSelected = ContextCompat.getColor(this, R.color.colorPrimary);
        int colorUnselected = ContextCompat.getColor(this, R.color.colorGrayText);

        // Reset all
        imgExplore.setColorFilter(colorUnselected);
        txtExplore.setTextColor(colorUnselected);
        imgFavorites.setColorFilter(colorUnselected);
        txtFavorites.setTextColor(colorUnselected);
        imgReservations.setColorFilter(colorUnselected);
        txtReservations.setTextColor(colorUnselected);
        imgProfile.setColorFilter(colorUnselected);
        txtProfile.setTextColor(colorUnselected);

        // Highlight selected
        switch (selectedTab) {
            case 1: // Explore
                imgExplore.setColorFilter(colorSelected);
                txtExplore.setTextColor(colorSelected);
                break;
            case 2: // Favorites
                imgFavorites.setColorFilter(colorSelected);
                txtFavorites.setTextColor(colorSelected);
                break;
            case 3: // Reservations
                imgReservations.setColorFilter(colorSelected);
                txtReservations.setTextColor(colorSelected);
                break;
            case 4: // Profile
                imgProfile.setColorFilter(colorSelected);
                txtProfile.setTextColor(colorSelected);
                break;
        }
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
