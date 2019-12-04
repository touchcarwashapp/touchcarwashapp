package com.touchcarwash_driver;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.touchcarwash_driver.db.UserDatabaseHandler;

public class FcmMessagingService extends FirebaseMessagingService {
    public long notitime = System.currentTimeMillis();
    public UserDatabaseHandler udb;

    public void onMessageReceived(RemoteMessage remoteMessage) {
    }
}
