package com.example.loqui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loqui.data.model.Room;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ItemContainerUserForwardBinding;
import com.example.loqui.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class UsersForwardAdapter extends RecyclerView.Adapter<UsersForwardAdapter.UserForwardViewHolder> {

    private final List<Object> objects;
    private final UsersForwardAdapter.Listener listener;
    private List<TextView> textViews;

    public interface Listener {
        void sendDialogResult(Object object);
    }

    public UsersForwardAdapter(List<Object> objects, UsersForwardAdapter.Listener listener) {
        this.objects = objects;
        this.listener = listener;
        this.textViews = new ArrayList<>();
    }

    @NonNull
    @Override
    public UserForwardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserForwardBinding binding = ItemContainerUserForwardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserForwardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserForwardViewHolder holder, int position) {
        holder.setUserData(objects.get(position));
        textViews.add(holder.binding.btnSend);
    }

    public List<TextView> getTextViews() {
        return textViews;
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    class UserForwardViewHolder extends RecyclerView.ViewHolder {

        ItemContainerUserForwardBinding binding;

        public UserForwardViewHolder(@NonNull ItemContainerUserForwardBinding item) {
            super(item.getRoot());
            binding = item;
        }

        void setUserData(Object object) {
            if (object instanceof User) {
                User user = (User) object;
                binding.tvTextName.setText(user.getFullName());
                binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(user.getImage()));
            } else if (object instanceof Room) {
                Room room = (Room) object;
                binding.tvTextName.setText(room.getName());
                binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(room.getAvatar()));
            }

            binding.btnSend.setOnClickListener(v -> {
                listener.sendDialogResult(object);
            });

        }
    }
}
