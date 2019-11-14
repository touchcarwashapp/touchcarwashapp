package com.touchcarwash_driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.github.angads25.toggle.widget.LabeledSwitch;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    RelativeLayout navigationbar;
    Typeface face;
    ImageView option;
    TextView drivername,gooffline,address,km,txtpendingjob,txtupcomingjob,pendingjobcount,upcomingjobcount;
    ImageView driverpic,editkm,editaddress;
    LabeledSwitch status;
    final DatabaseHandler db=new DatabaseHandler(this);
    final UserDatabaseHandler udb=new UserDatabaseHandler(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (db.getscreenwidth().equalsIgnoreCase("")) {
            int width = getResources().getDisplayMetrics().widthPixels;
            db.addscreenwidth(width+"");
        }

        if (udb.get_userid().equalsIgnoreCase("")) {
            startActivity(new Intent(getApplicationContext(), Registration.class));
            finish();
            return;
        }

        face = Typeface.createFromAsset(getAssets(), "proxibold.otf");
        navigationbar = (RelativeLayout) findViewById(R.id.navigationbar);
        editaddress=findViewById(R.id.editaddress);
        drivername=findViewById(R.id.drivername);
        gooffline=findViewById(R.id.gooffline);
        address=findViewById(R.id.address);
        km=findViewById(R.id.km);
        txtpendingjob=findViewById(R.id.txtpendingjob);
        txtupcomingjob=findViewById(R.id.txtupcomingjob);
        pendingjobcount=findViewById(R.id.pendingjobcount);
        upcomingjobcount=findViewById(R.id.upcomingjobcount);

        driverpic=findViewById(R.id.driverpic);
        editkm=findViewById(R.id.editkm);
        status=findViewById(R.id.status);

        address.setText(db.get_address());
        km.setText("Within "+db.get_radiouskm()+" Km Works radious");

        if(db.get_address().equalsIgnoreCase(""))
        {

        }
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigview);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.txthelp)).setTypeface(face);
        option=findViewById(R.id.option);
        option.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    ((DrawerLayout) findViewById(R.id.drawer_layout)).openDrawer((int) GravityCompat.START);
                } catch (Exception e) {
                }
            }
        });

        Glide.with(this).load(Temp.weblink+"drivers/"+udb.get_userid()+".jpg").apply(RequestOptions.circleCropTransform().placeholder(R.drawable.placeholder).signature(new ObjectKey(udb.get_userimgsig()))).transition(DrawableTransitionOptions.withCrossFade()).into(driverpic);


        drivername.setText(udb.get_username());



    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
//        if (id == R.id.search) {
//            Static_Variable.viewd_pfle = 1;
//            startActivity(new Intent(getApplicationContext(), Search_Result.class));
//        } else if (id == R.id.myprofile) {
//            Static_Variable.viewd_pfle = 1;
//            startActivity(new Intent(getApplicationContext(), My_info.class));
//        } else if (id == R.id.fvrtupdates) {
//            Static_Variable.viewd_pfle = 1;
//            startActivity(new Intent(getApplicationContext(), Chintha_Fvrtusers.class));
//        }
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer((int) GravityCompat.START);
        return true;
    }
}
