package com.touchcarwash_driver.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.touchcarwash_driver.Temp;
import com.touchcarwash_driver.db.UserDatabaseHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

public class Update_Vehicle_Location {

    public Context context;
    public UserDatabaseHandler udb;
    public NetConnection cd;
    public String pkey = "", vehcileid = "", lat = "", lng = "", orderid = "";

    public Update_Vehicle_Location(Context cx) {
        context = cx;
        udb = new UserDatabaseHandler(context);
        cd = new NetConnection(context);
    }

    public class locupdate extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... arg0) {

            try {
                String link = Temp.weblink + "update_washvehiclelocation.php";
                String data = URLEncoder.encode("item", "UTF-8")
                        + "=" + URLEncoder.encode(vehcileid + ":%" + lat + ":%" + lng + ":%" + orderid, "UTF-8");
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter
                        (conn.getOutputStream());
                wr.write(data);
                wr.flush();
                BufferedReader reader = new BufferedReader
                        (new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            } catch (Exception e) {
                return new String("Unable to connect server! Please check your internet connection");
            }
        }

        @Override
        protected void onPostExecute(String result) {

            try {

                if (result.contains("ok")) {
                    udb.delete_vehcilelocation(pkey);
                    locationupdate();
                }

            } catch (Exception a) {

            }


        }
    }


    public void locationupdate() {
        try {
            ArrayList<String> id1 = udb.get_vehcilelocation();
            String[] c = id1.toArray(new String[id1.size()]);

            if (c.length > 0) {
                pkey = c[0];
                vehcileid = c[1];
                lat = c[2];
                lng = c[3];
                orderid = c[4];

                if (cd.isConnectingToInternet()) {
                    new locupdate().execute();
                }

            }
        } catch (Exception a) {

        }

    }

}
