package com.touchcarwash_driver.Adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.touchcarwash_driver.R
import java.util.*

class ConfirmOrderAdapter(val bookingsList: ArrayList<String>, val click: (Int) -> Unit) :
        RecyclerView.Adapter<ConfirmOrderAdapter.ViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val layout = LayoutInflater.from(p0.context).inflate(R.layout.static_confirm_card, p0, false)
        return ViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return bookingsList.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bind(bookingsList[p1], p1)
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: String, pos: Int) {
//            val rep = RequestOptions().placeholder(R.drawable.placeholder)
//            Glide.with(view.context).load(Connectionpaths.baseurl + item.productImage)
//                    .apply(rep)
//                    .transition(DrawableTransitionOptions.withCrossFade(300))
//                    .into()
        }
    }
}

//data class Orders()
