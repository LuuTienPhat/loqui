package com.example.loqui.adapter;

import android.app.Activity;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loqui.R;
import com.example.loqui.data.model.Attachment;
import com.example.loqui.databinding.ItemContainerFileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;

import timber.log.Timber;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private final List<Attachment> attachments;
    private Activity activity;

    public interface Listener {
        void sendDialogResult(Attachment Attachment);
    }

    public FileAdapter(Activity activity, List<Attachment> attachments) {
        this.activity = activity;
        this.attachments = attachments;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ItemContainerFileBinding binding = ItemContainerFileBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FileViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        holder.setUserData(attachments.get(position));
    }

    @Override
    public int getItemCount() {
        return attachments.size();
    }

    public class FileViewHolder extends RecyclerView.ViewHolder {

        private ItemContainerFileBinding binding;

        public FileViewHolder(@NonNull ItemContainerFileBinding item) {
            super(item.getRoot());
            binding = item;
        }

        void setUserData(Attachment attachment) {
            binding.tvFileName.setText(attachment.getFullName());
            binding.tvFileSize.setText(attachment.getSize());

            binding.getRoot().setOnClickListener(v -> {
                downloadFile(attachment);
            });

//            binding.btnDownload.setOnClickListener(v -> {
//                downloadFile(attachment);
//            });
        }

        private void downloadFile(Attachment attachment) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl(attachment.getPath());
            StorageReference islandRef = storageRef.child(attachment.getFullName());

            File rootPath = new File(Environment.getExternalStorageDirectory(), activity.getResources().getString(R.string.app_name));
            if (!rootPath.exists()) {
                rootPath.mkdirs();
            }

            final File localFile = new File(rootPath, attachment.getFullName());

            islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Timber.tag("firebase ").e(";local tem file created  created %s", localFile.toString());
                    //  updateDb(timestamp,localFile.toString(),position);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Timber.tag("firebase ").e(";local tem file not created  created %s", exception.toString());
                }
            });
        }
    }
}

