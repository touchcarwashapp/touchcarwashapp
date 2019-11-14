package com.touchcarwashadmin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;
import io.github.lizhangqu.coreprogress.ProgressHelper;
import io.github.lizhangqu.coreprogress.ProgressUIListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Add_Offer extends AppCompatActivity {
    ImageView back;
    ConnectionDetecter cd;
    final DatabaseHandler db = new DatabaseHandler(this);
    public Dialog dialog;
    Typeface face;
    Typeface face1;
    public ProgressBar pb1;
    ProgressDialog pd;
    public TextView persentage;
    Spinner vehicleid;
    ProgressBar prb1,prb2;
    public Button stop;
    TextView text;
    TextView txtvehicleid,txtwashcount,txtoffercount;
    EditText washcount,offercount;
    Button update;
    Call call;
    boolean requestgoing=true;
    List<String> lst_vehiclesn = new ArrayList();
    List<String> lst_vehiclename = new ArrayList();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__offer);
        face = Typeface.createFromAsset(getAssets(), "proxibold.otf");
        face1 = Typeface.createFromAsset(getAssets(), "proximanormal.ttf");
        pd = new ProgressDialog(this);

        cd = new ConnectionDetecter(this);
        back = (ImageView) findViewById(R.id.back);
        prb1 = (ProgressBar) findViewById(R.id.pb1);
        prb2 = (ProgressBar) findViewById(R.id.pb2);
        update = (Button) findViewById(R.id.update);
        text = (TextView) findViewById(R.id.text);
        vehicleid=findViewById(R.id.vehicleid);
        txtvehicleid = (TextView) findViewById(R.id.txtvehicleid);
        txtwashcount=findViewById(R.id.txtwashcount);
        txtoffercount=findViewById(R.id.txtoffercount);
        washcount=findViewById(R.id.washcount);
        offercount=findViewById(R.id.offercount);

        text.setTypeface(face);
        update.setTypeface(face);


        txtvehicleid.setTypeface(face);
        txtwashcount.setTypeface(face);
        txtoffercount.setTypeface(face);


        washcount.setTypeface(face1);
        offercount.setTypeface(face1);

        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onBackPressed();
            }
        });


        update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {

                    if (vehicleid.getSelectedItemPosition()<=0) {
                        Toasty.info(getApplicationContext(), "Please select vehicle", Toast.LENGTH_SHORT).show();
                    }
                    else if (washcount.getText().toString().equalsIgnoreCase("")) {
                        Toasty.info(getApplicationContext(), "Please enter washcount", Toast.LENGTH_SHORT).show();
                        washcount.requestFocus();
                    }
                    else if (offercount.getText().toString().equalsIgnoreCase("")) {
                        Toasty.info(getApplicationContext(), "Please enter offer count", Toast.LENGTH_SHORT).show();
                        offercount.requestFocus();
                    }
                    else {

                        if (cd.isConnectingToInternet()) {
                            uploadingprogress();
                        } else {
                            Toasty.info(getApplicationContext(), Temp.nointernet, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                } catch (Exception a) {

                }
            }
        });


        if (Temp.offeredit == 1) {

            washcount.setText(Temp.offer_washcount);
            offercount.setText(Temp.offer_offercount);
        }

        ArrayList<String> id1 = db.getvehicles();
        String[] k = (String[]) id1.toArray(new String[id1.size()]);

        if(k.length>0)
        {
            loadvehicles();


        }
        else
        {
            new featching_washvehicle().execute();
        }
    }




    public void loadvehicles()
    {

        ArrayList<String> id1 = db.getvehicles();
        String[] k = (String[]) id1.toArray(new String[id1.size()]);


        lst_vehiclesn.clear();
        lst_vehiclename.clear();
        lst_vehiclesn.add("0");
        lst_vehiclename.add("Select Vehicle");

        for(int i=0;i<k.length;i++)
        {
            lst_vehiclesn.add(k[i].trim());
            i=i+1;
            lst_vehiclename.add(k[i].trim());
        }

        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(Add_Offer.this, android.R.layout.simple_spinner_item, lst_vehiclename) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextColor(ViewCompat.MEASURED_STATE_MASK);
                ((TextView) v).setTextSize(16.0f);
                ((TextView) v).setTypeface(face);
                return v;
            }

            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                ((TextView) v).setTextColor(ViewCompat.MEASURED_STATE_MASK);
                ((TextView) v).setTextSize(16.0f);
                ((TextView) v).setTypeface(face);
                return v;
            }
        };
        dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vehicleid.setAdapter(dataAdapter2);
        vehicleid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View arg1, int arg2, long arg3) {


            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        if(Temp.offeredit==1)
        {
            if(!Temp.offer_vehicleid.equalsIgnoreCase(""))
            {
                vehicleid.setSelection(Integer.parseInt(Temp.offer_vehicleid));
            }
        }

    }
    public class featching_washvehicle extends AsyncTask<String, Void, String> {

        public void onPreExecute() {
            pd.setMessage("Loading Vehicles....");
            pd.setCancelable(false);
            pd.show();
        }
        public String doInBackground(String... arg0) {
            try {
                String link= Temp.weblink +"getvehiclelist_foraddprice.php";
                String data  = URLEncoder.encode("item", "UTF-8")
                        + "=" + URLEncoder.encode("", "UTF-8");
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
                while((line = reader.readLine()) != null)
                {
                    sb.append(line);
                }
                return sb.toString();
            } catch (Exception e) {
                return new String("Unable to connect server! Please check your internet connection");
            }

        }
        public void onPostExecute(String result) {
            try {
                pd.dismiss();
                if (result.contains(":%ok")) {
                    try {
                        String[] got = result.split(":%");
                        int k = (got.length - 1) / 2;
                        int m = -1;
                        lst_vehiclename.add("Select Vehicle");
                        for (int i = 1; i <= k; i++) {
                            m=m+1;
                            int a=m;
                            m=m+1;
                            int a1=m;
                            db.add_vehicles(got[a],got[a1]);
                        }
                        loadvehicles();
                    } catch (Exception e) {
                    }
                }


            } catch (Exception e2) {
            }
        }
    }

    public void uploadingprogress() {
        try {
            dialog = new Dialog(this);
            dialog.requestWindowFeature(1);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialogupload);
            pb1 = (ProgressBar) dialog.findViewById(R.id.pb1);
            persentage = (TextView) dialog.findViewById(R.id.persentage);
            stop = (Button) dialog.findViewById(R.id.stop);
            uploadfiletoserver();
            stop.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    try {
                        requestgoing=false;
                        call.cancel();
                        dialog.dismiss();
                    } catch (Exception e) {
                    }
                }
            });
            dialog.show();
        } catch (Exception e) {
        }
    }

    public void uploadfiletoserver()
    {
        requestgoing=true;
        pb1.setVisibility(View.VISIBLE);
        dialog.setCancelable(false);
        persentage.setVisibility(View.VISIBLE);
        update.setEnabled(false);

        MediaType contentType=MediaType.parse("text/plain; charset=utf-8");
        OkHttpClient client;
        OkHttpClient.Builder client1 = new OkHttpClient.Builder();
        client1.connectTimeout(5, TimeUnit.MINUTES);
        client1.readTimeout(5,TimeUnit.MINUTES);
        client1.writeTimeout(5,TimeUnit.MINUTES);

        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
        bodyBuilder.setType(MultipartBody.FORM);

        bodyBuilder.addFormDataPart("isedit", null, RequestBody.create(contentType, Temp.offeredit+""));
        bodyBuilder.addFormDataPart("editsn", null,RequestBody.create(contentType, Temp.offer_sn));
        bodyBuilder.addFormDataPart("vehicleid", null,RequestBody.create(contentType, lst_vehiclesn.get(vehicleid.getSelectedItemPosition())));
        bodyBuilder.addFormDataPart("washcount", null,RequestBody.create(contentType,washcount.getText().toString()));
        bodyBuilder.addFormDataPart("offercount", null,RequestBody.create(contentType,offercount.getText().toString()));

        MultipartBody body = bodyBuilder.build();

        RequestBody requestBody = ProgressHelper.withProgress(body, new ProgressUIListener() {

            //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
            @Override
            public void onUIProgressStart(long totalBytes) {
                super.onUIProgressStart(totalBytes);

            }

            @Override
            public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                persentage.setText((int) (100 * percent)+"%");
                //progress.setText("numBytes:" + numBytes + " bytes" + "\ntotalBytes:" + totalBytes + " bytes" + "\npercent:" + percent * 100 + " %" + "\nspeed:" + speed * 1000 / 1024 / 1024 + "  MB/秒");
            }
            @Override
            public void onUIProgressFinish() {
                super.onUIProgressFinish();

            }

        });
        Request request = new Request.Builder()
                .url(Temp.weblink+"addoffersbyadmin.php")
                .post(requestBody)
                .build();
        client = client1.build();
        call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pb1.setVisibility(View.GONE);
                        persentage.setVisibility(View.GONE);
                        update.setEnabled(true);
                        dialog.dismiss();
                        pd.dismiss();
                        if(requestgoing==true)
                        {
                            Toast.makeText(getApplicationContext(),"Please try later",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
            @Override
            public void onResponse(Call call, final Response response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            String result=response.body().string();
                            pb1.setVisibility(View.GONE);
                            persentage.setVisibility(View.GONE);
                            update.setEnabled(true);
                            dialog.dismiss();
                            if (result.contains("ok")) {

                                Toasty.info(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                                finish();
                            } else if (result.contains("exit")) {
                                Toasty.info(getApplicationContext(), "Sorry ! This offer is exist", Toast.LENGTH_SHORT).show();
                            } else {
                                Toasty.info(getApplicationContext(), Temp.tempproblem, Toast.LENGTH_SHORT).show();
                            }

                        }
                        catch (Exception a)
                        {

                        }
                    }
                });
            }
        });
    }
}
