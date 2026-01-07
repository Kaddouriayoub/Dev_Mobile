package com.example.myapplication.ui.client.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Workspace;
import com.example.myapplication.ui.adapters.ClientWorkspaceAdapter;

import io.realm.Realm;
import io.realm.RealmResults;

public class ExploreFragment extends Fragment {

    private Realm realm;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rvPopularSpaces);

        realm = Realm.getDefaultInstance();

        RealmResults<Workspace> workspaces = realm.where(Workspace.class)
                .equalTo("status", "AVAILABLE")
                .findAll();

        ClientWorkspaceAdapter adapter =
                new ClientWorkspaceAdapter(workspaces);

        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) realm.close();
    }
}
