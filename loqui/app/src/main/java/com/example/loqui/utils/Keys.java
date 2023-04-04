package com.example.loqui.utils;

import java.util.HashMap;

public class Keys {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_COLLECTION_FRIEND = "friend";
    public static final String KEY_COLLECTION_ATTS = "atts";
    public static final String KEY_COLLECTION_ATTACHMENT = "attachment";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "loquiPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_AVATAR = "avatar";

    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";

    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";

    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";

    public static final String KEY_AVAILABILITY = "availability";

    public static final String KEY_COLLECTION_ROOM = "room";

    public static final String KEY_COLLECTION_RECIPIENT = "recipient";
    public static final String KEY_ID = "id";
    public static final String KEY_STATUS = "status";
    public static final String KEY_CREATED_DATE = "created_date";
    public static final String KEY_MODIFIED_DATE = "modified_date";
    public static final String KEY_ROOM_ID = "room_id";

    public static final String KEY_COLLECTION_YOUR_STATUS = "your_status";
    public static final String KEY_ICON = "icon";
    public static final String KEY_HOUR = "hour";
    public static final String KEY_DUE_DATE = "due_date";

    public static final String KEY_COLLECTION_LOCATION = "location";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LNG = "lng";

    public static final String KEY_USERNAME = "username";
    public static final String KEY_FIRSTNAME = "firstname";
    public static final String KEY_LASTNAME = "lastname";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_DOCUMENT_REF = "document_ref";


    public static final String KEY_TYPE = "type";
    public static final String KEY_REPLY_ID = "reply_id";
    public static final String KEY_FRIEND_ID = "friend_id";

    public static final String KEY_END_DATE = "end_date";

    public static final String KEY_FACEBOOK_ID = "facebook_id";


    public static final String KEY_MESSAGE_ID = "message_id";
    public static final String KEY_ATTACHMENT_ID = "attachments_id";
    public static final String KEY_PATH = "path";
    public static final String KEY_SIZE = "size";
    public static final String KEY_EXTENSION = "extension";

    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";

    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";


    public static HashMap<String, String> remoteMsgHeaders = null;

    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAJFSC5Kc:APA91bH51Lap6gAqLmjNloWfvf4L1MAO9aiZmBakMcyctVZlQwyLkFfawVGHa6RpweikkPWM89RPp8w85hthciVmsroYxaobZy4YDQF-ZJ5ekK8qJc673bOjB63NnZbg0yj1DtI3lqCR"
            );
            remoteMsgHeaders.put(REMOTE_MSG_CONTENT_TYPE, "application/json");
        }
        return remoteMsgHeaders;
    }

    public static final String KEY_NOTIFICATION_TYPE = "notification_type";
    public static final String KEY_CALL_TYPE = "call_type";
    public static final String KEY_CALL_RESPONSE = "call_response";

    public static final String KEY_COLLECTION_SETTINGS = "settings";
    public static final String KEY_SETTING_DO_NOT_DISTURB = "setting_do_not_disturb";
    public static final String KEY_SETTING_MESSAGE_REQUEST = "setting_message_request";

    public static final String KEY_ADMIN_ID = "admin_id";
}
