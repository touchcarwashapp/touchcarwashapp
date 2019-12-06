package com.touchcarwash_driver.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.touchcarwash_driver.MapActivity
import com.touchcarwash_driver.R
import com.touchcarwash_driver.dto.res.CommonJobsRes
import com.touchcarwash_driver.utils.UserHelper
import kotlinx.android.synthetic.main.static_pending_card.view.*
import org.jetbrains.anko.find
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import org.w3c.dom.Text

class PendingOrderAdapter(val pendingPendingOrders: ArrayList<PendingOrder>, val click: (Int) -> Unit) :
        RecyclerView.Adapter<PendingOrderAdapter.ViewHolder>() {

    companion object {
        const val CUST_MAP_LAT = "cust_map_lat"
        const val CUST_MAP_LNG = "cust_map_lng"
    }


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val layout = LayoutInflater.from(p0.context).inflate(R.layout.static_pending_card, p0, false)
        return ViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return pendingPendingOrders.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bind(pendingPendingOrders[p1], p1)
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: PendingOrder, pos: Int) {

            //setting vehicle image
            val rep = RequestOptions().placeholder(R.drawable.placeholder)
            Glide.with(view.context)
                    .load(item.vehicleImage)
                    .apply(rep)
                    .transition(withCrossFade())
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

            view.customerDetailsBtn.setOnClickListener {
                viewDetails(it.context, item.customerName, item.customerPlace, item.customerContactOne, item.customerContactTwo, item.customerPinCode)
            }

            view.locationBtn.setOnClickListener {
                viewLocation(item.addressObj, it.context)
            }

        }

    }


    private fun viewDetails(context: Context, name: String, place: String, phone: String, alterPhone: String, pincode: String) {
        val dialog = UserHelper.createDialog(context, 0.9f, 0.6f, R.layout.dialog_customer_details)
        val custName = dialog.find<TextView>(R.id.customerName)
        val custPlace = dialog.find<TextView>(R.id.place)
        val custPhone = dialog.find<TextView>(R.id.phone)
        val alternatePhone = dialog.find<TextView>(R.id.alternatePhone)
        val pin = dialog.find<TextView>(R.id.pincode)
        val closeBtn = dialog.find<Button>(R.id.closeBtn)

        custName.text = name
        custPlace.text = place
        custPhone.text = phone
        alternatePhone.text = alterPhone
        pin.text = pincode

        closeBtn.setOnClickListener { dialog.dismiss() }
    }

    private fun viewLocation(addressObj: CommonJobsRes.Data.Location,context: Context) {

        when(addressObj.addresstype) {
            "1" -> {
                val addressSplit = addressObj.address.address.split(",")
                context.startActivity(context.intentFor<MapActivity>(CUST_MAP_LAT to addressSplit[0], CUST_MAP_LNG to addressSplit[1]))
            }
            "2" -> {
                viewDetails(context,addressObj.address.name,addressObj.address.place,"NA","NA",addressObj.address.pincode)
            }
        }
    }
}


//data class

data class PendingOrder(
        val vehicleName: String,
        val vehicleImage: String,
        val vehicleImageSign: String,
        val washTypesList: ArrayList<CommonJobsRes.Data.Washtype>,
        val accessTypesList: ArrayList<CommonJobsRes.Data.Accessory>,
        val totalPrice: String,
        val addressObj: CommonJobsRes.Data.Location,
        val customerName: String,
        val customerPlace: String,
        val customerContactOne: String,
        val customerContactTwo: String,
        val customerPinCode: String
)