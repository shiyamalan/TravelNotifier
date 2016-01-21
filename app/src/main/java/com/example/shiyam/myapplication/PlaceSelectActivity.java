package com.example.shiyam.myapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shiyam.myapplication.models.AddressInfo;
import com.example.shiyam.myapplication.models.CustomAdapter;
import com.example.shiyam.myapplication.models.StaticData;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlaceSelectActivity extends AppCompatActivity {

    public static ProgressDialog progressDialog;
    public static Map<String,String> msgMap = new HashMap<>();

    public static HashMap<String,LatLng> map = new HashMap<>();
    List<String> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StaticData.current_context = PlaceSelectActivity.this;

        list.add("Jaffna");
        list.add("Mannar");


        initPlaceName();

    }

    public void initPlaceName(){

        NameLoader loader = new NameLoader();
        loader.execute();
        setUpProgress();
    }

    public class NameLoader extends AsyncTask<Void,Integer,Void> {

        List<LatLng> points = StaticData.points;
        int i = 0;
        @Override
        protected Void doInBackground(Void... params) {

            Set<String> placeActualNames = new HashSet<>();

//            for(LatLng point:points) {
//                String str = AddressInfo.getAddress(point.latitude, point.longitude);
//                placeActualNames.add(str);
//
//
//                if(map.containsKey(str)) {
//                    List<LatLng> points = map.get(str);
//                    points.add(point);
//                    map.put(str, points);
//                }else{
//                    List<LatLng> points = new ArrayList<>();
//                    points.add(point);
//                    map.put(str,points);
//                }
//                Log.e("Place: " + Integer.toString(i++) + " : ", str);
//            }

            Map<String,CustomMarker> selectedPoints = MainActivity.userSelectedPoints;
            List<CustomMarker> listPoint = new ArrayList<>(selectedPoints.values());

            if(listPoint != null){
                for(CustomMarker customMarker:listPoint){

                    double lat = customMarker.getCustomMarkerLatitude();
                    double lon = customMarker.getCustomMarkerLongitude();
                    LatLng latLng = new LatLng(lat,lon);
                    String name = AddressInfo.getAddress(latLng);

                    placeActualNames.add(name);
                    map.put(name, latLng);

                }
            }
            Log.e("ALl Places","Places\n" + placeActualNames);
            list = new ArrayList<>(placeActualNames);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setMessage("Path Name Updating" + Integer.toString(i) +
                    "/" + Integer.toString(points.size()));
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();

            ListView listView = (ListView) findViewById(R.id.list_view_places);
            // get data from the table by the ListAdapter
            CustomAdapter customAdapter = new CustomAdapter(list);
            listView.setAdapter(customAdapter);
            registerForContextMenu(listView);
            //listView.setOnItemClickListener(this);


        }
    }
    private void setUpProgress(){
        progressDialog = new ProgressDialog(PlaceSelectActivity.this);
        progressDialog.setMessage("Parsing Place_Name Information\nPlease Wait....");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_place, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_back){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addEvents(View view){
        /*
        click added to notifcation list
         */
        GPSTrackerOwn gpsTrackerOwn = new GPSTrackerOwn(PlaceSelectActivity.this);
        if(gpsTrackerOwn.getIsGPSTrackingEnabled()){
            // is enabled the gps
           // Log.e("Current Place", " Some"+gpsTrackerOwn.getAddressLine(PlaceSelectActivity.this));
        }else{
            /*
            enable the GPS
             */
            showSettingsAlert();

            if(gpsTrackerOwn.getIsGPSTrackingEnabled()){
                //is enable the gps
                Log.e("Currnet Place", gpsTrackerOwn.getAddressLine(StaticData.current_context));
            }
        }
    }
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();

        String selectedName = list.get(info.position);
        switch (item.getItemId()) {
            case R.id.add:
                addToEvent(selectedName,"");
                return true;
            case R.id.edit:
                editMessage(selectedName);
                return true;
            case R.id.remove:
                removeFromEvent(selectedName);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    private void editMessage(String selectedName) {

        /*
        edit already created message
         */

        if(msgMap.containsKey(selectedName)){
            String mesg = msgMap.get(selectedName);
            addToEvent(selectedName,mesg);
        }
    }
    private void removeFromEvent(final String selectedName) {
        /*
        it removes places and msg from msgMap
         */
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PlaceSelectActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Delete Confirmation From Event List");

        // Setting Dialog Message
        alertDialog.setMessage("Are you sure want to delete this place " + selectedName + " ?");


        // set dialog message
        alertDialog
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                //result.setText(userInput.getText());
                                if (msgMap.containsKey(selectedName)) {
                                    msgMap.remove(selectedName);
                                    return;
                                }else {
                                    Toast.makeText(PlaceSelectActivity.this, "You did not add  this place " + selectedName + " to event list",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialogView = alertDialog.create();

        // show it
        alertDialogView.show();


    }

    private void addToEvent(final String placeName,String msg) {
        /*
        makes this places name to event list
         */
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(PlaceSelectActivity.this);
        View promptsView = li.inflate(R.layout.layout_message, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                PlaceSelectActivity.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);
        userInput.setText(msg);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                //result.setText(userInput.getText());
                                String msg = userInput.getText().toString();
                                msgMap.put(placeName,msg);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    /**
     * Function to show settings alert dialog
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PlaceSelectActivity.this);

        //Setting Dialog Title
        alertDialog.setTitle(R.string.GPSAlertDialogTitle);

        //Setting Dialog Message
        alertDialog.setMessage(R.string.GPSAlertDialogMessage);

        //On Pressing Setting button
        alertDialog.setPositiveButton(R.string.action_settings, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        //On pressing cancel button
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }
}
