package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.ArrayList;
import java.util.List;

public class AddOrderFragment extends Fragment {

    private AutoCompleteTextView atWorkspace, atClient, atStatus;
    private TextInputEditText etDate, etStartTime, etEndTime, etTotalPrice;
    private Button btnCreate;
    private Realm realm;

    private List<Workspace> workspaceList;
    private List<Client> clientList;
    private Workspace selectedWorkspace;
    private Client selectedClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_order, container, false);
        
        atWorkspace = view.findViewById(R.id.at_workspace);
        atClient = view.findViewById(R.id.at_client);
        atStatus = view.findViewById(R.id.at_status);
        etDate = view.findViewById(R.id.et_date);
        etStartTime = view.findViewById(R.id.et_start_time);
        etEndTime = view.findViewById(R.id.et_end_time);
        etTotalPrice = view.findViewById(R.id.et_total_price);
        btnCreate = view.findViewById(R.id.btn_create_order);

        realm = Realm.getDefaultInstance();

        setupDropdowns();

        btnCreate.setOnClickListener(v -> createOrder());

        return view;
    }

    private void setupDropdowns() {
        // Status Dropdown
        String[] statuses = new String[]{"PENDING", "CONFIRMED", "CANCELLED"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses);
        atStatus.setAdapter(statusAdapter);

        // Load Workspaces
        RealmResults<Workspace> workspaces = realm.where(Workspace.class).findAll();
        workspaceList = new ArrayList<>(realm.copyFromRealm(workspaces)); // Detached copy for simpler list handling
        List<String> workspaceNames = new ArrayList<>();
        for (Workspace w : workspaceList) {
            workspaceNames.add(w.getName());
        }
        ArrayAdapter<String> workspaceAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, workspaceNames);
        atWorkspace.setAdapter(workspaceAdapter);
        atWorkspace.setOnItemClickListener((parent, view, position, id) -> {
            selectedWorkspace = workspaceList.get(position);
        });

        // Load Clients
        RealmResults<Client> clients = realm.where(Client.class).findAll();
        clientList = new ArrayList<>(realm.copyFromRealm(clients));
        List<String> clientNames = new ArrayList<>();
        for (Client c : clientList) {
            // Assuming Client has an ID, but maybe we want to show something else? 
            // The Client model only has id, discountRate, loyaltyPoints, etc. 
            // It doesn't seem to have a 'name' field based on previous interaction (User has name, Client is separate?).
            // Let's check Client model again. If it links to User, we might need that.
            // For now, let's display "Client #" + ID
            clientNames.add("Client #" + c.getId());
        }
        ArrayAdapter<String> clientAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, clientNames);
        atClient.setAdapter(clientAdapter);
        atClient.setOnItemClickListener((parent, view, position, id) -> {
            selectedClient = clientList.get(position);
        });
    }

    private void createOrder() {
        if (selectedWorkspace == null || selectedClient == null) {
            Toast.makeText(getContext(), "Please select Workspace and Client", Toast.LENGTH_SHORT).show();
            return;
        }

        final String date = etDate.getText().toString();
        final String startTime = etStartTime.getText().toString();
        final String endTime = etEndTime.getText().toString();
        final String priceStr = etTotalPrice.getText().toString();
        final String statusStr = atStatus.getText().toString();

        if (date.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        realm.executeTransaction(r -> {
            Reservation reservation = r.createObject(Reservation.class, System.currentTimeMillis());
            
            reservation.setReservationDate(date);
            reservation.setStartTime(startTime);
            reservation.setEndTime(endTime);
            reservation.setTotalPrice(Double.parseDouble(priceStr));
            
            try {
                reservation.setStatus(ReservationStatus.valueOf(statusStr));
            } catch (Exception e) {
                reservation.setStatus(ReservationStatus.PENDING);
            }

            // Link relationships
            // Note: Since selectedWorkspace is detached (copyFromRealm), we should find the attached one or set ID
            // Ideally, we set the ID and let Realm handle relationship if managed, or find the managed object.
            
            Workspace managedWorkspace = r.where(Workspace.class).equalTo("id", selectedWorkspace.getId()).findFirst();
            reservation.setWorkspace(managedWorkspace);
            reservation.setWorkspaceId(managedWorkspace.getId());

            Client managedClient = r.where(Client.class).equalTo("id", selectedClient.getId()).findFirst();
            reservation.setClient(managedClient);
            reservation.setClientId(managedClient.getId());
        });

        Toast.makeText(getContext(), "Order Created", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }
}
