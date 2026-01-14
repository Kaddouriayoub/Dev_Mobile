package com.example.myapplication.ui.admin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.model.User;
import com.example.myapplication.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import io.realm.Realm;

public class EditProfileFragment extends Fragment {

    private ImageView imgProfilePic, btnBack;
    private TextInputEditText etFullName, etEmail;
    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnChangePhoto, btnSave;
    private Realm realm;
    private SessionManager sessionManager;
    private User currentUser;
    private String selectedImagePath;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        // Copy image to internal storage
                        String savedPath = copyImageToInternalStorage(uri);
                        if (savedPath != null) {
                            selectedImagePath = savedPath;
                            loadImageIntoView(imgProfilePic, savedPath);
                            Toast.makeText(getContext(), "Photo selected", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        imgProfilePic = view.findViewById(R.id.img_profile_pic);
        btnBack = view.findViewById(R.id.btn_back);
        etFullName = view.findViewById(R.id.et_full_name);
        etEmail = view.findViewById(R.id.et_email);
        etCurrentPassword = view.findViewById(R.id.et_current_password);
        etNewPassword = view.findViewById(R.id.et_new_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        btnChangePhoto = view.findViewById(R.id.btn_change_photo);
        btnSave = view.findViewById(R.id.btn_save);

        realm = Realm.getDefaultInstance();
        sessionManager = new SessionManager(requireContext());

        loadCurrentUser();

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        btnChangePhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnSave.setOnClickListener(v -> saveProfile());

        return view;
    }

    private void loadCurrentUser() {
        Long userId = sessionManager.getUserId();
        if (userId != -1) {
            currentUser = realm.where(User.class).equalTo("id", userId).findFirst();
            if (currentUser != null) {
                etFullName.setText(currentUser.getFullName());
                etEmail.setText(currentUser.getEmail());

                // Load current profile image
                String profileImage = currentUser.getProfileImage();
                if (profileImage != null && !profileImage.isEmpty()) {
                    selectedImagePath = profileImage;
                    loadImageIntoView(imgProfilePic, profileImage);
                }
            }
        }
    }

    private void saveProfile() {
        String newName = etFullName.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null) {
            Toast.makeText(getContext(), "Error: User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user wants to change password
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean isChangingPassword = !currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty();

        if (isChangingPassword) {
            // Validate password change
            if (currentPassword.isEmpty()) {
                Toast.makeText(getContext(), "Please enter your current password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!currentPassword.equals(currentUser.getPassword())) {
                Toast.makeText(getContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a new password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(getContext(), "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(getContext(), "New passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Update profile
        realm.executeTransaction(r -> {
            currentUser.setFullName(newName);
            if (selectedImagePath != null) {
                currentUser.setProfileImage(selectedImagePath);
            }
            if (isChangingPassword) {
                currentUser.setPassword(newPassword);
            }
        });

        String message = isChangingPassword ?
                "Profile and password updated successfully" :
                "Profile updated successfully";
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }

    private String copyImageToInternalStorage(Uri uri) {
        try {
            // Create profile_images directory if it doesn't exist
            File imagesDir = new File(requireContext().getFilesDir(), "profile_images");
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }

            // Generate unique filename
            String fileName = "profile_" + UUID.randomUUID().toString() + ".jpg";
            File destFile = new File(imagesDir, fileName);

            // Copy the image
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }

            OutputStream outputStream = new FileOutputStream(destFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            return destFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadImageIntoView(ImageView imageView, String imagePath) {
        try {
            if (imagePath.startsWith("/")) {
                File file = new File(imagePath);
                if (file.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        return;
                    }
                }
            } else {
                Uri uri = Uri.parse(imagePath);

                if ("file".equals(uri.getScheme())) {
                    File file = new File(uri.getPath());
                    if (file.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                            return;
                        }
                    }
                } else if ("content".equals(uri.getScheme())) {
                    InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                    if (inputStream != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Keep default image
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }
}
