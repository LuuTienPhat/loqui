package com.example.loqui;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loqui.data.model.LanguageManager;
import com.example.loqui.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private LanguageManager languageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        languageManager = new LanguageManager(this);
        languageManager.updateResource(languageManager.getLanguage());
        super.onCreate(savedInstanceState);
//        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_main);

        Button filledButton = findViewById(R.id.filledButton);

        filledButton.setOnClickListener(view -> {
            if (languageManager.getLanguage().equals("en")) {
                languageManager.updateResource("vi");
            } else {
                languageManager.updateResource("en");
            }
            recreate();
        });
    }
}