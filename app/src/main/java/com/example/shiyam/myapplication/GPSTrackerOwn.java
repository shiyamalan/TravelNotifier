package com.example.shiyam.myapplication;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.shiyam.myapplication.models.AddressInfo;
import com.example.shiyam.myapplication.models.StaticData;
import com.google.android.gms.maps.model.LatLng;

/**
 * Create this Class from tutorial :
 * http://www.androidhive.info/2012/07/android-gps-location-manager-tutorial
 *
 * For Geocoder read this : http://stackoverflow.com/questions/472313/android-reverse-geocoding-getfromlocation
 *
 */

public class GPSTrackerOwn extends Service implements LocationListener {

    // Get Class Name
    private static String TAG = GPSTrackerOwn.class.getName();

    private  Context mContext =null;

    // flag for GPS Status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS Tracking is enabled
    boolean isGPSTrackingEnabled = false;

    Location location;
    double latitude;
    double longitude;

    // How many Geocoder should return our GPSTracker
    int geocoderMaxResults = 1;

    // The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    // Store LocationManager.GPS_PROVIDER or LocationManager.NETWORK_PROVIDER information
    private String provider_info;

    public GPSTrackerOwn(Context context) {
        this.mContext = context;
        getLocation();
    }

    public GPSTrackerOwn(){

    }
    /**
     * Try to get my current location by GPS or Network Provider
     */
    public void getLocation() {

        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            //getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            //getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            // Try to get location if you GPS Service is enabled
            if (isGPSEnabled) {
                this.isGPSTrackingEnabled = true;

                Log.d(TAG, "Application use GPS Service");

                /*
                 * This provider determines location using
                 * satellites. Depending on conditions, this provider may take a while to return
                 * a location fix.
                 */

                provider_info = LocationManager.GPS_PROVIDER;

            } else if (isNetworkEnabled) { // Try to get location if you Network Service is enabled
                this.isGPSTrackingEnabled = true;

                Log.d(TAG, "Application use Network State to get GPS coordinates");

                /*
                 * This provider determines location based on
                 * availability of cell tower and WiFi access points. Results are retrieved
                 * by means of a network lookup.
                 */
                provider_info = LocationManager.NETWORK_PROVIDER;

            }

            // Application can use GPS or Network Provider
            if (!provider_info.isEmpty()) {
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return;
//                }
                locationManager.requestLocationUpdates(
                        provider_info,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        this
                );

                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(provider_info);
                    updateGPSCoordinates();
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            Log.e(TAG, "Impossible to connect to LocationManager", e);
        }
    }

    /**
     * Update GPSTracker latitude and longitude
     */
    public void updateGPSCoordinates() {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            Map<String,LatLng> map = PlaceSelectActivity.map;
            List<LatLng> list = new ArrayList<>(map.values());

            LatLng currentPosition = new LatLng(latitude,longitude);
            String currentName = AddressInfo.getAddress(currentPosition);
            for(LatLng outer:list){
                    String placeName = AddressInfo.getAddress(outer);
                    if((latitude == outer.latitude && longitude == outer.longitude)
                            || currentName.equalsIgnoreCase(placeName)){
                        /****
                            matched
                         call notification message
                         */

                        String msg = PlaceSelectActivity.msgMap.get(placeName);

                        if(msg == null){
                            msg = "You are in " +placeName + " You have to do something";
                        }
                        createNotification(msg,placeName);
                        return;

                }
            }

        }
    }



    /**
     * GPSTracker latitude getter and setter
     * @return latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        return latitude;
    }

    /**
     * GPSTracker longitude getter and setter
     * @return
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        return longitude;
    }

    /**
     * GPSTracker isGPSTrackingEnabled getter.
     * Check GPS/wifi is enabled
     */
    public boolean getIsGPSTrackingEnabled() {

        return this.isGPSTrackingEnabled;
    }

    /**
     * Stop using GPS listener
     * Calling this method will stop using GPS in your app
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.removeUpdates(GPSTrackerOwn.this);
        }
    }


    /**
     * Get list of address by latitude and longitude
     * @return null or List<Address>
     */
    public List<Address> getGeocoderAddress(Context context) {
        if (location != null) {

            Geocoder geocoder = new Geocoder(context, Locale.ENGLISH);

            try {
                /**
                 * Geocoder.getFromLocation - Returns an array of Addresses
                 * that are known to describe the area immediately surrounding the given latitude and longitude.
                 */
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, this.geocoderMaxResults);

                return addresses;
            } catch (IOException e) {
                //e.printStackTrace();
                Log.e(TAG, "Impossible to connect to Geocoder", e);
            }
        }

        return null;
    }

    /**
     * Try to get AddressLine
     * @return null or addressLine
     */
    public String getAddressLine(Context context) {
        List<Address> addresses = getGeocoderAddress(context);

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String addressLine = address.getAddressLine(0);

            return addressLine;
        } else {
            return null;
        }
    }

    /**
     * Try to get Locality
     * @return null or locality
     */
    public String getLocality(Context context) {
        List<Address> addresses = getGeocoderAddress(context);

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String locality = address.getLocality();

            return locality;
        }
        else {
            return null;
        }
    }

    /**
     * Try to get Postal Code
     * @return null or postalCode
     */
    public String getPostalCode(Context context) {
        List<Address> addresses = getGeocoderAddress(context);

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String postalCode = address.getPostalCode();

            return postalCode;
        } else {
            return null;
        }
    }

    /**
     * Try to get CountryName
     * @return null or postalCode
     */
    public String getCountryName(Context context) {
        List<Address> addresses = getGeocoderAddress(context);
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String countryName = address.getCountryName();

            return countryName;
        } else {
            return null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        this.location = location;
        updateGPSCoordinates();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void createNotification(String msg,String place) {

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            long[] pattern = {500,500,500,500,500,500,500,500,500};
            // Invoking the default notification service
            NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setContentTitle(place);
            mBuilder.setContentText(msg);
            mBuilder.setTicker("New Notification");
            mBuilder.setSmallIcon(R.drawable.ic_launcher);
            mBuilder.setSound(alarmSound);
            mBuilder.setVibrate(pattern);
            mBuilder.setLights(Color.BLUE, 500, 500);

            // Increase notification number every time a new notification arrives
            mBuilder.setNumber(0);

            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(StaticData.current_context, NotificationReceiverActivity.class);
            resultIntent.putExtra("placeName", place);
            resultIntent.putExtra("Message",msg);

            //This ensures that navigating backward from the Activity leads out of the app to Home page
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(StaticData.current_context);

            // Adds the back stack for the Intent
            stackBuilder.addParentStack(NotificationReceiverActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =stackBuilder.getPendingIntent(0,PendingIntent.FLAG_ONE_SHOT);
            //can only be used once
            // start the activity when the user clicks the notification text
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager myNotificationManager = (NotificationManager) StaticData.current_context.getSystemService(Context.NOTIFICATION_SERVICE);
            // pass the Notification object to the system
            myNotificationManager.notify(0, mBuilder.build());


        //        // Prepare intent which is triggered if the
//        // notification is selected
//        Intent intent = new Intent(this, NotificationReceiverActivity.class);
//        PendingIntent pIntent = PendingIntent.getActivity(StaticData.current_context,
//                (int) System.currentTimeMillis(), intent, 0);
//        long[] pattern = {500,500,500,500,500,500,500,500,500};
//        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//
//        // Build notification
//        // Actions are just fake
//        Notification noti = new Notification.Builder(this)
//                .setContentTitle("New Notification For Doing Activity Current Place :" + place)
//                .setContentText(msg).setSmallIcon(R.drawable.ic_launcher)
//                .setContentIntent(pIntent)
//                .addAction(R.drawable.ic_launcher, "Cancel", pIntent)
//                .addAction(R.drawable.ic_launcher, "View", pIntent)
//                .addAction(R.drawable.ic_launcher, "Snooze", pIntent)
//                .setLights(Color.BLUE, 500, 500)
//                .setVibrate(pattern)
//                .setSound(alarmSound)
//                .build();
//        NotificationManager notificationManager = (NotificationManager) StaticData.current_context.getSystemService(NOTIFICATION_SERVICE);
//        // hide the notification after its selected
//        noti.flags |= Notification.FLAG_AUTO_CANCEL;
//
//        notificationManager.notify(0, noti);

    }
}