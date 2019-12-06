package com.touchcarwash_driver.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.touchcarwash_driver.R
import com.touchcarwash_driver.dto.res.CommonJobsRes
import kotlinx.android.synthetic.main.static_pending_card.view.*

class ConfirmedOrderAdapter(val confirmedOrders: ArrayList<ConfirmOrder>, val click: (Int) -> Unit) :
        RecyclerView.Adapter<ConfirmedOrderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val layout = LayoutInflater.from(p0.context).inflate(R.layout.static_pending_card, p0, false)
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
            view.washType.text = washType.substring(0,washType.length-2)

            var access = ""
            item.accessTypesList.forEach { access += "${it.typename}, " }
            view.accessories.text = access.substring(0,access.length-2)

            view.price.text = item.totalPrice

        }


    }
}


//data class

data class ConfirmOrder(
        val vehicleName: String,
        val vehicleImage: String,
        val vehicleImageSign: String,
        val washTypesList: ArrayList<CommonJobsRes.Data.Washtype>,
        val accessTypesList: ArrayList<CommonJobsRes.Data.Accessory>,
        val totalPrice: String
)
