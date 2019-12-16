package com.touchcarwash_driver

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.touchcarwash_driver.adapters.ConfirmOrder
import com.touchcarwash_driver.adapters.ConfirmedOrderAdapter
import com.touchcarwash_driver.adapters.PendingOrder
import com.touchcarwash_driver.adapters.PendingOrderAdapter
import com.touchcarwash_driver.db.UserDatabaseHandler
import com.touchcarwash_driver.dto.res.CommonJobsRes
import com.zemose.network.RetrofitClientInstance
import com.zemose.network.UserService
import kotlinx.android.synthetic.main.activity_common_order.*
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommonOrder : AppCompatActivity() {

    lateinit var udb: UserDatabaseHandler
    lateinit var pd: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_order)

        udb = UserDatabaseHandler(this)
        pd = ProgressDialog(this)

        when (intent.action) {
            MainActivity.PENDING_ACTION -> {
                fetchPendingJobs()
            }
            MainActivity.CONFIRM_ACTION -> {
                fetchConfirmedJobs()
            }
        }


    }

    private fun fetchConfirmedJobs() {
        val service = RetrofitClientInstance.retrofitInstance?.create(UserService::class.java)
        val call = service?.getConfirmedList("application/x-www-form-urlencoded", udb._userid)
        call?.enqueue(object : Callback<CommonJobsRes> {
            override fun onFailure(call: Call<CommonJobsRes>, t: Throwable) {
                pd.dismiss()
            }

            override fun onResponse(call: Call<CommonJobsRes>, response: Response<CommonJobsRes>) {
                val body = response.body()
                val status = body?.response?.status
                val result = body?.data!!
                if (status.equals("Success", ignoreCase = true)) {
                    pd.dismiss()
                    val confirmedOrdersList = ArrayList<ConfirmOrder>()
                    for (element in result) {
                        confirmedOrdersList.add(
                                ConfirmOrder(
                                        element.vehicledetails.vehiclename,
                                        "${Temp.weblink}custvehicleimg/${element.vehicledetails.custvehcileid}.jpg",
                                        element.vehicledetails.custvehicleimgsig,
                                        element.washtypes as ArrayList<CommonJobsRes.Data.Washtype>,
                                        element.accessories as ArrayList<CommonJobsRes.Data.Accessory>,
                                        element.totalamount,
                                        element.orderdetails.orderid,
                                        element.location,
                                        element.customerDetails.contact1,
                                        element.customerDetails.contact2
                                ))
                    }

                    displayConfirmedJobs(confirmedOrdersList)

                } else {
                    pd.dismiss()
                    toast(Temp.tempproblem)
                }
            }
        })
    }

    private fun fetchPendingJobs() {
        val service = RetrofitClientInstance.retrofitInstance?.create(UserService::class.java)
        val call = service?.getPendingList("application/x-www-form-urlencoded", udb._userid)
        call?.enqueue(object : Callback<CommonJobsRes> {
            override fun onFailure(call: Call<CommonJobsRes>, t: Throwable) {
                pd.dismiss()
            }

            override fun onResponse(call: Call<CommonJobsRes>, response: Response<CommonJobsRes>) {
                val body = response.body()
                val status = body?.response?.status
                val result = body?.data!!
                if (status.equals("Success", ignoreCase = true)) {
                    pd.dismiss()
                    val pendingOrdersList = ArrayList<PendingOrder>()
                    for (element in result) {
                        pendingOrdersList.add(
                                PendingOrder(
                                        element.vehicledetails.vehiclename,
                                        "${Temp.weblink}custvehicleimg/${element.vehicledetails.custvehcileid}.jpg",
                                        element.vehicledetails.custvehicleimgsig,
                                        element.washtypes as ArrayList<CommonJobsRes.Data.Washtype>,
                                        element.accessories as ArrayList<CommonJobsRes.Data.Accessory>,
                                        element.totalamount,
                                        element.location,
                                        element.customerDetails.name,
                                        element.customerDetails.place,
                                        element.customerDetails.contact1,
                                        element.customerDetails.contact2,
                                        element.customerDetails.pincode,
                                        element.orderdetails.orderid
                                ))
                    }

                    displayPendingJobs(pendingOrdersList)

                } else {
                    pd.dismiss()
                    toast(Temp.tempproblem)
                }
            }
        })
    }

    private fun displayPendingJobs(jobs: ArrayList<PendingOrder>) {
        orderHead.text = "Pending Orders"
        if (jobs.isNotEmpty()) {
            jobsListRecycler.visibility = View.VISIBLE
            backBtn.setOnClickListener { onBackPressed() }
            jobsListRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            val adapter = PendingOrderAdapter(jobs) { pos ->
                toast("$pos")
            }
            jobsListRecycler.adapter = adapter
        } else {
            emptyLayoutContainer.visibility = View.VISIBLE
        }


    }

    private fun displayConfirmedJobs(jobs: ArrayList<ConfirmOrder>) {
        emptyText.text = "You have no confirmed orders.."
        orderHead.text = "Confirmed Orders"
        if (jobs.isNotEmpty()) {
            jobsListRecycler.visibility = View.VISIBLE
            backBtn.setOnClickListener { onBackPressed() }
            jobsListRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            val adapter = ConfirmedOrderAdapter(jobs) { pos ->

            }
            jobsListRecycler.adapter = adapter
        } else {
            emptyLayoutContainer.visibility = View.VISIBLE
        }

    }

}
