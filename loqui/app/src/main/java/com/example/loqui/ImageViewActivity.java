package com.example.loqui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.loqui.constants.Constants;
import com.example.loqui.data.model.Attachment;
import com.example.loqui.databinding.ActivityImageViewBinding;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class ImageViewActivity  extends BaseActivity {

    private ActivityImageViewBinding binding;
    private Attachment attachment;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportActionBar().setElevation(0);

        init();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        attachment = (Attachment) this.getIntent().getSerializableExtra(Constants.ATTACHMENT);


        Glide.with(this.getApplicationContext())
                .asBitmap()
                .load(attachment.getPath())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        binding.ivImage.setImageBitmap(resource);
                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu_image_view_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.btnDownload) {
            downloadFile(attachment);
        }
        return super.onOptionsItemSelected(item);
    }


    private void downloadFile(Attachment attachment) {
        try {
            File localFile = File.createTempFile(attachment.getName(), attachment.getExtension());
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference httpsReference = storage.getReferenceFromUrl(attachment.getPath());
            httpsReference
                    .getFile(localFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        MyToast.showLongToast(this, "downloaded to " + localFile.getAbsolutePath());
                    })
                    .addOnFailureListener(e -> {
                        MyToast.showLongToast(this, e.getMessage());
                    });

        } catch (IOException e) {
            MyToast.showLongToast(this, e.getMessage());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}