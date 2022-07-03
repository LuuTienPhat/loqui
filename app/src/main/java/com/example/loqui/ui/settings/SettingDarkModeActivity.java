package com.example.loqui.ui.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.CompoundButton;

import com.example.loqui.R;
import com.example.loqui.databinding.ActivityMainBinding;
import com.example.loqui.databinding.ActivitySettingDarkModeBinding;

public class SettingDarkModeActivity extends AppCompatActivity {

    private ActivitySettingDarkModeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingDarkModeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.switchDarkMode.setOnCheckedChangeListener((compoundButton, checked) -> {
            
        });
    }
}