package com.touchcarwash_driver

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.touchcarwash_driver.adapters.ConfirmedOrderAdapter
import com.touchcarwash_driver.adapters.Item
import com.touchcarwash_driver.dto.res.CommonJobsRes
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_image_upload.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.io.IOException

class UploadImagesActivity : AppCompatActivity() {

    companion object {
        const val FRONT_IMG_TITLE = "Pick Front Image"
        const val BACK_IMG_TITLE = "Pick Back Image"
        const val RIGHT_IMG_TITLE = "Pick Right-Side Image"
        const val LEFT_IMG_TITLE = "Pick Left-Side Image"
        const val TOP_IMG_TITLE = "Pick Top Image"
        const val FRONT_IMG_INT = 1
        const val BACK_IMG_INT = 2
        const val RIGHT_IMG_INT = 3
        const val LEFT_IMG_INT = 4
        const val TOP_IMG_INT = 5
    }

    lateinit var ctx: Context
    lateinit var progress: ProgressDialog
    private var imgName = ""
    private var directoryPath = ""
    private var STORAGE_PERMISSION_IMAGE_GROUP = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_upload)


        if (intent.action == WashTypeListActivity.GROUP_IMG_UPLOAD_AFTER) {
            uploadImage.text = "END WASH"
        }

        ctx = this
        progress = ProgressDialog(this)

        directoryPath = if (intent.action == StartTrackingActivity.ACTION_IMG_UPLOAD) {
            "/CarWashImages/${StartTrackingActivity.ACTION_IMG_UPLOAD}"
        } else if (intent.action == WashTypeListActivity.GROUP_IMG_UPLOAD_AFTER) {
            "/CarWashImages/${WashTypeListActivity.GROUP_IMG_UPLOAD_AFTER}"
        } else {
            "/CarWashImages"
        }

        frontImage.setOnClickListener {
            checkImgPermission(FRONT_IMG_TITLE, 1)
        }

        backImage.setOnClickListener {
            checkImgPermission(BACK_IMG_TITLE, 2)
        }

        sideRightImage.setOnClickListener {
            checkImgPermission(RIGHT_IMG_TITLE, 3)
        }

        sideLeftImage.setOnClickListener {
            checkImgPermission(LEFT_IMG_TITLE, 4)
        }

        topImage.setOnClickListener {
            checkImgPermission(TOP_IMG_TITLE, 5)
        }

        uploadImage.setOnClickListener {

            if (validateImages("FR.jpg") && validateImages("BA.jpg") && validateImages("RS.jpg") && validateImages("LS.jpg") && validateImages("TP.jpg")) {
                if (intent.action == StartTrackingActivity.ACTION_IMG_UPLOAD) {
                    //intent data passing between activities
                    val vehicleName = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_NAME)
                    val vehicleWash = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_WASH)
                    val vehicleAccess = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_ACCESS)
                    val vehicleAmount = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_AMOUNT)
                    val vehicleImage = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_IMAGE)
                    val vehicleImageSign = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_IMAGE_SIGN)!!
                    val washTypesList = intent.getSerializableExtra(ConfirmedOrderAdapter.VEHICLE_WASH_LIST) as ArrayList<CommonJobsRes.Data.Washtype>
                    val accessTypesList = intent.getSerializableExtra(ConfirmedOrderAdapter.VEHICLE_ACCESS_LIST) as ArrayList<CommonJobsRes.Data.Accessory>
                    val orderId = intent.getStringExtra(ConfirmedOrderAdapter.ORDER_ID)!!

                    startActivity(intentFor<CarWashingActivity>(
                            ConfirmedOrderAdapter.VEHICLE_NAME to vehicleName,
                            ConfirmedOrderAdapter.VEHICLE_WASH to vehicleWash,
                            ConfirmedOrderAdapter.VEHICLE_ACCESS to vehicleAccess,
                            ConfirmedOrderAdapter.VEHICLE_AMOUNT to vehicleAmount,
                            ConfirmedOrderAdapter.VEHICLE_IMAGE to vehicleImage,
                            ConfirmedOrderAdapter.VEHICLE_IMAGE_SIGN to vehicleImageSign,
                            ConfirmedOrderAdapter.VEHICLE_WASH_LIST to washTypesList,
                            ConfirmedOrderAdapter.VEHICLE_ACCESS_LIST to accessTypesList,
                            ConfirmedOrderAdapter.ORDER_ID to orderId
                    ))

                } else if (intent.action == WashTypeListActivity.GROUP_IMG_UPLOAD_AFTER) {

                    val washItemsChecked = intent.getSerializableExtra(WashTypeListActivity.CHECKED_WASH_ITEMS) as ArrayList<Item>
                    val accessItemsChecked = intent.getSerializableExtra(WashTypeListActivity.CHECKED_ACCESS_ITEMS) as ArrayList<Item>
                    val orderId = intent.getStringExtra(ConfirmedOrderAdapter.ORDER_ID)!!
                    val washTime = intent.getStringExtra(CarWashingActivity.WASH_TIME)!!

                    startActivity(intentFor<PaymentActivity>(
                            WashTypeListActivity.CHECKED_WASH_ITEMS to washItemsChecked,
                            WashTypeListActivity.CHECKED_ACCESS_ITEMS to accessItemsChecked,
                            ConfirmedOrderAdapter.ORDER_ID to orderId,
                            CarWashingActivity.WASH_TIME to washTime
                    ))
                }

            } else {
                toast("Please attach all images..")
            }
        }
    }


    private fun checkImgPermission(title: String, type: Int) {
        STORAGE_PERMISSION_IMAGE_GROUP = type
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_IMAGE_GROUP)
        } else {
            createFolder(directoryPath)
            selectImage(title)
        }
    }

    private fun selectImage(title: String) {
        val options = arrayOf<CharSequence>("Remove Photo", "Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setItems(options) { dialog, item ->
            if (options[item] == "Take Photo") {
                when (title) {
                    FRONT_IMG_TITLE -> {
                        EasyImage.openCamera(this, FRONT_IMG_INT)
                    }
                    BACK_IMG_TITLE -> {
                        EasyImage.openCamera(this, BACK_IMG_INT)
                    }
                    RIGHT_IMG_TITLE -> {
                        EasyImage.openCamera(this, RIGHT_IMG_INT)
                    }
                    LEFT_IMG_TITLE -> {
                        EasyImage.openCamera(this, LEFT_IMG_INT)
                    }
                    TOP_IMG_TITLE -> {
                        EasyImage.openCamera(this, TOP_IMG_INT)
                    }
                }

            } else if (options[item] == "Choose from Gallery") {

                when (title) {
                    FRONT_IMG_TITLE -> {
                        EasyImage.openGallery(this, FRONT_IMG_INT)
                    }
                    BACK_IMG_TITLE -> {
                        EasyImage.openGallery(this, BACK_IMG_INT)
                    }
                    RIGHT_IMG_TITLE -> {
                        EasyImage.openGallery(this, RIGHT_IMG_INT)
                    }
                    LEFT_IMG_TITLE -> {
                        EasyImage.openGallery(this, LEFT_IMG_INT)
                    }
                    TOP_IMG_TITLE -> {
                        EasyImage.openGallery(this, TOP_IMG_INT)
                    }
                }
            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            } else if (options[item] == "Remove Photo") {
                progress.setMessage("Removing Image...")
                progress.setCancelable(false)
                progress.show()

                when (title) {
                    FRONT_IMG_TITLE -> {
                        frontImage.setImageBitmap(null)
                        progress.dismiss()
                    }

                    BACK_IMG_TITLE -> {
                        backImage.setImageBitmap(null)
                        progress.dismiss()
                    }

                    RIGHT_IMG_TITLE -> {
                        sideRightImage.setImageBitmap(null)
                        progress.dismiss()
                    }

                    LEFT_IMG_TITLE -> {
                        sideLeftImage.setImageBitmap(null)
                        progress.dismiss()
                    }

                    TOP_IMG_TITLE -> {
                        topImage.setImageBitmap(null)
                        progress.dismiss()
                    }
                }
            }
        }
        builder.show()
    }

    private fun createFolder(directoryPath: String) {
        //create the directory with no media file in it
        val folder = File(Environment.getExternalStorageDirectory().toString() + directoryPath)
        if (!folder.exists()) {
            folder.mkdir()
            val f1 =
                    File(Environment.getExternalStorageDirectory().toString() + directoryPath + "/" + ".nomedia")
            try {
                f1.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun createFile(directoryPath: String, fileName: String): File? {
        val f: File? =
                File(Environment.getExternalStorageDirectory().toString() + directoryPath + "/" + fileName)
        return try {
            f?.createNewFile()
            f
        } catch (ex: IOException) {
            null
        }
    }

    private fun cropImage(file: File, imageFile: File, context: Context, toolbarColor: Int, statusBarColor: Int) {
        try {
            val uri = Uri.fromFile(file)
            val options = UCrop.Options()
            options.setToolbarColor(resources.getColor(toolbarColor))
            options.setStatusBarColor(resources.getColor(statusBarColor))
            options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
            options.setCompressionQuality(80)
            options.setToolbarTitle("Crop Image")
            UCrop.of(Uri.fromFile(imageFile), uri)
                    .withOptions(options)
                    .withAspectRatio(4f, 3f)
                    .start(context as Activity)
        } catch (a: Exception) {
            Toast.makeText(applicationContext, Log.getStackTraceString(a), Toast.LENGTH_LONG).show();
        }
    }

    private fun validateImages(imageName: String): Boolean {
        val folder = File(Environment.getExternalStorageDirectory().toString() + directoryPath)
        val images = if (folder.exists()) {
            folder.listFiles { _, name ->
                name.endsWith(".jpg")
            }
        } else {
            arrayOf(File(""))
        }
        return (images.find { it.name == imageName }) != null
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {

            override fun onImagePickerError(e: Exception?, source: EasyImage.ImageSource?, type: Int) {}

            override fun onImagePicked(imageFile: File, source: EasyImage.ImageSource, type: Int) {

                when (type) {
                    1 -> {
                        imgName = "FR.jpg"
                        val f = createFile(directoryPath, imgName)
                        if (f != null) cropImage(f, imageFile, ctx, R.color.gradientCenter, R.color.gradientCenter)
                    }

                    2 -> {
                        imgName = "BA.jpg"
                        val f = createFile(directoryPath, imgName)
                        if (f != null) cropImage(f, imageFile, ctx, R.color.gradientCenter, R.color.gradientCenter)
                    }

                    3 -> {
                        imgName = "RS.jpg"
                        val f = createFile(directoryPath, imgName)
                        if (f != null) cropImage(f, imageFile, ctx, R.color.gradientCenter, R.color.gradientCenter)
                    }

                    4 -> {
                        imgName = "LS.jpg"
                        val f = createFile(directoryPath, imgName)
                        if (f != null) cropImage(f, imageFile, ctx, R.color.gradientCenter, R.color.gradientCenter)
                    }

                    5 -> {
                        imgName = "TP.jpg"
                        val f = createFile(directoryPath, imgName)
                        if (f != null) cropImage(f, imageFile, ctx, R.color.gradientCenter, R.color.gradientCenter)
                    }
                }


            }
        })


        when (requestCode) {
            UCrop.REQUEST_CROP -> {
                try {
                    val imagePath = UCrop.getOutput(data!!)!!.path
//                    Log.d("ooooo", "$imagePath ::::: $imgName")
                    progress.setMessage("Uploading image...")
                    progress.setCancelable(false)
                    progress.show()
                    when (imgName) {
                        "FR.jpg" -> {
                            frontImage.setImageBitmap(BitmapFactory.decodeFile(imagePath))
                            progress.dismiss()
                        }

                        "BA.jpg" -> {
                            backImage.setImageBitmap(BitmapFactory.decodeFile(imagePath))
                            progress.dismiss()
                        }

                        "RS.jpg" -> {
                            sideRightImage.setImageBitmap(BitmapFactory.decodeFile(imagePath))
                            progress.dismiss()
                        }

                        "LS.jpg" -> {
                            sideLeftImage.setImageBitmap(BitmapFactory.decodeFile(imagePath))
                            progress.dismiss()
                        }

                        "TP.jpg" -> {
                            topImage.setImageBitmap(BitmapFactory.decodeFile(imagePath))
                            progress.dismiss()
                        }

                    }

                } catch (a: Exception) {

                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {

            1 -> {
                createFolder(directoryPath)
                selectImage(FRONT_IMG_TITLE)
            }

            2 -> {
                createFolder(directoryPath)
                selectImage(BACK_IMG_TITLE)
            }

            3 -> {
                createFolder(directoryPath)
                selectImage(RIGHT_IMG_TITLE)
            }

            4 -> {
                createFolder(directoryPath)
                selectImage(LEFT_IMG_TITLE)
            }

            5 -> {
                createFolder(directoryPath)
                selectImage(TOP_IMG_TITLE)
            }


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