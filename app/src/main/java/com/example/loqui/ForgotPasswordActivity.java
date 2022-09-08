package com.example.loqui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loqui.databinding.ActivityForgotPasswordBinding;
import com.example.loqui.utils.FirebaseHelper;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText EmailF;
    private Button btnResetP;

    FirebaseAuth mAuth;

    private ActivityForgotPasswordBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

//        EmailF = (EditText) findViewById(R.id.etEmailForgot);
//        btnResetP = (Button) findViewById(R.id.btnResetPass);
//
//        mAuth = FirebaseAuth.getInstance();
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        binding.btnResetPass.setOnClickListener(v -> {
            String email = binding.etEmailForgot.toString();
            FirebaseHelper.findUser(database, preferenceManager.getString(Keys.KEY_USER_ID))
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                        }
                    });
        });

    }

//    private void resetPassword() {
//        String email = EmailF.getText().toString().trim();
//
//        if (email.isEmpty()) {
//            EmailF.setError("Please type your email!");
//            EmailF.requestFocus();
//            return;
//        }
//
//        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            EmailF.setError("Email is not right!");
//            EmailF.requestFocus();
//        }
//
//        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()) {
//                    //Toast.makeText(ForgotPasswordActivity.this, "Hãy kiểm tra (Hộp thư đến) của bạn để tiến hành thiết lập lại mật khẩu!", Toast.LENGTH_SHORT).show();
//                    Toast.makeText(ForgotPasswordActivity.this, "Please check your inbox message!", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
//                    startActivity(intent);
//                } else {
//                    Toast.makeText(ForgotPasswordActivity.this, "Couldn't send email! Please check your email address", Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        });
//    }
}