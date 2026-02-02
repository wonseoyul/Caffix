package com.example.caffix;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // 푸시 알림을 수신할 때 호출됩니다.
        // remoteMessage를 통해 수신한 알림 데이터를 처리할 수 있습니다.
        // 예를 들어, 알림을 사용자에게 표시하거나 특정 작업을 수행할 수 있습니다.
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        // FCM 서버로부터 새 토큰을 받을 때 호출됩니다.
        // 토큰을 사용하여 알림을 보낼 대상을 식별하거나 서버에 등록할 수 있습니다.
    }
}