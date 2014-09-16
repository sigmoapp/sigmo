package com.ttl.sigmod;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.TrafficStats;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Telephony;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;





public class SigMoDataService extends Service {

    MyPhoneStateListenercs myListenercs;

    boolean startThread=true;
    String DTAG="SigMoD_B";
    String Version="4_7";
    String datDisplay;
    int mCurrentHour=0;
    String mTotalRX="";
    String mTotalTX="";
    String mMobileRX="";
    String mMobileTX="";
    String mBatteryStat="";
    String mBatteryChargeStatus="";
    String mBatteryPlugtype="";
    String mBatteryTemp="";

    LocationManager lm;
    static int updateState=0;
    int hour_day ;




    static boolean testRunning=false;
    static boolean eventrunning=false;
    static int numberOfRecord=0;




    String mIPaddress="";
    String mLoacalIP="";
    long mDNSTime=0;
    long mHTTPTime=0;
    String mLatency="";
    String mMake="";
    String mModel="";

    String mSignalS="";
    String mRxqual="";
    String mCallState="IDLE";
    String mIMSI="";

    String mDatNW="";
    String mDataState="Connected";
    String mNetworkOp="";
    String mLat="";
    String mLong="";
    String mAccuracy="";
    String mAltitude="";


    LinkedList<String> urlList = new LinkedList<String>();
    //StringBuffer csvData=new StringBuffer();
    String csvData ="";
    int sampleTime=10;
    String mIMEI="";
    String mAPN="";
    String mPhone="";
    String mCid,mLac;
    String mCellID;
    public String m_NL="";


    public String ftpUrl2="ftp://203.196.128.55";

    public StringBuffer eventBuffer=new StringBuffer();


    float mUthpt=0;
    float mDthpt=0;


    public static  int singleThread=0;
    /*
     * Information of the preferred APN
     */


    public void getBasicInfo(){
        //Log.d(DTAG,"Inside getBasicInfo() start 1");

        mCurrentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        //Log.d(DTAG,"Inside getBasicInfo() battery info");
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        int level,scale,s,p,te;

        try{
             level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
             scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
             s = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
             p = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
             te= batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-1);

        }catch(Exception e){
            level=0;
            scale=0;
            s=0;
            p=0;
            te=0;
        }





        float batteryPct = level / (float)scale;

        mBatteryStat=batteryPct+"";

        mBatteryTemp=te+"";

        switch (s)
        {
            case 1:
                mBatteryChargeStatus="UNKNOWN";
                break;
            case 2:
                mBatteryChargeStatus="CHARGING";
                break;
            case 3:
                mBatteryChargeStatus="DISCHARGING";
                break;
            case 4:
                mBatteryChargeStatus="NOT_CHARGING";
                break;
            case 5:
                mBatteryChargeStatus="FULL";
                break;
            default:
                mBatteryChargeStatus="UNKNOWN";
        }

        switch(p)
        {

            case 1:
                mBatteryPlugtype="AC_CHARGER";
                break;
            case 2:
                mBatteryPlugtype="USB_CABLE";
                break;
            case 3:
                mBatteryPlugtype="WIRELESS";
                break;

        }

        mLoacalIP=getLocalIpAddress();
       // Log.d(DTAG,"Inside getBasicInfo() IP info");
        mMake = Build.MANUFACTURER;
        mModel = Build.MODEL;
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mIMSI=tm.getSubscriberId();
        //Network Type
        switch(tm.getNetworkType())
        {
            case 0: mDatNW ="UNKNOWN";break;
            case 1: mDatNW ="GPRS";break;
            case 2: mDatNW ="EDGE";break;
            case 3: mDatNW ="UMTS";break;
            case 4: mDatNW ="CDMA";break;
            case 5: mDatNW ="EVDO_0";break;
            case 6: mDatNW ="EVDO_A";break;
            case 8: mDatNW ="HSDPA";break;
            case 9: mDatNW ="HSUPA";break;
            case 10: mDatNW ="HSPA";break;
            case 13: mDatNW ="EVDO_B";break;
        }

        mDataState=null;
        switch(tm.getDataState())
        {
            case 0: mDataState="Disconnected"; break;
            case 1: mDataState="Setting up"; break;
            case 2: mDataState="Connected"; break;
            case 3: mDataState="Suspended"; break;
        }
        mNetworkOp=tm.getNetworkOperator();
        mIMEI = tm.getDeviceId();

        ////////////////////APN Name/////////////////


        mAPN="APN_NF";

        getDafaultAPN();

        ////////////// Cell ID//////////////////
        mPhone=null;
        switch(tm.getPhoneType())
        {
            case 0: mPhone="UNKNOWN"; break;
            case 1: mPhone="GSM"; break;
            case 2: mPhone="CDMA"; break;
            case 3: mPhone="SIP"; break;
        }

        if(mPhone.equals("GSM")){

            mCid ="";
            mLac ="";
            try{
                GsmCellLocation cellLocation = (GsmCellLocation)tm.getCellLocation();


                if (cellLocation != null)
                {
                    mCid =	""+cellLocation.getCid();
                    mLac = ""+cellLocation.getLac();
                }

            }catch(Exception e){
                Log.d(DTAG,"Inside getBasicInfo() CellInfo info"+e);
            }


            mCellID=mLac+mCid+"";
        }
        else
        {
            mCid ="";
            mLac ="";
            try{
                CdmaCellLocation cellLocation = (CdmaCellLocation)tm.getCellLocation();


                if (cellLocation != null)
                {
                    mCid =	""+cellLocation.getBaseStationId();
                    mLac = cellLocation.getSystemId() + "-" + cellLocation.getNetworkId() + "|" + cellLocation.getBaseStationLatitude() + "," + cellLocation.getBaseStationLongitude();
                }

            }catch(Exception e){
                Log.d(DTAG,"Inside getBasicInfo() CellInfo info 2 "+e);
            }
        }
        //Log.d(DTAG,"Inside getBasicInfo() DataState="+mDataState+"   CallState="+mCallState);
    }


    private int getDafaultAPN() {
        int id = -1;

        mAPN=Telephony.Carriers.APN;
        return id;

    }

    public String getLocalIpAddress() {
        String sAddr ;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                //Log.d(DTAG,"Network Interface="+intf.getName());
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    sAddr = inetAddress.getHostAddress().toUpperCase();
                    boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);

                    if (!inetAddress.isLoopbackAddress()&& isIPv4) {


                             return sAddr;


                    }
                }
            }
        }catch (Exception ex) {
            //Log.d(DTAG, ex.toString());
        }
        return "";
    }

    public int sftpLoad(String filename){
        int port = 21;
        String user = "sigmo";
        String pass = "Gold@2014";
        int stat=0;
        FTPClient ftpClient = new FTPClient();
        try {

            //writeTestFile(mIMSI);
            ftpClient.connect(new URL(ftpUrl2).getHost(), port);
            ftpClient.login(user, pass);
            //Log.d(DTAG, "Login code :"+ftpClient.login("", ""));

            //ftpClient.login("anonymous", "");
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // APPROACH #1: uploads first file using an InputStream
            filename.substring(filename.lastIndexOf("/")+1);
            File firstLocalFile = new File(filename);

            //String firstRemoteFile = mIMSI+ ".txt";
            InputStream inputStream = new FileInputStream(firstLocalFile);
            boolean done = ftpClient.storeFile(filename.substring(filename.lastIndexOf("/")+1), inputStream);

            //Toast.makeText(getApplicationContext(), "Uplink Thpt="+mUthpt, 2000).show();
            inputStream.close();
            if (done) {
                System.out.println("The file is uploaded successfully.");
                stat=1;

            }
            Log.d(DTAG, " log Upload Reply code :"+ftpClient.getReplyCode());

            ftpClient.logout();
            ftpClient.disconnect();





        }catch(Exception e)
        {
            //Log.d(DTAG, e.getMessage());
        }
        return stat;
    }

    public void getConfigFile(){
        int port = 21;
        String user = "sigmo";
        String pass = "Gold@2014";
        // Download File
        FTPClient ftpClient = new FTPClient();
        try {


            ftpClient.connect(new URL(ftpUrl2).getHost(), port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            //File firstLocalFile = new File(Environment.getExternalStorageDirectory() + "/sigmod/sigmod_config.txt");
            String firstRemoteFile =  "sigmod_config.txt";
            //Log.d(DTAG, "Downloading Config file Reply code :"+ftpClient.getReplyCode());

            InputStream inConfig= ftpClient.retrieveFileStream(firstRemoteFile);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inConfig));
            //StringBuilder out = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                urlList.add(line);
                //out.append(line+"\n");

                //Log.d(DTAG, "ConfigLine:  "+line);
            }
                //Log.d(DTAG, "Downloading config file complete ");



            ftpClient.logout();
            ftpClient.disconnect();
        }catch(Exception e)
        {
            //Log.d(DTAG, e.getMessage());
        }

    }

    public void ftpTest(boolean link){

        int port = 21;
        String user = "sigmo";
        String pass = "Gold@2014";
        long startUploadTime;
        long endUploadTime;

        long startDownloadTime;
        long endDownloadTime;

        if(link){

            FTPClient ftpClient = new FTPClient();
            try {

                //writeTestFile(mIMSI);
                ftpClient.connect(new URL(ftpUrl2).getHost(), port);
                ftpClient.login(user, pass);
                //Log.d(DTAG, "Login code :"+ftpClient.login("", ""));

                //ftpClient.login("anonymous", "");
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                // APPROACH #1: uploads first file using an InputStream
                File firstLocalFile = new File(Environment.getExternalStorageDirectory() + "/sigmod/"+mIMSI+ ".txt");

                String firstRemoteFile = mIMSI+ ".txt";
                InputStream inputStream = new FileInputStream(firstLocalFile);
                mUthpt=0;
                System.out.println("Start uploading first file");
                startUploadTime=System.currentTimeMillis();
                boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
                endUploadTime=System.currentTimeMillis();

                //Toast.makeText(getApplicationContext(), "Uplink Thpt="+mUthpt, 2000).show();
                inputStream.close();
                if (done) {
                    //System.out.println("The first file is uploaded successfully:"+);
                    //Log.d(DTAG, "Upload complete "+inputStream.toString().length());
                    mUthpt=(104857*8)/(endUploadTime-startUploadTime);
                }
                //Log.d(DTAG, "Upload Reply code :"+ftpClient.getReplyCode());

                ftpClient.logout();
                ftpClient.disconnect();




            }catch(Exception e)
            {
                //Log.d(DTAG, e.getMessage());
            }


        }
        else{
            // Download File
            FTPClient ftpClient = new FTPClient();
            try {


                ftpClient.connect(new URL(ftpUrl2).getHost(), port);
                ftpClient.login(user, pass);
                //Log.d(DTAG, "Login code :"+ftpClient.login("", ""));

                //ftpClient.login("anonymous", "");
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);


                File firstLocalFile = new File(Environment.getExternalStorageDirectory() + "/sigmod/"+mIMSI+ ".txt");
                firstLocalFile.delete();
                String firstRemoteFile = mIMSI+ ".txt";

                //Log.d(DTAG, "Download Reply code :"+ftpClient.getReplyCode());
                mDthpt=0;
                OutputStream out=new FileOutputStream(firstLocalFile);
                startDownloadTime=System.currentTimeMillis();
                boolean downloadDone=ftpClient.retrieveFile(firstRemoteFile, out);

                endDownloadTime=System.currentTimeMillis();
                if(downloadDone){
                    //Log.d(DTAG, "Download complete ");
                    mDthpt=(104857*8)/(endDownloadTime-startDownloadTime);
                }

                out.close();

                ftpClient.logout();
                ftpClient.disconnect();



            }catch(Exception e)
            {
                //Log.d(DTAG, e.getMessage());
                mUthpt=0;
                mDthpt=0;
            }



        }



    }


    public void pingTest(String url){

        mLatency="";

        try {
            StringBuffer echo = new StringBuffer();
            Runtime runtime = Runtime.getRuntime();
            Process proc;



            proc = runtime.exec("/system/bin/ping -c 4 " + new URL(url).getHost());






            try {
                proc.waitFor();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), e+"", 4000);
            }
            int exit = proc.exitValue();
            if (exit == 0) {
                InputStreamReader reader = new InputStreamReader(proc.getInputStream());
                BufferedReader buffer = new BufferedReader(reader);
                String line ;
                while ((line = buffer.readLine()) != null) {
                    echo.append(line + "\n");
                }
                String s = echo.toString();

                if (s.contains("0% packet loss")) {
                    try{
                        int start = s.indexOf("/mdev = ");
                        int end = s.indexOf(" ms", start);
                        s = s.substring(start + 8, end);
                        String stats[] = s.split("/");
                        mLatency= stats[1];
                        // Toast.makeText(getApplicationContext(), "0% packet loss", 3000);
                    }catch(Exception e){
                        e.printStackTrace();
                        //Toast.makeText(getApplicationContext(), e+"", 4000);
                        mLatency="0";
                    }


                } else if (s.contains("100% packet loss")) {

                    mLatency= "PF100";

                } else if (s.contains("% packet loss")) {

                    mLatency= "PF_PARTIAL";

                } else if (s.contains("unknown host")) {

                    mLatency= "UNKNOWNHOST";

                } else {
                    mLatency+= "OTHERERR";
                }
                datDisplay=s;
            }

            else if (exit == 1) {
                mLatency= "PF1";

            } else {
                mLatency= "PF2";

            } }
        catch (Exception e) {
            // body.append("Error\n");
            e.printStackTrace();
            //Toast.makeText(getApplicationContext(), e+"", 4000);
        }



    }


    public long httpTest(String url){
        long startHttp;
        long stopHttp;
        long respTime;

        try {

            startHttp= System.currentTimeMillis();
            HttpClient httpclient = new DefaultHttpClient();
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
            HttpConnectionParams.setSoTimeout(httpParameters, 5000);
            HttpResponse response = httpclient.execute(new HttpGet(url));
            //textResult = response.toString();
            stopHttp= System.currentTimeMillis();
            respTime=stopHttp-startHttp;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            respTime=0;
            //e.printStackTrace();
            //textResult = e.toString();
            //Toast.makeText(getApplicationContext(), textResult, 5000).show();

        }
        return respTime;
    }

    public long dnsTest(String url) {
        long startDNS=0;
        long stopDNS=0;
        String ipadd="";
        long delay;


        boolean exception;
        try {
            startDNS= System.currentTimeMillis();
            InetAddress addr;
            addr= InetAddress.getByName(new URL(url).getHost());
            mLoacalIP=InetAddress.getLocalHost().getHostAddress();
            ipadd = addr.getHostAddress();
            exception=false;
            stopDNS= System.currentTimeMillis();
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //String textResult = e.toString();
            //Toast.makeText(getApplicationContext(), textResult, 5000).show();
            //delay=0;
            exception=true;
        }


        if(!exception){
            delay =stopDNS-startDNS;
            mIPaddress=ipadd;
        }
        else
        {
            mIPaddress="NotResolved";
            delay =0;
        }
        return delay;
    }
    class ServiceWorker implements Runnable  {


        public void run() {

            while(startThread){

                hour_day= Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                //Log.d(DTAG,"Hour of Day"+hour_day);
                try{

                    CheckFilesandUpload();
                    getBasicInfo();
                }catch (Exception e){
                    //log2file("\nException in run @ Check upload:  "+e);
                }

                if(lm==null)
                {
                   lm= (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");


                Log.d(DTAG,"data state "+mDataState+" callState "+mCallState);

                if(mDataState.equals("Connected")&&mCallState.equals("IDLE"))    {
                    //Log.d(DTAG,"Before Test Start...inside condition...SigMoD background");
                    //DataTest myTask1 =	new DataTest();
                    //myTask1.execute();
                    if(!eventrunning)
                    {

                        if(write2file(eventBuffer.toString()))
                        {
                            eventBuffer.delete(0,eventBuffer.length());
                        }


                    }
                    //sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                    //currentDateandTime = sdf.format(new Date());
                    //log2file("\n Before executing all data test: "+currentDateandTime);
                    executeTest();
                    //sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                    //currentDateandTime = sdf.format(new Date());
                    //log2file("\n After executing all data test: "+currentDateandTime);
                    Log.d(DTAG,"Complete Tests................");
                }


                try {
                    Thread.sleep(1200*1000);
                } catch (InterruptedException e) {

                    Log.d(DTAG,"wait..................."+e);
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
    }

    public boolean write2file(String dataTowrite){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        FileOutputStream fos ;
        File filename = new File(Environment.getExternalStorageDirectory() + "/sigmod/DataLog_"+Version+"_"+mIMSI+ "_" + sdf.format(new Date()) + ".txt");

        try {
//		File root = new File(Environment.getExternalStorageDirectory() + "/log.txt");
            //boolean f = root.createNewFile();
            fos = new FileOutputStream(filename, true);
            try {
                FileWriter fWriter = new FileWriter(fos.getFD());
                fWriter.write(dataTowrite);
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

    public boolean log2file(String dataTowrite){

        //SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        FileOutputStream fos =null;
        File filename = new File(Environment.getExternalStorageDirectory() + "/sigmod/Log_"+Version+"_"+mIMSI + ".txt");

        try {
//		File root = new File(Environment.getExternalStorageDirectory() + "/log.txt");
            //boolean f = root.createNewFile();
            fos = new FileOutputStream(filename, true);
            try {
                FileWriter fWriter = new FileWriter(fos.getFD());
                fWriter.write(dataTowrite);
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
            String oldCallState=mCallState;
            eventrunning=true;
            //Log.d(DTAG,"Inside call State listener old state "+mCallState);

            switch(state){
                case TelephonyManager.CALL_STATE_IDLE:

                        if(oldCallState.equals("RINGING"))
                        {
                            getBasicInfo();
                            executeVoice("MISSED_CALL:"+incomingNumber);
                        }else if(oldCallState.equals("OFFHOOK"))
                        {
                            getBasicInfo();
                            executeVoice("CALL_COMPLETE:"+incomingNumber);
                        }

                    mCallState="IDLE";
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:

                    if(oldCallState.equals("RINGING"))
                    {
                        getBasicInfo();
                        executeVoice("MTC_ANS:"+incomingNumber);
                    }else if(oldCallState.equals("IDLE"))
                    {
                        getBasicInfo();
                        executeVoice("MOC:");
                    }

                    mCallState="OFFHOOK";

                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    if(oldCallState.equals("IDLE"))
                    {
                        getBasicInfo();
                        executeVoice("MTC_ATT:"+incomingNumber);
                    }

                    mCallState="RINGING";

                    break;

            }
            //Log.d(DTAG,"Inside call State listener new state "+mCallState);
            eventrunning=false;
        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength){
            super.onSignalStrengthsChanged(signalStrength);
//Get Signal Strength
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String currentSig=mSignalS;

            if (signalStrength.isGsm())
            {
                mSignalS=String.valueOf(-113 + 2 * signalStrength.getGsmSignalStrength());
                mRxqual =""+signalStrength.getGsmBitErrorRate();

            }
            else
            {
                try{
                    mSignalS= signalStrength.getCdmaDbm()+", "+signalStrength.getCdmaEcio();
                }catch(Exception e)
                {
                    mSignalS="0";
                }


            }

            if(signalStrength.isGsm())
            {
                List <NeighboringCellInfo> mNL = tm.getNeighboringCellInfo();
                String stringNeighboring = "Neighboring List - Lac : Cid : Psc : type : RSSI\n";
                mCid ="";
                mLac ="";
                try{
                    GsmCellLocation cellLocation = (GsmCellLocation)tm.getCellLocation();


                    if (cellLocation != null)
                    {
                        mCid =	""+cellLocation.getCid();
                        mLac = ""+cellLocation.getLac();
                    }

                }catch(Exception e){
                    //Log.d(DTAG,""+e);
                }

                stringNeighboring = "";
                String NL2File=null;
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
                            + dBm + " & ";


                }

                //DataCaptured=DataCaptured + stringNeighboring;
                m_NL=stringNeighboring;



            }else
            {
                mCid ="";
                mLac ="";
                try{
                    CdmaCellLocation cellLocation = (CdmaCellLocation)tm.getCellLocation();


                    if (cellLocation != null)
                    {
                        mCid =	""+cellLocation.getBaseStationId();
                        mLac = cellLocation.getSystemId() + "-" + cellLocation.getNetworkId() + "|" + cellLocation.getBaseStationLatitude() + "," + cellLocation.getBaseStationLongitude();
                    }

                }catch(Exception e){
                    //Log.d(DTAG,""+e);
                }
            }

             getBasicInfo();
            if(!currentSig.equals(mSignalS)){
                executeVoice("SigChange");
                //Log.d(DTAG,"write old SS="+currentSig+" , new SS="+mSignalS);
               // Log.d(DTAG,""+e);
            }else
            {
                //Log.d(DTAG,"old SS="+currentSig+" , new SS="+mSignalS);
            }


        }

    }
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(DTAG, "Received start id " + startId + ": " + intent);
        singleThread++;


        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        myListenercs = new MyPhoneStateListenercs();
        tm.listen(myListenercs, PhoneStateListener.LISTEN_CALL_STATE|PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        //myListenerss =new MyPhoneStateListenerss();
        // tm.listen(myListenercs, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        //Notification notice = new Notification(R.drawable.ic_launcher, "Ticker text", System.currentTimeMillis());
        //getBasicInfo();
        try{
            // LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 15, locationListener);
            Log.d(DTAG,"requestLocationUpdates....60000,15");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
            String currentDateandTime = sdf.format(new Date());
            //log2file("\nAfter location Request Updates:  "+currentDateandTime+" 60000,15");
            updateState=0;


        }catch(Exception e)
        {
            //Log.d(DTAG,"Exception:" +e);
            //log2file("\nException in onStart:  "+e);
        }




        File folder = new File(Environment.getExternalStorageDirectory() + "/sigmod");
        folder.mkdirs();
        //Log.d(DTAG,"Inside Background Service");





        if(singleThread==1){

        Log.d(DTAG,"before start thread@@@@");
        Thread thr = new Thread(null, new ServiceWorker(),"SigMoDataService");
        thr.start();
        Log.d(DTAG,"after start thread&&&&&&");
         }

        return START_STICKY;
    }

    public void executeTest() {
        testRunning=true;
        numberOfRecord++;
        Log.d(DTAG,"execute test 1 ");
        if(numberOfRecord>240)
        {
            numberOfRecord=0;
            getConfigFile();
        }else if(numberOfRecord ==0 || numberOfRecord==120)
        {
            getConfigFile();
        }
        Log.d(DTAG,"execute test 2 ");




            try{

                mTotalRX = ""+ TrafficStats.getTotalRxBytes();
                mTotalTX =""+TrafficStats.getMobileTxBytes();

                mMobileRX=""+TrafficStats.getMobileRxBytes();
                mMobileTX=""+TrafficStats.getMobileTxBytes();
                Log.d(DTAG,"execute test 3 ");

            }catch(Exception e)
            {
                mTotalRX="";
                mTotalTX="";
                mMobileRX="";
                mMobileTX="";
            }



        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Log.d(DTAG,"execute test 4 ");
        String currentDateandTime = sdf.format(new Date());
        String LoacalIP=getLocalIpAddress();
        //String fileName=Environment.getExternalStorageDirectory() + "/sigmod/"+mIMSI+  ".txt";
        //uploadData(fileName);
        //ftpTest(true);
        //ftpTest(false);
        Log.d(DTAG,"execute test 5 ");
        String statusString=currentDateandTime;
        mHTTPTime=0;
        mDNSTime=0;
        //Log.d(DTAG,"Inside Execute Test ##########");
        csvData="";
        //udpTest("http://8.8.8.8");
        if(urlList.size()<2)
        {
            //executeVoice("Idle");
            getConfigFile();
            Log.d(DTAG,"execute test 5.0 ");
        }
        else if(urlList.size()>5){
            for(int i=0;i<urlList.size();i++){

                Log.d(DTAG,"execute test 5.1 ");
                currentDateandTime = sdf.format(new Date());
                //statusString=statusString+"\n"+mURLList[i]+".";
                //publishProgress();
                String test= urlList.get(i);
                mHTTPTime=0;
                mDNSTime=0;
                Log.d(DTAG,"execute test 5.2 ");

            /*if(i>0){
               // mHTTPTime= httpTest(mURLList[i]);

                //statusString=statusString+".";
                //publishProgress();

            }*/
                try {
                    mHTTPTime= httpTest(urlList.get(i));
                    Log.d(DTAG,"execute test 5.3 ");

                }catch(Exception e)
                {
                    mHTTPTime=0;
                }
                //executeVoice("Idle");
                try {
                    mDNSTime= dnsTest(urlList.get(i));
                    Log.d(DTAG,"execute test 5.4 ");

                }catch(Exception e)
                {
                    mDNSTime=0;
                }

                try {
                    pingTest(urlList.get(i));
                    Log.d(DTAG,"execute test 5.5 ");

                }catch(Exception e)
                {

                }
                //executeVoice("Idle");
                //mDNSTime= dnsTest(mURLList[i]);

                //statusString=statusString+".";
                //publishProgress();

                //pingTest(mURLList[i]);
                Log.d(DTAG,"execute test 6 ");
                mUthpt=0;
                mDthpt=0;
                if(i==0){
                    //executeVoice("Idle");
                    ftpTest(true);
                    //statusString=statusString+".";
                    //publishProgress();
                    //executeVoice("Idle");
                    ftpTest(false);
                    //statusString=statusString+".";
                    //publishProgress();
                }
                //getDafaultAPN();
                Log.d(DTAG,"execute test 7 ");
                csvData=csvData+
                        Version+"|"+
                        mDatNW+"|"+
                        mMake+"|"+
                        mModel+"|"+
                        mIMEI+"|"+
                        mIMSI+"|"+
                        currentDateandTime+"|"+
                        LoacalIP+"|"+
                        mNetworkOp+"|"+
                        mAPN+"|"+
                        mLac+"-"+mCid+"|"+
                        mSignalS+"|"+
                        mRxqual+"|"+
                        m_NL+"|"+
                        urlList.get(i)+"|"+
                        mIPaddress+"|"+
                        mLatency+"|"+
                        mHTTPTime+"|"
                        +mDNSTime+"|"+
                        +mUthpt+"|"+
                        +mDthpt+"|"
                        +mLat+"|"
                        +mLong+"|"
                        +mAltitude+"|"
                        +mAccuracy+"|"
                        +mTotalRX+"|"
                        +mTotalTX+"|"
                        +mMobileRX+"|"
                        +mMobileTX+"|"
                        +mBatteryStat+"|"
                        +mBatteryChargeStatus+"|"
                        +mBatteryPlugtype+"|"
                        +mBatteryTemp
                        +"\n";
                Log.d(DTAG,csvData);
            }
        }

        if(write2file(csvData))
        {
            csvData="";
        }


        testRunning=false;
    }

    public void executeVoice(String info) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());
        String cdata;
        //eventBuffer =new StringBuffer();

        cdata= Version+"|"+
                mDatNW+"|"+
                mMake+"|"+
                mModel+"|"+
                mIMEI+"|"+
                mIMSI+"|"+
                currentDateandTime+"|"+
                info+"|"+
                mNetworkOp+"|"+
                mAPN+"|"+
                mLac+"-"+mCid+"|"+
                mSignalS+"|"+
                mRxqual+"|"+
                m_NL+"|"+
                "NoURL|"+
                "NA"+"|"+
                "0|"+
                "0|"+
                "0|"+
                "0|"+
                "0|"+
                mLat+"|"+
                mLong+"|"+
                mAltitude+"|"+
                mAccuracy+"|"
                +mTotalRX+"|"
                +mTotalTX+"|"
                +mMobileRX+"|"
                +mMobileTX+"|"
                +mBatteryStat+"|"
                +mBatteryChargeStatus+"|"
                +mBatteryPlugtype+"|"
                +mBatteryTemp
                +"\n";
        //write2file(cdata);
        eventBuffer.append(cdata);
        //Log.d(DTAG,cdata);

    }

    @Override
    public void onCreate() {

        super.onCreate();
        //d(DTAG,"Data Service on create....");

		/*if(singleThread==1){
			executeTest();
		}*/




    }


    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    //////File related
    private void CheckFilesandUpload()    {
        //String filename = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        File folder = new File(Environment.getExternalStorageDirectory() + "/sigmod/");
        String dateString = sdf.format(new Date());
        String fileType = null;
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isFile()) {
                String name = fileEntry.getName();
                String itd=name.substring(name.lastIndexOf(".")+1);

                if(itd.equalsIgnoreCase("txt")){
                    if(!name.contains(dateString) ){
                        boolean test = false;
                        if( name.contains("DataLog")){
                            test = compressFile(fileEntry, 2);
                        }

                        if(test){
                            fileType = ".txt.gz";
                            try{
                                File file1 = new File(fileEntry.getAbsolutePath());
                                if(file1.isFile()){
                                    file1.delete();
                                }

                            }catch(Exception e){
                                //Log.d(DTAG,"Unable to delete log file after upload attempt");
                            }


                        }else{
                            fileType = ".txt";
                        }
                    }//
                }
                String filename = fileEntry.getAbsolutePath();
                final String threadString = filename;//.substring(0,filename.lastIndexOf("."))+fileType;

                Thread MyThread = new Thread() {
                    String threadData = threadString;
                    public void run()
                    {
                        //Log.d(DTAG,"-----------------Inside Run for "+threadData);
                        int status;
                        File file;
                        String ext = threadData.substring(threadData.lastIndexOf(".")+1);
                        if(threadData.contains("DataLog") && threadData.contains("txt.gz")){
                            status =sftpLoad(threadData);
                            if(status==1){
                                try{
                                    file = new File(threadData);
                                    if(file.isFile()){
                                        file.delete();
                                    }

                                }catch(Exception e){
                                    //Log.d(DTAG,"Unable to delete log file after upload attempt");
                                }
                            }


                        }



                    }
                };
                if( mDataState.equalsIgnoreCase("Connected")&&mCallState.equals("IDLE"))
                {
                    MyThread.start();
                }
            }
        }//
    }


    private boolean compressFile(File file, int type)    {
        final int BUFFER = 2048;
        ZipOutputStream zos = null;
        GZIPOutputStream gzos = null;
        String target = null;
        try {
            int size;
            byte[] buffer = new byte[BUFFER];
            BufferedInputStream origin = null;
            String path = file.getAbsolutePath();
            //    String filename1 = path.substring(path.lastIndexOf("/") + 1);
            String filename = file.getName();
            String filepathDirectory = path.substring(0,path.lastIndexOf("/"));
            target = filepathDirectory+"/"+filename.substring(0,filename.lastIndexOf("."));
            if(type == 1){
                target+=".zip";
            }else if(type == 2){
                target+=".txt.gz";
            }else{
                return false;
            }

            FileOutputStream dest = new FileOutputStream(target);
            FileInputStream fi = new FileInputStream(path);
            origin = new BufferedInputStream(fi, BUFFER);

            if(type == 1){

                zos = new ZipOutputStream(
                        new BufferedOutputStream(dest));
                zos.putNextEntry(new ZipEntry(filename));
                while ((size = origin.read(buffer, 0, BUFFER)) > 0) {
                    zos.write(buffer, 0, size);
                }
                origin.close();
                zos.closeEntry();
            }else if (type == 2){
                gzos = new GZIPOutputStream(
                        new FileOutputStream(target));
                while ((size = origin.read(buffer, 0, BUFFER)) > 0) {
                    gzos.write(buffer, 0, size);
                }
                origin.close();
                gzos.finish();
            }else{
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            //writeError("ZipException:IOException "+e);
        }
        finally {
            try {
                if(zos!=null)
                    zos.close();
            } catch (IOException e) {
                e.printStackTrace();
                //writeError("ZipException:IOException "+e);
            }
            try {
                if(gzos!=null)
                    gzos.close();
            } catch (IOException e) {
                e.printStackTrace();
                //writeError("ZipException:IOException "+e);
            }
        }
        if(type == 1){
            if(isValid(target)){
                return true;
            }else{
                return false;
            }
        }
        return true;
    }
    private boolean isValid(String file) {
        ZipFile zipfile = null;
        ZipInputStream zis = null;
        boolean returnValue = false;

        try {
            zipfile = new ZipFile(file);
            zis = new ZipInputStream(new FileInputStream(file));
            ZipEntry ze = zis.getNextEntry();
            if(ze == null) {
                returnValue = false;
            }
            while(ze != null) {
                // if it throws an exception fetching any of the following then we know the file is corrupted.
                zipfile.getInputStream(ze);
                if((ze.getCrc() == -1)||(ze.getCompressedSize() == -1)){
                    returnValue = false;
                }else{
                    returnValue = true;
                }
                ze = zis.getNextEntry();
            }
        } catch (ZipException e) {
            //writeError("ZipException "+e);
            returnValue = false;
        } catch (IOException e) {
            returnValue = false;
            //writeError("ZipException:IOException "+e);
        } finally {
            try {
                if (zipfile != null) {
                    zipfile.close();
                    zipfile = null;
                }
            } catch (IOException e) {
                //writeError("ZipException:IOException "+e);
            } try {
                if (zis != null) {
                    zis.close();
                    zis = null;
                }
            } catch (IOException e) {
                //writeError("ZipException:IOException "+e);

            }
        }
        return returnValue;
    }

    final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            //Log.d(DTAG,"Inside Location change");
            try{

                float acc=location.getAccuracy();
                double altitude=location.getAltitude();
                double lon = location.getLongitude();
                double lat = location.getLatitude();
                mLat = lat+"";
                mLong = lon+"";
                mAccuracy=acc+"";
                mAltitude=altitude+"";

                //SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                //String dateString = sdf.format(new Date());

                Log.d(DTAG,"Update Lat "+lat+" Lon"+lon+" Altitude="+altitude+"  Accuracy:"+acc);

                try{

                    hour_day= Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

                    if(hour_day >=22 || hour_day <=6)
                    {
                       // LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                        if(updateState!=1){
                            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3600000, 10, locationListener);
                            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                            //String currentDateandTime = sdf.format(new Date());
                            //log2file("\n"+hour_day+" Location Request Updates: "+currentDateandTime+"  3600000,50");
                            updateState=1;
                        }

                    }
                    else  if(acc<150)
                    {
                        //LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                        if(updateState!=2){
                            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 900000, 20, locationListener);

                            updateState=2;
                        }

                        //Log.d(DTAG," 15 MIN Update Lat "+lat+" Lon"+lon+" Altitude="+altitude+"  Accuracy:"+acc);
                    }
                    else
                    {
                       // LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                        if(updateState!=3){
                            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000, 50, locationListener);

                            updateState=3;
                        }

                        //Log.d(DTAG," 5 Min Update Lat "+lat+" Lon"+lon+" Altitude="+altitude+"  Accuracy:"+acc);
                    }


                }catch(Exception e)
                {
                    //Log.d(DTAG,"Exception:" +e);
                }

            }catch(Exception e){
                Log.d(DTAG,"Exception"+e);
            }

        }

        public void onProviderDisabled(String arg0) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
            builder.setTitle("GPS Status");  // GPS not found
            builder.setMessage("GPS is disabled. Please enable it"); // Want to enable?
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            Log.d(DTAG,"onProviderDisabled");
            // TODO Auto-generated method stub
        }
        public void onProviderEnabled(String arg0) {
            // TODO Auto-generated method stub
        }
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
            // TODO Auto-generated method stub
        }
    };
    //type = 1 -> zip 8
    //type = 2 -> gzip compress

}

