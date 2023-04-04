package com.example.loqui.activities.settings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.loqui.ChangePasswordActivity;
import com.example.loqui.databinding.ActivityMeBinding;
import com.example.loqui.dialog.CustomDialog;
import com.example.loqui.utils.FirebaseHelper;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import timber.log.Timber;


public class MeActivity extends AppCompatActivity implements CustomDialog.Listener {

    private ActivityMeBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String encodedImage;
    private static final int MY_REQUEST_CODE = 0;
    //    final String DATABASE_NAME = "tudien.db";
//    DatabaseAccess DB;
//    SQLiteDatabase database;
//    EditText tvHoten, tvEmail, tvSdt, tvUID;
//    TextView tvtaikhoan, tvTen;
//    Button btnCapNhat, btnChangePassword;
    //    LinearLayout btnSynchFromFirebase, btnSynchToFirebase;
    String iduser;
    //    User user;
    ProgressDialog progressDialog;
    StorageReference storageReference;
    //    private Main mMainActivity;
    private Uri mUri;
    private boolean changeimage = false;
//    private ImageView imageView;
//    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportActionBar().setElevation(0);

//        DB = DatabaseAccess.getInstance(getApplicationContext());
        //binding = ActivityMainBinding.inflate(getLayoutInflater());
        //setContentView(binding.getRoot());
//        AnhXa();

//        iduser = DB.iduser;
        // mMainActivity = new MainActivity() ;
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if(user == null){
//            return;
//        }
//        LayUser();

        init();
        setListeners();
//        LayImage();
//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onClickRequestPermission();
//
//            }
//        });
    }

    private void getUser() {

        FirebaseHelper.findUser(database, preferenceManager.getString(Keys.KEY_USER_ID))
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        queryDocumentSnapshots.getDocuments().get(0).getReference().get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(documentSnapshot.getString(Keys.KEY_AVATAR)));
                                    binding.etLastName.setText(documentSnapshot.getString(Keys.KEY_LASTNAME));
                                    binding.etFirstname.setText(documentSnapshot.getString(Keys.KEY_FIRSTNAME));
                                    binding.etPhone.setText(documentSnapshot.getString(Keys.KEY_PHONE));
                                    binding.etEmail.setText(documentSnapshot.getString(Keys.KEY_EMAIL));
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    MyToast.showShortToast(this.getApplicationContext(), e.getMessage());
                    Timber.d(e);
                });
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        getUser();
        encodedImage = preferenceManager.getString(Keys.KEY_AVATAR);
    }

    private void setListeners() {
        binding.ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

        binding.btnUpdate.setOnClickListener(v -> {
            loading(true);
            HashMap<String, Object> user = new HashMap<>();
            user.put(Keys.KEY_LASTNAME, binding.etLastName.getText().toString());
            user.put(Keys.KEY_FIRSTNAME, binding.etFirstname.getText().toString());
            user.put(Keys.KEY_EMAIL, binding.etEmail.getText().toString());
            user.put(Keys.KEY_PHONE, binding.etPhone.getText().toString());
            user.put(Keys.KEY_AVATAR, encodedImage);
            user.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

            FirebaseHelper.findUser(database, preferenceManager.getString(Keys.KEY_USER_ID))
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                            queryDocumentSnapshots.getDocuments().get(0).getReference()
                                    .update(user)
                                    .addOnSuccessListener(unused -> {
//                                        getUser();
                                        preferenceManager.putString(Keys.KEY_FIRSTNAME, binding.etLastName.getText().toString());
                                        preferenceManager.putString(Keys.KEY_LASTNAME, binding.etFirstname.getText().toString());
                                        preferenceManager.putString(Keys.KEY_EMAIL, binding.etEmail.getText().toString());
                                        preferenceManager.putString(Keys.KEY_PHONE, binding.etPhone.getText().toString());
                                        preferenceManager.putString(Keys.KEY_AVATAR, encodedImage);
                                        loading(false);
                                        MyToast.showShortToast(this.getApplicationContext(), "Updated Successfully");
                                    })
                                    .addOnFailureListener(e -> {
                                        loading(false);
                                        MyToast.showLongToast(this.getApplicationContext(), e.getMessage());
                                        Timber.d(e);
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        loading(false);
                        MyToast.showLongToast(this.getApplicationContext(), e.getMessage());
                        Timber.d(e);
                    });

        });

        binding.btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this.getApplicationContext(), ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) {
                Uri imageUri = result.getData().getData();
                try {
                    InputStream is = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    binding.ivAvatar.setImageBitmap(bitmap);
                    encodedImage = encodedImage(bitmap);
                } catch (FileNotFoundException ex) {
                    Timber.tag(getClass().getName()).d(ex);
                    ex.printStackTrace();
                }
            }
        }
    });

    private String encodedImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

//    private void CapNhatThongTin() {
//        String hoten = tvHoten.getText().toString();
//        String sdt = tvSdt.getText().toString();
//        if (hoten == "" || sdt == "") {
//            Toast.makeText(this, "Không hợp lệ", Toast.LENGTH_SHORT).show();
//        } else {
//            Boolean checkupdate = DB.capnhatthongtin(DB.iduser, hoten, sdt);
//            if (checkupdate == true) {
//                Toast.makeText(this, "Câp nhật thành công", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "Thất bại", Toast.LENGTH_SHORT).show();
//            }
//        }
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user == null) {
//            return;
//        }
//        String strfullname = hoten;
//
//        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
//                .setDisplayName(strfullname)
//                .build();
//        if (changeimage == true) {
//            profileUpdates = new UserProfileChangeRequest.Builder()
//                    .setPhotoUri(mUri)
//                    .build();
//            uploadImage();
//        }
//
//        user.updateProfile(profileUpdates)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            Toast.makeText(AccountInformationActivity.this, "Update Success", Toast.LENGTH_SHORT).show();
//                            //mMainActivity.showUserInformation();
//
//
//                        }
//                    }
//                });
//
//    }

//    private void uploadImage() {
//
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setTitle("Uploading File....");
//        progressDialog.show();
//
//
////        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CANADA);
////        Date now = new Date();
////        String fileName = formatter.format(now);
//        String fileName = user.getIduser();
//        storageReference = FirebaseStorage.getInstance().getReference("userimage/" + fileName);
//
//
//        storageReference.putFile(mUri)
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//                        //binding.firebaseimage.setImageURI(null);
//                        Toast.makeText(AccountInformationActivity.this, "Successfully Uploaded", Toast.LENGTH_SHORT).show();
//                        if (progressDialog.isShowing())
//                            progressDialog.dismiss();
//
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//
//                        if (progressDialog.isShowing())
//                            progressDialog.dismiss();
//                        Toast.makeText(AccountInformationActivity.this, "Failed to Upload", Toast.LENGTH_SHORT).show();
//
//
//                    }
//                });
//
//    }

//    public void setBitmapImageView(Bitmap bitmapImageView) {
//        imageView.setImageBitmap(bitmapImageView);
//    }

//    private void TruyenThongTin() {
//        //Truyền thông tin
//        tvHoten.setText(user.getHoTen());
////        tvTen.setText(user.getHoTen());
////        tvtaikhoan.setText(user.getEmail());
////        tvPoint.setText(String.valueOf(user.getPoint()));
//        tvEmail.setText(user.getEmail());
//        tvSdt.setText(user.getSDT());
//        tvUID.setText(user.getIduser());
//        imageView = findViewById(R.id.img_avatar);
//    }

//    public void LayUser() {
//        database = Database.initDatabase(AccountInformationActivity.this, DATABASE_NAME);
//        Cursor cursor = database.rawQuery("SELECT * FROM User WHERE ID_User = ?", new String[]{String.valueOf(DB.iduser)});
////        Cursor cursor = database.rawQuery("SELECT * FROM User WHERE ID_User = ?",new String[]{String.valueOf("UaceqeYAkxY2sGqZfWsUGeSxcRA2")});
//
//        if (cursor != null && cursor.moveToNext()) {
////            cursor.moveToNext();
//            String Iduser = cursor.getString(0);
//            String HoTen = cursor.getString(1);
//            int Point = cursor.getInt(2);
//            String Email = cursor.getString(3);
//            String SDT = cursor.getString(4);
//            user = new User(Iduser, HoTen, Point, Email, SDT);
////            Toast.makeText(this, Iduser, Toast.LENGTH_LONG).show();
//            //setUserInformation();
////        ThongTinTaikhoanActivity.context = getApplicationContext();
//
//            //Glide.with(context).load(user.getPhotoUrl()).error(R.drawable.ic_avatar_default).into(imageView);
//            TruyenThongTin();
//        } else {
//            Toast.makeText(this, "FAILLLL ", Toast.LENGTH_LONG).show();
//        }
//
//
//    }

//    public void LayImage() {
//        StorageReference storageRef =
//                FirebaseStorage.getInstance().getReference();
//        storageRef.child("userimage/" + user.getIduser()).getDownloadUrl()
//                .addOnSuccessListener(new OnSuccessListener<Uri>() {
//                    @Override
//                    public void onSuccess(Uri uri) {
//                        Glide.with(AccountInformationActivity.this).load(uri).error(R.drawable.ic_avatar_default).into(imageView);
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception exception) {
//                        // Handle any errors
//                        Toast.makeText(AccountInformationActivity.this, "Load image fail", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//    }

    //    private void setUserInformation() {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if(user == null){
//            return;
//        }
//        Glide.with(this).load(user.getPhotoUrl()).error(R.drawable.ic_avatar_default).into(imageView);
//
//    }
//    public void openGallery() {
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        mActivityResultLauncher.launch(Intent.createChooser(intent, "select picture"));
//    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void sendDialogResult(CustomDialog.Result result, String request) {
//        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
//        databaseAccess.open();
//
//        GlobalVariables.db = FirebaseFirestore.getInstance();
//        if (request.equalsIgnoreCase("download") && result == CustomDialog.Result.OK) {
//            GlobalVariables.db.collection("saved_word").whereEqualTo("user_id", GlobalVariables.userId).get()
//                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                            GlobalVariables.listSavedWordId.clear();
//                            for (DocumentSnapshot snapshot : task.getResult()) {
//                                long wordId1 = snapshot.getLong("word_id");
//                                int wordId = (int) wordId1;
//                                GlobalVariables.listSavedWordId.add(wordId);
//                            }
//
//                            databaseAccess.synchSavedWordToSQLite(GlobalVariables.userId, GlobalVariables.listSavedWordId);
//                            Toast.makeText(getApplicationContext(), "Thành công", Toast.LENGTH_LONG);
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(AccountInformationActivity.this, "Oops ... something went wrong", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//
//        }
//        if (request.equalsIgnoreCase("upload") && result == CustomDialog.Result.OK) {
//            databaseAccess.synchSavedWordToFirebase(GlobalVariables.userId);
//
//            Toast.makeText(getApplicationContext(), "Thành công", Toast.LENGTH_LONG);
//        }
//        databaseAccess.close();

    }

//    public void openDialog(String confirmFor) {
//        String content = "";
//        if (confirmFor.equalsIgnoreCase("upload")) {
//            content = "Bạn có chắc muốn tải lên từ đã lưu?";
//        }
//        if (confirmFor.equalsIgnoreCase("download")) {
//            content = "Bạn có chắc muốn tải xuống từ đã lưu?";
//        }
//        CustomDialog upload_downloadConfirmCustomDialog = new CustomDialog(CustomDialog.Type.CONFIRM, "Xác nhận", content, confirmFor);
//        upload_downloadConfirmCustomDialog.show(getSupportFragmentManager(), "upload_downloadConfirmCustomDialog");
//    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.btnUpdate.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.btnUpdate.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}