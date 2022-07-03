package com.example.loqui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.loqui.adapter.ViewPagerAdapter;
import com.example.loqui.data.model.LanguageManager;
import com.example.loqui.databinding.ActivityMainBinding;
import com.example.loqui.ui.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private LanguageManager languageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        languageManager = new LanguageManager(this);
        languageManager.updateResource(languageManager.getLanguage());
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        binding.viewPager2.setAdapter(viewPagerAdapter);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.chats_page:
                    binding.viewPager2.setCurrentItem(0);
                    Toast.makeText(this, "Chats page", Toast.LENGTH_LONG).show();
                    break;
                case R.id.calls_page:
                    binding.viewPager2.setCurrentItem(1);
                    Toast.makeText(this, "Calls page", Toast.LENGTH_LONG).show();
                    break;
                case R.id.people_page:
                    binding.viewPager2.setCurrentItem(2);
                    Toast.makeText(this, "People page", Toast.LENGTH_LONG).show();
                    break;
                case R.id.settings_page:
                    binding.viewPager2.setCurrentItem(3);
                    Toast.makeText(this, "Settings page", Toast.LENGTH_LONG).show();
                    break;
            }
            return true;
        });



//        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageSelected(int position) {
//                super.onPageSelected(position);
//                switch (position) {
//                    case 0:
//                        break;
//                    case 1:
//                        break;
//                    case 2:
//                        break;
//                    case 3:
//                        break;
//                }
//            }
//        });

//        binding.filledButton.setOnClickListener(view -> {
//            if (languageManager.getLanguage().equals("en")) {
//                languageManager.updateResource("vi");
//            } else {
//                languageManager.updateResource("en");
//            }
//            recreate();
//        });
    }
}