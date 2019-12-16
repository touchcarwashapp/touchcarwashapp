package com.touchcarwash_driver.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.touchcarwash_driver.R
import kotlinx.android.synthetic.main.static_checkbox_card.view.*
import kotlinx.android.synthetic.main.static_items_card.view.itemName
import java.io.Serializable

class ItemListAdapter(val washTypes: ArrayList<Item>, val click: (Int,Boolean) -> Unit) :
        RecyclerView.Adapter<ItemListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val layout = LayoutInflater.from(p0.context).inflate(R.layout.static_checkbox_card, p0, false)
        return ViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return washTypes.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bind(washTypes[p1], p1)
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: Item, pos: Int) {
            view.itemName.text = item.name
            view.itemDesc.text = item.description

            view.checkBox.setOnCheckedChangeListener { compoundButton, b ->
                click(pos,b)
            }
        }

    }

}

data class Item(val id: String, val name: String, val description: String, val offerPrice: String, var isChecked: Boolean): Serializable