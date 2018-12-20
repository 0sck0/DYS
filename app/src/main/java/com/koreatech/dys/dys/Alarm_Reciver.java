package com.koreatech.dys.dys;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Alarm_Reciver extends BroadcastReceiver {
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        // intent로부터 전달받은 string
        String get_yout_string = intent.getExtras().getString("state");

        Intent service_intent = new Intent(context, AlarmPlayService.class);
        Intent service_activity = new Intent(context, AlarmActivity.class);
        service_activity.putExtra("state", get_yout_string);
        service_intent.putExtra("state", get_yout_string);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            this.context.startForegroundService(service_intent);
            if(get_yout_string.equals("alarm on"))
                this.context.startActivity(service_activity);

        }else{
            this.context.startService(service_intent);
            if(get_yout_string.equals("alarm on"))
                this.context.startActivity(service_activity);
        }
    }
}
