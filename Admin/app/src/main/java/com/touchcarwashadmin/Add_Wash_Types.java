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

public class Add_Wash_Types extends AppCompatActivity {

    ImageView back;
    ConnectionDetecter cd;
    final DatabaseHandler db = new DatabaseHandler(this);
    public Dialog dialog;
    Typeface face;
    Typeface face1;
    public ProgressBar pb1;
    ProgressDialog pd;
    public TextView persentage;
    ProgressBar prb1,prb2;
    public Button stop;
    TextView text;
    TextView txttypename,txtdiscription,txtogprice,txtofferprice;
    EditText typename,discription,ogprice,offerprice;
    Button update;
    Call call;
    boolean requestgoing=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__wash__types);
        face = Typeface.createFromAsset(getAssets(), "proxibold.otf");
        face1 = Typeface.createFromAsset(getAssets(), "proximanormal.ttf");
        pd = new ProgressDialog(this);

        cd = new ConnectionDetecter(this);
        back = (ImageView) findViewById(R.id.back);
        prb1 = (ProgressBar) findViewById(R.id.pb1);
        prb2 = (ProgressBar) findViewById(R.id.pb2);
        update = (Button) findViewById(R.id.update);
        text = (TextView) findViewById(R.id.text);
        txttypename=findViewById(R.id.txttypename);
        txtdiscription =findViewById(R.id.txtdiscription);
        txtogprice=findViewById(R.id.txtogprice);
        txtofferprice=findViewById(R.id.txtofferprice);
        ogprice=findViewById(R.id.ogprice);
        offerprice=findViewById(R.id.offerprice);
        typename=findViewById(R.id.typename);
        discription=findViewById(R.id.discription);
        text.setTypeface(face);
        update.setTypeface(face);


        txttypename.setTypeface(face);
        txtdiscription.setTypeface(face);
        txtogprice.setTypeface(face);
        txtofferprice.setTypeface(face);


        ogprice.setTypeface(face1);
        offerprice.setTypeface(face1);
        typename.setTypeface(face1);
        discription.setTypeface(face1);

        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onBackPressed();
            }
        });


        update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    if (typename.getText().toString().equalsIgnoreCase("")) {
                        Toasty.info(getApplicationContext(), "Please enter typename", Toast.LENGTH_SHORT).show();
                        typename.requestFocus();
                    }
                    else if (discription.getText().toString().equalsIgnoreCase("")) {
                        Toasty.info(getApplicationContext(), "Please enter discription", Toast.LENGTH_SHORT).show();
                        discription.requestFocus();
                    }
                    else if (ogprice.getText().toString().equalsIgnoreCase("")) {
                        Toasty.info(getApplicationContext(), "Please enter ogprice", Toast.LENGTH_SHORT).show();
                        ogprice.requestFocus();
                    }
                    else if (offerprice.getText().toString().equalsIgnoreCase("")) {
                        Toasty.info(getApplicationContext(), "Please enter offer price", Toast.LENGTH_SHORT).show();
                        offerprice.requestFocus();
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


        if (Temp.typeedit == 1) {

            ogprice.setText(Temp.type_ogprice);
            offerprice.setText(Temp.type_offerprice);
            typename.setText(Temp.type_typename);
            discription.setText(Temp.type_discription);
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

        bodyBuilder.addFormDataPart("isedit", null, RequestBody.create(contentType, Temp.typeedit+""));
        bodyBuilder.addFormDataPart("editsn", null,RequestBody.create(contentType, Temp.type_sn));
        bodyBuilder.addFormDataPart("vehicleid", null,RequestBody.create(contentType, Temp.type_vehicleid));
        bodyBuilder.addFormDataPart("typename", null,RequestBody.create(contentType,typename.getText().toString()));
        bodyBuilder.addFormDataPart("discription", null,RequestBody.create(contentType,discription.getText().toString()));
        bodyBuilder.addFormDataPart("ogprice", null,RequestBody.create(contentType,ogprice.getText().toString()));
        bodyBuilder.addFormDataPart("offerprice", null,RequestBody.create(contentType,offerprice.getText().toString()));

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
                .url(Temp.weblink+"addwashtypebyadmin.php")
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
                                Toasty.info(getApplicationContext(), "Sorry ! This wash type is exist", Toast.LENGTH_SHORT).show();
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

