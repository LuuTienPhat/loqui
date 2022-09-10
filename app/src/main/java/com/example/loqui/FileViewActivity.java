package com.example.loqui;

import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loqui.constants.Constants;
import com.example.loqui.data.model.Attachment;
import com.example.loqui.databinding.ActivityFileViewBinding;
import com.example.loqui.utils.FileOpen;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class FileViewActivity extends AppCompatActivity {

    private ActivityFileViewBinding binding;
    private Attachment attachment;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFileViewBinding.inflate(getLayoutInflater());
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

        viewFile(attachment);
    }

    private void viewFile(Attachment attachment) {
        File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        try {
            File localFile = File.createTempFile(attachment.getName(), "." + attachment.getExtension(), downloadDirectory);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference httpsReference = storage.getReferenceFromUrl(attachment.getPath());
            httpsReference
                    .getFile(localFile)
                    .addOnSuccessListener(taskSnapshot -> {

                        MyToast.showLongToast(this, "downloaded to " + localFile.getAbsolutePath());

                        try {
                            FileOpen.openFile(this.getApplicationContext(), localFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

//                            try {
//                                //InputStreamReader inputStreamReader = new InputStreamReader(getContentResolver().openInputStream(uri));
//                                InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(localFile));
//                                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//                                StringBuilder sb = new StringBuilder();
//                                String s;
//                                while ((s = bufferedReader.readLine()) != null) {
//                                    sb.append(s);
//                                }
//                                String fileContent = sb.toString();
//
//                                binding.text.setText(fileContent);
//
//                            } catch (Exception ex) {
//                                MyToast.showLongToast(this, "error " + ex.getMessage());
//                            }


                    })
                    .addOnFailureListener(e -> {
                        MyToast.showLongToast(this, e.getMessage());
                    });

        } catch (IOException e) {
            MyToast.showLongToast(this, e.getMessage());
        }
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
        File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        try {
            File localFile = File.createTempFile(attachment.getName(), attachment.getExtension(), downloadDirectory);
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
}