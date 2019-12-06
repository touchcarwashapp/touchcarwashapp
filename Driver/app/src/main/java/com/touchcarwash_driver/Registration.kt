package com.touchcarwash_driver

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.touchcarwash_driver.db.UserDatabaseHandler
import com.touchcarwash_driver.dto.res.RegRes
import com.touchcarwash_driver.utils.tryToConnect
import com.zemose.network.RetrofitClientInstance
import com.zemose.network.UserService
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_registration.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Registration : AppCompatActivity() {

    lateinit var pd: ProgressDialog
    private var udb = UserDatabaseHandler(this)

    companion object {
        const val PERMISSION_ALL = 1

        fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
            if (context != null) {
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(context, permission) != 0) {
                        return false
                    }
                }
            }
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        pd = ProgressDialog(this)
        val face = Typeface.createFromAsset(assets, "proxibold.otf")
        text.text = Temp.apptitle
        mobile.typeface = face
        register.typeface = face
        FirebaseApp.initializeApp(this)
        val PERMISSIONS = arrayOf(android.Manifest.permission.INTERNET, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_NETWORK_STATE, android.Manifest.permission.CALL_PHONE, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)

        if (!hasPermissions(this, *PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL)
        }
        register.setOnClickListener {
            if (mobile.text.toString().equals("", ignoreCase = true)) {
                Toasty.info(applicationContext, "Please enter your registered mobile number", 1).show()
                mobile.requestFocus()
            } else {
                tryToConnect {
                    pd.setMessage("Please wait...")
                    pd.setCancelable(false)
                    pd.show()
                    FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            pd.dismiss()
                            Toasty.info(applicationContext, "Temporary error ! Please try after 10 minutes", 1).show()
                            return@OnCompleteListener
                        }
                        udb.addfcmid((task.result as InstanceIdResult).token)
                        registerUser(mobile.text.toString(), udb.getfcmid())
                    })
                }
            }
        }
    }

    private fun registerUser(mobile: String, fcmid: String) {
        try {
            val service = RetrofitClientInstance.retrofitInstance?.create(UserService::class.java)
            val call = service?.signUp("application/x-www-form-urlencoded", mobile, fcmid)

            call?.enqueue(object : Callback<RegRes> {
                override fun onFailure(call: Call<RegRes>, t: Throwable) {
                    pd.dismiss()
                    toast("Please Try again Later")
                }

                override fun onResponse(call: Call<RegRes>, response: Response<RegRes>) {
                    pd.dismiss()
                    val body = response.body()!!
                    udb.adduser(body.data.sn,body.data.name,body.data.washvehicleid,body.data.registernumber,body.data.imgsig,body.data.driveimgsig,null,null,null)
                    startActivity(intentFor<MainActivity>())
                    finish()
                }
            })
        } catch (e: Exception) {
            Log.d("rrrrr", Log.getStackTraceString(e))
        }
    }
}
