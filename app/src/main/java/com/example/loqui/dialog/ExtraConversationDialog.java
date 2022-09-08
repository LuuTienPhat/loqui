package com.example.loqui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.loqui.data.model.ChatMessage;
import com.example.loqui.databinding.DialogConversationExtraBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ExtraConversationDialog extends BottomSheetDialogFragment {

    public enum Result {
        ARCHIVE, DELETE
    }

    public interface Listener {
        void sendDialogResult(ExtraConversationDialog.Result result, ChatMessage chatMessage);
    }

    //    private Context context;
    private Listener listener;
    private ChatMessage chatMessage;

    //    public ExtraConversationDialog(Context context, ChatMessage chatMessage) {
//        this.context = context;
//        this.chatMessage = chatMessage;
//    }
    public ExtraConversationDialog(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (ExtraConversationDialog.Listener) getParentFragment();
        } catch (ClassCastException e) {

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        DialogConversationExtraBinding binding = DialogConversationExtraBinding.inflate(inflater);

        binding.btnArchive.setOnClickListener(v -> {
            listener.sendDialogResult(Result.ARCHIVE, chatMessage);
            dismiss();
        });

        binding.btnDelete.setOnClickListener(v -> {
            listener.sendDialogResult(Result.DELETE, chatMessage);
            dismiss();
        });

        return binding.getRoot();
    }
}
