package com.example.loqui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.loqui.ImageViewActivity;
import com.example.loqui.constants.Constants;
import com.example.loqui.data.model.Attachment;
import com.example.loqui.databinding.ItemImageBinding;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private List<Attachment> attachments;
    private Context context;

    public ImageAdapter(List<Attachment> attachments, Context context) {
        this.attachments = attachments;
        this.context = context;
    }

    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemImageBinding binding = ItemImageBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ImageAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, int position) {
        holder.setData(attachments.get(position));
    }

    @Override
    public int getItemCount() {
        return attachments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ItemImageBinding binding;

        public ViewHolder(@NonNull ItemImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(Attachment attachment) {
            Glide
                    .with(context)
                    .load(attachment.getPath())
                    .into(binding.ivImage);

            binding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(context.getApplicationContext(), ImageViewActivity.class);
                intent.putExtra(Constants.ATTACHMENT, attachment);
                context.startActivity(intent);
            });
        }
    }
}
