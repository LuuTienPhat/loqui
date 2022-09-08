package com.example.loqui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loqui.databinding.ActivityChangePasswordBinding;
import com.example.loqui.utils.FirebaseHelper;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this.getApplicationContext());

        binding.btnChangePassword.setOnClickListener(v -> {
            if (isValidInformation()) {
                FirebaseHelper.findUser(database, preferenceManager.getString(Keys.KEY_USER_ID))
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                queryDocumentSnapshots.getDocuments().get(0).getReference()
                                        .update(Keys.KEY_PASSWORD, binding.etNewPassword.getText().toString().trim())
                                        .addOnSuccessListener(unused -> {
                                            finish();
                                            MyToast.showLongToast(getApplicationContext(), "Update password successfully");
                                        });

                            } else {
                                MyToast.showLongToast(getApplicationContext(), "Cannot change password");
                            }
                        });
            }
        });
    }


    private Boolean isValidInformation() {
        Boolean result = true;

        if (binding.etOldPassword.getText().toString().trim().isEmpty()) {
            MyToast.showLongToast(getApplicationContext(), "Enter old password");
//            Toasty.warning(getApplicationContext(), "Enter username", Toasty.LENGTH_SHORT).show();
            return false;
        }
        if (binding.etNewPassword.getText().toString().trim().isEmpty()) {
            MyToast.showLongToast(getApplicationContext(), "Enter new password");
//            Toasty.warning(getApplicationContext(), "Enter password", Toasty.LENGTH_SHORT).show();
            return false;
        }
        if (binding.etConfirm.getText().toString().trim().isEmpty()) {
            MyToast.showLongToast(getApplicationContext(), "Confirm your new password");
//            Toasty.warning(getApplicationContext(), "Confirm your password", Toasty.LENGTH_SHORT).show();
            return false;
        } else if (binding.etOldPassword.getText().toString().equals(binding.etNewPassword.getText().toString())) {
            MyToast.showLongToast(getApplicationContext(), "The new password must be different to old password");
            return false;
        } else if (!binding.etConfirm.getText().toString().equals(binding.etNewPassword.getText().toString())) {
//            Toasty.warning(getApplicationContext(), "Password & confirm password must be the same", Toasty.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

}