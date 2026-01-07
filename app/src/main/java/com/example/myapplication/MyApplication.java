package com.example.myapplication;

import android.app.Application;
import com.example.myapplication.model.Client;
import com.example.myapplication.model.Role;
import com.example.myapplication.model.SubscriptionType;
import com.example.myapplication.model.User;
import com.example.myapplication.model.Workspace;
import com.example.myapplication.model.WorkspaceType;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("workspace.realm")
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded() // Useful during development
                .build();
        Realm.setDefaultConfiguration(config);

        // Add initial test data
        initializeTestData();
    }

    private void initializeTestData() {
        Realm realm = Realm.getDefaultInstance();
        
        // Check if data already exists
        if (realm.where(User.class).count() == 0) {
            realm.executeTransaction(r -> {
                // 1. Create Admin User
                User admin = r.createObject(User.class, 1L);
                admin.setFullName("Mourad Admin");
                admin.setEmail("admin@workspace.com");
                admin.setRole(Role.ADMIN);
                admin.setCreatedAt("01/01/2024");
                admin.setEnabled(true);
                admin.setProfileImage("https://example.com/admin.jpg");

                // 2. Create a Client User
                User clientUser = r.createObject(User.class, 2L);
                clientUser.setFullName("John Doe");
                clientUser.setEmail("john@client.com");
                clientUser.setRole(Role.CLIENT);
                clientUser.setCreatedAt("15/01/2024");
                clientUser.setEnabled(true);

                // 3. Create Client Profile linked to Client User
                Client client = r.createObject(Client.class, 2L); // Same ID as User for simplicity
                client.setDiscountRate(0.0);
                client.setLoyaltyPoints(100);
                client.setSubscriptionType(SubscriptionType.FREE);
                client.setTotalReservations(0);

                // 4. Create some Workspaces
                Workspace w1 = r.createObject(Workspace.class, 101L);
                w1.setName("Downtown Coworking");
                w1.setDescription("A modern open space in the city center with high-speed wifi.");
                w1.setType(WorkspaceType.OPEN_SPACE);
                w1.setCapacity(50);
                w1.setPricePerHour(15.0);
                w1.setCity("New York");
                w1.setAddress("123 Broadway Ave");
                w1.setStatus("AVAILABLE");

                Workspace w2 = r.createObject(Workspace.class, 102L);
                w2.setName("Executive Meeting Room");
                w2.setDescription("Private meeting room for up to 10 people with projector.");
                w2.setType(WorkspaceType.MEETING_ROOM);
                w2.setCapacity(10);
                w2.setPricePerHour(50.0);
                w2.setCity("New York");
                w2.setAddress("123 Broadway Ave");
                w2.setStatus("AVAILABLE");
                
                Workspace w3 = r.createObject(Workspace.class, 103L);
                w3.setName("Private Office Suite");
                w3.setDescription("Fully furnished private office for small teams.");
                w3.setType(WorkspaceType.DESK);
                w3.setCapacity(4);
                w3.setPricePerHour(100.0);
                w3.setCity("San Francisco");
                w3.setAddress("456 Market St");
                w3.setStatus("AVAILABLE");
            });
        }
        
        realm.close();
    }
}
