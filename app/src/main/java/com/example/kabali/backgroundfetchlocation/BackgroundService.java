package com.example.kabali.backgroundfetchlocation;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

public class BackgroundService extends Service {

    private final LocationServiceBinder binder = new LocationServiceBinder();
    private final String TAG = "Backgroundservice";
    private LocationListener mLocationListener;
    private LocationManager mLocaitonManager;

    private final int LOCATION_INTERVAL = 500;
    private final int LOCATION_DISTANCE = 10;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private class LocationListener implements android.location.LocationListener
    {

        private Location lastLocation = null;
        private final String TAG = "LocationListener";
        private Location mLastLocation;

        public LocationListener(String provider)
        {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;
            Log.wtf(TAG,"LocationChanged "+location.getLatitude());

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

            Log.wtf(TAG,"LOCATIONSTATUS "+status);
        }

        @Override
        public void onProviderEnabled(String provider) {

            Log.wtf(TAG,"ONPROVIDERENABLED "+provider);
        }

        @Override
        public void onProviderDisabled(String provider) {

            Log.wtf(TAG,"ONPROVIDERDISABLED "+provider);
        }
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId)
    {
        super.onStartCommand(intent,flags,startId);
        return START_NOT_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate()
    {
        Log.wtf(TAG,"ONCREATE ");
        startForeground(12345678,getNotification());
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(mLocaitonManager != null)
        {
            try {
                mLocaitonManager.removeUpdates(mLocationListener);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    private void initializeLocationManager()
    {
        if(mLocaitonManager == null)
        {
            mLocaitonManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void startTracking()
    {
        initializeLocationManager();
        mLocationListener = new LocationListener(LocationManager.GPS_PROVIDER);

        try {
            mLocaitonManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,LOCATION_INTERVAL,LOCATION_DISTANCE,mLocationListener);
        }
        catch (java.lang.SecurityException ex) {
            // Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            // Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

    }

    public void stopTracking()
    {
        this.onDestroy();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification getNotification()
    {
        NotificationChannel channel = new NotificationChannel("channel01","mychannel", NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Notification.Builder  builder= new Notification.Builder(getApplicationContext(),"channel01").setAutoCancel(true);
        return builder.build();
    }

    public class LocationServiceBinder extends Binder
    {
        public BackgroundService getService()
        {
            return BackgroundService.this;
        }
    }
}
