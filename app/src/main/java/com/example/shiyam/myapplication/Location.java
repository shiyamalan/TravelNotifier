package com.example.shiyam.myapplication;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.example.shiyam.myapplication.models.StaticData;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * Created by shiyam on 1/15/16.
 */
public class Location {


    public LatLng getLocationFromAddress(Context context,String strAddress) {

        if(context == null)
            context = StaticData.current_context;
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return p1;
    }
}
