package com.example.loqui.listeners;

import com.example.loqui.data.model.User;

public interface UserListener {
    void onUserClicked(User user);

    void onUserChecked(User user, boolean isChecked);

    void onUserRemoved(User user);
}
