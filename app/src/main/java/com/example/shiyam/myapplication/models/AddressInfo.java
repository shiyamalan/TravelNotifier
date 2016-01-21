package com.example.shiyam.myapplication.models;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.example.shiyam.myapplication.MainActivity;
import com.example.shiyam.myapplication.PlaceSelectActivity;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by shiyam on 1/16/16.
 */
public class AddressInfo {


    public static String countryName;
    static Geocoder geocoder;
    static {
       geocoder = new Geocoder(StaticData.current_context, Locale.getDefault());
    }
    public static String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                countryName = address.getCountryName();
                String feature1 = address.getFeatureName();
                result.append(feature1);
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }
    public static String getAddress(LatLng latLng){

        return getAddress(latLng.latitude,latLng.longitude);
    }
}
