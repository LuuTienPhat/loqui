package com.example.loqui.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.loqui.MediaFragment;
import com.example.loqui.constants.Constants;
import com.example.loqui.data.model.Attachment;
import com.example.loqui.data.model.Room;

import java.util.List;

public class AttachmentPagerAdapter extends FragmentStateAdapter {

    private List<Attachment> attachments;
    private Room room;

    public AttachmentPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    public AttachmentPagerAdapter(@NonNull FragmentActivity fragmentActivity, @NonNull Room room) {
        super(fragmentActivity);
        this.room = room;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.ROOM, room);
                bundle.putString(Constants.ATTACHMENT, Constants.MEDIA);
                MediaFragment mediaFragment = new MediaFragment();
                mediaFragment.setArguments(bundle);
                return mediaFragment;
            }
            default: {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.ROOM, room);
                bundle.putString(Constants.ATTACHMENT, Constants.FILE);
                MediaFragment mediaFragment = new MediaFragment();
                mediaFragment.setArguments(bundle);
//                FileFragment fileFragment = new FileFragment();
//                fileFragment.setArguments(bundle);
                return mediaFragment;
            }
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
