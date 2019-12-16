package com.touchcarwash_driver

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.touchcarwash_driver.adapters.ConfirmedOrderAdapter
import com.touchcarwash_driver.dto.res.CommonJobsRes
import com.touchcarwash_driver.dto.res.DefaultRes
import com.touchcarwash_driver.utils.UserHelper
import com.touchcarwash_driver.utils.tryToConnect
import com.zemose.network.RetrofitClientInstance
import com.zemose.network.UserService
import kotlinx.android.synthetic.main.activity_start_tracking.*
import kotlinx.android.synthetic.main.activity_track_detail.*
import kotlinx.android.synthetic.main.activity_track_detail.reached
import org.jetbrains.anko.find
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TrackingDetailActivity : AppCompatActivity() {

    private val PERMISSIONS =
            arrayOf(Manifest.permission.CALL_PHONE)

    private val REQUEST_CALL_CODE = 1901
    private lateinit var phone: String
    private lateinit var progress: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_detail)

        progress = ProgressDialog(this)

        reached.setOnClickListener {
            val orderId = intent.getStringExtra(ConfirmedOrderAdapter.ORDER_ID)!!
            tryToConnect {
                reachedDestination(orderId)
            }
        }


        val customerDetails = intent?.getSerializableExtra(ConfirmedOrderAdapter.CUSTOMER_DETAIL) as CommonJobsRes.Data.Location
        name.text = customerDetails.address.name
        customerPlace.text = customerDetails.address.place
        customerAddress.text = customerDetails.address.address
        customerContactOne.text = intent?.getStringExtra(ConfirmedOrderAdapter.CUST_CONTACT)
        customerAlternate.text = intent?.getStringExtra(ConfirmedOrderAdapter.CUST_ALTERNATE)
        backBtn.setOnClickListener { onBackPressed() }
        phoneContact.setOnClickListener {
            phone = customerContactOne.text.toString()
            makePhoneCall(phone)
        }
        phoneAlternate.setOnClickListener {
            phone = customerAlternate.text.toString()
            makePhoneCall(phone)
        }


    }

    private fun initiateStartWash(orderId: String) {
        val dialog = UserHelper.createDialog(this, 0.9f, 0.5f, R.layout.static_start_wash_card)
        val name = dialog.find<TextView>(R.id.carName)
        val image = dialog.find<ImageView>(R.id.carImage)
        val wash = dialog.find<TextView>(R.id.washType)
        val accessory = dialog.find<TextView>(R.id.accessories)
        val amount = dialog.find<TextView>(R.id.price)
        val startWashBtn = dialog.find<Button>(R.id.startWashBtn)


        val vehicleName = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_NAME)
        val vehicleWash = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_WASH)
        val vehicleAccess = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_ACCESS)
        val vehicleAmount = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_AMOUNT)
        val vehicleImage = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_IMAGE)
        val vehicleImageSign = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_IMAGE_SIGN)!!
        val washTypesList = intent.getSerializableExtra(ConfirmedOrderAdapter.VEHICLE_WASH_LIST) as ArrayList<CommonJobsRes.Data.Washtype>
        val accessTypesList = intent.getSerializableExtra(ConfirmedOrderAdapter.VEHICLE_ACCESS_LIST) as ArrayList<CommonJobsRes.Data.Accessory>

        //setting vehicle image
        val rep = RequestOptions().placeholder(R.drawable.placeholder)
        Glide.with(this)
                .load(vehicleImage)
                .apply(rep)
                .transition(DrawableTransitionOptions.withCrossFade())
                .signature(ObjectKey(vehicleImageSign))
                .into(image)
        name.text = vehicleName
        wash.text = vehicleWash
        accessory.text = vehicleAccess
        amount.text = UserHelper.convertToPrice(this, vehicleAmount.toDouble())

        startWashBtn.setOnClickListener {
            startActivity(intentFor<UploadImagesActivity>(
                    ConfirmedOrderAdapter.VEHICLE_NAME to vehicleName,
                    ConfirmedOrderAdapter.VEHICLE_WASH to vehicleWash,
                    ConfirmedOrderAdapter.VEHICLE_ACCESS to vehicleAccess,
                    ConfirmedOrderAdapter.VEHICLE_AMOUNT to vehicleAmount,
                    ConfirmedOrderAdapter.VEHICLE_IMAGE to vehicleImage,
                    ConfirmedOrderAdapter.VEHICLE_IMAGE_SIGN to vehicleImageSign,
                    ConfirmedOrderAdapter.VEHICLE_WASH_LIST to washTypesList,
                    ConfirmedOrderAdapter.VEHICLE_ACCESS_LIST to accessTypesList,
                    ConfirmedOrderAdapter.ORDER_ID to orderId)
                    .setAction(StartTrackingActivity.ACTION_IMG_UPLOAD))
        }
    }



    private fun reachedDestination(orderId: String) {
        val service = RetrofitClientInstance.retrofitInstance?.create(UserService::class.java)
        val call = service?.reachedDestination("application/x-www-form-urlencoded", orderId, "-1")
        call?.enqueue(object : Callback<DefaultRes> {
            override fun onFailure(call: Call<DefaultRes>, t: Throwable) {
                progress.dismiss()
            }

            override fun onResponse(call: Call<DefaultRes>, response: Response<DefaultRes>) {
                val body = response.body()
                val status = body?.response?.status
                if (status.equals("Success", ignoreCase = true)) {
                    progress.dismiss()
                    //show start wash dialog
                    initiateStartWash(orderId)
                } else {
                    progress.dismiss()
                    toast(Temp.tempproblem)
                }
            }
        })
    }

    private fun makePhoneCall(phone: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CALL_CODE)
        } else {
            AlertDialog.Builder(this)
                    .setTitle("Are you sure you want to call this number ?")
                    .setPositiveButton("yes") { _, _ ->
                        val callIntent = Intent(Intent.ACTION_CALL)
                        callIntent.data = Uri.parse("tel:$phone")
                        startActivity(callIntent)
                    }
                    .setNegativeButton("cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()


        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CALL_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.isNotEmpty()) {
                makePhoneCall(phone)
            }
        }
    }


}
