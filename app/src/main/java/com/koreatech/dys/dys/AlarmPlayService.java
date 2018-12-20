package com.koreatech.dys.dys;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class AlarmPlayService extends Service {
    MediaPlayer mediaPlayer;
    int startId;
    boolean isRunning;

    public class AlarmBinder extends Binder {
        // 클라이언트가 호출할 수 있는 공개 메소드가 있는 현재 Service 객체 반환
        AlarmPlayService getService() {
            return AlarmPlayService.this;
        }
    }

    // 위에서 정의한 Binder 클래스의 객체 생성
    // Binder 클래스는 Interface인 IBinder를 구현한 클래스
    private final IBinder mBinder = new AlarmBinder();

    // Service 연결이 되었을 때 호출되는 메소드
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("AlarmService", "onBind()");

        // 위에서 생성한 LocalBinder 객체를 반환
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String getState = intent.getExtras().getString("state");

        assert getState != null;
        switch (getState) {
            case "alarm on":
                startId = 1;
                break;
            case "alarm off":
                startId = 0;
                break;
            default:
                startId = 0;
                break;
        }

        // 알람음 재생 X , 알람음 시작 클릭
        if(!this.isRunning && startId == 1) {
            mediaPlayer = MediaPlayer.create(this,R.raw.alarm);
            mediaPlayer.start();
            mediaPlayer.setLooping(true);
            set_notification();
            this.isRunning = true;
            this.startId = 0;
        }

        // 알람음 재생 O , 알람음 종료 버튼 클릭
        else if(this.isRunning && startId == 0) {

            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();

            this.isRunning = false;
            this.startId = 0;
        }

        // 알람음 재생 X , 알람음 종료 버튼 클릭
        else if(!this.isRunning && startId == 0) {

            this.isRunning = false;
            this.startId = 0;

        }

        // 알람음 재생 O , 알람음 시작 버튼 클릭
        else if(this.isRunning && startId == 1){

            this.isRunning = true;
            this.startId = 1;
        }

        else {
        }
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            stopForeground(true);
        }
        Log.d("onDestory() 실행", "서비스 파괴");

    }
    private void set_notification()
    {
        Intent intent = new Intent(this, AlarmActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "default";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("DYS")
                    .setContentText("알람음이 재생됩니다.")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pIntent)
                    .build();

            startForeground(1, notification);
        }
    }
}
