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
import java.util.UUID;

public class AddEditWorkspaceFragment extends Fragment {

    private TextInputEditText etName, etDescription, etLocation, etPrice, etCapacity;
    private AutoCompleteTextView atType;
    private Button btnSave;
    private Realm realm;
    private Long workspaceId; // Null if adding new

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_edit_workspace, container, false);
        
        etName = view.findViewById(R.id.et_name);
        etDescription = view.findViewById(R.id.et_description);
        etLocation = view.findViewById(R.id.et_location);
        etPrice = view.findViewById(R.id.et_price);
        etCapacity = view.findViewById(R.id.et_capacity);
        atType = view.findViewById(R.id.at_type);
        btnSave = view.findViewById(R.id.btn_save);

        realm = Realm.getDefaultInstance();

        // Check if editing
        if (getArguments() != null && getArguments().containsKey("WORKSPACE_ID")) {
            workspaceId = getArguments().getLong("WORKSPACE_ID");
            loadWorkspaceData(workspaceId);
        }

        setupTypeDropdown();

        btnSave.setOnClickListener(v -> saveWorkspace());

        return view;
    }

    private void setupTypeDropdown() {
        String[] types = new String[]{"DESK", "MEETING_ROOM", "OPEN_SPACE"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, types);
        atType.setAdapter(adapter);
    }

    private void loadWorkspaceData(Long id) {
        Workspace workspace = realm.where(Workspace.class).equalTo("id", id).findFirst();
        if (workspace != null) {
            etName.setText(workspace.getName());
            etDescription.setText(workspace.getDescription());
            etLocation.setText(workspace.getAddress()); // Using address for location
            etPrice.setText(String.valueOf(workspace.getPricePerHour()));
            etCapacity.setText(String.valueOf(workspace.getCapacity()));
            if (workspace.getType() != null) {
                atType.setText(workspace.getType().name(), false);
            }
            btnSave.setText("Update Workspace");
        }
    }

    private void saveWorkspace() {
        final String name = etName.getText().toString();
        final String description = etDescription.getText().toString();
        final String location = etLocation.getText().toString();
        final String priceStr = etPrice.getText().toString();
        final String capacityStr = etCapacity.getText().toString();
        final String typeStr = atType.getText().toString();

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        realm.executeTransaction(r -> {
            Workspace workspace;
            if (workspaceId != null) {
                workspace = r.where(Workspace.class).equalTo("id", workspaceId).findFirst();
            } else {
                workspace = r.createObject(Workspace.class, System.currentTimeMillis()); // Use timestamp as ID for now
            }

            if (workspace != null) {
                workspace.setName(name);
                workspace.setDescription(description);
                workspace.setAddress(location);
                workspace.setCity(location); // Simplified for now
                workspace.setPricePerHour(Double.parseDouble(priceStr));
                workspace.setCapacity(capacityStr.isEmpty() ? 0 : Integer.parseInt(capacityStr));
                try {
                    workspace.setType(WorkspaceType.valueOf(typeStr));
                } catch (IllegalArgumentException e) {
                    workspace.setType(WorkspaceType.DESK); // Default
                }
                workspace.setStatus("AVAILABLE");
            }
        });

        Toast.makeText(getContext(), "Workspace Saved", Toast.LENGTH_SHORT).show();
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
