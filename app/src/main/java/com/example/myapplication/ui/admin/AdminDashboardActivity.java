package com.example.myapplication.ui.admin;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.myapplication.R;
import com.example.myapplication.model.Reservation;
import com.example.myapplication.model.ReservationStatus;
import com.example.myapplication.model.Workspace;
import io.realm.Realm;
import io.realm.RealmResults;

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

        // Create test data if needed
        createTestDataIfNeeded();

        // Load Default Fragment
        loadFragment(new AdminWorkspacesFragment(), 1);
    }

    private void createTestDataIfNeeded() {
        Realm realm = Realm.getDefaultInstance();
        try {
            // Check if we have workspaces but no orders (for testing)
            RealmResults<Workspace> workspaces = realm.where(Workspace.class).findAll();
            RealmResults<Reservation> reservations = realm.where(Reservation.class).findAll();

            // Only create test orders if there are workspaces but no orders
            if (workspaces.size() > 0 && reservations.size() == 0) {
                realm.executeTransaction(r -> {
                    Workspace firstWorkspace = workspaces.first();
                    if (firstWorkspace != null) {
                        // Create test order 1 - PENDING (client order)
                        Reservation order1 = r.createObject(Reservation.class, System.currentTimeMillis());
                        order1.setClientName("Ahmed Bennani");
                        order1.setNumberOfPlaces(2);
                        order1.setReservationDate("15/01/2026");
                        order1.setStartTime("09:00");
                        order1.setEndTime("17:00");
                        order1.setTotalPrice(160.0);
                        order1.setStatus(ReservationStatus.PENDING);
                        order1.setAdminOrder(false);
                        order1.setWorkspace(firstWorkspace);
                        order1.setWorkspaceId(firstWorkspace.getId());

                        // Create test order 2 - PENDING (client order)
                        Reservation order2 = r.createObject(Reservation.class, System.currentTimeMillis() + 1);
                        order2.setClientName("Fatima Alaoui");
                        order2.setNumberOfPlaces(1);
                        order2.setReservationDate("16/01/2026");
                        order2.setStartTime("10:00");
                        order2.setEndTime("14:00");
                        order2.setTotalPrice(80.0);
                        order2.setStatus(ReservationStatus.PENDING);
                        order2.setAdminOrder(false);
                        order2.setWorkspace(firstWorkspace);
                        order2.setWorkspaceId(firstWorkspace.getId());

                        // Create test order 3 - CONFIRMED
                        Reservation order3 = r.createObject(Reservation.class, System.currentTimeMillis() + 2);
                        order3.setClientName("Youssef Idrissi");
                        order3.setNumberOfPlaces(3);
                        order3.setReservationDate("14/01/2026");
                        order3.setStartTime("08:00");
                        order3.setEndTime("18:00");
                        order3.setTotalPrice(300.0);
                        order3.setStatus(ReservationStatus.CONFIRMED);
                        order3.setAdminOrder(false);
                        order3.setWorkspace(firstWorkspace);
                        order3.setWorkspaceId(firstWorkspace.getId());

                        // Update workspace available places for the confirmed order
                        int newAvailable = firstWorkspace.getAvailablePlaces() - 3;
                        firstWorkspace.setAvailablePlaces(Math.max(0, newAvailable));
                        if (firstWorkspace.getAvailablePlaces() == 0) {
                            firstWorkspace.setStatus("FULL");
                        }
                    }
                });
            }
        } finally {
            realm.close();
        }
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
