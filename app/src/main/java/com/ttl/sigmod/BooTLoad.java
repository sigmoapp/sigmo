package com.ttl.sigmod;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by lenovo on 28-08-2014.
 */
public class BooTLoad extends BroadcastReceiver {

    @Override



    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
            try{

                Intent intent1 =new Intent(context,com.ttl.sigmod.SigMoDataService.class);

                Calendar cal =Calendar.getInstance();

                PendingIntent pintent = PendingIntent.getService(context, 0, intent1, 0);
                AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

                alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 3600*1000, pintent);

                //Intent serviceIntent =new Intent(context,com.ttl.sigmod.SigMoDataService.class);

                //context.startService(serviceIntent);

            }catch(Exception e){
                Log.d("SigmoD", "Exception" + e);
            }


        }


    }

}
