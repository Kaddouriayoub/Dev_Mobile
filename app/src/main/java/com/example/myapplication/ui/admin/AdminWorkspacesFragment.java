package com.example.myapplication.ui.admin;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.Workspace;
import com.example.myapplication.ui.adapters.WorkspaceAdapter;
import com.example.myapplication.utils.SessionManager;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import java.io.File;
import java.util.ArrayList;

public class AdminWorkspacesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ImageView btnAdd, btnSearch, btnCloseSearch;
    private CardView searchContainer;
    private EditText etSearch;
    private Realm realm;
    private WorkspaceAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_workspaces, container, false);

        recyclerView = view.findViewById(R.id.recycler_workspaces);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnAdd = view.findViewById(R.id.btn_add_workspace);
        btnSearch = view.findViewById(R.id.btn_search);
        btnCloseSearch = view.findViewById(R.id.btn_close_search);
        searchContainer = view.findViewById(R.id.search_container);
        etSearch = view.findViewById(R.id.et_search);

        btnAdd.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AddEditWorkspaceFragment())
                .addToBackStack(null)
                .commit();
        });

        // Search functionality
        btnSearch.setOnClickListener(v -> {
            searchContainer.setVisibility(View.VISIBLE);
            etSearch.requestFocus();
        });

        btnCloseSearch.setOnClickListener(v -> {
            searchContainer.setVisibility(View.GONE);
            etSearch.setText("");
            if (adapter != null) {
                adapter.filter("");
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Initialize Realm
        realm = Realm.getDefaultInstance();

        // Clean up broken images first
        cleanupBrokenImages();

        loadWorkspaces();

        return view;
    }

    private void cleanupBrokenImages() {
        RealmResults<Workspace> allWorkspaces = realm.where(Workspace.class).findAll();

        realm.executeTransaction(r -> {
            for (Workspace workspace : allWorkspaces) {
                RealmList<String> images = workspace.getImages();
                if (images != null && !images.isEmpty()) {
                    ArrayList<String> validImages = new ArrayList<>();

                    for (String imageUri : images) {
                        if (imageUri != null && isImageValid(imageUri)) {
                            validImages.add(imageUri);
                        } else {
                            Log.d("ImageCleanup", "Removing broken image: " + imageUri);
                        }
                    }

                    // Update with only valid images
                    if (validImages.size() != images.size()) {
                        images.clear();
                        images.addAll(validImages);
                    }
                }
            }
        });
    }

    private boolean isImageValid(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }

        try {
            // Check if it's an absolute file path (starts with /)
            if (imagePath.startsWith("/")) {
                File file = new File(imagePath);
                boolean exists = file.exists();
                Log.d("ImageCleanup", "File path: " + imagePath + " exists: " + exists);
                return exists;
            }

            Uri uri = Uri.parse(imagePath);

            // Check if it's a file URI
            if ("file".equals(uri.getScheme())) {
                File file = new File(uri.getPath());
                return file.exists();
            }

            // Check if it's a content URI - try to open it
            if ("content".equals(uri.getScheme())) {
                try {
                    getContext().getContentResolver().openInputStream(uri).close();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private void loadWorkspaces() {
        // Get current admin's ID from session
        SessionManager sessionManager = new SessionManager(getContext());
        Long currentAdminId = sessionManager.getUserId();

        // Filter workspaces by current admin ID
        RealmResults<Workspace> workspaces = realm.where(Workspace.class)
                .equalTo("adminId", currentAdminId)
                .findAll();

        adapter = new WorkspaceAdapter(getContext(), workspaces, new WorkspaceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Workspace workspace) {
                // Open preview
                AdminWorkspacePreviewFragment fragment = AdminWorkspacePreviewFragment.newInstance(workspace.getId());
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onEditClick(Workspace workspace) {
                // Handle edit click
                AddEditWorkspaceFragment fragment = new AddEditWorkspaceFragment();
                Bundle args = new Bundle();
                args.putLong("WORKSPACE_ID", workspace.getId());
                fragment.setArguments(args);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }
}
