package com.touchcarwash_driver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Chronometer
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.touchcarwash_driver.adapters.ConfirmedOrderAdapter
import com.touchcarwash_driver.dto.res.CommonJobsRes
import kotlinx.android.synthetic.main.activity_car_washing.*
import org.jetbrains.anko.find
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.runOnUiThread
import java.util.*

class CarWashingActivity : AppCompatActivity() {

    companion object {
        const val WASH_TIME = "washTime"
    }

    lateinit var chronometer: Chronometer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_washing)


        //intent data passing between activities
        val vehicleName = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_NAME)
        val vehicleWash = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_WASH)
        val vehicleAccess = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_ACCESS)
        val vehicleAmount = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_AMOUNT)
        val vehicleImage = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_IMAGE)
        val vehicleImageSign = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_IMAGE_SIGN)!!
        val washTypesList = intent.getSerializableExtra(ConfirmedOrderAdapter.VEHICLE_WASH_LIST) as ArrayList<CommonJobsRes.Data.Washtype>
        val accessTypesList = intent.getSerializableExtra(ConfirmedOrderAdapter.VEHICLE_ACCESS_LIST) as ArrayList<CommonJobsRes.Data.Accessory>
        val orderId = intent.getStringExtra(ConfirmedOrderAdapter.ORDER_ID)


        //setting vehicle image
        val rep = RequestOptions().placeholder(R.drawable.placeholder)
        Glide.with(this)
                .load(vehicleImage)
                .apply(rep)
                .transition(DrawableTransitionOptions.withCrossFade())
                .signature(ObjectKey(vehicleImageSign))
                .into(carImage)
        carName.text = vehicleName
        washType.text = vehicleWash
        accessories.text = vehicleAccess

        // start the chronometer
        chronometer = find<Chronometer>(R.id.timerUp)
        chronometer.start()

        washCompleteBtn.setOnClickListener {
            chronometer.stop()
            startActivity(intentFor<WashTypeListActivity>(
                    ConfirmedOrderAdapter.VEHICLE_AMOUNT to vehicleAmount,
                    ConfirmedOrderAdapter.VEHICLE_WASH_LIST to washTypesList,
                    ConfirmedOrderAdapter.VEHICLE_ACCESS_LIST to accessTypesList,
                    ConfirmedOrderAdapter.ORDER_ID to orderId,
                    WASH_TIME to chronometer.text
            ))
        }
    }

    override fun onStop() {
        super.onStop()
        chronometer.stop()
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
                .setMessage("Cancel your order and quit?")
                .setCancelable(false)
                .setPositiveButton("Quit") { _, i ->
                    startActivity(intentFor<MainActivity>()
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
                .setNegativeButton("Cancel") { dialogInterface, i ->
                    dialogInterface.dismiss()
                }.show()
    }

}
