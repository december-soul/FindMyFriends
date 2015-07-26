package com.example.patrick.findmyfriends;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by patrick on 11.07.15.
 */
public class MyLocationListener implements LocationListener
{
    private Location mCurrent;
    public MyLocationListener(Location current) {
        mCurrent = current;
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrent.set(location);
        double longitude, lattitude;

        longitude = location.getLongitude();
        lattitude = location.getLatitude();

        //((TextView)findViewById(R.id.longi)).setText("Longitude : " + longitude);
        //((TextView)findViewById(R.id.latti)).setText("Lattitude : " + lattitude);
        Log.w("myApp", "Longitude : " + longitude);
        Log.w("myApp", "Lattitude : " + lattitude);

    }

    @Override
    public void onProviderDisabled(String provider) {

        //((TextView)findViewById(R.id.warnGPS)).setText("GPS Desactivé");

    }

    @Override
    public void onProviderEnabled(String provider) {
        //((TextView)findViewById(R.id.warnGPS)).setText("GPS Activé");

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

}