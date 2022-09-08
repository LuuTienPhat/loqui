package com.example.loqui.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loqui.AccountInformationActivity;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ItemContainerMemberBinding;
import com.example.loqui.utils.Utils;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private final List<User> users;
    //private Activity activity;
    private Fragment fragment;
    private MemberAdapter.Listener listener;

    public interface Listener {
        void sendDialogResult(MemberAdapter.Result result, User user);
    }

    public enum Result {
        LONG, SHORT
    }

    public MemberAdapter(Fragment fragment, List<User> users) {
        this.users = users;
        this.fragment = fragment;
        this.listener = (MemberAdapter.Listener) fragment;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerMemberBinding binding = ItemContainerMemberBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new MemberViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        holder.setData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {

        private ItemContainerMemberBinding binding;

        public MemberViewHolder(@NonNull ItemContainerMemberBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(User user) {
            binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(user.getImage()));
            binding.tvTextName.setText(user.getFullName());
            binding.btnOption.setOnClickListener(v -> {
                listener.sendDialogResult(MemberAdapter.Result.SHORT, user);
            });
        }
    }

}

