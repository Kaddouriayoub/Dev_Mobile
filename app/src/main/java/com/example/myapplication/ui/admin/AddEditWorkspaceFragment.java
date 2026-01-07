package com.example.myapplication.ui.admin;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myapplication.R;
import com.example.myapplication.model.Workspace;
import com.example.myapplication.model.WorkspaceType;
import com.google.android.material.textfield.TextInputEditText;
import io.realm.Realm;
import io.realm.RealmList;
import java.util.ArrayList;
import java.util.List;

public class AddEditWorkspaceFragment extends Fragment {

    private TextInputEditText etName, etDescription, etLocation, etPrice, etCapacity;
    private AutoCompleteTextView atType, atStatus;
    private Button btnSave, btnDelete;
    private LinearLayout imagesContainer;
    private FrameLayout btnAddImage;
    private Realm realm;
    private Long workspaceId;

    private List<String> selectedImageUris = new ArrayList<>();

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register image picker launcher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    // Take persistable permission so the URI remains accessible
                    try {
                        requireContext().getContentResolver().takePersistableUriPermission(
                            uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (SecurityException e) {
                        // Some content providers don't support persistable permissions
                    }

                    selectedImageUris.add(uri.toString());
                    addImageToContainer(uri.toString());
                }
            }
        );
    }

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
        atStatus = view.findViewById(R.id.at_status);
        btnSave = view.findViewById(R.id.btn_save);
        btnDelete = view.findViewById(R.id.btn_delete);
        imagesContainer = view.findViewById(R.id.images_container);
        btnAddImage = view.findViewById(R.id.btn_add_image);

        realm = Realm.getDefaultInstance();

        // Check if editing
        if (getArguments() != null && getArguments().containsKey("WORKSPACE_ID")) {
            workspaceId = getArguments().getLong("WORKSPACE_ID");
            loadWorkspaceData(workspaceId);
            btnDelete.setVisibility(View.VISIBLE);
        }

        setupTypeDropdown();
        setupStatusDropdown();

        btnAddImage.setOnClickListener(v -> openImagePicker());
        btnSave.setOnClickListener(v -> saveWorkspace());
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());

        return view;
    }

    private void setupTypeDropdown() {
        String[] types = new String[]{"DESK", "MEETING_ROOM", "OPEN_SPACE"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, types);
        atType.setAdapter(adapter);
    }

    private void setupStatusDropdown() {
        String[] statuses = new String[]{"AVAILABLE", "FULL", "MAINTENANCE"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses);
        atStatus.setAdapter(adapter);
        atStatus.setText("AVAILABLE", false); // Default status
    }

    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

    private void addImageToContainer(String imageUri) {
        // Create a new FrameLayout for the image with delete button
        FrameLayout imageFrame = new FrameLayout(requireContext());
        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(
            dpToPx(100), dpToPx(100)
        );
        frameParams.setMarginEnd(dpToPx(8));
        imageFrame.setLayoutParams(frameParams);

        // Create ImageView for the selected image
        ImageView imageView = new ImageView(requireContext());
        imageView.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageURI(Uri.parse(imageUri));

        // Create delete button overlay
        ImageView deleteBtn = new ImageView(requireContext());
        FrameLayout.LayoutParams deleteBtnParams = new FrameLayout.LayoutParams(
            dpToPx(24), dpToPx(24)
        );
        deleteBtnParams.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
        deleteBtnParams.setMargins(0, dpToPx(4), dpToPx(4), 0);
        deleteBtn.setLayoutParams(deleteBtnParams);
        deleteBtn.setImageResource(android.R.drawable.ic_delete);
        deleteBtn.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
        deleteBtn.setPadding(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));

        // Set click listener to remove image
        deleteBtn.setOnClickListener(v -> {
            selectedImageUris.remove(imageUri);
            imagesContainer.removeView(imageFrame);
        });

        imageFrame.addView(imageView);
        imageFrame.addView(deleteBtn);

        // Add before the "add" button (which should always be first)
        imagesContainer.addView(imageFrame, imagesContainer.getChildCount() - 0);
        // Actually, let's add it after the add button but before any other images
        // We want the add button first, then images
        // So we insert at position 1 (right after the add button at position 0)
        imagesContainer.removeView(imageFrame);
        imagesContainer.addView(imageFrame, 1);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void loadWorkspaceData(Long id) {
        Workspace workspace = realm.where(Workspace.class).equalTo("id", id).findFirst();
        if (workspace != null) {
            etName.setText(workspace.getName());
            etDescription.setText(workspace.getDescription());
            etLocation.setText(workspace.getAddress());
            etPrice.setText(String.valueOf(workspace.getPricePerHour()));
            etCapacity.setText(String.valueOf(workspace.getCapacity()));
            if (workspace.getType() != null) {
                atType.setText(workspace.getType().name(), false);
            }
            if (workspace.getStatus() != null) {
                atStatus.setText(workspace.getStatus(), false);
            }

            // Load existing images
            if (workspace.getImages() != null) {
                for (String imageUri : workspace.getImages()) {
                    selectedImageUris.add(imageUri);
                    addImageToContainer(imageUri);
                }
            }

            btnSave.setText("Update Workspace");
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Workspace")
            .setMessage("Are you sure you want to delete this workspace? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> deleteWorkspace())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteWorkspace() {
        if (workspaceId == null) return;

        realm.executeTransaction(r -> {
            Workspace workspace = r.where(Workspace.class).equalTo("id", workspaceId).findFirst();
            if (workspace != null) {
                workspace.deleteFromRealm();
            }
        });

        Toast.makeText(getContext(), "Workspace Deleted", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }

    private void saveWorkspace() {
        final String name = etName.getText().toString();
        final String description = etDescription.getText().toString();
        final String location = etLocation.getText().toString();
        final String priceStr = etPrice.getText().toString();
        final String capacityStr = etCapacity.getText().toString();
        final String typeStr = atType.getText().toString();
        final String statusStr = atStatus.getText().toString();

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        realm.executeTransaction(r -> {
            Workspace workspace;
            if (workspaceId != null) {
                workspace = r.where(Workspace.class).equalTo("id", workspaceId).findFirst();
            } else {
                workspace = r.createObject(Workspace.class, System.currentTimeMillis());
            }

            if (workspace != null) {
                workspace.setName(name);
                workspace.setDescription(description);
                workspace.setAddress(location);
                workspace.setCity(location);
                workspace.setPricePerHour(Double.parseDouble(priceStr));
                workspace.setCapacity(capacityStr.isEmpty() ? 0 : Integer.parseInt(capacityStr));
                try {
                    workspace.setType(WorkspaceType.valueOf(typeStr));
                } catch (IllegalArgumentException e) {
                    workspace.setType(WorkspaceType.DESK);
                }
                workspace.setStatus(statusStr.isEmpty() ? "AVAILABLE" : statusStr);

                // Save images
                RealmList<String> images = workspace.getImages();
                if (images == null) {
                    images = new RealmList<>();
                }
                images.clear();
                images.addAll(selectedImageUris);
                workspace.setImages(images);
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
