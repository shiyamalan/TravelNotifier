package com.example.shiyam.myapplication.models;

/**
 * Created by shiyam on 1/19/16.
 */

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shiyam.myapplication.R;

import java.util.List;

public class CustomAdapter extends BaseAdapter{
    List<String> result;
    Context context;
    private static LayoutInflater inflater=null;
    public CustomAdapter(List<String> prgmNameList) {
        // TODO Auto-generated constructor stub
        result=prgmNameList;
        context=StaticData.current_context;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return result.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView tv;
        ImageView imge;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.layout_list_places, null);


        holder.tv=(TextView) rowView.findViewById(R.id.txtPlaceName);
        holder.tv.setText(result.get(position));
        holder.imge = (ImageView) rowView.findViewById(R.id.addbtn);

//        rowView.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                Toast.makeText(context, "You Clicked "+result.get(position), Toast.LENGTH_LONG).show();
//            }
//        });
        return rowView;
    }



}