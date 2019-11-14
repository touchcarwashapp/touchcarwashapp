package com.touchcarwashadmin;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FcmMessagingService extends FirebaseMessagingService {
    public long notitime = System.currentTimeMillis();
    public UserDatabaseHandler udb;

    public void onMessageReceived(RemoteMessage remoteMessage) {
    }
}
