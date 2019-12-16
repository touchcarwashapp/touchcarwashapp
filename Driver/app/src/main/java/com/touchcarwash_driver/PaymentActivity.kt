package com.touchcarwash_driver

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.touchcarwash_driver.adapters.ConfirmedOrderAdapter
import com.touchcarwash_driver.adapters.Item
import com.touchcarwash_driver.adapters.PaymentItem
import com.touchcarwash_driver.adapters.PaymentsItemsAdapter
import com.touchcarwash_driver.db.UserDatabaseHandler
import com.touchcarwash_driver.dto.res.DefaultRes
import com.touchcarwash_driver.utils.UserHelper
import com.touchcarwash_driver.utils.tryToConnect
import com.zemose.network.RetrofitClientInstance
import com.zemose.network.UserService
import kotlinx.android.synthetic.main.activity_payment.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.find
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class PaymentActivity : AppCompatActivity() {

    lateinit var paymentItemsList: ArrayList<PaymentItem>
    private var totalAmount = 0.0
    lateinit var progress: ProgressDialog
    lateinit var udb: UserDatabaseHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        progress = ProgressDialog(this)
        udb = UserDatabaseHandler(this)

        //extra amount check
        extraAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (!p0.isNullOrEmpty()) {
                    var amount = 0.0
                    var balanceAmount = 0.0
                    if (discount.text.toString().isNotEmpty()) {
                        amount = (totalAmount + p0.toString().toDouble()) - discount.text.toString().toDouble()
                    } else {
                        amount = totalAmount + p0.toString().toDouble()
                    }

                    if (!received.text.isNullOrEmpty()) {
                        balanceAmount = amount - UserHelper.trimCurrency(received.text.toString())
                    } else {
                        balanceAmount = amount
                    }
                    grandTotal.setText(UserHelper.convertToPrice(this@PaymentActivity, amount))
                    balance.setText(UserHelper.convertToPrice(this@PaymentActivity, balanceAmount))
                } else {

                    var amount = 0.0
                    var balanceAmount = 0.0
                    if (discount.text.toString().isNotEmpty()) {
                        amount = (totalAmount) - discount.text.toString().toDouble()
                    } else {
                        amount = totalAmount
                    }

                    if (!received.text.isNullOrEmpty()) {
                        balanceAmount = amount - UserHelper.trimCurrency(received.text.toString())
                    } else {
                        balanceAmount = amount
                    }
                    grandTotal.setText(UserHelper.convertToPrice(this@PaymentActivity, amount))
                    balance.setText(UserHelper.convertToPrice(this@PaymentActivity, balanceAmount))
                }

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //
            }

        })

        //discount amount check
        discount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (!p0.isNullOrEmpty()) {
                    var amount = 0.0
                    var balanceAmount = 0.0
                    if (extraAmount.text.toString().isNotEmpty()) {
                        amount = (totalAmount + extraAmount.text.toString().toDouble()) - p0.toString().toDouble()
                    } else {
                        amount = totalAmount - p0.toString().toDouble()
                    }

                    if (!received.text.isNullOrEmpty()) {
                        balanceAmount = amount - received.text.toString().toDouble()
                    } else {
                        balanceAmount = amount
                    }
                    grandTotal.setText(UserHelper.convertToPrice(this@PaymentActivity, amount))
                    balance.setText(UserHelper.convertToPrice(this@PaymentActivity, balanceAmount))
                } else {
                    var amount = 0.0
                    var balanceAmount = 0.0
                    if (extraAmount.text.toString().isNotEmpty()) {
                        amount = (totalAmount + extraAmount.text.toString().toDouble())
                    } else {
                        amount = totalAmount
                    }

                    if (!received.text.isNullOrEmpty()) {
                        balanceAmount = amount - received.text.toString().toDouble()
                    } else {
                        balanceAmount = amount
                    }
                    grandTotal.setText(UserHelper.convertToPrice(this@PaymentActivity, amount))
                    balance.setText(UserHelper.convertToPrice(this@PaymentActivity, balanceAmount))
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //
            }

        })

        //received amount check
        received.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (!p0.isNullOrEmpty()) {
                    var balanceAmount = 0.0
                    if (!received.text.isNullOrEmpty()) {
                        balanceAmount = UserHelper.trimCurrency(grandTotal.text.toString()) - received.text.toString().toDouble()
                    } else {
                        balanceAmount = UserHelper.trimCurrency(grandTotal.text.toString())
                    }
                    balance.setText(UserHelper.convertToPrice(this@PaymentActivity, balanceAmount))
                } else {
                    val balanceAmount = UserHelper.trimCurrency(grandTotal.text.toString())
                    balance.setText(UserHelper.convertToPrice(this@PaymentActivity, balanceAmount))
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //
            }

        })

        val washTypesChecked = intent.getSerializableExtra(WashTypeListActivity.CHECKED_WASH_ITEMS) as ArrayList<Item>
        val accessTypesChecked = intent.getSerializableExtra(WashTypeListActivity.CHECKED_ACCESS_ITEMS) as ArrayList<Item>
        val orderId = intent.getStringExtra(ConfirmedOrderAdapter.ORDER_ID)!!
        val washTime = intent.getStringExtra(CarWashingActivity.WASH_TIME)!!
        setPaymentItems(washTypesChecked, accessTypesChecked)

        backBtn.setOnClickListener { onBackPressed() }

        payment.setOnClickListener {
            if (!received.text.isNullOrEmpty()) {
                tryToConnect {

                    var washTypes = ""
                    var accessTypes = ""

                    washTypesChecked.forEach { washTypes += "${it.id}," }
                    washTypes = washTypes.substring(0, washTypes.length - 1)

                    accessTypesChecked.forEach { accessTypes += "${it.id}," }
                    accessTypes = accessTypes.substring(0, accessTypes.length - 1)

                    finishWash(
                            orderId,
                            washTypes,
                            accessTypes,
                            getImages("/CarWashImages/BeforeWash/"),
                            getImages("/CarWashImages/AfterWash/"),
                            UserHelper.trimCurrency(priceTotal.text.toString()).toString(),
                            extraAmount.text.toString(),
                            discount.text.toString(),
                            UserHelper.trimCurrency(grandTotal.text.toString()).toString(),
                            received.text.toString(),
                            UserHelper.trimCurrency(balance.text.toString()).toString(),
                            washTime,
                            this
                    )
                }
            }
        }
    }

    private fun setPaymentItems(washTypesChecked: ArrayList<Item>, accessTypesChecked: ArrayList<Item>) {
        paymentItemsList = arrayListOf()

        for (i in washTypesChecked) {
            if (i.isChecked) {
                paymentItemsList.add(
                        PaymentItem(
                                i.name,
                                i.offerPrice,
                                "wash type"
                        )
                )
            }
        }

        for (i in accessTypesChecked) {
            if (i.isChecked) {
                paymentItemsList.add(
                        PaymentItem(
                                i.name,
                                i.offerPrice,
                                "accessories"
                        )
                )
            }
        }

        paymentRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val adapter = PaymentsItemsAdapter(paymentItemsList)
        paymentRecycler.adapter = adapter

        for (i in paymentItemsList) {
            totalAmount += i.offerPrice.toDouble()
        }

        priceTotal.text = UserHelper.convertToPrice(this, totalAmount)
        grandTotal.setText(UserHelper.convertToPrice(this, totalAmount))
        balance.setText(UserHelper.convertToPrice(this, totalAmount))

    }

    private fun finishWash(
            id: String,
            washTypes: String,
            accessTypes: String,
            beforeWash: Array<File>,
            afterWash: Array<File>,
            netTotal: String,
            extraAmount: String,
            discount: String,
            grandTotal: String,
            received: String,
            balance: String,
            washTime: String,
            ctx: Context) {

        progress.setMessage("Paying and finishing please wait...")
        progress.setCancelable(false)
        progress.show()
        // extra data
        val orderId = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), id)
        val netTotal = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), netTotal)
        val extraAmount = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), extraAmount)
        val discount = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), discount)
        val grandTotal = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), grandTotal)
        val received = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), received)
        val balance = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), balance)
        val washTime = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), washTime)
        val washTypes = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), washTypes)
        val accessTypes = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), accessTypes)
        val userId = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), udb._userid)


        val service = RetrofitClientInstance.retrofitInstance?.create(UserService::class.java)

        val call = service?.finishWash(
                orderId,
                checkPhotoExist(beforeWash, "FR.jpg"),
                checkPhotoExist(beforeWash, "BA.jpg"),
                checkPhotoExist(beforeWash, "RS.jpg"),
                checkPhotoExist(beforeWash, "LS.jpg"),
                checkPhotoExist(beforeWash, "TP.jpg"),
                checkPhotoExist(afterWash, "FR.jpg"),
                checkPhotoExist(afterWash, "BA.jpg"),
                checkPhotoExist(afterWash, "RS.jpg"),
                checkPhotoExist(afterWash, "LS.jpg"),
                checkPhotoExist(afterWash, "TP.jpg"),
                createImageReqObj(beforeWash, "bf_front", "FR.jpg"),
                createImageReqObj(beforeWash, "bf_back", "BA.jpg"),
                createImageReqObj(beforeWash, "bf_right", "RS.jpg"),
                createImageReqObj(beforeWash, "bf_left", "LS.jpg"),
                createImageReqObj(beforeWash, "bf_top", "TP.jpg"),
                createImageReqObj(afterWash, "af_front", "FR.jpg"),
                createImageReqObj(afterWash, "af_back", "BA.jpg"),
                createImageReqObj(afterWash, "af_right", "RS.jpg"),
                createImageReqObj(afterWash, "af_left", "LS.jpg"),
                createImageReqObj(afterWash, "af_top", "TP.jpg"),
                washTypes,
                accessTypes,
                netTotal,
                extraAmount,
                discount,
                grandTotal,
                received,
                balance,
                washTime,
                userId
        )

        call?.enqueue(object : Callback<DefaultRes> {
            override fun onFailure(call: Call<DefaultRes>, t: Throwable) {
                progress.dismiss()
                toast("Sorry ! Unable to update , Please try later")
            }

            override fun onResponse(call: Call<DefaultRes>, response: Response<DefaultRes>) {
                val body = response.body()
                val status = body?.response?.status
                if (status.equals("Success", ignoreCase = true)) {
                    //delete images from local
                    deleteImages("/CarWashImages/BeforeWash/")
                    deleteImages("/CarWashImages/AfterWash/")
                    progress.dismiss()

                    val dialog = UserHelper.createDialog(ctx, 0.9f, 0.5f, R.layout.rate_dialog)
                    val rateBtn = dialog.find<Button>(R.id.rate)
                    val cancelBtn = dialog.find<Button>(R.id.cancel)
                    val description = dialog.find<EditText>(R.id.rateDesc)

                    rateBtn.setOnClickListener {
                        tryToConnect {
                            rateUs(id, description.text.toString(), dialog)
                        }
                    }

                    cancelBtn.setOnClickListener { dialog.dismiss() }

                } else {
                    progress.dismiss()
                    toast(Temp.tempproblem)
                }
            }
        })
    }

    private fun rateUs(orderId: String, desc: String, dialog: Dialog) {
        val service = RetrofitClientInstance.retrofitInstance?.create(UserService::class.java)

        val call = service?.rateDriver("application/x-www-form-urlencoded", orderId, desc)

        call?.enqueue(object : Callback<DefaultRes> {
            override fun onFailure(call: Call<DefaultRes>, t: Throwable) {
                progress.dismiss()
                toast("Sorry ! Unable to update , Please try later")
            }

            override fun onResponse(call: Call<DefaultRes>, response: Response<DefaultRes>) {
                val body = response.body()
                val status = body?.response?.status
                if (status.equals("Success", ignoreCase = true)) {
                    progress.dismiss()
                    dialog.dismiss()
                    startActivity(intentFor<MainActivity>().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    finish()
                } else {
                    progress.dismiss()
                    toast(Temp.tempproblem)
                }
            }
        })
    }

    private fun getImages(directoryPath: String): Array<File> {
        val folder = File(Environment.getExternalStorageDirectory().toString() + directoryPath)
        val images = if (folder.exists()) {
            folder.listFiles { _, name ->
                name.endsWith(".jpg")
            }
        } else {
            arrayOf(File(""))
        }
        return images!!
    }

    private fun deleteImages(directoryPath: String) {
        val folder = File(Environment.getExternalStorageDirectory().toString() + directoryPath)
        val images = if (folder.exists()) {
            folder.listFiles { _, name ->
                name.endsWith(".jpg")
            }
        } else {
            arrayOf(File(""))
        }
        for (i in images) {
            val image = File(Environment.getExternalStorageDirectory().toString() + directoryPath + i.name)
            image.delete()
        }
    }

    private fun checkPhotoExist(images: Array<File>, imageName: String): RequestBody {
        val exist = if ((images.find { it.name == imageName }) != null) {
            "1"
        } else {
            "0"
        }
        return RequestBody.create("multipart/form-data".toMediaTypeOrNull(), exist)
    }

    private fun createImageReqObj(images: Array<File>, paramName: String, imageName: String): MultipartBody.Part {
        val index = images.indexOfFirst { it.name == imageName }
        val reqFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), images[index])
        return MultipartBody.Part.createFormData(paramName, images[index].name, reqFile)
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
