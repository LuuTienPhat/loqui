package com.example.loqui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loqui.databinding.ActivitySwitchAccountBinding;

public class SwitchAccountActivity extends AppCompatActivity {

    private ActivitySwitchAccountBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySwitchAccountBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_switch_account);
    }
}