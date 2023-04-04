package com.example.loqui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.loqui.R;
import com.example.loqui.databinding.DialogChatExtraBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import timber.log.Timber;

public class ExtraChatDialog extends BottomSheetDialogFragment {

    public enum Result {
        LOCATION, FILE, PHOTO, CAMERA
    }

    public interface Listener {
        //void sendLocation(LocationDetail locationDetail);
        void sendDialogResult(ExtraChatDialog.Result result);
    }

    View convertView = null;

    private Listener listener;
    private Context context;

    public ExtraChatDialog(Context context) {
        this.context = context;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (Listener) getActivity();
        } catch (ClassCastException e) {
            Timber.tag(this.getClass().getName()).e(e);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        DialogChatExtraBinding binding = DialogChatExtraBinding.inflate(inflater);

        binding.btnSendLocation.setOnClickListener(v -> {
//            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.context);
//            if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//                    && ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//
//                fusedLocationClient.getCurrentLocation(100, null)
//                        .addOnSuccessListener(requireActivity(), location -> {
//                            if (location != null) {
//                                // Logic to handle location object
//                                LocationDetail locationDetail = new LocationDetail();
//                                locationDetail.setLat(String.valueOf(location.getLatitude()));
//                                locationDetail.setLng(String.valueOf(location.getLongitude()));
////                                listener.sendLocation(locationDetail);
//                            }
//                        })
//                        .addOnFailureListener(e -> {
//                            Timber.tag(this.getClass().getName()).e(e);
////                            listener.sendLocation(null);
//                        });
//            }
            listener.sendDialogResult(Result.LOCATION);
            dismiss();
        });

        binding.btnCamera.setOnClickListener(v -> {
            listener.sendDialogResult(Result.CAMERA);
            dismiss();
//            Intent intent = new Intent(context.getApplicationContext(), CropActivity.class);
//            intent.putExtra("request", CropActivity.OPEN_CAMERA_CODE);
//            startActivity(intent);
        });

        binding.btnPhoto.setOnClickListener(v -> {
            listener.sendDialogResult(Result.PHOTO);
            dismiss();
//            Intent intent = new Intent(context.getApplicationContext(), CropActivity.class);
//            intent.putExtra("request", CropActivity.OPEN_GALLERY_CODE);
//            startActivity(intent);
        });

        binding.btnFile.setOnClickListener(v -> {
            listener.sendDialogResult(Result.FILE);
            dismiss();
//            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//            intent.setType("file/*");
//            requireActivity().startActivityForResult(intent, 998);

        });


        return binding.getRoot();
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }
}
