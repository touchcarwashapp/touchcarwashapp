package com.touchcarwash_driver

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.touchcarwash_driver.db.UserDatabaseHandler
import com.touchcarwash_driver.dto.res.AmountRes
import com.touchcarwash_driver.utils.tryToConnect
import com.zemose.network.RetrofitClientInstance
import com.zemose.network.UserService
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_account.*
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AccountActivity : AppCompatActivity() {

    lateinit var udb: UserDatabaseHandler
    private val TEZ_REQUEST_CODE = 123
    private val GOOGLE_TEZ_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        udb = UserDatabaseHandler(this)

        backBtn.setOnClickListener { onBackPressed() }

        pay.setOnClickListener {
            openGooglePay()
        }
    }


    private fun fetchAccountDetails() {
        val service = RetrofitClientInstance.retrofitInstance?.create(UserService::class.java)
        val call = service?.getAmounts("application/x-www-form-urlencoded", udb._userid)
        call?.enqueue(object : Callback<AmountRes> {
            override fun onFailure(call: Call<AmountRes>, t: Throwable) {
                //
            }

            override fun onResponse(call: Call<AmountRes>, response: Response<AmountRes>) {
                val body = response.body()
                val status = body?.response?.status
                val result = body?.data!!

                if (status.equals("Success", ignoreCase = true)) {
                    collected.text = result.collected.toString()
                    paid.text = result.recieved.toString()
                    pending.text = result.balance.toString()
                } else {
                    toast(Temp.tempproblem)
                }
            }
        })
    }

    private fun openGooglePay() {
        val uri = Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", "jeevasuthra@okicici")
                .appendQueryParameter("pn", "Kamasuthra App")
                .appendQueryParameter("tr", System.currentTimeMillis().toString() + "")
                .appendQueryParameter("tn", "App Full Version")
                .appendQueryParameter("am", "20.0")
                .appendQueryParameter("cu", "INR")
                .appendQueryParameter("url", "https://malayalamakamsuthrabilling.xyz")
                .build()
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri
        intent.setPackage(GOOGLE_TEZ_PACKAGE_NAME)
        if(intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, TEZ_REQUEST_CODE)
        } else {
            toast("error no such app")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TEZ_REQUEST_CODE) {
            if (data?.getStringExtra("Status").equals("SUCCESS", ignoreCase = true)) { //Recall myaccountdriver.php API
                Toasty.success(applicationContext, "Payment Sucesss", Toast.LENGTH_LONG).show()
            } else {
                Toasty.info(applicationContext, "Payment Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        tryToConnect {
            fetchAccountDetails()
        }
    }

}

