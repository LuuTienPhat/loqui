package com.example.loqui.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.loqui.AccountInformationActivity;
import com.example.loqui.ChatActivity;
import com.example.loqui.IncomingCallActivity;
import com.example.loqui.R;
import com.example.loqui.constants.CallResponse;
import com.example.loqui.constants.Constants;
import com.example.loqui.constants.NotificationType;
import com.example.loqui.constants.Receiver;
import com.example.loqui.constants.RoomType;
import com.example.loqui.data.model.CallDetail;
import com.example.loqui.data.model.Room;
import com.example.loqui.data.model.User;
import com.example.loqui.network.ApiClient;
import com.example.loqui.network.ApiService;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Timber.tag("FCM").d("Token: %s", token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
//        Timber.tag("FCM").d("Message: %s", remoteMessage.getNotification().getBody());
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        String notificationType = remoteMessage.getData().get(Keys.KEY_NOTIFICATION_TYPE);
        if (notificationType.equals(NotificationType.MESSAGE) && !preferenceManager.getBoolean(Keys.KEY_SETTING_DO_NOT_DISTURB)) {
            handleMessageNotification(remoteMessage);
        }
        //
        else if (notificationType.equals(NotificationType.CALL)) {

            try {
                CallDetail call = new CallDetail();
                call.setId(remoteMessage.getData().get(Constants.CALL_ID));
                call.setCallType(remoteMessage.getData().get(Constants.CALL_TYPE));
                call.setCreatedDate(remoteMessage.getData().get(Keys.KEY_CREATED_DATE));

                User caller = new User();
                try {
                    JSONObject object = new JSONObject(remoteMessage.getData().get(Constants.CALLER));
                    caller.setId(object.getString(Keys.KEY_ID));
                    caller.setToken(object.getString(Keys.KEY_FCM_TOKEN));
                    call.setCaller(caller);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (remoteMessage.getData().get(Keys.KEY_ROOM_ID) != null) {
                    Room room = new Room();
                    room.setId(remoteMessage.getData().get(Keys.KEY_ROOM_ID));
                    room.setType(remoteMessage.getData().get(Constants.ROOM_TYPE));
                    call.setRoom(room);
                }

//            caller.setId(remoteMessage.getData().get(Keys.KEY_SENDER_ID));

//            JSONArray participants = new JSONArray();

                if (remoteMessage.getData().get(Keys.KEY_CALL_RESPONSE).equals(CallResponse.NEW_INVITATION)) {

                    Intent intent = new Intent(this, IncomingCallActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(Constants.CALL, (Serializable) call);
                    startActivity(intent);

                }

//            if (remoteMessage.getData().get(Keys.KEY_CALL_RESPONSE).equals(CallResponse.NEW_INVITATION)) {
//                if (!call.getCaller().getId().equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
//                    Intent intent = new Intent(this, IncomingCallActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    intent.putExtra(Constants.CALL, (Serializable) call);
//                    startActivity(intent);
//                }
//            }
                // Nếu người gọi nhận được RECEIVED (do người nhận gửi)
                if (remoteMessage.getData().get(Keys.KEY_CALL_RESPONSE).equals(CallResponse.RECEIVED_INVITATION)) {
                    //if (call.getParticipants().size() == 1) {
//                    if (IncomingCallActivity.incomingCallActivity != null) {
//                        IncomingCallActivity.incomingCallActivity.finish();
//                    }
                    Intent intent = new Intent(Receiver.RECEIVE_OUTGOING_CALL_ACTIVITY);
                    intent.putExtra(Keys.KEY_USER_ID, remoteMessage.getData().get(Keys.KEY_USER_ID));
                    sendBroadcast(intent);

                    // }
                }

                // Nếu người gọi nhận được DECLINE (do người nhận gửi)
                if (remoteMessage.getData().get(Keys.KEY_CALL_RESPONSE).equals(CallResponse.DECLINE_INVITATION)) {
                    //if (call.getParticipants().size() == 1) {
//                    if (IncomingCallActivity.incomingCallActivity != null) {
//                        IncomingCallActivity.incomingCallActivity.finish();
//                    }
                    if (call.getRoom().getType().equals(RoomType.CALL_TWO)) { // Nếu phòng chỉ có 2 người
                        sendBroadcast(new Intent(Receiver.CLOSE_OUTGOING_CALL_ACTIVITY));
                    } else { // Nếu phòng có nhiều người
                        Intent intent = new Intent(Receiver.JOIN_OUTGOING_CALL_ACTIVITY);
                        intent.putExtra(Keys.KEY_USER_ID, remoteMessage.getData().get(Keys.KEY_USER_ID));
                        sendBroadcast(intent);
                    }

                    // }
                }

                // Nếu người nhận cuộc gọi nhận được CANCEL (do người gọi gửi)
                if (remoteMessage.getData().get(Keys.KEY_CALL_RESPONSE).equals(CallResponse.CANCEL_INVITATION)) {
//                    if (IncomingCallActivity.incomingCallActivity != null) {
//                        IncomingCallActivity.incomingCallActivity.finish();
//                    }

                    sendBroadcast(new Intent(Receiver.CLOSE_INCOMING_CALL_ACTIVITY));

//                    if (call.getRoom().getType().equals(RoomType.TWO)) { // Nếu phòng chỉ có 2 người
//
//                    } else { // Nếu phòng có nhiều người
//
//                    }

                }
//
//
//                } else
                // Nếu người gọi nhận được ACCEPT (do người nhận gửi)
                if (remoteMessage.getData().get(Keys.KEY_CALL_RESPONSE).equals(CallResponse.ACCEPT_INVITATION)) {
//                Intent intent = new Intent(this, Jit)
//                if (!remoteMessage.getData().get(Keys.KEY_SENDER_ID).equals(call.getId())) {
//                Intent intent = new Intent(this, JitsiActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                intent.putExtra(Constants.CALL, (Serializable) call);
//                startActivity(intent);
                    sendBroadcast(new Intent(IncomingCallActivity.BROADCAST_OPEN_JITSI));
                    //}
                }
            } catch (Exception ex) {
                Timber.e(ex);
            }
//            }

        } else if (notificationType.equals(NotificationType.FRIEND_REQUEST)) { // Nhận thông báo lời mời kết bạn
            if (!preferenceManager.getBoolean(Keys.KEY_SETTING_DO_NOT_DISTURB)) {
                handleFriendRequestMessage(remoteMessage);
            }
        } else if (notificationType.equals(NotificationType.FRIEND_ACCEPT)) { // Nhận thông báo đã chấp nhận lời mời kết bạn
            if (!preferenceManager.getBoolean(Keys.KEY_SETTING_DO_NOT_DISTURB)) {
                handleFriendRequestMessage(remoteMessage);
            }
        }
    }

    private void handleMessageNotification(@NonNull RemoteMessage remoteMessage) {
        User user = new User();
        user.setId(remoteMessage.getData().get(Keys.KEY_USER_ID));
        user.setFirstName(remoteMessage.getData().get(Keys.KEY_FIRSTNAME));
        user.setLastName(remoteMessage.getData().get(Keys.KEY_LASTNAME));
        user.setToken(remoteMessage.getData().get(Keys.KEY_FCM_TOKEN));

        String roomId = remoteMessage.getData().get(Keys.KEY_ROOM_ID);

        int notificationId = new Random().nextInt();
        String channelId = "chat_message";

        Intent intent = new Intent(this.getApplicationContext(), ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //intent.putExtra(Keys.KEY_USER, user);
        intent.putExtra(Keys.KEY_ROOM_ID, roomId);
        intent.putExtra(Constants.OPEN_FROM_NOTIFICATION, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.getApplicationContext(), channelId);
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setContentTitle(user.getFullName());
        builder.setContentText(remoteMessage.getData().get(Keys.KEY_MESSAGE));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get(Keys.KEY_MESSAGE)));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Chat Message";
            String channelDescription = "This notification channel is used for chat message notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this.getApplicationContext());
        notificationManagerCompat.notify(notificationId, builder.build());
    }

    private void handleFriendRequestMessage(@NonNull RemoteMessage remoteMessage) {
        User user = new User();
        user.setId(remoteMessage.getData().get(Keys.KEY_USER_ID));
        user.setFirstName(remoteMessage.getData().get(Keys.KEY_FIRSTNAME));
        user.setLastName(remoteMessage.getData().get(Keys.KEY_LASTNAME));
        user.setToken(remoteMessage.getData().get(Keys.KEY_FCM_TOKEN));


        int notificationId = new Random().nextInt();
        String channelId = "friend_request";

        Intent intent = new Intent(this.getApplicationContext(), AccountInformationActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.OPEN_FROM_NOTIFICATION, true);
        intent.putExtra(Keys.KEY_USER, user);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.getApplicationContext(), channelId);
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setContentTitle(user.getFullName());
        builder.setContentText(remoteMessage.getData().get(Keys.KEY_MESSAGE));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get(Keys.KEY_MESSAGE)));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Friend Request";
            String channelDescription = "This notification channel is used for friend request notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notificationId, builder.build());
    }

    public static void sendNotification(Context context, String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Keys.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
                                //MyToast.showLongToast(context, error.getString("error"));
                                return;
                            }
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                        //MyToast.showLongToast(context, ex.getMessage());
                    }

                    //MyToast.showLongToast(context, "Notification sent successfully");
                } else {
                    //MyToast.showLongToast(context, "error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                //MyToast.showLongToast(context, "Error: " + t.getMessage());
            }
        });
    }

//    public static void callAlert(String message, final Context context) {
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
//        alertDialogBuilder.setTitle("MyApplication.");
//        alertDialogBuilder.setMessage(message);
//        alertDialogBuilder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
//
//            public void onClick(DialogInterface dialog, int which) {
//                ((Activity) context).finish();
//            } //end onClick.
//        }); // end alertDialog.setButton.
//        alertDialogBuilder.show();
//    }
}
