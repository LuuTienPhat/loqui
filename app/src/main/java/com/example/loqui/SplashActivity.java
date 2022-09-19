package com.example.loqui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loqui.utils.Keys;
import com.example.loqui.utils.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {
    //DatabaseAccess DB;

    //private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        //DB = DatabaseAccess.getInstance(getApplicationContext());
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                nextActivity();
            }
        }, 2000);
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        //database = FirebaseFirestore.getInstance();
    }


    private void nextActivity() {
        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (!preferenceManager.getBoolean(Keys.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

//        if (user == null) {
//            //Chưa login
//
//
//        } else {
//            DB.iduser = FirebaseAuth.getInstance().getCurrentUser().getUid();
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
////            Toast.makeText(getApplicationContext(),
////                    DB.iduser,
////                    Toast.LENGTH_LONG)
////                    .show();
//        }
        finish();
    }
}