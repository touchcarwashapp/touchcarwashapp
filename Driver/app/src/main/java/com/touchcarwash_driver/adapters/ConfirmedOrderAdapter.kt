package com.touchcarwash_driver.adapters

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.touchcarwash_driver.R
import com.touchcarwash_driver.StartTrackingActivity
import com.touchcarwash_driver.Temp
import com.touchcarwash_driver.TrackingDetailActivity
import com.touchcarwash_driver.dto.res.CommonJobsRes
import com.touchcarwash_driver.dto.res.DefaultRes
import com.touchcarwash_driver.utils.tryToConnect
import com.zemose.network.RetrofitClientInstance
import com.zemose.network.UserService
import kotlinx.android.synthetic.main.static_confirm_list_card.view.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ConfirmedOrderAdapter(val confirmedOrders: ArrayList<ConfirmOrder>, val click: (Int) -> Unit) :
        RecyclerView.Adapter<ConfirmedOrderAdapter.ViewHolder>() {

    companion object {
        const val VEHICLE_LOCATION = "vehicleLoc"
        const val CUSTOMER_DETAIL = "cutomerDetail"
        const val CUST_CONTACT = "customerContactOne"
        const val CUST_ALTERNATE = "customerContactTwo"
        const val VEHICLE_NAME = "vehicleName"
        const val VEHICLE_WASH = "vehicleWash"
        const val VEHICLE_ACCESS = "vehicleAccess"
        const val VEHICLE_AMOUNT = "vehicleAmount"
        const val VEHICLE_WASH_LIST = "vehicleWashList"
        const val VEHICLE_ACCESS_LIST = "vehicleAccessList"
        const val VEHICLE_IMAGE = "vehicleImage"
        const val VEHICLE_IMAGE_SIGN = "vehicleImgSign"
        const val ORDER_ID = "orderId"


    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val layout = LayoutInflater.from(p0.context).inflate(R.layout.static_confirm_list_card, p0, false)
        return ViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return confirmedOrders.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bind(confirmedOrders[p1], p1)
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: ConfirmOrder, pos: Int) {

            //setting vehicle image
            val rep = RequestOptions().placeholder(R.drawable.placeholder)
            Glide.with(view.context)
                    .load(item.vehicleImage)
                    .apply(rep)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .signature(ObjectKey(item.vehicleImageSign))
                    .into(view.carImage)
            view.carName.text = item.vehicleName

            var washType = ""
            item.washTypesList.forEach { washType += "${it.typename}, " }
            view.washType.text = washType.substring(0, washType.length - 2)

            var access = ""
            item.accessTypesList.forEach { access += "${it.typename}, " }
            view.accessories.text = access.substring(0, access.length - 2)

            view.price.text = item.totalPrice

            view.startBtn.setOnClickListener {
                (it.context as Activity).tryToConnect {
                    startTracking(
                            item.orderId,
                            ProgressDialog(it.context),
                            it.context,
                            item.customerLocation,
                            item.contactOne,
                            item.contactTwo,
                            item.vehicleName,
                            washType,
                            access,
                            item.totalPrice,
                            item.vehicleImage,
                            item.vehicleImageSign,
                            item.washTypesList,
                            item.accessTypesList,
                            pos)
                }
            }

        }
    }

    private fun startTracking(orderId: String,
                              progress: ProgressDialog,
                              context: Context,
                              custLoc: CommonJobsRes.Data.Location,
                              contactOne: String,
                              contactTwo: String,
                              vehicleName: String,
                              vehicleWash: String,
                              vehicleAccess: String,
                              vehicleAmount: String,
                              vehicleImage: String,
                              vehicleImageSign: String,
                              washTypeList: ArrayList<CommonJobsRes.Data.Washtype>,
                              accessTypeList: ArrayList<CommonJobsRes.Data.Accessory>,
                              pos: Int
    ) {
        progress.setMessage("Starting tracking please wait...")
        progress.setCancelable(false)
        progress.show()
        val service = RetrofitClientInstance.retrofitInstance?.create(UserService::class.java)
        val call = service?.startTracking("application/x-www-form-urlencoded", orderId)
        call?.enqueue(object : Callback<DefaultRes> {
            override fun onFailure(call: Call<DefaultRes>, t: Throwable) {
                progress.dismiss()
            }

            override fun onResponse(call: Call<DefaultRes>, response: Response<DefaultRes>) {
                val body = response.body()
                val status = body?.response?.status
                if (status.equals("Success", ignoreCase = true)) {
                    progress.dismiss()
                    //remove the order from array upon success
                    confirmedOrders.removeAt(pos)
                    if (custLoc.addresstype == "1") {
                        context.startActivity(context.intentFor<StartTrackingActivity>(
                                VEHICLE_LOCATION to custLoc.address.address,
                                CUST_CONTACT to contactOne,
                                CUST_ALTERNATE to contactTwo,
                                VEHICLE_NAME to vehicleName,
                                VEHICLE_WASH to vehicleWash,
                                VEHICLE_ACCESS to vehicleAccess,
                                VEHICLE_AMOUNT to vehicleAmount,
                                VEHICLE_IMAGE to vehicleImage,
                                VEHICLE_IMAGE_SIGN to vehicleImageSign,
                                VEHICLE_WASH_LIST to washTypeList,
                                VEHICLE_ACCESS_LIST to accessTypeList,
                                ORDER_ID to orderId
                        ))
                    } else if (custLoc.addresstype == "2") {
                        context.startActivity(context.intentFor<TrackingDetailActivity>(
                                CUSTOMER_DETAIL to custLoc,
                                CUST_CONTACT to contactOne,
                                CUST_ALTERNATE to contactTwo,
                                VEHICLE_NAME to vehicleName,
                                VEHICLE_WASH to vehicleWash,
                                VEHICLE_ACCESS to vehicleAccess,
                                VEHICLE_AMOUNT to vehicleAmount,
                                VEHICLE_IMAGE to vehicleImage,
                                VEHICLE_IMAGE_SIGN to vehicleImageSign,
                                VEHICLE_WASH_LIST to washTypeList,
                                VEHICLE_ACCESS_LIST to accessTypeList,
                                ORDER_ID to orderId
                        ))
                    }
                } else {
                    context.toast(Temp.tempproblem)
                }
            }
        })
    }
}


//data class

data class ConfirmOrder(
        val vehicleName: String,
        val vehicleImage: String,
        val vehicleImageSign: String,
        val washTypesList: ArrayList<CommonJobsRes.Data.Washtype>,
        val accessTypesList: ArrayList<CommonJobsRes.Data.Accessory>,
        val totalPrice: String,
        val orderId: String,
        val customerLocation: CommonJobsRes.Data.Location,
        val contactOne: String,
        val contactTwo: String
)
