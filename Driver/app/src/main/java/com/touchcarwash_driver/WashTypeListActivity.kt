package com.touchcarwash_driver

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.touchcarwash_driver.adapters.ConfirmedOrderAdapter
import com.touchcarwash_driver.adapters.Item
import com.touchcarwash_driver.adapters.ItemListAdapter
import com.touchcarwash_driver.dto.res.CommonJobsRes
import kotlinx.android.synthetic.main.activity_wash_type_list.*
import org.jetbrains.anko.intentFor

class WashTypeListActivity : AppCompatActivity() {

    companion object {
        const val GROUP_IMG_UPLOAD_AFTER = "AfterWash"
        const val CHECKED_WASH_ITEMS = "checkedWashItems"
        const val CHECKED_ACCESS_ITEMS = "checkedAccessItems"
    }

    lateinit var washItems: ArrayList<Item>
    lateinit var accessItems: ArrayList<Item>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wash_type_list)

        val washTypesList = intent.getSerializableExtra(ConfirmedOrderAdapter.VEHICLE_WASH_LIST) as ArrayList<CommonJobsRes.Data.Washtype>
        val accessTypesList = intent.getSerializableExtra(ConfirmedOrderAdapter.VEHICLE_ACCESS_LIST) as ArrayList<CommonJobsRes.Data.Accessory>
        val orderId = intent.getStringExtra(ConfirmedOrderAdapter.ORDER_ID)!!
        val washTime = intent.getStringExtra(CarWashingActivity.WASH_TIME)!!

        listWashTypes(washTypesList)
        listAccessTypes(accessTypesList)

        finishBtn.setOnClickListener {
            startActivity(intentFor<UploadImagesActivity>(
                    CHECKED_WASH_ITEMS to washItems,
                    CHECKED_ACCESS_ITEMS to accessItems,
                    ConfirmedOrderAdapter.ORDER_ID to orderId,
                    CarWashingActivity.WASH_TIME to washTime
            ).setAction(GROUP_IMG_UPLOAD_AFTER))
        }

    }

    private fun listAccessTypes(accessTypes: ArrayList<CommonJobsRes.Data.Accessory>) {
        if (accessTypes.isNotEmpty()) {

            accessItems = arrayListOf()

            for (i in accessTypes) {
                accessItems.add(Item(i.typeid,i.typename, i.discription, i.offerprice, false))
            }
            accessRecycle.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            val adapter = ItemListAdapter(accessItems) { pos, isChecked ->
                accessItems[pos].isChecked = isChecked
            }

            accessRecycle.adapter = adapter
        }
    }

    private fun listWashTypes(washTypes: ArrayList<CommonJobsRes.Data.Washtype>) {
        if (washTypes.isNotEmpty()) {

            washItems = arrayListOf()

            for (i in washTypes) {
                washItems.add(Item(i.typeid, i.typename, i.discription, i.offerprice, false))
            }
            washTypeRecycle.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            val adapter = ItemListAdapter(washItems) { pos, isChecked ->
                washItems[pos].isChecked = isChecked
            }

            washTypeRecycle.adapter = adapter
        }

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
