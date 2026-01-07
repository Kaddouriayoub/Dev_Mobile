package com.example.myapplication.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.Workspace;
import com.example.myapplication.ui.adapters.WorkspaceAdapter;
import io.realm.Realm;
import io.realm.RealmResults;

public class AdminWorkspacesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ImageView btnAdd;
    private Realm realm;
    private WorkspaceAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_workspaces, container, false);
        
        recyclerView = view.findViewById(R.id.recycler_workspaces);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        btnAdd = view.findViewById(R.id.btn_add_workspace);

        btnAdd.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AddEditWorkspaceFragment())
                .addToBackStack(null)
                .commit();
        });

        // Initialize Realm
        realm = Realm.getDefaultInstance();
        
        loadWorkspaces();

        return view;
    }

    private void loadWorkspaces() {
        RealmResults<Workspace> workspaces = realm.where(Workspace.class).findAll();
        
        adapter = new WorkspaceAdapter(getContext(), workspaces, workspace -> {
            // Handle edit click
             AddEditWorkspaceFragment fragment = new AddEditWorkspaceFragment();
             Bundle args = new Bundle();
             args.putLong("WORKSPACE_ID", workspace.getId());
             fragment.setArguments(args);

             getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
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
