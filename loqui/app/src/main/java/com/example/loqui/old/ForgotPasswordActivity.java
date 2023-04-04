package com.example.loqui.old;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.loqui.R;
import com.example.loqui.SignInActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText EmailF;
    private Button btnResetP;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        EmailF = (EditText) findViewById(R.id.etEmailForgot);
        btnResetP = (Button) findViewById(R.id.btnResetPass);

        mAuth = FirebaseAuth.getInstance();

        btnResetP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        String email = EmailF.getText().toString().trim();

        if (email.isEmpty()) {
            EmailF.setError("Please type your email!");
            EmailF.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            EmailF.setError("Email is not right!");
            EmailF.requestFocus();
        }

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    //Toast.makeText(ForgotPasswordActivity.this, "Hãy kiểm tra (Hộp thư đến) của bạn để tiến hành thiết lập lại mật khẩu!", Toast.LENGTH_SHORT).show();
                    Toast.makeText(ForgotPasswordActivity.this, "Please check your inbox message!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Couldn't send email! Please check your email address", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}