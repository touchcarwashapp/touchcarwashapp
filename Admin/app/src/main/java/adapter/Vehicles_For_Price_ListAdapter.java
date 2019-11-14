package adapter;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.touchcarwashadmin.Accorie_List;
import com.touchcarwashadmin.Add_Vehicle;
import com.touchcarwashadmin.ConnectionDetecter;
import com.touchcarwashadmin.DatabaseHandler;
import com.touchcarwashadmin.R;
import com.touchcarwashadmin.Temp;
import com.touchcarwashadmin.Vehicle_Management;
import com.touchcarwashadmin.WashTypes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

import data.Vehicles_FeedItem;
import data.Vehicles_For_Price_FeedItem;

public class Vehicles_For_Price_ListAdapter extends BaseAdapter {

    public Activity activity;
    public ConnectionDetecter cd;
    public Context context;
    public DatabaseHandler db;
    Typeface face;
    public List<Vehicles_For_Price_FeedItem> feedItems;
    private LayoutInflater inflater;
    public String pcatid = "";
    ProgressDialog pd;
    int pos = 0;
    public Vehicles_For_Price_ListAdapter(Activity activity2, List<Vehicles_For_Price_FeedItem> feedItems2) {
        activity = activity2;
        feedItems = feedItems2;
        context = activity2.getApplicationContext();
        cd = new ConnectionDetecter(context);
        pd = new ProgressDialog(activity2);
        db = new DatabaseHandler(context);
        face = Typeface.createFromAsset(context.getAssets(), "proxibold.otf");
    }

    public int getCount() {
        return feedItems.size();
    }

    public Object getItem(int location) {
        return feedItems.get(location);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        if (inflater == null) {
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_vehicles_forprice, null);
        }
        TextView vehiclecompany= (TextView) convertView.findViewById(R.id.vehiclecompany);
        ImageView image = (ImageView) convertView.findViewById(R.id.image);
        Button washtypes=convertView.findViewById(R.id.washtypes);
        Button accessories=convertView.findViewById(R.id.accessories);

        Vehicles_For_Price_FeedItem item = (Vehicles_For_Price_FeedItem) feedItems.get(position);

        RequestOptions rep = new RequestOptions().signature(new ObjectKey(item.getImgsig()));
        Glide.with(context).load(Temp.weblink+"vehiclesmall/"+item.getSn()+".jpg").apply(rep).transition(DrawableTransitionOptions.withCrossFade()).into(image);
        vehiclecompany.setTypeface(face);
        vehiclecompany.setText(item.getVehiclecompany());

        washtypes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Vehicles_For_Price_FeedItem item = (Vehicles_For_Price_FeedItem) feedItems.get(position);
                Temp.vehicleid = item.getSn();
                Intent i = new Intent(context, WashTypes.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        });

        accessories.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Vehicles_For_Price_FeedItem item = (Vehicles_For_Price_FeedItem) feedItems.get(position);
                Temp.vehicleid = item.getSn();
                Intent i = new Intent(context, Accorie_List.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        });


        return convertView;
    }
}
