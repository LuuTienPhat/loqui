package com.example.loqui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loqui.data.model.Recipient;
import com.example.loqui.databinding.ActivityNicknameBinding;
import com.example.loqui.dialog.NickNameDialog;
import com.example.loqui.utils.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NicknameActivity extends AppCompatActivity implements NickNameDialog.Listener {

    private ActivityNicknameBinding binding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private List<Recipient> recipients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNicknameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

        
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        recipients = new ArrayList<>();
    }

    private void load() {

    }


    @Override
    public void sendDialogResult(NickNameDialog.Result result, String request) {
        if (result.equals(NickNameDialog.Result.SAVE)) {

        }
        if (result.equals(NickNameDialog.Result.REMOVE)) {

        }
        if (result.equals(NickNameDialog.Result.CANCEL)) {

        }
    }
}