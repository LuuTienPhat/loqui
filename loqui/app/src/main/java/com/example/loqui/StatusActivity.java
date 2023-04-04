package com.example.loqui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.loqui.constants.Constants;
import com.example.loqui.constants.StatusStatus;
import com.example.loqui.data.model.Status;
import com.example.loqui.databinding.ActivityStatusBinding;
import com.example.loqui.utils.FirebaseHelper;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class StatusActivity extends BaseActivity {

    private ActivityStatusBinding binding;
    private Status status;
    private MenuItem btnSave, btnDelete;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    private String activityType = null;

    public static final String ADD_STATUS = "add_status";
    public static final String MODIFY_STATUS = "modify_status";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportActionBar().setElevation(0);

        init();
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        activityType = getIntent().getStringExtra(Constants.TYPE);
        if (activityType.equals(ADD_STATUS)) {
            status = new Status();
            binding.btnApply.setVisibility(View.GONE);

        }

        if (activityType.equals(MODIFY_STATUS)) {
            status = (Status) this.getIntent().getSerializableExtra(Constants.STATUS);

            binding.etName.setText(status.getName());
            binding.etIcon.setText(status.getIcon());
        }

        if (preferenceManager.getBoolean(Constants.STATUS)) {
            if (preferenceManager.getString(Constants.STATUS_ID).equals(status.getId())) {
                binding.btnApply.setText("UNSET");
            } else {
                binding.btnApply.setText("SET");
            }
        } else {
            binding.btnApply.setText("SET");
        }


        binding.btnApply.setOnClickListener(v -> {
            if (preferenceManager.getBoolean(Constants.STATUS)) { //Kiểm tra có status nào được set chưa
                if (preferenceManager.getString(Constants.STATUS_ID).equals(status.getId())) {
                    unSetStatus();
                } else {
                    setStatus();
                }
            } else { // Nếu chưa có status nào được xét
                setStatus();
            }

        });
    }

    private void setStatus() {
        preferenceManager.putBoolean(Constants.STATUS, true);
        preferenceManager.putString(Constants.STATUS_ID, status.getId());
        preferenceManager.putString(Constants.STATUS_NAME, status.getName());
        preferenceManager.putString(Constants.STATUS_ICON, status.getIcon());

        database.collection(Keys.KEY_COLLECTION_YOUR_STATUS)
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .whereEqualTo(Keys.KEY_STATUS, StatusStatus.ACTIVE)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        List<Task<Void>> taskList = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            Task<Void> t = documentSnapshot.getReference().update(Keys.KEY_STATUS, StatusStatus.INACTIVE);
                            taskList.add(t);
                        }

                        Tasks.whenAllComplete(taskList)
                                .addOnSuccessListener(tasks -> {
                                    database.collection(Keys.KEY_COLLECTION_YOUR_STATUS).document(status.getId())
                                            .update(Keys.KEY_STATUS, StatusStatus.ACTIVE)
                                            .addOnSuccessListener(unused -> {
                                                binding.btnApply.setText("UNSET");
                                                MyToast.showLongToast(this.getApplicationContext(), "Changed to this status");
                                            });
                                });
                    } else {
                        database.collection(Keys.KEY_COLLECTION_YOUR_STATUS).document(status.getId())
                                .update(Keys.KEY_STATUS, StatusStatus.ACTIVE)
                                .addOnSuccessListener(unused -> {
                                    binding.btnApply.setText("UNSET");
                                    MyToast.showLongToast(this.getApplicationContext(), "Changed to this status");
                                });
                    }
                });
    }

    private void unSetStatus() {
        preferenceManager.putBoolean(Constants.STATUS, false);
        preferenceManager.putString(Constants.STATUS_ID, null);
        preferenceManager.putString(Constants.STATUS_NAME, null);
        preferenceManager.putString(Constants.STATUS_ICON, null);

        database.collection(Keys.KEY_COLLECTION_YOUR_STATUS).document(status.getId())
                .update(Keys.KEY_STATUS, StatusStatus.INACTIVE)
                .addOnSuccessListener(unused -> {
                    binding.btnApply.setText("SET");
                    MyToast.showLongToast(this.getApplicationContext(), "Unsetted status");
                    //binding.btnApply.setText("REMOVE");
                });
        //preferenceManager.putString(Constants.STATUS_NAME);
    }

//    private void changeUI(Boolean isSet) {
//        if (isSet) {
//            binding.btnApply.setVisibility(View.GONE);
//            binding.btnRemove.setVisibility(View.VISIBLE);
//        } else {
//            binding.btnApply.setVisibility(View.VISIBLE);
//            binding.btnRemove.setVisibility(View.GONE);
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu_status_activity, menu);
        btnSave = menu.findItem(R.id.btnSaveStatus);
        btnDelete = menu.findItem(R.id.btnDeleteStatus);

        if (activityType.equals(ADD_STATUS)) {
            btnDelete.setVisible(false);
        } else {
            btnDelete.setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnSaveStatus:
                handleOnBtnSaveClicked();
                return true;

            case R.id.btnDeleteStatus:
                handleOnBtnDeleteClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isInformationValid() {
        boolean result = true;

        if (binding.etName.getText().toString().isEmpty()) {
            result = false;
        } else if (binding.etIcon.getText().toString().isEmpty()) {
            result = false;
        }

        if (!result) {
            MyToast.showLongToast(this.getApplicationContext(), "Please fill all the fields");
        }
//        if (Objects.requireNonNull(binding.etName.getEditText()).getText().toString().trim().isEmpty()) {
//            result = false;
//        }
//        if (Objects.requireNonNull(binding.etHour.getEditText()).getText().toString().trim().isEmpty()) {
//            result = false;
//        }
        return result;
    }

    private void handleOnBtnSaveClicked() {
        if (isInformationValid()) {

            if (activityType.equals(ADD_STATUS)) {
                HashMap<String, String> s = new HashMap<>();
                String statusId = FirebaseHelper.generateId(database, Keys.KEY_COLLECTION_YOUR_STATUS);
                s.put(Keys.KEY_ID, statusId);
                s.put(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
                s.put(Keys.KEY_STATUS, StatusStatus.MEW);
                s.put(Keys.KEY_NAME, binding.etName.getText().toString());
                s.put(Keys.KEY_ICON, binding.etIcon.getText().toString());
                s.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
                s.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
//            s.put(Keys.KEY_DUE_DATE, "");

                database.collection(Keys.KEY_COLLECTION_YOUR_STATUS)
                        .document(statusId)
                        .set(s)
                        .addOnSuccessListener(documentReference -> {
                            MyToast.showShortToast(this.getApplicationContext(), "Add status successfully");

                            this.status.setId(statusId);
                            this.status.setName(binding.etName.getText().toString());
                            this.status.setIcon(binding.etIcon.getText().toString());

                            activityType = MODIFY_STATUS;
                            binding.btnApply.setVisibility(View.VISIBLE);
                            binding.btnApply.setText("SET");

                        }).addOnFailureListener(e -> {
                            Timber.tag(this.getClass().getName()).e(e);
                        });


            } else {
                HashMap<String, Object> s = new HashMap<>();
                s.put(Keys.KEY_NAME, binding.etName.getText().toString());
                s.put(Keys.KEY_ICON, binding.etIcon.getText().toString());
                s.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
//            s.put(Keys.KEY_DUE_DATE, "");

                database.collection(Keys.KEY_COLLECTION_YOUR_STATUS)
                        .document(status.getId())
                        .update(s)
                        .addOnSuccessListener(documentReference -> {
                            MyToast.showShortToast(this.getApplicationContext(), "Update status successfully");

                            this.status.setId(status.getId());
                            this.status.setName(binding.etName.getText().toString());
                            this.status.setIcon(binding.etIcon.getText().toString());

                            if (preferenceManager.getBoolean(Constants.STATUS)) { //Nếu status này đang được sử dụng trong ứng dụng
                                if (preferenceManager.getString(Constants.STATUS_ID).equals(status.getId())) {
                                    preferenceManager.putString(Constants.STATUS_NAME, status.getName());
                                    preferenceManager.putString(Constants.STATUS_ICON, status.getIcon());
                                }
                            }

                        }).addOnFailureListener(e -> {
                            Timber.tag(this.getClass().getName()).e(e);
                        });


            }


        }
    }

    private void findStatus(String statusId, String userId) {
        database.collection(Keys.KEY_COLLECTION_YOUR_STATUS)
                .whereEqualTo(Keys.KEY_ID, statusId)
                .whereEqualTo(Keys.KEY_USER_ID, userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().getDocuments().isEmpty()) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            status.setId(documentSnapshot.getString(Keys.KEY_ID));
                            status.setName(documentSnapshot.getString(Keys.KEY_NAME));
                        }
                    } else {
                        Timber.tag(this.getClass().getName()).e(task.getException());
                    }
                });
    }

    private void handleOnBtnDeleteClicked() {
//        database.collection(Keys.KEY_COLLECTION_YOUR_STATUS)
//                .document(status.getId())
//                .delete()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        MyToast.showShortToast(this.getApplicationContext(), "Delete status successfully");
//                        finish();
//                    } else {
//                        Timber.tag(this.getClass().getName()).e(task.getException().toString());
//                    }
//                });

        if (preferenceManager.getBoolean(Constants.STATUS)) {
            if (preferenceManager.getString(Constants.STATUS_ID).equals(status.getId())) {

                preferenceManager.putBoolean(Constants.STATUS, false);
                preferenceManager.putString(Constants.STATUS_ID, null);
                preferenceManager.putString(Constants.STATUS_NAME, null);
                preferenceManager.putString(Constants.STATUS_ICON, null);
            }
        }

        database.collection(Keys.KEY_COLLECTION_YOUR_STATUS)
                .document(status.getId())
                .update(Keys.KEY_STATUS, StatusStatus.DELETED)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        MyToast.showLongToast(this.getApplicationContext(), "Delete status successfully");
                        finish();
                    } else {
                        Timber.tag(this.getClass().getName()).e(task.getException().toString());
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}