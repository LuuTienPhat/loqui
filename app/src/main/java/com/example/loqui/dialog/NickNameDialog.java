package com.example.loqui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.loqui.databinding.DialogNickNameBinding;

import timber.log.Timber;

public class NickNameDialog extends DialogFragment {
    public enum Result {
        CANCEL, SAVE, REMOVE
    }

    public interface Listener {
        void sendDialogResult(NickNameDialog.Result result, String request);
    }

    View convertView = null;

    private NickNameDialog.Listener listener;
    private Context context;
    private Integer position;

    public NickNameDialog(Context context, Integer position) {
        this.context = context;
        this.position = position;
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
        DialogNickNameBinding binding = DialogNickNameBinding.inflate(inflater);

        binding.btnSave.setOnClickListener(v -> {
            listener.sendDialogResult(Result.SAVE, "");
        });

        binding.btnCancel.setOnClickListener(v -> {
            listener.sendDialogResult(Result.CANCEL, "");
            dismiss();
        });

        binding.btnRemove.setOnClickListener(v -> {
            listener.sendDialogResult(NickNameDialog.Result.REMOVE, "");
        });

        return binding.getRoot();
    }
}
