package com.example.loqui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loqui.data.model.Room;
import com.example.loqui.databinding.ItemContainerUserCallBinding;

import java.util.List;

public class CallAdapter extends RecyclerView.Adapter<CallAdapter.UserViewHolder> {

    private final List<Room> rooms;
    private final Listener listener;
//    private Activity activity;

    public interface Listener {
        void sendDialogResult(Room room);
    }

    public CallAdapter(List<Room> rooms, CallAdapter.Listener listener) {
//        this.activity = activity;
        this.rooms = rooms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserCallBinding itemContainerUserCallBinding = ItemContainerUserCallBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(itemContainerUserCallBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(rooms.get(position));
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        ItemContainerUserCallBinding binding;

        public UserViewHolder(@NonNull ItemContainerUserCallBinding item) {
            super(item.getRoot());
            binding = item;
        }

        void setUserData(Room room) {
//            binding.ivAvatar.setImageBitmap(null);

        }
    }
}
