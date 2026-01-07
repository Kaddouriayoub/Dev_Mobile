package com.example.myapplication.ui.admin;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myapplication.R;
import com.example.myapplication.model.Reservation;
import com.example.myapplication.model.ReservationStatus;
import com.example.myapplication.model.Workspace;
import com.google.android.material.textfield.TextInputEditText;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddOrderFragment extends Fragment {

    private AutoCompleteTextView atWorkspace;
    private TextInputEditText etNumberOfPlaces, etClientName, etDate, etStartTime, etEndTime, etTotalPrice;
    private TextView tvAvailablePlaces;
    private Button btnCreate;
    private Realm realm;

    private List<Workspace> workspaceList;
    private Workspace selectedWorkspace;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_order, container, false);

        atWorkspace = view.findViewById(R.id.at_workspace);
        etNumberOfPlaces = view.findViewById(R.id.et_number_of_places);
        etClientName = view.findViewById(R.id.et_client_name);
        tvAvailablePlaces = view.findViewById(R.id.tv_available_places);
        etDate = view.findViewById(R.id.et_date);
        etStartTime = view.findViewById(R.id.et_start_time);
        etEndTime = view.findViewById(R.id.et_end_time);
        etTotalPrice = view.findViewById(R.id.et_total_price);
        btnCreate = view.findViewById(R.id.btn_create_order);

        realm = Realm.getDefaultInstance();

        setupWorkspaceDropdown();
        setupDateTimePickers();

        btnCreate.setOnClickListener(v -> createOrder());

        return view;
    }

    private void setupWorkspaceDropdown() {
        RealmResults<Workspace> workspaces = realm.where(Workspace.class).findAll();
        workspaceList = new ArrayList<>(realm.copyFromRealm(workspaces));

        List<String> workspaceNames = new ArrayList<>();
        for (Workspace w : workspaceList) {
            workspaceNames.add(w.getName() + " (" + w.getAvailablePlaces() + "/" + w.getCapacity() + " available)");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, workspaceNames);
        atWorkspace.setAdapter(adapter);

        atWorkspace.setOnItemClickListener((parent, view, position, id) -> {
            selectedWorkspace = workspaceList.get(position);
            tvAvailablePlaces.setVisibility(View.VISIBLE);
            tvAvailablePlaces.setText("Available places: " + selectedWorkspace.getAvailablePlaces() + "/" + selectedWorkspace.getCapacity());

            if (selectedWorkspace.getAvailablePlaces() == 0) {
                tvAvailablePlaces.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                tvAvailablePlaces.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }
        });
    }

    private void setupDateTimePickers() {
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                String date = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                etDate.setText(date);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        etStartTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
                String time = String.format("%02d:%02d", hourOfDay, minute);
                etStartTime.setText(time);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        etEndTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
                String time = String.format("%02d:%02d", hourOfDay, minute);
                etEndTime.setText(time);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });
    }

    private void createOrder() {
        if (selectedWorkspace == null) {
            Toast.makeText(getContext(), "Please select a workspace", Toast.LENGTH_SHORT).show();
            return;
        }

        final String clientName = etClientName.getText().toString().trim();
        final String numberOfPlacesStr = etNumberOfPlaces.getText().toString().trim();
        final String date = etDate.getText().toString().trim();
        final String startTime = etStartTime.getText().toString().trim();
        final String endTime = etEndTime.getText().toString().trim();
        final String priceStr = etTotalPrice.getText().toString().trim();

        if (clientName.isEmpty()) {
            Toast.makeText(getContext(), "Please enter client name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (numberOfPlacesStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter number of places", Toast.LENGTH_SHORT).show();
            return;
        }

        int numberOfPlaces = Integer.parseInt(numberOfPlacesStr);

        // Check if enough places available
        Workspace currentWorkspace = realm.where(Workspace.class).equalTo("id", selectedWorkspace.getId()).findFirst();
        if (currentWorkspace == null) {
            Toast.makeText(getContext(), "Workspace not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (numberOfPlaces > currentWorkspace.getAvailablePlaces()) {
            Toast.makeText(getContext(), "Not enough places available! Only " + currentWorkspace.getAvailablePlaces() + " left.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (date.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(getContext(), "Please fill date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        final double totalPrice = priceStr.isEmpty() ? 0 : Double.parseDouble(priceStr);

        realm.executeTransaction(r -> {
            // Create reservation
            Reservation reservation = r.createObject(Reservation.class, System.currentTimeMillis());

            reservation.setReservationDate(date);
            reservation.setStartTime(startTime);
            reservation.setEndTime(endTime);
            reservation.setTotalPrice(totalPrice);
            reservation.setNumberOfPlaces(numberOfPlaces);
            reservation.setClientName(clientName);
            reservation.setAdminOrder(true); // Admin orders are marked

            // Admin orders are auto-confirmed
            reservation.setStatus(ReservationStatus.CONFIRMED);

            // Link workspace
            Workspace managedWorkspace = r.where(Workspace.class).equalTo("id", selectedWorkspace.getId()).findFirst();
            reservation.setWorkspace(managedWorkspace);
            reservation.setWorkspaceId(managedWorkspace.getId());

            // Update available places (since admin orders are auto-confirmed)
            int newAvailable = managedWorkspace.getAvailablePlaces() - numberOfPlaces;
            managedWorkspace.setAvailablePlaces(Math.max(0, newAvailable));

            // Update workspace status if full
            if (managedWorkspace.getAvailablePlaces() == 0) {
                managedWorkspace.setStatus("FULL");
            }
        });

        Toast.makeText(getContext(), "Order Created (Confirmed)", Toast.LENGTH_SHORT).show();
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
