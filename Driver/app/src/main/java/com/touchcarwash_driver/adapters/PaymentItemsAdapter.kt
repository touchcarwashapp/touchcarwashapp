package com.touchcarwash_driver.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.touchcarwash_driver.R
import kotlinx.android.synthetic.main.static_items_card.view.*

class PaymentsItemsAdapter(val ItemTypes: ArrayList<PaymentItem>) :
        RecyclerView.Adapter<PaymentsItemsAdapter.ViewHolder>() {


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val layout = LayoutInflater.from(p0.context).inflate(R.layout.static_items_card, p0, false)
        return ViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return ItemTypes.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bind(ItemTypes[p1], p1)
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: PaymentItem, pos: Int) {
            view.itemName.text = item.name
            view.price.text = item.offerPrice
            view.itemType.text = item.type
        }

    }

}

data class PaymentItem(val name: String, val offerPrice: String, var type: String)