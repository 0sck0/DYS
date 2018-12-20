package com.koreatech.dys.dys;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class AlarmActivity extends AppCompatActivity {

    private String get_yout_string;
    private Button fin;
    Intent service_intent;


    AlarmPlayService mService;
    boolean mBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {

        // Service에 연결(bound)되었을 때 호출되는 callback 메소드
        // Service의 onBind() 메소드에서 반환한 IBinder 객체를 받음 (두번째 인자)
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AlarmPlayService.AlarmBinder binder = (AlarmPlayService.AlarmBinder)service;
            mService = binder.getService();

            mBound = true;
        }

        // Service 연결 해제되었을 때 호출되는 callback 메소드
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("MainActivity", "onServiceDisconnected()");

            mBound = false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        Intent intent = getIntent();
        Intent bindintent = new Intent(this, AlarmPlayService.class);

        // Service에 연결하기 위해 bindService 호출, 생성한 intent 객체와 구현한 ServiceConnection의 객체를 전달
        // boolean bindService(Intent service, ServiceConnection conn, int flags)
        bindService(bindintent, mConnection, Context.BIND_AUTO_CREATE);
        service_intent = new Intent(getApplicationContext(), AlarmPlayService.class);
        fin = (Button)findViewById(R.id.btn_finish);
        fin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(getApplicationContext(), AlarmPlayService.class));
                mService.stopForeground(true);
                finish();
            }
        });

        service_intent.putExtra("state", get_yout_string);
    }


}
