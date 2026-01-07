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

        // Force create PENDING orders for testing status changes
        forceCreatePendingOrders();

        // Load Default Fragment
        loadFragment(new AdminWorkspacesFragment(), 1);
    }

    private void createTestDataIfNeeded() {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<Workspace> workspaces = realm.where(Workspace.class).findAll();

            // Check if there are any PENDING orders for testing
            RealmResults<Reservation> pendingReservations = realm.where(Reservation.class)
                    .equalTo("status", ReservationStatus.PENDING.name())
                    .findAll();

            // Create PENDING test orders if there are workspaces but no PENDING orders
            if (workspaces.size() > 0 && pendingReservations.size() == 0) {
                realm.executeTransaction(r -> {
                    Workspace firstWorkspace = workspaces.first();
                    if (firstWorkspace != null) {
                        double pricePerHour = firstWorkspace.getPricePerHour();
                        long baseId = System.currentTimeMillis();

                        // Create test order 1 - PENDING (client order)
                        Reservation order1 = r.createObject(Reservation.class, baseId);
                        order1.setClientName("Ahmed Bennani");
                        order1.setReservationDate("2026-1-15");
                        order1.setStartTime("9");
                        order1.setEndTime("12");
                        order1.setTotalPrice(3 * pricePerHour);
                        order1.setStatus(ReservationStatus.PENDING);
                        order1.setAdminOrder(false);
                        order1.setWorkspace(firstWorkspace);
                        order1.setWorkspaceId(firstWorkspace.getId());

                        // Create test order 2 - PENDING (client order)
                        Reservation order2 = r.createObject(Reservation.class, baseId + 1);
                        order2.setClientName("Fatima Alaoui");
                        order2.setReservationDate("2026-1-16");
                        order2.setStartTime("10");
                        order2.setEndTime("14");
                        order2.setTotalPrice(4 * pricePerHour);
                        order2.setStatus(ReservationStatus.PENDING);
                        order2.setAdminOrder(false);
                        order2.setWorkspace(firstWorkspace);
                        order2.setWorkspaceId(firstWorkspace.getId());

                        // Create test order 3 - PENDING (client order)
                        Reservation order3 = r.createObject(Reservation.class, baseId + 2);
                        order3.setClientName("Karim Tazi");
                        order3.setReservationDate("2026-1-17");
                        order3.setStartTime("14");
                        order3.setEndTime("17");
                        order3.setTotalPrice(3 * pricePerHour);
                        order3.setStatus(ReservationStatus.PENDING);
                        order3.setAdminOrder(false);
                        order3.setWorkspace(firstWorkspace);
                        order3.setWorkspaceId(firstWorkspace.getId());

                        // Create test order 4 - PENDING (client order)
                        Reservation order4 = r.createObject(Reservation.class, baseId + 3);
                        order4.setClientName("Sara Mohammedi");
                        order4.setReservationDate("2026-1-18");
                        order4.setStartTime("8");
                        order4.setEndTime("11");
                        order4.setTotalPrice(3 * pricePerHour);
                        order4.setStatus(ReservationStatus.PENDING);
                        order4.setAdminOrder(false);
                        order4.setWorkspace(firstWorkspace);
                        order4.setWorkspaceId(firstWorkspace.getId());
                    }
                });
            }
        } finally {
            realm.close();
        }
    }

    // Method to force create PENDING test orders (call from onCreate if needed for testing)
    private void forceCreatePendingOrders() {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<Workspace> workspaces = realm.where(Workspace.class).findAll();

            if (workspaces.size() > 0) {
                realm.executeTransaction(r -> {
                    Workspace firstWorkspace = workspaces.first();
                    if (firstWorkspace != null) {
                        double pricePerHour = firstWorkspace.getPricePerHour();
                        long baseId = System.currentTimeMillis();

                        // Force create PENDING orders for testing
                        Reservation order1 = r.createObject(Reservation.class, baseId + 100);
                        order1.setClientName("Test Client 1 - PENDING");
                        order1.setReservationDate("2026-1-20");
                        order1.setStartTime("9");
                        order1.setEndTime("12");
                        order1.setTotalPrice(3 * pricePerHour);
                        order1.setStatus(ReservationStatus.PENDING);
                        order1.setAdminOrder(false);
                        order1.setWorkspace(firstWorkspace);
                        order1.setWorkspaceId(firstWorkspace.getId());

                        Reservation order2 = r.createObject(Reservation.class, baseId + 101);
                        order2.setClientName("Test Client 2 - PENDING");
                        order2.setReservationDate("2026-1-21");
                        order2.setStartTime("14");
                        order2.setEndTime("18");
                        order2.setTotalPrice(4 * pricePerHour);
                        order2.setStatus(ReservationStatus.PENDING);
                        order2.setAdminOrder(false);
                        order2.setWorkspace(firstWorkspace);
                        order2.setWorkspaceId(firstWorkspace.getId());
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
