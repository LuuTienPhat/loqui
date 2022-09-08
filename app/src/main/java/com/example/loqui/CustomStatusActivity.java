package com.example.loqui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.example.loqui.adapter.StatusAdapter;
import com.example.loqui.constants.Constants;
import com.example.loqui.constants.StatusStatus;
import com.example.loqui.data.model.Status;
import com.example.loqui.databinding.ActivityCustomStatusBinding;
import com.example.loqui.listeners.StatusListener;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CustomStatusActivity extends AppCompatActivity implements StatusListener {

    private ActivityCustomStatusBinding binding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private List<Status> statuses;
    private StatusAdapter statusAdapter;
    private ListenerRegistration listenerRegistration;

    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportActionBar().setElevation(0);

        init();

        listenerRegistration = database.collection(Keys.KEY_COLLECTION_YOUR_STATUS)
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .addSnapshotListener(eventListener);


        //getStatuses();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();

        statuses = new ArrayList<>();
        statusAdapter = new StatusAdapter(statuses, this, preferenceManager);
        binding.rvStatus.setAdapter(statusAdapter);

        binding.lySwipe.setOnRefreshListener(() -> {
            listenerRegistration.remove();

            int count = this.statuses.size();
            this.statuses.clear();
            statusAdapter.notifyItemRangeRemoved(0, count);

            listenerRegistration = database.collection(Keys.KEY_COLLECTION_YOUR_STATUS)
                    .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                    .addSnapshotListener(eventListener);

            binding.lySwipe.setRefreshing(false);
        });
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        loading(true);
        if (error != null) {
            showErrorMessage();
            loading(false);
            return;
        }
        if (value != null) {
            int count = statuses.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();

                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    Status status = new Status();
                    status.setId(queryDocumentSnapshot.getString(Keys.KEY_ID));
                    status.setUserId(queryDocumentSnapshot.getString(Keys.KEY_USER_ID));
                    status.setName(queryDocumentSnapshot.getString(Keys.KEY_NAME));
                    status.setIcon(queryDocumentSnapshot.getString(Keys.KEY_ICON));
                    status.setStatus(queryDocumentSnapshot.getString(Keys.KEY_STATUS));
//                    status.setHour(queryDocumentSnapshot.getString(Keys.KEY_HOUR));
//                    status.setEndDate(queryDocumentSnapshot.getString(Keys.KEY_END_DATE));

                    if (!status.getStatus().equals(StatusStatus.DELETED)) {
                        this.statuses.add(status);
                        statusAdapter.notifyItemInserted(this.statuses.size() - 1);
                    }
                }
                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    Status status = new Status();
                    status.setId(queryDocumentSnapshot.getString(Keys.KEY_ID));
                    status.setUserId(queryDocumentSnapshot.getString(Keys.KEY_USER_ID));
                    status.setName(queryDocumentSnapshot.getString(Keys.KEY_NAME));
                    status.setIcon(queryDocumentSnapshot.getString(Keys.KEY_ICON));
                    status.setStatus(queryDocumentSnapshot.getString(Keys.KEY_STATUS));

                    int statusIndex = findStatus(status.getId());
                    if (status.getStatus().equals(StatusStatus.DELETED)) {
                        statusAdapter.notifyItemRemoved(statusIndex);
                        this.statuses.remove(statusIndex);
                    } else {
                        this.statuses.set(statusIndex, status);
                        statusAdapter.notifyItemChanged(statusIndex);
                    }


                }
            }

            loading(false);
            if (statuses.isEmpty()) {
                showErrorMessage();
            }
        } else {
            loading(false);
        }
    };

    private void showErrorMessage() {
        binding.tvErrorMessage.setText(String.format("%s", "No status available"));
        binding.tvErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu_custom_status, menu);


        menu.findItem(R.id.btnSearch).setVisible(false);
        SearchView searchView = (SearchView) menu.findItem(R.id.btnSearch).getActionView();
        searchView.setQueryHint("Search");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.btnAdd) {
            handleBtnAddClicked();
            return true;
//            case R.id.btnRemove: {
//                handleBtnRemoveClicked();
//                return true;
//            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleBtnRemoveClicked() {
    }

    private void handleBtnAddClicked() {
        Intent intent = new Intent(getApplicationContext(), StatusActivity.class);
        intent.putExtra(Constants.TYPE, StatusActivity.ADD_STATUS);
        startActivity(intent);
    }

    private int findStatus(String statusId) {
        int index = -1;
        for (Status status : this.statuses
        ) {
            if (status.getId().equals(statusId)) {
                index = this.statuses.indexOf(status);
                break;
            }
        }
        return index;
    }

    @Override
    public void onStatusClicked(Status status) {
//        database.collection(Keys.KEY_COLLECTION_YOUR_STATUS)
//                .document(status.getId())
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    documentSnapshot.getReference().update(Keys.KEY_STATUS, StatusStatus.ACTIVE);
//                    HashMap<String, Object> s = new HashMap<>();
//                    s.put(Keys.KEY_STATUS, StatusStatus.ACTIVE);
//                    s.put(Keys.KEY_DUE_DATE, String.valueOf(System.currentTimeMillis() + 3600000));
//                    documentSnapshot.getReference().update(s)
//                            .addOnSuccessListener(unused -> {
//                                preferenceManager.putString(Constants.STATUS_NAME, documentSnapshot.getString(Keys.KEY_NAME));
//                                preferenceManager.putString(Constants.STATUS_ICON, documentSnapshot.getString(Keys.KEY_ICON));
//
//                                MyToast.showLongToast(this, "Your status is changed");
//                            });
//                });

        Intent intent = new Intent(getApplicationContext(), StatusActivity.class);
        intent.putExtra(Constants.TYPE, StatusActivity.MODIFY_STATUS);
        intent.putExtra(Constants.STATUS, status);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}