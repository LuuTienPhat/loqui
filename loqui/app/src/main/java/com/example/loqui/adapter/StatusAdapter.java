package com.example.loqui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loqui.constants.Constants;
import com.example.loqui.data.model.Status;
import com.example.loqui.databinding.ItemContainerStatusBinding;
import com.example.loqui.listeners.StatusListener;
import com.example.loqui.utils.PreferenceManager;

import java.util.List;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {

    private final List<Status> statuses;
    private final StatusListener statusListener;
    private final PreferenceManager preferenceManager;


    public StatusAdapter(List<Status> statuses, StatusListener statusListener, PreferenceManager preferenceManager) {
        this.statuses = statuses;
        this.statusListener = statusListener;
        this.preferenceManager = preferenceManager;
    }


    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerStatusBinding itemContainerStatusBinding = ItemContainerStatusBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new StatusViewHolder(itemContainerStatusBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        holder.setStatusData(statuses.get(position));
    }

    @Override
    public int getItemCount() {
        return statuses.size();
    }

    class StatusViewHolder extends RecyclerView.ViewHolder {

        ItemContainerStatusBinding binding;

        public StatusViewHolder(@NonNull ItemContainerStatusBinding item) {
            super(item.getRoot());
            binding = item;
        }

        void setStatusData(Status status) {
            //binding.ivIcon.setImageBitmap(null);
            binding.tvIcon.setText(status.getIcon());
            binding.tvName.setText(status.getName());

            if (preferenceManager.getBoolean(Constants.STATUS)) {
                if (preferenceManager.getString(Constants.STATUS_ID).equals(status.getId())) {
                    binding.tvStatus.setText("Active");
                    binding.tvStatus.setVisibility(View.VISIBLE);
                } else {
                    binding.tvStatus.setVisibility(View.GONE);
                }
            } else {
                binding.tvStatus.setVisibility(View.GONE);
            }

            //binding.tvHour.setText(status.getHour());
            binding.getRoot().setOnClickListener(view -> statusListener.onStatusClicked(status));

        }
    }

//    private Bitmap getStatusImage(String encodedImage) {
//        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
//        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//    }
}
