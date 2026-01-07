package com.example.myapplication.ui.client;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Workspace;
import com.example.myapplication.ui.adapters.ClientWorkspaceAdapter;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

public class ExploreFragment extends Fragment {

    private Realm realm;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.rvPopularSpaces);
        EditText etSearch = view.findViewById(R.id.etSearch);

        realm = Realm.getDefaultInstance();

        // Initial load
        loadWorkspaces("");

        // 🔍 Search on ENTER
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                String query = etSearch.getText().toString().trim();
                loadWorkspaces(query);
                return true;
            }
            return false;
        });

        return view;
    }

    private void loadWorkspaces(String query) {

        RealmResults<Workspace> results;

        if (query.isEmpty()) {
            results = realm.where(Workspace.class)
                    .equalTo("status", "AVAILABLE")
                    .findAll();
        } else {
            results = realm.where(Workspace.class)
                    .equalTo("status", "AVAILABLE")
                    .beginGroup()
                    .contains("name", query, Case.INSENSITIVE)
                    .or()
                    .contains("description", query, Case.INSENSITIVE)
                    .or()
                    .contains("address", query, Case.INSENSITIVE)
                    .or()
                    .contains("city", query, Case.INSENSITIVE)
                    .endGroup()
                    .findAll();
        }

        ClientWorkspaceAdapter adapter =
                new ClientWorkspaceAdapter(results, workspaceId -> {

                    WorkspaceDetailsFragment fragment =
                            WorkspaceDetailsFragment.newInstance(workspaceId);

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                });

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) realm.close();
    }
}
