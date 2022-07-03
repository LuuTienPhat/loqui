package com.example.loqui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.loqui.CallFragment;
import com.example.loqui.ChatFragment;
import com.example.loqui.PeopleFragment;
import com.example.loqui.ui.settings.SettingsFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: {
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;
            }
            case 1: {
                CallFragment callFragment = new CallFragment();
                return callFragment;
            }
            case 2: {
                PeopleFragment peopleFragment = new PeopleFragment();
                return peopleFragment;
            }
            default: {
                SettingsFragment settingsFragment = new SettingsFragment();
                return settingsFragment;
            }
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
