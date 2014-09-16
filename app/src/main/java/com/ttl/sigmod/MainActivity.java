package com.ttl.sigmod;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Telephony;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;


//import org.apache.commons.net.ftp.FTP;
//import org.apache.commons.net.ftp.FTPClient;





public class MainActivity extends Activity {




    MyPhoneStateListenercs myListenercs;
    private AutoUpdateApk aua;

    String DTAG="SigMoD";


    TextView pingText,statusText;
     String mLoacalIP="";
     String mMake="";
    String mModel="";
    String mState="";
    String mSignalS="";
    String mCallState="";
    String mIMSI="";
    String mDatNW="";
    String mDataState="";
    String mNetworkOp="";

    //StringBuffer csvData=new StringBuffer();

    String mIMEI="";
    String mAPN="";
    String mPhone="";
    int mCid;
    String mCellID;
    String mLac="";
    public String m_NL="";






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBasicInfo();
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        myListenercs = new MyPhoneStateListenercs();
        tm.listen(myListenercs, PhoneStateListener.LISTEN_CALL_STATE|PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);



        setContentView(R.layout.activity_main);



        pingText=(TextView)findViewById(R.id.textView1);
        statusText= (TextView)findViewById(R.id.textView2);



        File folder = new File(Environment.getExternalStorageDirectory() + "/sigmod");
        folder.mkdirs();

        //ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        try{
            writeTestFile(mIMSI);
        }catch(Exception e)
        {

        }

        LocationManager locationManager = null;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if( !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("GPS Status");  // GPS not found
            builder.setMessage("GPS is disabled. Please enable it"); // Want to enable?
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            //builder.setNegativeButton("No", null);
            builder.create().show();
            return;
        }

        try
        {
            Intent intent =new Intent(getApplicationContext(),com.ttl.sigmod.SigMoDataService.class);
            Calendar cal =Calendar.getInstance();
            PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
            AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

            try {
                alarm.cancel(pintent);
            } catch (Exception e) {
                Log.e(DTAG, "AlarmManager update was not canceled. " + e.toString());
            }

            alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 3600*1000, pintent);
            Log.d(DTAG," Alarm Set");

            intent.setAction("com.ttl.sigmod.SigMoDataService");
            startService(intent);
            Log.d(DTAG," Intent Started");
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "Alarm Setting Fail.", Toast.LENGTH_SHORT).show();

        }

        aua = new AutoUpdateApk(getApplicationContext());



        Toast.makeText(getApplicationContext(), "Tests are running in Background. Please do not close this window.", Toast.LENGTH_LONG).show();

    }







    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);


        return true;
    }


    public void getBasicInfo() {
        //Log.d(DTAG,"Inside getBasicInfo() start 1");
        mLoacalIP = getLocalIpAddress();

        mMake = Build.MANUFACTURER;
        mModel = Build.MODEL;
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mIMSI = tm.getSubscriberId();
        //Network Type
        switch (tm.getNetworkType()) {
            case 0:
                mDatNW = "UNKNOWN";
                break;
            case 1:
                mDatNW = "GPRS";
                break;
            case 2:
                mDatNW = "EDGE";
                break;
            case 3:
                mDatNW = "UMTS";
                break;
            case 4:
                mDatNW = "CDMA";
                break;
            case 5:
                mDatNW = "EVDO_0";
                break;
            case 6:
                mDatNW = "EVDO_A";
                break;
            case 8:
                mDatNW = "HSDPA";
                break;
            case 9:
                mDatNW = "HSUPA";
                break;
            case 10:
                mDatNW = "HSPA";
                break;
            case 13:
                mDatNW = "EVDO_B";
                break;
        }

        mDataState = null;
        switch (tm.getDataState()) {
            case 0:
                mDataState = "Disconnected";
                break;
            case 1:
                mDataState = "Setting up";
                break;
            case 2:
                mDataState = "Connected";
                break;
            case 3:
                mDataState = "Suspended";
                break;
        }
        mNetworkOp = tm.getNetworkOperator();
        mIMEI = tm.getDeviceId();

////////////////////APN Name/////////////////


        mAPN = "APN_NF";

        getDafaultAPN();
        ////////////// Cell ID//////////////////
        mPhone = null;
        switch (tm.getPhoneType()) {
            case 0:
                mPhone = "UNKNOWN";
                break;
            case 1:
                mPhone = "GSM";
                break;
            case 2:
                mPhone = "CDMA";
                break;
            case 3:
                mPhone = "SIP";
                break;
        }

        if (mPhone.equals("GSM")) {

            mCid = 0;
            //mLac =0;
            try {
                GsmCellLocation cellLocation = (GsmCellLocation) tm.getCellLocation();


                if (cellLocation != null) {
                    mCid = cellLocation.getCid();
                    mLac = "" + cellLocation.getLac();
                }

            } catch (Exception e) {
                Log.d(DTAG, "" + e);
            }
        }
            if (mPhone.equals("CDMA")) {

                mCid = 0;
                //mLac =0;
                try {
                    CdmaCellLocation cellLocation = (CdmaCellLocation) tm.getCellLocation();


                    if (cellLocation != null) {
                        mCid = cellLocation.getBaseStationId();
                        mLac = cellLocation.getSystemId() + "-" + cellLocation.getNetworkId() + "|" + cellLocation.getBaseStationLatitude() + "," + cellLocation.getBaseStationLongitude();
                    }

                } catch (Exception e) {
                    Log.d(DTAG, "" + e);
                }

                mCellID = mLac + mCid + "";
            }
            Log.d(DTAG, "Inside getBasicInfo() DataState=" + mDataState + "   CallState=" + mCallState);
        }


    private int getDafaultAPN() {
        int id = -1;
        try{
            mAPN= Telephony.Carriers.APN;

        }catch(Exception e)
        {

        }

        return id;

    }

    public String getLocalIpAddress() {
        String sAddr ="";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                //Log.d(DTAG,"Network Interface="+intf.getName());
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    sAddr = inetAddress.getHostAddress().toUpperCase();
                    boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);

                    if (!inetAddress.isLoopbackAddress()&& isIPv4) {

                        if (isIPv4) {
                            //Log.d(DTAG,"IPV4="+sAddr);
                            return sAddr;

                        }
                    }
                }
            }
        }catch (Exception ex) {
            Log.d(DTAG, ex.toString());
        }
        return "";
    }
    public boolean writeTestFile(String IMSI){


        FileOutputStream fos =null;
        File filename = new File(Environment.getExternalStorageDirectory() + "/sigmod/"+IMSI+ ".txt");
        if(filename.delete()){
            //Toast.makeText(getApplicationContext(), "Deleting Existing Test File", 2000).show();

        }


        String testString="0123456789";
        try {
//		File root = new File(Environment.getExternalStorageDirectory() + "/log.txt");
            //boolean f = root.createNewFile();
            fos = new FileOutputStream(filename, true);
            try {
                FileWriter fWriter = new FileWriter(fos.getFD());
                for(int i=0;i<10240;i++)
                {
                    fWriter.write(testString);
                }

                fWriter.close();
                //Toast.makeText(getApplicationContext(), "File write complete", 2000).show();;
            } catch (Exception e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), e+"", 2000).show();

            } finally {
                fos.getFD().sync();
                fos.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //Toast.makeText(getApplicationContext(), e+"", 2000).show();
            return false;
        }
        return true;
    }




    class MyPhoneStateListenercs extends PhoneStateListener{
        @Override
        public void onCallStateChanged(int state,String incomingNumber){
            super.onCallStateChanged(state, incomingNumber);

            switch(state){
                case TelephonyManager.CALL_STATE_IDLE:
                   // mCallState="IDLE";
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                   // mCallState="OFFHOOK";
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    //mCallState="RINGING";
                    break;

            }
        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength){
            super.onSignalStrengthsChanged(signalStrength);
//Get Signal Strength
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);


            if (signalStrength.isGsm())
            {
                mSignalS=String.valueOf(-113 + 2 * signalStrength.getGsmSignalStrength());

            }

            if(signalStrength.isGsm())
            {
                List <NeighboringCellInfo> mNL = tm.getNeighboringCellInfo();
                String stringNeighboring = "Neighboring List - Lac : Cid : Psc : type : RSSI\n";
                mCid =0;
                //mLac =0;
                try{
                    GsmCellLocation cellLocation = (GsmCellLocation)tm.getCellLocation();


                    if (cellLocation != null)
                    {
                        mCid =	cellLocation.getCid();
                        mLac =  ""+cellLocation.getLac();
                        mCellID=""+cellLocation.getCid();
                    }

                }catch(Exception e){
                    Log.d(DTAG,""+e);
                }

                stringNeighboring = "";

                for(int i = 0; i < mNL.size(); i++)
                {
                    String dBm;
                    int rssi = mNL.get(i).getRssi();
                    if(rssi == NeighboringCellInfo.UNKNOWN_RSSI)
                    {
                        dBm = "Unknown RSSI";
                    }
                    else
                    {
                        if(rssi >= 0 && rssi < 32)
                        {
                            dBm = String.valueOf(-113 + 2 * rssi) + " dBm";
                        }
                        else
                        {
                            dBm = "Unknown value:" + Integer.toString(rssi);
                        }
                    }

                    stringNeighboring = stringNeighboring
                            + String.valueOf(mNL.get(i).getLac()) + " - "
                            + String.valueOf(mNL.get(i).getCid()) + ":"
                            + String.valueOf(mNL.get(i).getPsc()) + ":"
                            + String.valueOf(mNL.get(i).getNetworkType()) + ":"
                            + dBm + " \n ";


                }
                String mDataActivity="";
                switch(tm.getDataActivity())
                {
                    case 0: mDataActivity="DATA_ACTIVITY_NONE"; break;
                    case 1: mDataActivity="DATA_ACTIVITY_IN";break;
                    case 2: mDataActivity="DATA_ACTIVITY_OUT";break;
                    case 3: mDataActivity="DATA_ACTIVITY_OUT ";break;
                    case 4: mDataActivity="DATA_ACTIVITY_DORMANT";break;

                }

                //DataCaptured=DataCaptured + stringNeighboring;
                m_NL=stringNeighboring;
                if(!SigMoDataService.testRunning){
                    statusText.setText("Neighbour List  :\n"+m_NL);
                }


                pingText.setText("Current Cell:   "+mLac+"-"+mCellID
                        +"\nSignal Strength:   "+mSignalS+"dBm"
                        +"\nLocalIP:   "+getLocalIpAddress()
                        +"\nIMSI:   "+mIMSI
                        +"\nModel:   "+mModel
                        +"\nData Network:   "+mDatNW+"\nData Activity:"+mDataActivity
                        +"\nSignal Quality: "+signalStrength.getGsmBitErrorRate()
                        +"\nCall State: "+mState);

            }

        }

    }


}
