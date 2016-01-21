package com.example.shiyam.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shiyam.myapplication.models.AddressInfo;
import com.example.shiyam.myapplication.models.DirectionsJSONParser;
import com.example.shiyam.myapplication.models.PlaceArrayAdapter;
import com.example.shiyam.myapplication.models.StaticData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity  implements
        GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks{

    GoogleMap map;
    Map<String,CustomMarker> markerPoints;
    public static Map<String,CustomMarker> userSelectedPoints;
    private HashMap<CustomMarker, Marker> markersHashMap;
    private Iterator<Map.Entry<CustomMarker, Marker>> iter;
    private CameraUpdate cu;
    private static final String LOG_TAG = "MainActivity";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private AutoCompleteTextView mAutocompleteStartTextView;
    private AutoCompleteTextView mAutocompleteEndTextView;
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;


    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));
    public static ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setUpMarkersHashMap();
        // Getting reference to SupportMapFragment of the activity_main
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);

        // Getting Map for the SupportMapFragment
        map = fm.getMap();

        //set up context
        StaticData.current_context = getApplicationContext();
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
        map.setMyLocationEnabled(true);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();
        mAutocompleteStartTextView = (AutoCompleteTextView) findViewById(R.id
                .inputStart);
        mAutocompleteEndTextView = (AutoCompleteTextView) findViewById(R.id.inputEnd);
        mAutocompleteStartTextView.setOnItemClickListener(mAutocompleteClickListener);
        mAutocompleteEndTextView.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,
                BOUNDS_MOUNTAIN_VIEW, null);
        mAutocompleteStartTextView.setAdapter(mPlaceArrayAdapter);
        mAutocompleteEndTextView.setAdapter(mPlaceArrayAdapter);
        mAutocompleteEndTextView.setEnabled(false);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                int size = markerPoints.size();
                if(size>1){
//                    map.clear();
//                    markerPoints.clear();
                    String id =  point.latitude + " "+ point.latitude;
                    CustomMarker customMarker = new CustomMarker(id,
                            point.latitude, point.longitude);
                    customMarker.setMarker(getMarker(customMarker, "User"));
                    userSelectedPoints.put(id, customMarker);
                    Marker marker = map.addMarker(getMarkerOptions(customMarker, "User"));
                    marker.showInfoWindow();
                    marker.setDraggable(true);

                    startAnimation(customMarker);


                    Button button = (Button) findViewById(R.id.button3);
                    button.setEnabled(true);
                }
                size = markerPoints.size();

                if(size <2) {
                    if (size == 0) {
                        setUpMarkers(point, "Start");
                    } else {
                        setUpMarkers(point, "End");
                    }
                }
            }
        });
    }

    //put the two markers on google map
    private void setUpMarkers(LatLng point,String id){

        if(markerPoints.size()>1){
            map.clear();
            markerPoints.clear();
        }
        CustomMarker customMarker = new CustomMarker(id, point.latitude, point.longitude);
        markerPoints.put(id, customMarker);

//        MarkerOptions options = new MarkerOptions();
//
//        options.position(point);
//
//        if(id.equals("Start")){
//            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//        }else if(id.equals("End")){
//            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//        }
//
//        map.addMarker(options);
        addMarker(id,customMarker,"Nothing");
        startAnimation(customMarker);

        if(markerPoints.size() >= 2){
            LatLng origin = new LatLng(markerPoints.get("Start").getCustomMarkerLatitude()
                    ,markerPoints.get("Start").getCustomMarkerLongitude());
            LatLng dest = new LatLng(markerPoints.get("End").getCustomMarkerLatitude(),
                    markerPoints.get("End").getCustomMarkerLongitude());

            String url = getDirectionsUrl(origin, dest);

            DownloadTask downloadTask = new DownloadTask();

            downloadTask.execute(url);

            setUpProgress();
        }
    }
    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.setMessage("Updating Path Information");
            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }


    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            progressDialog.setMessage("Creating Path Line on Map \n Please wait...");
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            List<String> addressList = new ArrayList<>();

            if(result == null || result.size() == 0){
                Toast.makeText(MainActivity.this,"No routes found",Toast.LENGTH_LONG);
                progressDialog.dismiss();
                return;
            }
            // Traversing through all the routes
            for(int i=0; result != null && i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    //Log.e("Latitude:" + point.get("lat"), "Longtitude:" + point.get("lng"));
                    points.add(position);
                }
                // Adding all the points in the route to LineOptions
                StaticData.points = points;
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.BLUE);

                Toast.makeText(MainActivity.this,"You can now select places on path",
                        Toast.LENGTH_LONG);
            }

            // Drawing polyline in the Google Map for the i-th route
            map.addPolyline(lineOptions);
            FocusPlaces();
            progressDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.action_refresh){

            mAutocompleteEndTextView.setText("");
            mAutocompleteStartTextView.setText("");
            mAutocompleteEndTextView.setEnabled(false);

            mAutocompleteStartTextView.setEnabled(true);
            Button b = (Button) findViewById(R.id.button3);
            b.setEnabled(false);
            markerPoints.clear();
            userSelectedPoints.clear();
            map.clear();
            return true;
        }else if(id == R.id.action_clear){

            userSelectedPoints.clear();
        }

        return super.onOptionsItemSelected(item);
    }
    // this is method to help us set up a Marker that stores the Markers we want
    // to plot on the map
    public void setUpMarkersHashMap() {
        if (markerPoints == null) {
            markerPoints = new HashMap<String, CustomMarker>();
        }
        if(userSelectedPoints == null){
            userSelectedPoints = new HashMap<>();
        }
    }

    // this is method to help us add a Marker into the hashmap that stores the
    // Markers
    public void addMarkerToHashMap(CustomMarker customMarker, Marker marker) {
        setUpMarkersHashMap();
        if(markersHashMap.size()>1){
            map.clear();
            markersHashMap.clear();
        }
        markersHashMap.put(customMarker,marker);
    }

    public void SelectPlaces(View v){
        mAutocompleteStartTextView.setText("");
        mAutocompleteStartTextView.setHint("Enter Start Place Here");

        map.clear();
        markersHashMap.clear();
    }

    public void FocusPlaces(){
        zoomAnimateLevelToFitMarkers(120);
    }

    private void setUpProgress(){
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Downloading route information");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    public void ShowPath(View v){

        String start = "";
        String end = "";
        if(!start.equals("") && !end.equals("")){
            //show line red as path

            setUpProgress();
            DownloadTask task = new DownloadTask();
            List<CustomMarker> cust = new ArrayList(markersHashMap.keySet());
            LatLng origin = new LatLng(cust.get(0).getCustomMarkerLatitude(),
                    cust.get(1).getCustomMarkerLongitude());
            LatLng destionation = new LatLng(cust.get(1).getCustomMarkerLatitude(),
                    cust.get(1).getCustomMarkerLongitude());
            String url = getDirectionsUrl(origin,destionation);
            task.execute(url);
            progressDialog.show();
        }else{

            if(start.equals("")){
                Toast.makeText(this,"Select the Starting place",Toast.LENGTH_LONG);
                return;
            }else{
                Toast.makeText(this,"Select the End or destination place",Toast.LENGTH_LONG);
                return;
            }
        }
    }
    // this is method to help us fit the Markers into specific bounds for camera
    // position
    public void zoomAnimateLevelToFitMarkers(int padding) {
        LatLngBounds.Builder b = new LatLngBounds.Builder();

        Set<String> set = markerPoints.keySet();
        List<String> keyList = new ArrayList<>(set);
        for (String key:keyList) {
            CustomMarker customMarker = (CustomMarker) markerPoints.get(key);
            LatLng ll = new LatLng(customMarker.getCustomMarkerLatitude(), customMarker.getCustomMarkerLongitude());
            b.include(ll);
        }
        LatLngBounds bounds = b.build();
        // Change the padding as per needed
        cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        map.animateCamera(cu);
    }

    public void startAnimation(CustomMarker customMarker) {
            animateMarker(customMarker, new LatLng(customMarker.getCustomMarkerLatitude(),
                    customMarker.getCustomMarkerLongitude()));
//            FocusPlaces();
    }
    // this is method to animate the Marker. There are flavours for all Android
    // versions
    public void animateMarker(CustomMarker customMarker, LatLng latlng) {
        if (findMarker(customMarker) != null) {

            LatLngInterpolator latlonInter = new LatLngInterpolator.LinearFixed();
            latlonInter.interpolate(20,
                    new LatLng(customMarker.getCustomMarkerLatitude(), customMarker.getCustomMarkerLongitude()), latlng);

            customMarker.setCustomMarkerLatitude(latlng.latitude);
            customMarker.setCustomMarkerLongitude(latlng.longitude);

            if (android.os.Build.VERSION.SDK_INT >= 14) {
                MarkerAnimation.animateMarkerToICS(findMarker(customMarker), new LatLng(customMarker.getCustomMarkerLatitude(),
                        customMarker.getCustomMarkerLongitude()), latlonInter);
            } else if (android.os.Build.VERSION.SDK_INT >= 11) {
                MarkerAnimation.animateMarkerToHC(findMarker(customMarker), new LatLng(customMarker.getCustomMarkerLatitude(),
                        customMarker.getCustomMarkerLongitude()), latlonInter);
            } else {
                MarkerAnimation.animateMarkerToGB(findMarker(customMarker), new LatLng(customMarker.getCustomMarkerLatitude(),
                        customMarker.getCustomMarkerLongitude()), latlonInter);
            }
        }
    }

    // this is method to help us find a Marker that is stored into the hashmap
    public Marker findMarker(CustomMarker customMarker) {
        Marker marker = null;
        marker = customMarker.getMarker();

        return  marker;
    }


    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(LOG_TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i(LOG_TAG, "Fetching details for ID: " + item.placeId);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            String place_name = place.getName().toString();
            double place_latitude = place.getLatLng().latitude;
            double place_longitude = place.getLatLng().longitude;

            if(!mAutocompleteEndTextView.isEnabled()){
                mAutocompleteEndTextView.setEnabled(true);
                mAutocompleteStartTextView.setEnabled(false);
                mAutocompleteStartTextView.setText(place_name);
                LatLng point = new LatLng(place_latitude,place_longitude);
                setUpMarkers(point,"Start");
                FocusPlaces();
            }else{
                mAutocompleteEndTextView.setEnabled(false);
                mAutocompleteEndTextView.setText(place_name);
                LatLng point = new LatLng(place_latitude,place_longitude);
                setUpMarkers(point, "End");

            }


        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(LOG_TAG, "Google Places API connected.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }

    // this is method to help us add a Marker to the map
    public void addMarker(String id,CustomMarker customMarker,String type) {

        Marker marker = map.addMarker(getMarkerOptions(customMarker, type));
        marker.showInfoWindow();
        marker.setDraggable(true);

        customMarker.setMarker(getMarker(customMarker,type));
        markerPoints.put(id, customMarker);
    }
    private Marker getMarker(CustomMarker customMarker,String type){
        MarkerOptions markerOption =getMarkerOptions(customMarker,type);
        Marker newMark = map.addMarker(markerOption);

        return newMark;
    }

    private MarkerOptions getMarkerOptions(CustomMarker customMarker, String type){

        MarkerOptions markerOptions = new MarkerOptions();
        LatLng pos = new LatLng(customMarker.getCustomMarkerLatitude(),
                customMarker.getCustomMarkerLongitude());
        markerOptions.position(pos);
        markerOptions.title(AddressInfo.getAddress(pos));
        markerOptions.snippet("Place Name");
        if(type.equals("User")) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.mark_blue));
        }else{
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.mark_red));
        }
        return markerOptions;

    }
    public void LoadSubPlaces(View v){
        Button b = (Button)v;
        if(b.isEnabled()){


            Map<String,CustomMarker> selectedPoints = MainActivity.userSelectedPoints;
            List<CustomMarker> listPoint = new ArrayList<>(selectedPoints.values());

            if(listPoint != null){
                for(CustomMarker customMarker:listPoint){

                    double lat = customMarker.getCustomMarkerLatitude();
                    double lon = customMarker.getCustomMarkerLongitude();
                    LatLng latLng = new LatLng(lat,lon);

                    if(!isContainingPoints(latLng)){

                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

                        //Setting Dialog Title
                        alertDialog.setTitle(R.string.confirmationTitle);

                        //Setting Dialog Message
                        alertDialog.setMessage(R.string.question1);

                        //On Pressing Setting button
                        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Intent intent = new Intent(MainActivity.this,PlaceSelectActivity.class);
                                startActivity(intent);
                            }
                        });

                        //On pressing cancel button
                        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.cancel();
                            }
                        });
                    }

                }
            }
            Intent intent = new Intent(MainActivity.this,PlaceSelectActivity.class);
            startActivity(intent);

        }
    }


    public static boolean isContainingPoints(LatLng point){

        List<LatLng> points = StaticData.points;
        boolean isContaining = false;

        for(LatLng tempPoint:points){
            double lat = tempPoint.latitude;
            double lon = tempPoint.longitude;

            if(lat == point.latitude && lon == point.longitude){
                    isContaining = true;
                    break;
            }
        }
        return isContaining;
    }
}
