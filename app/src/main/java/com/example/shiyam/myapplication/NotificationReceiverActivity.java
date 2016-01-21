package com.example.shiyam.myapplication;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class NotificationReceiverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_notification_result);

        TextView textViewMsg= (TextView) findViewById(R.id.textMsg);
        TextView textViewPlace= (TextView) findViewById(R.id.textPlaceName);

        Bundle extras = getIntent().getExtras();

        if(extras !=null){
            String placeName = extras.getString("placeName");
            String msg  = extras.getString("Message");

            textViewMsg.setText(msg);
            textViewPlace.setText(placeName);
        }

    }


}
