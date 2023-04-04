package com.example.loqui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.loqui.constants.MessageStatus;
import com.example.loqui.data.model.ChatMessage;
import com.example.loqui.databinding.DialogChatOptionBinding;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.PreferenceManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import timber.log.Timber;

public class DownloadOptionDialog extends BottomSheetDialogFragment {

    public enum Result {
        DOWNLOAD
    }

    public interface Listener {
        void sendDialogResult(DownloadOptionDialog.Result result, ChatMessage chatMessage);
    }

    View convertView = null;
    PreferenceManager preferenceManager = null;

    private Listener listener;
    private ChatMessage chatMessage;
//    private Context context;
//    private String request;
//    private Integer position;

    public DownloadOptionDialog(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
//        this.context = context;
//        chatMessage = request;
//        this.position = position;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (Listener) getParentFragmentManager();
        } catch (ClassCastException e) {
            Timber.tag(this.getClass().getName()).e(e);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        DialogChatOptionBinding binding = DialogChatOptionBinding.inflate(inflater);

        if (chatMessage.getStatus().equals(MessageStatus.DELETED)) {
            binding.btnRemove.setVisibility(View.GONE);
            binding.btnForward.setVisibility(View.GONE);
            binding.btnCopy.setVisibility(View.GONE);
        }

        if (!chatMessage.getUserId().equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
            binding.btnRemove.setVisibility(View.GONE);
        }

        return binding.getRoot();
    }
}
