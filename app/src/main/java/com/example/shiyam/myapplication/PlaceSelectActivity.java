package com.example.shiyam.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.shiyam.myapplication.models.AddressInfo;
import com.example.shiyam.myapplication.models.StaticData;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlaceSelectActivity extends AppCompatActivity {

    public static ProgressDialog progressDialog;
    List<String> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//         initPlaceName();
    }

    public void initPlaceName(){

        NameLoader loader = new NameLoader();
        loader.execute();
        setUpProgress();
    }

    public class NameLoader extends AsyncTask<Void,List<String>,Void> {

        @Override
        protected Void doInBackground(Void... params) {


            List<LatLng> points = StaticData.points;
            Set<String> placeActualNames = new HashSet<>();
            for(LatLng point:points) {
                String str = AddressInfo.getAddress(point.latitude, point.longitude);
                placeActualNames.add(str);
            }


            Log.e("ALl Places","Places\n" + placeActualNames);
            list = new ArrayList<>(placeActualNames);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();

            ListView list = (ListView) findViewById(R.id.list_view_places);
            // get data from the table by the ListAdapter
            ListAdapter customAdapter = new ListAdapter(StaticData.current_context,R.layout.layout_list_places);

            list .setAdapter(customAdapter);
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
    public class ListAdapter extends ArrayAdapter<String> {

        public ListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public ListAdapter(Context context, int resource, List<String> items) {
            super(context, resource, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.layout_list_places, null);
            }

                TextView placeName = (TextView) v.findViewById(R.id.txtPlaceName);
                placeName.setText(list.get(position));
            return v;
        }

    }



}
