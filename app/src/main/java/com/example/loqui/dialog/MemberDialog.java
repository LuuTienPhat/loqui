package com.example.loqui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.loqui.data.model.User;
import com.example.loqui.databinding.DialogMemberBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MemberDialog extends BottomSheetDialogFragment {

    public static String GROUP_ADMIN = "group_admin";
    public static String GROUP_MEMBER = "group_member";

    public enum Result {
        REMOVE, ADMIN, INFO
    }

    public interface Listener {
        void sendDialogResult(MemberDialog.Result result, User user);
    }

    //    private Context context;
    private Listener listener;
    private User user;
    private String request;

    //    public ExtraConversationDialog(Context context, ChatMessage chatMessage) {
//        this.context = context;
//        this.chatMessage = chatMessage;
//    }
    public MemberDialog(User user, String request) {
        this.user = user;
        this.request = request;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (MemberDialog.Listener) getParentFragment();
        } catch (ClassCastException e) {

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        DialogMemberBinding binding = DialogMemberBinding.inflate(inflater);

        if (request.equals(GROUP_ADMIN)) {
            binding.btnSetAdmin.setVisibility(View.GONE);
        } else if (request.equals(GROUP_MEMBER)) {
            binding.btnSetAdmin.setVisibility(View.GONE);
            binding.btnRemove.setVisibility(View.GONE);
        }

        binding.btnRemove.setOnClickListener(v -> {
            listener.sendDialogResult(Result.REMOVE, user);
            dismiss();
        });

        binding.btnSetAdmin.setOnClickListener(v -> {
            listener.sendDialogResult(Result.ADMIN, user);
            dismiss();
        });

        binding.btnInfo.setOnClickListener(v -> {
            listener.sendDialogResult(Result.INFO, user);
            dismiss();
        });


        return binding.getRoot();
    }
}
