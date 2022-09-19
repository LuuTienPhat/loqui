package com.example.loqui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.loqui.FileViewActivity;
import com.example.loqui.ImageViewActivity;
import com.example.loqui.R;
import com.example.loqui.constants.Constants;
import com.example.loqui.constants.MessageStatus;
import com.example.loqui.constants.MessageType;
import com.example.loqui.data.model.Attachment;
import com.example.loqui.data.model.ChatMessage;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ItemContainerMessageStatusBinding;
import com.example.loqui.databinding.ItemContainerReceivedMessageBinding;
import com.example.loqui.databinding.ItemContainerSentMessageBinding;
import com.example.loqui.dialog.ChatOptionDialog;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public enum Result {
        AUDIO_CALL, VIDEO_CALL
    }

    public interface Listener {
        void sendDialogResult(ChatAdapter.Result result, ChatMessage chatMessage);
    }

    private final List<ChatMessage> chatMessages;
    private final List<User> receivers;
//    private final String senderId;
//    private final Bitmap receivedProfileImage;

    public static final int VIEW_TYPE_STATUS = 0;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

//    public static final int VIEW_TYPE_SENT_LOCATION = 3;
//    public static final int VIEW_TYPE_RECEIVED_LOCATION = 4;

    private final AppCompatActivity activity;
    private final PreferenceManager preferenceManager;
    private final ChatAdapter.Listener listener;
//    private final FragmentManager fragmentManager;


//    public ChatAdapter(List<ChatMessage> chatMessages, String senderId, Bitmap receivedProfileImage) {
//        this.chatMessages = chatMessages;
//        this.senderId = senderId;
//        this.receivedProfileImage = receivedProfileImage;
//    }

    public ChatAdapter(AppCompatActivity activity, List<ChatMessage> chatMessages, List<User> receivers, PreferenceManager preferenceManager, ChatAdapter.Listener listener) {
        this.chatMessages = chatMessages;
        this.activity = activity;
        this.receivers = receivers;
        this.preferenceManager = preferenceManager;
        this.listener = listener;
//        this.fragmentManager = fragmentManager;
//        this.senderId = senderId;
//        this.receivedProfileImage = receivedProfileImage;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder returnedViewHolder = null;
        if (viewType == VIEW_TYPE_SENT) {
            returnedViewHolder = new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(
                                    parent.getContext()),
                            parent,
                            false));
        } else if (viewType == VIEW_TYPE_RECEIVED) {
            returnedViewHolder = new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(
                                    parent.getContext()),
                            parent,
                            false));
        }
//        else if (viewType == VIEW_TYPE_SENT_LOCATION) {
//            returnedViewHolder = new SentMessageLocationViewHolder(
//                    activity,
//                    ItemContainerSentLocationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
//            );
//        } else if (viewType == VIEW_TYPE_RECEIVED_LOCATION) {
//            returnedViewHolder = new ReceivedMessageLocationViewHolder(
//                    activity,
//                    ItemContainerReceivedLocationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
//            );
//        }
        else if (viewType == VIEW_TYPE_STATUS) {
            returnedViewHolder = new MessageStatusViewHolder(
                    ItemContainerMessageStatusBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        }
        return returnedViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_STATUS) {
            ((MessageStatusViewHolder) holder).setData(chatMessages.get(position));
        } else {
            if (getItemViewType(position) == VIEW_TYPE_SENT) {
                ((SentMessageViewHolder) holder).setData(chatMessages.get(position), holder.itemView.getContext());

//            ((ReceivedMessageViewHolder) holder).binding.tvMessage.setOnLongClickListener(v -> {
//                ChatOptionDialog chatOptionDialog = new ChatOptionDialog(this.context, position);
//                chatOptionDialog.show(((AppCompatActivity) this.context).getSupportFragmentManager(), "");
//                return true;
//            });
            } else if (getItemViewType(position) == VIEW_TYPE_RECEIVED) {
                ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position));

//            ((ReceivedMessageViewHolder) holder).binding.tvMessage.setOnLongClickListener(v -> {
//                ChatOptionDialog chatOptionDialog = new ChatOptionDialog(this.context, position);
//                chatOptionDialog.show(((AppCompatActivity) this.context).getSupportFragmentManager(), "");
//                return true;
//            });
            }

//            if (getItemViewType(position) == VIEW_TYPE_SENT_LOCATION) {
//                ((SentMessageLocationViewHolder) holder).setData(chatMessages.get(position));
//
//            } else if (getItemViewType(position) == VIEW_TYPE_RECEIVED_LOCATION) {
//                ((ReceivedMessageLocationViewHolder) holder).setData(chatMessages.get(position));
//            }

            holder.itemView.setOnLongClickListener(v -> {
                ChatOptionDialog chatOptionDialog = new ChatOptionDialog(chatMessages.get(position));
                chatOptionDialog.show(activity.getSupportFragmentManager(), "");
                return true;
            });
        }

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = 0;
        PreferenceManager preferenceManager = new PreferenceManager(activity.getApplicationContext());
        String messageType = chatMessages.get(position).getType();

        if (messageType.equals(MessageType.STATUS)) {
            viewType = VIEW_TYPE_STATUS;
        } else {
            if (chatMessages.get(position).getUserId().equals(preferenceManager.getString(Keys.KEY_USER_ID))) {

//                if (messageType.equals(MessageType.LOCATION)) {
//                    viewType = VIEW_TYPE_SENT_LOCATION;
//                } else {
                viewType = VIEW_TYPE_SENT;
                //}

            } else {
//                if (messageType.equals(MessageType.LOCATION)) {
//                    viewType = VIEW_TYPE_RECEIVED_LOCATION;
//                } else {
                viewType = VIEW_TYPE_RECEIVED;
                //}
            }
        }
        return viewType;
    }

    public class SentMessageViewHolder extends RecyclerView.ViewHolder {

        final ItemContainerSentMessageBinding binding;


        public SentMessageViewHolder(ItemContainerSentMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        public void setData(ChatMessage chatMessage, Context context) {
            if (chatMessage.getStatus() != null && chatMessage.getStatus().equals(MessageStatus.DELETED)) {
                binding.tvMessage.setText("This message has been removed");
            } else {
                FirebaseFirestore database = FirebaseFirestore.getInstance();

                if (chatMessage.getReplyId() != null && !chatMessage.getReplyId().isEmpty()) {
                    binding.tvMessage.setBackground(activity.getDrawable(R.drawable.background_sent_message_reply));
                    database.collection(Keys.KEY_COLLECTION_CHAT)
                            .document(chatMessage.getReplyId())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                String message = documentSnapshot.getString(Keys.KEY_MESSAGE);
                                if (documentSnapshot.getString(Keys.KEY_STATUS).equals(MessageStatus.DELETED)) {
                                    binding.tvReplyMessage.setText("This message has been removed");
                                } else {
                                    binding.tvReplyMessage.setText(message);
                                }

                                if (documentSnapshot.getString(Keys.KEY_TYPE).equals(MessageType.MEDIA) || documentSnapshot.getString(Keys.KEY_TYPE).equals(MessageType.FILE)) {
                                    binding.tvReplyMessage.setText("@ Attachment");
                                }

                                if (documentSnapshot.getString(Keys.KEY_TYPE).equals(MessageType.AUDIO_CALL)) {
                                    binding.tvReplyMessage.setText("\uD83D\uDCDE Audio Call");
                                }

                                if (documentSnapshot.getString(Keys.KEY_TYPE).equals(MessageType.VIDEO_CALL)) {
                                    binding.tvReplyMessage.setText("\uD83D\uDCF9 Video Call");
                                }

                                binding.tvReplyMessage.setVisibility(View.VISIBLE);
                                binding.lyReplyMessage.setVisibility(View.VISIBLE);

                                if (documentSnapshot.getString(Keys.KEY_USER_ID).equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
                                    binding.tvMessageReplyTo.setText("You reply message to yourself");
                                } else {
                                    database.collection(Keys.KEY_COLLECTION_USERS)
                                            .whereEqualTo(Keys.KEY_ID, documentSnapshot.getString(Keys.KEY_USER_ID))
                                            .get()
                                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                                DocumentSnapshot documentSnapshot1 = queryDocumentSnapshots.getDocuments().get(0);
                                                String firstname = documentSnapshot1.getString(Keys.KEY_FIRSTNAME);
                                                binding.tvMessageReplyTo.setText("You reply message to " + firstname);
                                            });
                                }
                            });

                }
                if (chatMessage.getStatus().equals(MessageStatus.FORWARDED)) {
                    binding.lyReplyMessage.setVisibility(View.VISIBLE);
                    binding.tvReplyMessage.setVisibility(View.GONE);

                    if (chatMessage.getUserId().equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
                        binding.tvMessageReplyTo.setText("You forwarded a message");
                    }
                } else {
                    binding.lyReplyMessage.setVisibility(View.GONE);
                    binding.tvReplyMessage.setVisibility(View.GONE);
                }

                changeUI(chatMessage.getType());

                if (chatMessage.getType().equals(MessageType.MEDIA)) {
                    database.collection(Keys.KEY_COLLECTION_ATTS)
                            .whereEqualTo(Keys.KEY_MESSAGE_ID, chatMessage.getId())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {

                                List<String> media = new ArrayList<>();
                                if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                        media.add(documentSnapshot.getString(Keys.KEY_ATTACHMENT_ID));
                                    }
                                }
                                database.collection(Keys.KEY_COLLECTION_ATTACHMENT)
                                        .whereIn(Keys.KEY_ID, media)
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                            List<Attachment> attachments = new ArrayList<>();

                                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots1.getDocuments()) {
                                                Attachment attachment = new Attachment();
                                                attachment.setId(documentSnapshot.getString(Keys.KEY_ID));
                                                attachment.setName(documentSnapshot.getString(Keys.KEY_NAME));
                                                attachment.setExtension(documentSnapshot.getString(Keys.KEY_EXTENSION));
                                                attachment.setPath(documentSnapshot.getString(Keys.KEY_PATH));
                                                attachments.add(attachment);
                                            }

                                            binding.getRoot().setOnClickListener(v -> {
                                                Intent intent = new Intent(activity.getApplicationContext(), ImageViewActivity.class);
                                                intent.putExtra(Constants.ATTACHMENT, attachments.get(0));
                                                activity.startActivity(intent);
                                            });

                                            Glide.with(activity.getApplicationContext())
                                                    .load(attachments.get(0).getPath())
                                                    .fitCenter()
                                                    .centerInside()
                                                    .into(binding.ivImage);

                                        });

                            });

                }
                if (chatMessage.getType().equals(MessageType.LOCATION)) {
                    binding.getRoot().setOnClickListener(v -> {

                        String message = chatMessage.getMessage();
                        String[] coordinate = message.split(",");
                        String latitude = coordinate[0];
                        String longitude = coordinate[1];

                        String strUri = "http://maps.google.com/maps?q=loc:" + latitude + "," + longitude;
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
                        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                        activity.startActivity(intent);
                    });
                }
                if (chatMessage.getType().equals(MessageType.FILE)) {
                    database.collection(Keys.KEY_COLLECTION_ATTS)
                            .whereEqualTo(Keys.KEY_MESSAGE_ID, chatMessage.getId())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {

                                List<String> media = new ArrayList<>();
                                if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                        media.add(documentSnapshot.getString(Keys.KEY_ATTACHMENT_ID));
                                    }
                                }
                                database.collection(Keys.KEY_COLLECTION_ATTACHMENT)
                                        .whereIn(Keys.KEY_ID, media)
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                            List<Attachment> attachments = new ArrayList<>();

                                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots1.getDocuments()) {
                                                Attachment attachment = new Attachment();
                                                attachment.setId(documentSnapshot.getString(Keys.KEY_ID));
                                                attachment.setName(documentSnapshot.getString(Keys.KEY_NAME));
                                                attachment.setSize(documentSnapshot.getString(Keys.KEY_SIZE));
                                                attachment.setExtension(documentSnapshot.getString(Keys.KEY_EXTENSION));
                                                attachment.setPath(documentSnapshot.getString(Keys.KEY_PATH));
                                                attachments.add(attachment);
                                            }

                                            binding.tvFileName.setText(attachments.get(0).getFullName());
                                            binding.tvFileSize.setText(Utils.humanReadableByteCountSI(Long.parseLong(attachments.get(0).getSize())));

                                            binding.getRoot().setOnClickListener(v -> {
                                                Intent intent = new Intent(activity.getApplicationContext(), FileViewActivity.class);
                                                intent.putExtra(Constants.ATTACHMENT, attachments.get(0));
                                                activity.startActivity(intent);
                                            });

//                                            Glide.with(activity.getApplicationContext())
//                                                    .load(attachments.get(0).getPath())
//                                                    .fitCenter()
//                                                    .centerInside()
//                                                    .into(binding.ivImage);

                                        });

                            });
//                    Attachment attachment = chatMessage.getAttachment();
//                    binding.tvFileName.setText(attachment.getFullName());
//                    binding.tvFileSize.setText(attachment.getSize());

                }
                if (chatMessage.getType().equals(MessageType.AUDIO_CALL) || chatMessage.getType().equals(MessageType.VIDEO_CALL)) {
                    if (chatMessage.getType().equals(MessageType.AUDIO_CALL)) {
                        binding.ivCallType.setBackground(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_round_call_24));
                        binding.tvCallType.setText("Audio Call");

                        binding.getRoot().setOnClickListener(v -> {
                            listener.sendDialogResult(Result.AUDIO_CALL, chatMessage);
                        });


                    } else if (chatMessage.getType().equals(MessageType.VIDEO_CALL)) {
                        binding.ivCallType.setBackground(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_round_videocam_24));
                        binding.tvCallType.setText("Video Call");

                        binding.getRoot().setOnClickListener(v -> {
                            listener.sendDialogResult(Result.VIDEO_CALL, chatMessage);
                        });
                    }


//                    binding.getRoot().setOnClickListener(v -> {
//                        String message = chatMessage.getMessage();
//                        String[] coordinate = message.split(",");
//                        String latitude = coordinate[0];
//                        String longitude = coordinate[1];
//
//                        String strUri = "http://maps.google.com/maps?q=loc:" + latitude + "," + longitude;
//                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
//                        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
//                        activity.startActivity(intent);
//                    });

                } else {
                    binding.tvMessage.setText(chatMessage.getMessage());
                }

            }
            binding.tvDateTime.setText(Utils.getReadableDateTime(chatMessage.getCreatedDate()));
        }

        private void changeUI(String type) {
            if (type.equals(MessageType.MESSAGE)) {
                binding.tvMessage.setVisibility(View.VISIBLE);
                binding.ivImage.setVisibility(View.GONE);
                binding.lyFile.setVisibility(View.GONE);
                binding.lyLocation.setVisibility(View.GONE);
                binding.lyCall.setVisibility(View.GONE);
            } else if (type.equals(MessageType.FILE)) {
                binding.tvMessage.setVisibility(View.GONE);
                binding.ivImage.setVisibility(View.GONE);
                binding.lyFile.setVisibility(View.VISIBLE);
                binding.lyLocation.setVisibility(View.GONE);
                binding.lyCall.setVisibility(View.GONE);
            } else if (type.equals(MessageType.MEDIA)) {
                binding.tvMessage.setVisibility(View.GONE);
                binding.ivImage.setVisibility(View.VISIBLE);
                binding.lyFile.setVisibility(View.GONE);
                binding.lyLocation.setVisibility(View.GONE);
                binding.lyCall.setVisibility(View.GONE);
            } else if (type.equals(MessageType.LOCATION)) {
                binding.tvMessage.setVisibility(View.GONE);
                binding.ivImage.setVisibility(View.GONE);
                binding.lyFile.setVisibility(View.GONE);
                binding.lyLocation.setVisibility(View.VISIBLE);
                binding.lyCall.setVisibility(View.GONE);
            } else if (type.equals(MessageType.AUDIO_CALL) || type.equals(MessageType.VIDEO_CALL)) {
                binding.tvMessage.setVisibility(View.GONE);
                binding.ivImage.setVisibility(View.GONE);
                binding.lyFile.setVisibility(View.GONE);
                binding.lyLocation.setVisibility(View.GONE);
                binding.lyCall.setVisibility(View.VISIBLE);
            }
        }
    }

    public class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(ChatMessage chatMessage) {
            User user = findReceiver(chatMessage.getUserId());
            FirebaseFirestore database = FirebaseFirestore.getInstance();

            if (chatMessage.getStatus() != null && chatMessage.getStatus().equals(MessageStatus.DELETED)) {
//                changeUI(chatMessage.getType());
                binding.tvMessage.setText("This message has been removed");
            } else {
                if (chatMessage.getReplyId() != null && !chatMessage.getReplyId().isEmpty()) {
                    binding.tvMessage.setBackground(activity.getDrawable(R.drawable.background_received_message_reply));
                    database.collection(Keys.KEY_COLLECTION_CHAT)
                            .document(chatMessage.getReplyId())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                String message = documentSnapshot.getString(Keys.KEY_MESSAGE);
                                if (documentSnapshot.getString(Keys.KEY_STATUS).equals(MessageStatus.DELETED)) {
                                    binding.tvReplyMessage.setText("This message has been removed");
                                } else {
                                    binding.tvReplyMessage.setText(message);
                                }

                                if (documentSnapshot.getString(Keys.KEY_TYPE).equals(MessageType.MEDIA) || documentSnapshot.getString(Keys.KEY_TYPE).equals(MessageType.FILE)) {
                                    binding.tvReplyMessage.setText("@ Attachment");
                                }

                                if (documentSnapshot.getString(Keys.KEY_TYPE).equals(MessageType.AUDIO_CALL)) {
                                    binding.tvReplyMessage.setText("\uD83D\uDCDE Audio Call");
                                }

                                if (documentSnapshot.getString(Keys.KEY_TYPE).equals(MessageType.VIDEO_CALL)) {
                                    binding.tvReplyMessage.setText("\uD83D\uDCF9 Video Call");
                                }

                                binding.tvReplyMessage.setVisibility(View.VISIBLE);
                                binding.lyReplyMessage.setVisibility(View.VISIBLE);

                                if (documentSnapshot.getString(Keys.KEY_USER_ID).equals(user.getId())) {
                                    binding.tvMessageReplyTo.setText(user.getFirstName() + " reply message to themself");
                                } else {
                                    database.collection(Keys.KEY_COLLECTION_USERS)
                                            .whereEqualTo(Keys.KEY_ID, documentSnapshot.getString(Keys.KEY_USER_ID))
                                            .get()
                                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                                DocumentSnapshot documentSnapshot1 = queryDocumentSnapshots.getDocuments().get(0);
                                                String firstname = documentSnapshot1.getString(Keys.KEY_FIRSTNAME);
                                                if (documentSnapshot1.getString(Keys.KEY_ID).equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
                                                    binding.tvMessageReplyTo.setText(user.getFirstName() + " reply message to you");
                                                } else {
                                                    binding.tvMessageReplyTo.setText(user.getFirstName() + " reply message to " + firstname);
                                                }

                                            });
                                }
                            });
                }
                if (chatMessage.getStatus().equals(MessageStatus.FORWARDED)) {
                    binding.lyReplyMessage.setVisibility(View.VISIBLE);
                    binding.tvReplyMessage.setVisibility(View.GONE);

                    database.collection(Keys.KEY_COLLECTION_USERS)
                            .whereEqualTo(Keys.KEY_ID, chatMessage.getUserId())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                DocumentSnapshot documentSnapshot1 = queryDocumentSnapshots.getDocuments().get(0);
                                String firstname = documentSnapshot1.getString(Keys.KEY_FIRSTNAME);
                                if (documentSnapshot1.getString(Keys.KEY_ID).equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
                                    binding.tvMessageReplyTo.setText(user.getFirstName() + " forwarded a message");
                                }
                            });

                } else {
                    binding.lyReplyMessage.setVisibility(View.GONE);
                    binding.tvReplyMessage.setVisibility(View.GONE);
                }

                changeUI(chatMessage.getType());
                if (chatMessage.getType().equals(MessageType.MEDIA)) {
                    database.collection(Keys.KEY_COLLECTION_ATTS)
                            .whereEqualTo(Keys.KEY_MESSAGE_ID, chatMessage.getId())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {

                                List<String> media = new ArrayList<>();
                                if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                        media.add(documentSnapshot.getString(Keys.KEY_ATTACHMENT_ID));
                                    }
                                }
                                database.collection(Keys.KEY_COLLECTION_ATTACHMENT)
                                        .whereIn(Keys.KEY_ID, media)
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                            List<Attachment> attachments = new ArrayList<>();

                                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots1.getDocuments()) {
                                                Attachment attachment = new Attachment();
                                                attachment.setId(documentSnapshot.getString(Keys.KEY_ID));
                                                attachment.setName(documentSnapshot.getString(Keys.KEY_NAME));
                                                attachment.setExtension(documentSnapshot.getString(Keys.KEY_EXTENSION));
                                                attachment.setPath(documentSnapshot.getString(Keys.KEY_PATH));
                                                attachments.add(attachment);
                                            }

                                            Glide.with(activity.getApplicationContext())
                                                    .load(attachments.get(0).getPath())
                                                    .fitCenter()
                                                    .centerInside()
                                                    .into(binding.ivImage);

                                            binding.getRoot().setOnClickListener(v -> {
                                                Intent intent = new Intent(activity.getApplicationContext(), ImageViewActivity.class);
                                                intent.putExtra(Constants.ATTACHMENT, attachments.get(0));
                                                activity.startActivity(intent);
                                            });

                                        });

                            });

                }
                if (chatMessage.getType().equals(MessageType.FILE)) {
                    database.collection(Keys.KEY_COLLECTION_ATTS)
                            .whereEqualTo(Keys.KEY_MESSAGE_ID, chatMessage.getId())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {

                                List<String> media = new ArrayList<>();
                                if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                        media.add(documentSnapshot.getString(Keys.KEY_ATTACHMENT_ID));
                                    }
                                }
                                database.collection(Keys.KEY_COLLECTION_ATTACHMENT)
                                        .whereIn(Keys.KEY_ID, media)
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                            List<Attachment> attachments = new ArrayList<>();

                                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots1.getDocuments()) {
                                                Attachment attachment = new Attachment();
                                                attachment.setId(documentSnapshot.getString(Keys.KEY_ID));
                                                attachment.setName(documentSnapshot.getString(Keys.KEY_NAME));
                                                attachment.setSize(documentSnapshot.getString(Keys.KEY_SIZE));
                                                attachment.setExtension(documentSnapshot.getString(Keys.KEY_EXTENSION));
                                                attachment.setPath(documentSnapshot.getString(Keys.KEY_PATH));
                                                attachments.add(attachment);
                                            }

                                            binding.tvFileName.setText(attachments.get(0).getFullName());
                                            binding.tvFileSize.setText(Utils.humanReadableByteCountSI(Long.parseLong(attachments.get(0).getSize())));

//                                            binding.getRoot().setOnClickListener(v -> {
//                                                Intent intent = new Intent(activity.getApplicationContext(), ImageViewActivity.class);
//                                                intent.putExtra(Constants.ATTACHMENT, attachments.get(0));
//                                                activity.startActivity(intent);
//                                            });

//                                            Glide.with(activity.getApplicationContext())
//                                                    .load(attachments.get(0).getPath())
//                                                    .fitCenter()
//                                                    .centerInside()
//                                                    .into(binding.ivImage);

                                        });

                            });

//                    Attachment attachment = chatMessage.getAttachment();
//                    binding.tvFileName.setText(attachment.getFullName());
//                    binding.tvFileSize.setText(attachment.getSize());

                }
                if (chatMessage.getType().equals(MessageType.LOCATION)) {
                    binding.getRoot().setOnClickListener(v -> {
                        String message = chatMessage.getMessage();
                        String[] coordinate = message.split(",");
                        String latitude = coordinate[0];
                        String longitude = coordinate[1];

                        String strUri = "http://maps.google.com/maps?q=loc:" + latitude + "," + longitude;
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
                        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                        activity.startActivity(intent);
                    });

                }
                if (chatMessage.getType().equals(MessageType.AUDIO_CALL) || chatMessage.getType().equals(MessageType.VIDEO_CALL)) {
                    if (chatMessage.getType().equals(MessageType.AUDIO_CALL)) {
                        binding.ivCallType.setBackground(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_round_call_24));
                        binding.tvCallType.setText("Audio Call");

                        binding.getRoot().setOnClickListener(v -> {
                            listener.sendDialogResult(Result.AUDIO_CALL, chatMessage);
                        });

                    } else if (chatMessage.getType().equals(MessageType.VIDEO_CALL)) {
                        binding.ivCallType.setBackground(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_round_videocam_24));
                        binding.tvCallType.setText("Video Call");

                        binding.getRoot().setOnClickListener(v -> {
                            listener.sendDialogResult(Result.VIDEO_CALL, chatMessage);
                        });
                    }


//                    binding.getRoot().setOnClickListener(v -> {
//                        String message = chatMessage.getMessage();
//                        String[] coordinate = message.split(",");
//                        String latitude = coordinate[0];
//                        String longitude = coordinate[1];
//
//                        String strUri = "http://maps.google.com/maps?q=loc:" + latitude + "," + longitude;
//                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
//                        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
//                        activity.startActivity(intent);
//                    });

                } else {
                    binding.tvMessage.setText(chatMessage.getMessage());
                }
            }
            binding.imageProfile.setImageBitmap(Utils.getBitmapFromEncodedString(user.getImage()));
            binding.tvDateTime.setText(Utils.getReadableDateTime(chatMessage.getCreatedDate()));

        }

        private void changeUI(String type) {
            if (type.equals(MessageType.MESSAGE)) {
                binding.tvMessage.setVisibility(View.VISIBLE);
                binding.ivImage.setVisibility(View.GONE);
                binding.lyFile.setVisibility(View.GONE);
                binding.lyLocation.setVisibility(View.GONE);
                binding.lyCall.setVisibility(View.GONE);
            } else if (type.equals(MessageType.FILE)) {
                binding.tvMessage.setVisibility(View.GONE);
                binding.ivImage.setVisibility(View.GONE);
                binding.lyFile.setVisibility(View.VISIBLE);
                binding.lyLocation.setVisibility(View.GONE);
                binding.lyCall.setVisibility(View.GONE);
            } else if (type.equals(MessageType.MEDIA)) {
                binding.tvMessage.setVisibility(View.GONE);
                binding.ivImage.setVisibility(View.VISIBLE);
                binding.lyFile.setVisibility(View.GONE);
                binding.lyLocation.setVisibility(View.GONE);
                binding.lyCall.setVisibility(View.GONE);
            } else if (type.equals(MessageType.LOCATION)) {
                binding.tvMessage.setVisibility(View.GONE);
                binding.ivImage.setVisibility(View.GONE);
                binding.lyFile.setVisibility(View.GONE);
                binding.lyLocation.setVisibility(View.VISIBLE);
                binding.lyCall.setVisibility(View.GONE);
            } else if (type.equals(MessageType.AUDIO_CALL) || type.equals(MessageType.VIDEO_CALL)) {
                binding.tvMessage.setVisibility(View.GONE);
                binding.ivImage.setVisibility(View.GONE);
                binding.lyFile.setVisibility(View.GONE);
                binding.lyLocation.setVisibility(View.GONE);
                binding.lyCall.setVisibility(View.VISIBLE);
            }
        }

        private User findReceiver(String userId) {
            User result = null;
            for (User u : receivers) {
                if (u.getId().equals(userId)) {
                    result = u;
                    break;
                }
            }
            return result;
        }

    }

//    public static class SentMessageLocationViewHolder extends RecyclerView.ViewHolder {
//
//        final ItemContainerSentLocationBinding binding;
//        private final AppCompatActivity activity;
//
//
//        public SentMessageLocationViewHolder(AppCompatActivity activity, ItemContainerSentLocationBinding binding) {
//            super(binding.getRoot());
//            this.binding = binding;
//            this.activity = activity;
//        }
//
//        public void setData(ChatMessage chatMessage) {
//            binding.tvDateTime.setText(Utils.getReadableDateTime(chatMessage.getCreatedDate()));
//            binding.getRoot().setOnClickListener(v -> {
//
//                String message = chatMessage.getMessage();
//                String[] coordinate = message.split(",");
//                String latitude = coordinate[0];
//                String longitude = coordinate[1];
//
////                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
////                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
////                activity.startActivity(intent);
//
//                String strUri = "http://maps.google.com/maps?q=loc:" + latitude + "," + longitude;
//                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
//                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
//                activity.startActivity(intent);
//            });
//        }
//    }

//    static class ReceivedMessageLocationViewHolder extends RecyclerView.ViewHolder {
//        private final ItemContainerReceivedLocationBinding binding;
//        private final AppCompatActivity activity;
//
//        public ReceivedMessageLocationViewHolder(AppCompatActivity activity, ItemContainerReceivedLocationBinding binding) {
//            super(binding.getRoot());
//            this.binding = binding;
//            this.activity = activity;
//        }
//
//        public void setData(ChatMessage chatMessage) {
//            binding.tvDateTime.setText(Utils.getReadableDateTime(chatMessage.getCreatedDate()));
//            binding.getRoot().setOnClickListener(v -> {
//
//                String message = chatMessage.getMessage();
//                String[] coordinate = message.split(",");
//                String latitude = coordinate[0];
//                String longitude = coordinate[1];
//
//                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
//                activity.startActivity(intent);
//            });
//        }
//    }

//    public static class SentMediaLocationViewHolder extends RecyclerView.ViewHolder {
//
//        final ItemContainerSentLocationBinding binding;
//        private final AppCompatActivity activity;
//
//
//        public SentMediaLocationViewHolder(AppCompatActivity activity, ItemContainerSentLocationBinding binding) {
//            super(binding.getRoot());
//            this.binding = binding;
//            this.activity = activity;
//        }
//
//        public void setData(ChatMessage chatMessage) {
//            binding.tvDateTime.setText(Utils.getReadableDateTime(chatMessage.getCreatedDate()));
//            binding.getRoot().setOnClickListener(v -> {
//
//                String message = chatMessage.getMessage();
//                String[] coordinate = message.split(",");
//                String latitude = coordinate[0];
//                String longitude = coordinate[1];
//
////                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
////                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
////                activity.startActivity(intent);
//
//                String strUri = "http://maps.google.com/maps?q=loc:" + latitude + "," + longitude;
//                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
//                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
//                activity.startActivity(intent);
//            });
//        }
//    }

//    static class ReceivedMediaLocationViewHolder extends RecyclerView.ViewHolder {
//        private final ItemContainerReceivedMediaBinding binding;
//        private final AppCompatActivity activity;
//
//        public ReceivedMediaLocationViewHolder(AppCompatActivity activity, ItemContainerReceivedMediaBinding binding) {
//            super(binding.getRoot());
//            this.binding = binding;
//            this.activity = activity;
//        }
//
//        public void setData(ChatMessage chatMessage) {
////            binding.getRoot().setOnClickListener(v -> {
////
////                String message = chatMessage.getMessage();
////                String[] coordinate = message.split(",");
////                String latitude = coordinate[0];
////                String longitude = coordinate[1];
////
////                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
////                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
////                activity.startActivity(intent);
////            });
//        }
//    }

    public static class MessageStatusViewHolder extends RecyclerView.ViewHolder {

        final ItemContainerMessageStatusBinding binding;


        public MessageStatusViewHolder(ItemContainerMessageStatusBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(ChatMessage chatMessage) {
            binding.tvMessage.setText(chatMessage.getMessage());
        }
    }
}
