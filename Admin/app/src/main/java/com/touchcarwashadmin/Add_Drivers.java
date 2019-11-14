package com.touchcarwashadmin;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.yalantis.ucrop.UCrop;

import java.io.BufferedReader;
import java.io.File;
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
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class Add_Drivers extends AppCompatActivity {
    ImageView back;
    ConnectionDetecter cd;
    final DatabaseHandler db = new DatabaseHandler(this);
    public Dialog dialog;
    Typeface face;
    Typeface face1;
    public Bitmap img;
    public float ogheight;
    public ProgressBar pb1;
    ProgressDialog pd;
    public TextView persentage;
    ImageView photo1,licenephoto1;
    public String photopath1 = "none",photopath2="none";
    Spinner washvehicleid;
    ProgressBar prb1,prb2;
    public Button stop;
    TextView text;
    TextView txtphoto1,txtwashvehicleid,txtname,txtplace,txtaddress,txtcontact1,txtcontact2,txtlicenceno,txtlicenephoto1;
    EditText name,place,address,contact1,contact2,licenceno;
    Button update;
    Call call;
    boolean requestgoing=true;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA};

    List<String> lst_wvsn = new ArrayList();
    List<String> lst_wvname = new ArrayList();
    public int whichimage=0; //1-photo 2-licencephoto
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__drivers);
        face = Typeface.createFromAsset(getAssets(), "proxibold.otf");
        face1 = Typeface.createFromAsset(getAssets(), "proximanormal.ttf");
        pd = new ProgressDialog(this);

        cd = new ConnectionDetecter(this);
        back = (ImageView) findViewById(R.id.back);
        prb1 = (ProgressBar) findViewById(R.id.pb1);
        prb2 = (ProgressBar) findViewById(R.id.pb2);
        photo1 = (ImageView) findViewById(R.id.photo1);
        licenephoto1=findViewById(R.id.licenephoto1);
        update = (Button) findViewById(R.id.update);
        washvehicleid=findViewById(R.id.washvehicleid);
        text = (TextView) findViewById(R.id.text);
        txtphoto1 = (TextView) findViewById(R.id.txtphoto1);
        txtwashvehicleid=findViewById(R.id.txtwashvehicleid);
        txtname=findViewById(R.id.txtname);
        txtplace=findViewById(R.id.txtplace);
        txtaddress=findViewById(R.id.txtaddress);
        txtcontact1=findViewById(R.id.txtcontact1);
        txtcontact2=findViewById(R.id.txtcontact2);
        txtlicenceno=findViewById(R.id.txtlicenceno);
        txtlicenephoto1=findViewById(R.id.txtlicenephoto1);

        name=findViewById(R.id.name);
        place=findViewById(R.id.place);
        address=findViewById(R.id.address);
        contact1=findViewById(R.id.contact1);
        contact2=findViewById(R.id.contact2);
        licenceno=findViewById(R.id.licenceno);

        txtwashvehicleid.setTypeface(face);
        txtname.setTypeface(face);
        txtplace.setTypeface(face);
        txtaddress.setTypeface(face);
        txtcontact1.setTypeface(face);
        txtcontact2.setTypeface(face);
        txtlicenceno.setTypeface(face);
        txtlicenephoto1.setTypeface(face);
        text.setTypeface(face);
        update.setTypeface(face);
        txtphoto1.setTypeface(face1);

        name.setTypeface(face1);
        place.setTypeface(face1);
        address.setTypeface(face1);
        contact1.setTypeface(face1);
        contact2.setTypeface(face1);
        licenceno.setTypeface(face1);

        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onBackPressed();
            }
        });

        photo1.post(new Runnable() {
            public void run() {
                ogheight = Float.parseFloat(db.getscreenwidth()) / 4.0f;
                ogheight *= 3.0f;
                photo1.getLayoutParams().height = Math.round(ogheight);
            }
        });

        licenephoto1.post(new Runnable() {
            public void run() {
                ogheight = Float.parseFloat(db.getscreenwidth()) / 4.0f;
                ogheight *= 3.0f;
                licenephoto1.getLayoutParams().height = Math.round(ogheight);
            }
        });

        photo1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    if (!hasPermissions(Add_Drivers.this, PERMISSIONS)) {
                        ActivityCompat.requestPermissions(Add_Drivers.this, PERMISSIONS, PERMISSION_ALL);
                    } else {
                        File folder = new File(Environment.getExternalStorageDirectory() + "/" + Temp.foldername);
                        if (!folder.exists()) {
                            folder.mkdir();
                            try {
                                new File(Environment.getExternalStorageDirectory() + "/" + Temp.foldername + "/.nomedia").createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        whichimage=1;
                        selectImage();
                    }

                } catch (Exception e2) {
                }
            }
        });

        licenephoto1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    if (!hasPermissions(Add_Drivers.this, PERMISSIONS)) {
                        ActivityCompat.requestPermissions(Add_Drivers.this, PERMISSIONS, PERMISSION_ALL);
                    } else {
                        File folder = new File(Environment.getExternalStorageDirectory() + "/" + Temp.foldername);
                        if (!folder.exists()) {
                            folder.mkdir();
                            try {
                                new File(Environment.getExternalStorageDirectory() + "/" + Temp.foldername + "/.nomedia").createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        whichimage=2;
                        selectImage();
                    }

                } catch (Exception e2) {
                }
            }
        });


        update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {

                    if (washvehicleid.getSelectedItemPosition()<=0) {
                        Toasty.info(getApplicationContext(), "Please select vehicle", Toast.LENGTH_SHORT).show();
                    }
                    else if (name.getText().toString().equalsIgnoreCase("")) {
                        Toasty.info(getApplicationContext(), "Please enter name", Toast.LENGTH_SHORT).show();
                        name.requestFocus();
                    }
                    else if (place.getText().toString().equalsIgnoreCase("")) {
                        Toasty.info(getApplicationContext(), "Please enter place", Toast.LENGTH_SHORT).show();
                        place.requestFocus();
                    }
                    else if (address.getText().toString().equalsIgnoreCase("")) {
                        Toasty.info(getApplicationContext(), "Please enter address", Toast.LENGTH_SHORT).show();
                        address.requestFocus();
                    }
                    else if (contact1.getText().toString().equalsIgnoreCase("")) {
                        Toasty.info(getApplicationContext(), "Please enter contact1", Toast.LENGTH_SHORT).show();
                        contact1.requestFocus();
                    }
                    else if (licenceno.getText().toString().equalsIgnoreCase("")) {
                        Toasty.info(getApplicationContext(), "Please enter licenceno", Toast.LENGTH_SHORT).show();
                        licenceno.requestFocus();
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


        if (Temp.driveredit == 1) {

            name.setText(Temp.driver_name);
            place.setText(Temp.driver_place);
            address.setText(Temp.driver_address);
            contact1.setText(Temp.driver_contact1);
            contact2.setText(Temp.driver_contact2);
            licenceno.setText(Temp.driver_licenceno);

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    prb1.setVisibility(View.VISIBLE);
                    RequestBuilder apply = Glide.with(getApplicationContext()).asBitmap().apply(new RequestOptions().signature(new ObjectKey(Temp.driver_imgsig)));
                    apply.load(Temp.weblink + "drivers/" + Temp.driver_sn + ".jpg").into(new SimpleTarget<Bitmap>() {
                        public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                            prb1.setVisibility(View.GONE);
                            photo1.setImageBitmap(bitmap);
                        }

                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            prb1.setVisibility(View.GONE);
                            photo1.setImageResource(R.drawable.nophoto);
                        }
                    });
                }
            });

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    prb2.setVisibility(View.VISIBLE);
                    RequestBuilder apply = Glide.with(getApplicationContext()).asBitmap().apply(new RequestOptions().signature(new ObjectKey(Temp.driver_licenceimgsig)));
                    apply.load(Temp.weblink + "licencephoto/" + Temp.driver_sn + ".jpg").into(new SimpleTarget<Bitmap>() {
                        public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                            prb2.setVisibility(View.GONE);
                            licenephoto1.setImageBitmap(bitmap);
                        }

                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            prb2.setVisibility(View.GONE);
                            licenephoto1.setImageResource(R.drawable.nophoto);
                        }
                    });
                }
            });

        }

        ArrayList<String> id1 = db.getwashvehicle();
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

    public void selectImage() {
        final CharSequence[] options = {"Remove Photo", "Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Stage Photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    EasyImage.openCamera(Add_Drivers.this, 1);
                } else if (options[item].equals("Choose from Gallery")) {
                    EasyImage.openGallery(Add_Drivers.this, 1);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                } else if (options[item].equals("Remove Photo")) {
                    if(whichimage==1)
                    {
                        photopath1 = "removed";
                        photo1.setImageDrawable(getResources().getDrawable(R.drawable.nophoto));
                    }
                    else if(whichimage==2)
                    {
                        photopath2 = "removed";
                        licenephoto1.setImageDrawable(getResources().getDrawable(R.drawable.nophoto));
                    }

                }
            }
        });
        builder.show();
    }


    public void loadvehicles()
    {
        ArrayList<String> id1 = db.getwashvehicle();
        String[] k = (String[]) id1.toArray(new String[id1.size()]);

        lst_wvsn.clear();
        lst_wvname.clear();
        lst_wvsn.add("0");
        lst_wvname.add("Select Vehicle");

        for(int i=0;i<k.length;i++)
        {
            lst_wvsn.add(k[i].trim());
            i=i+1;
            lst_wvname.add(k[i].trim());
        }

        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(Add_Drivers.this, android.R.layout.simple_spinner_item, lst_wvname) {
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
        washvehicleid.setAdapter(dataAdapter2);
        washvehicleid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View arg1, int arg2, long arg3) {


            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        if(Temp.driveredit==1)
        {
            if(!Temp.driver_washvehicleid.equalsIgnoreCase(""))
            {
                washvehicleid.setSelection(Integer.parseInt(Temp.driver_washvehicleid));
            }
        }

    }

    public class featching_washvehicle extends AsyncTask<String, Void, String> {

        public void onPreExecute() {
            pd.setMessage("Loading Wash Vehicles....");
            pd.setCancelable(false);
            pd.show();
        }
        public String doInBackground(String... arg0) {
            try {
                String link= Temp.weblink +"getvehiclelist_foradddriver.php";
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
                        for (int i = 1; i <= k; i++) {
                            m=m+1;
                            int a=m;
                            m=m+1;
                            int a1=m;
                            db.add_washvehicle(got[a],got[a1]);
                        }
                    } catch (Exception e) {
                    }

                    loadvehicles();
                }
            } catch (Exception e2) {
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data2) {
        super.onActivityResult(requestCode, resultCode, data2);
        EasyImage.handleActivityResult(requestCode, resultCode, data2, this, new DefaultCallback() {
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
            }

            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                File f=null;
                if(whichimage==1)
                {
                    f = new File(Environment.getExternalStorageDirectory()+"/"+Temp.foldername+"/productpic1.jpg");
                }
                else if(whichimage==2)
                {
                    f = new File(Environment.getExternalStorageDirectory()+"/"+Temp.foldername+"/productpic2.jpg");
                }
                try {
                    f.createNewFile();
                } catch (IOException e) {
                }
                try {
                    Uri uri = Uri.fromFile(f);
                    UCrop.Options options = new UCrop.Options();
                    options.setFreeStyleCropEnabled(false);
                    options.setToolbarColor(Color.parseColor("#205c14"));
                    options.setStatusBarColor(Color.parseColor("#2E7D32"));
                    options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
                    options.setCompressionQuality(80);
                    options.setToolbarTitle("Crop Image");
                    UCrop.of(Uri.fromFile(imageFile), uri).withOptions(options).withAspectRatio(4.0f, 3.0f).start(Add_Drivers.this);
                } catch (Exception e2) {
                }
            }
        });
        if (requestCode == UCrop.REQUEST_CROP) {
            try {
                if(whichimage==1)
                {
                    photopath1 = UCrop.getOutput(data2).getPath();
                    img = BitmapFactory.decodeFile(photopath1);
                    photo1.setImageBitmap(img);
                }
                else if(whichimage==2)
                {
                    photopath2 = UCrop.getOutput(data2).getPath();
                    img = BitmapFactory.decodeFile(photopath2);
                    licenephoto1.setImageBitmap(img);
                }

            } catch (Exception e) {
            }
        }
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (!(context == null || permissions == null)) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != 0) {
                    return false;
                }
            }
        }
        return true;
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
        if (photopath1.equalsIgnoreCase("none")) {
            bodyBuilder.addFormDataPart("image1","none");
        } else if (photopath1.equalsIgnoreCase("removed")) {
            bodyBuilder.addFormDataPart("image1","removed");
        } else {
            bodyBuilder.addFormDataPart("image1","filled");
            File sourceFile = new File(photopath1);
            bodyBuilder.addFormDataPart("photo1", sourceFile.getName(), RequestBody.create(MediaType.parse("image/jpg"), sourceFile));
        }


        if (photopath2.equalsIgnoreCase("none")) {
            bodyBuilder.addFormDataPart("image2","none");
        } else if (photopath2.equalsIgnoreCase("removed")) {
            bodyBuilder.addFormDataPart("image2","removed");
        } else {
            bodyBuilder.addFormDataPart("image2","filled");
            File sourceFile = new File(photopath2);
            bodyBuilder.addFormDataPart("photo2", sourceFile.getName(), RequestBody.create(MediaType.parse("image/jpg"), sourceFile));
        }

        bodyBuilder.addFormDataPart("isedit", null,RequestBody.create(contentType, Temp.driveredit+""));
        bodyBuilder.addFormDataPart("editsn", null,RequestBody.create(contentType, Temp.driver_sn));
        bodyBuilder.addFormDataPart("washvehicleid", null,RequestBody.create(contentType, lst_wvsn.get(washvehicleid.getSelectedItemPosition())));
        bodyBuilder.addFormDataPart("name", null,RequestBody.create(contentType,name.getText().toString()));
        bodyBuilder.addFormDataPart("place", null,RequestBody.create(contentType,place.getText().toString()));
        bodyBuilder.addFormDataPart("address", null,RequestBody.create(contentType,address.getText().toString()));
        bodyBuilder.addFormDataPart("contact1", null,RequestBody.create(contentType,contact1.getText().toString()));
        bodyBuilder.addFormDataPart("contact2", null,RequestBody.create(contentType,contact2.getText().toString()));
        bodyBuilder.addFormDataPart("licenceno", null,RequestBody.create(contentType,licenceno.getText().toString()));
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
                .url(Temp.weblink+"adddriversbyadmin.php")
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
                                File file1 = new File(photopath1);
                                if (file1.exists()) {
                                    file1.delete();
                                }

                                File file2 = new File(photopath2);
                                if (file2.exists()) {
                                    file2.delete();
                                }

                                Toasty.info(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                                finish();
                            } else if (result.contains("exit")) {
                                Toasty.info(getApplicationContext(), "Sorry ! This driver is exist", Toast.LENGTH_SHORT).show();
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
