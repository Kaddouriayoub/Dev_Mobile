package com.example.myapplication.ui.admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myapplication.R;
import com.example.myapplication.model.Role;
import com.example.myapplication.model.User;
import com.example.myapplication.ui.login.LoginActivity;
import com.example.myapplication.utils.SessionManager;
import io.realm.Realm;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminProfileFragment extends Fragment {

    private ImageView imgProfilePic;
    private TextView tvFullName, tvRole, tvEmail, tvCreatedAt;
    private Button btnLogout, btnEditProfile;
    private Realm realm;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_profile, container, false);

        imgProfilePic = view.findViewById(R.id.img_profile_pic);
        tvFullName = view.findViewById(R.id.tv_full_name);
        tvRole = view.findViewById(R.id.tv_role);
        tvEmail = view.findViewById(R.id.tv_email);
        tvCreatedAt = view.findViewById(R.id.tv_created_at);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);

        realm = Realm.getDefaultInstance();
        // Use getContext() or getActivity() safely
        if (getContext() != null) {
            sessionManager = new SessionManager(getContext());
        }

        loadProfileData();

        btnEditProfile.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new EditProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnLogout.setOnClickListener(v -> {
            if (sessionManager != null) {
                sessionManager.logoutUser();
            }
            // Navigate back to LoginActivity
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                // Clear the back stack so user cannot go back to admin dashboard
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish(); // Finish current activity (AdminDashboardActivity)
            } else {
                Toast.makeText(getContext(), "Error logging out", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadProfileData() {
        if (sessionManager == null) return;

        Long userId = sessionManager.getUserId();
        User user = null;

        if (userId != -1) {
             user = realm.where(User.class).equalTo("id", userId).findFirst();
        }

        // Fallback or Dummy only if login skipped (which shouldn't happen now)
        if (user == null) {
            user = realm.where(User.class).equalTo("role", Role.ADMIN.name()).findFirst();
        }

        if (user != null) {
            tvFullName.setText(user.getFullName());
            if (user.getRole() != null) {
                tvRole.setText(user.getRole().name());
            }
            tvEmail.setText(user.getEmail());

            // Format createdAt date
            String createdAt = user.getCreatedAt();
            if (createdAt != null && !createdAt.isEmpty()) {
                tvCreatedAt.setText(formatDate(createdAt));
            } else {
                tvCreatedAt.setText("Unknown");
            }

            // Load profile image
            String profileImage = user.getProfileImage();
            if (profileImage != null && !profileImage.isEmpty()) {
                loadProfileImage(profileImage);
            }
        }
    }

    private String formatDate(String createdAt) {
        try {
            // Try parsing as timestamp (milliseconds)
            long timestamp = Long.parseLong(createdAt);
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
            return sdf.format(date);
        } catch (NumberFormatException e) {
            // If it's not a timestamp, return as is (already formatted)
            return createdAt;
        }
    }

    private void loadProfileImage(String imagePath) {
        try {
            // Check if it's an absolute file path (starts with /)
            if (imagePath.startsWith("/")) {
                File file = new File(imagePath);
                if (file.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    if (bitmap != null) {
                        imgProfilePic.setImageBitmap(bitmap);
                        return;
                    }
                }
            } else {
                // It's a URI
                Uri uri = Uri.parse(imagePath);

                if ("file".equals(uri.getScheme())) {
                    File file = new File(uri.getPath());
                    if (file.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        if (bitmap != null) {
                            imgProfilePic.setImageBitmap(bitmap);
                            return;
                        }
                    }
                } else if ("content".equals(uri.getScheme())) {
                    InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                    if (inputStream != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();
                        if (bitmap != null) {
                            imgProfilePic.setImageBitmap(bitmap);
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
    public void onResume() {
        super.onResume();
        loadProfileData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }
}
