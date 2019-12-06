package com.touchcarwash_driver

import android.Manifest
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.touchcarwash_driver.db.UserDatabaseHandler
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.touchcarwash_driver.dto.res.DefaultRes
import com.touchcarwash_driver.dto.res.JobsRes
import com.touchcarwash_driver.utils.UserHelper
import com.yalantis.ucrop.UCrop
import com.zemose.network.RetrofitClientInstance
import com.zemose.network.UserService
import io.nlopez.smartlocation.SmartLocation
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.static_pending_card.view.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.find
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        val PERMISSIONS =
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        const val REQUEST_LOCATION = 1005

        //        var STORAGEPERMISSION = 1
        lateinit var progress: ProgressDialog
        const val PENDING_ACTION = "PendingOrderAction"
        const val CONFIRM_ACTION = "ConfirmOrderAction"
//        const val FRONT_IMG_TITLE = "Pick Front Image"
//        const val BACK_IMG_TITLE = "Pick Back Image"
//        const val RIGHT_IMG_TITLE = "Pick Right-Side Image"
//        const val LEFT_IMG_TITLE = "Pick Left-Side Image"
//        const val TOP_IMG_TITLE = "Pick Top Image"
//        const val FRONT_IMG_INT = 1
//        const val BACK_IMG_INT = 2
//        const val RIGHT_IMG_INT = 3
//        const val LEFT_IMG_INT = 4
//        const val TOP_IMG_INT = 5

        const val STORAGEPERMISSION = 79
    }

    private var imgName = ""
    private var udb = UserDatabaseHandler(this)
    lateinit var imageDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progress = ProgressDialog(this)

        if (udb.getscreenwidth().equals("", ignoreCase = true)) {
            val width = resources.displayMetrics.widthPixels
            udb.addscreenwidth(width.toString() + "")
        }

        if (udb._userid.equals("", ignoreCase = true)) {
            startActivity(Intent(applicationContext, Registration::class.java))
            finish()
            return
        }

        val face = Typeface.createFromAsset(assets, "proxibold.otf")

        //setting up toolbar
        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)

        if(udb._isonline==""){
            udb.addonlinestatus("0");
        }
        //setting data from the udb
        if(udb._isonline == "" || udb._isonline == "0") {
            status.isOn = false
            status.labelOff = "Go Online"
        } else {
            status.isOn = true
            status.labelOn = "Go Offline"
        }

        if (udb._address.isNullOrEmpty()) {
            addressTv.text = "Your address appear here."
        } else {
            addressTv.text = udb._address
        }

        if(udb._radiouskm.isNullOrEmpty()) {
            km.text = "Radius not available"
        } else {
            km.text = "Within " + udb._radiouskm + " Km radius"
        }



        //setting driver name
        drivername.text = udb._username

        //setting up hamburger button and drawer transitions
        val actionBarToggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(actionBarToggle)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        }
        actionBarToggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.navigview)
        (navigationView.getHeaderView(0).findViewById<View>(R.id.txthelp) as TextView).setTypeface(face)
        navigationView.setNavigationItemSelectedListener(this)

        driverpic.setOnClickListener {
            checkDriverImgPermission("Profile Photo")
        }

        //setting isOnline status
        status.setOnToggledListener { toggleableView, isOn ->
            val isOnline: String?
            if (isOn) {
                isOnline = "1"
            } else {
                isOnline = "0"
            }
            //update is online within server
            updateIsOnline(isOnline)
        }

        editaddress.setOnClickListener {
            checkLocation()
        }

        editkm.setOnClickListener {
            //create dialog
            val dialog = UserHelper.createDialog(this, 0.9F, 0.5F, R.layout.dialog_radius)
            val radius = dialog.find<TextInputEditText>(R.id.radiusEt)
            val radiusInp = dialog.find<TextInputLayout>(R.id.radiusInp)
            val radiusBtn = dialog.find<Button>(R.id.setRadius)
            val closeBtn = dialog.find<ImageView>(R.id.closeBtn)

            closeBtn.setOnClickListener { dialog.dismiss() }

            radiusBtn.setOnClickListener {
                if (!radius.text.isNullOrEmpty()) {
                    if (!radius.text.toString().contains(Regex("/[a-zA-Z*()&^%\$#@!<>?\"{}:|]/g"))) {
                        updateRadius(radius.text.toString(), dialog)
                    } else {
                        radiusInp.error = "Please enter numeric value"
                    }

                } else {
                    radiusInp.error = "Please enter valid radius."
                }
            }
        }

        pendingjobs.setOnClickListener {
            startActivity(intentFor<CommonOrder>().setAction(PENDING_ACTION))
        }

        upcomingjobs.setOnClickListener {
            startActivity(intentFor<CommonOrder>().setAction(CONFIRM_ACTION))
        }


    }


    private fun checkDriverImgPermission(title: String) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGEPERMISSION)
        } else {
            val folder =
                    File(Environment.getExternalStorageDirectory().toString() + "/driverProfile")
            if (!folder.exists()) {
                folder.mkdir()
                val f1 =
                        File(Environment.getExternalStorageDirectory().toString() + "/driverProfile" + "/" + ".nomedia")
                try {
                    f1.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            selectImage(title)
        }
    }

    private fun selectImage(title: String) {
        val options = arrayOf<CharSequence>("Remove Photo", "Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setItems(options) { dialog, item ->
            if (options[item] == "Take Photo") {
                EasyImage.openCamera(this, 90)
            } else if (options[item] == "Choose from Gallery") {
                EasyImage.openGallery(this, 90)
            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            } else if (options[item] == "Remove Photo") {
                progress.setMessage("Removing Image...")
                progress.setCancelable(false)
                progress.show()
                deletePic()
            }
        }
        builder.show()
    }

    private fun deletePic() {
        val service = RetrofitClientInstance.retrofitInstance?.create(UserService::class.java)
        val call = service?.removeProfilePhoto("application/x-www-form-urlencoded", "removed",udb._userid)
        call?.enqueue(object : Callback<DefaultRes> {
            override fun onFailure(call: Call<DefaultRes>, t: Throwable) {
                progress.dismiss()
                toast("Updated")
            }

            override fun onResponse(call: Call<DefaultRes>, response: Response<DefaultRes>) {
                val body = response.body()
                val status = body?.response?.status
                val result = body?.data!!
                if (status.equals("Success", ignoreCase = true)) {
                    progress.dismiss()
                    driverpic.setImageDrawable(resources.getDrawable(R.drawable.placeholder));
                    toast(result)
                } else {
                    progress.dismiss();
                    toast(Temp.tempproblem)
                }
            }
        })
    }

    private fun updateIsOnline(isOnlineStat: String) {
        val service = RetrofitClientInstance.retrofitInstance?.create(UserService::class.java)
        val call = service?.availabilityStatus("application/x-www-form-urlencoded", udb._userid, isOnlineStat)
        call?.enqueue(object : Callback<DefaultRes> {
            override fun onFailure(call: Call<DefaultRes>, t: Throwable) {
                progress.dismiss()
                toast("Updated")
            }

            override fun onResponse(call: Call<DefaultRes>, response: Response<DefaultRes>) {
                val body = response.body()
                val status = body?.response?.status
                val result = body?.data!!
                if (status.equals("Success", ignoreCase = true)) {
                    progress.dismiss()
                    toast(result)
                    Log.d("tttttt", "$isOnlineStat")
                    udb.update_isonline(isOnlineStat)
                } else {
                    progress.dismiss()
                    toast(Temp.tempproblem)
                }
            }
        })
    }

    private fun startLocation(context: Context) {
        progress.setMessage("Fetching your location")
        progress.setCancelable(false)
        progress.show()
        if (SmartLocation.with(context).location().state().locationServicesEnabled()) {
            if (SmartLocation.with(context).location().state().isAnyProviderAvailable) {
                SmartLocation.with(context)
                        .location()
                        .oneFix()
                        .start { p0 ->
                            val address = UserHelper.getCompleteAddressString(p0.latitude, p0.longitude, this)
                            updateLocation("${p0.latitude},${p0.longitude}", address)
                        }
            }
        } else {
            toast("no location services enabled")
        }
    }

    private fun stopLocation(context: Context) {
        SmartLocation.with(context).location().stop()
    }

    private fun checkLocation() {
        if (UserHelper.hasPermissions(this, PERMISSIONS)) {
            startLocation(this)
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_LOCATION)
        }

    }

    private fun updateLocation(location: String, address: String) {
        progress.setMessage("updating location please wait..")
        progress.setCancelable(false)
        progress.show()
        val service = RetrofitClientInstance.retrofitInstance?.create(UserService::class.java)
        val call = service?.updateLocation("application/x-www-form-urlencoded", udb._userid, address, location)
        call?.enqueue(object : Callback<DefaultRes> {
            override fun onFailure(call: Call<DefaultRes>, t: Throwable) {
                progress.dismiss()
            }

            override fun onResponse(call: Call<DefaultRes>, response: Response<DefaultRes>) {
                val body = response.body()
                val status = body?.response?.status
                val result = body?.data!!

                if (status.equals("Success", ignoreCase = true)) {
                    progress.dismiss()
                    toast(result)
                    udb.user_updateaddress(address, location)
                    addressTv.text = "$address"
                } else {
                    progress.dismiss();
                    toast(Temp.tempproblem)
                }
            }
        })
    }

    private fun updateRadius(radius: String, dialog: Dialog) {
        progress.setMessage("updating radius please wait..")
        progress.setCancelable(false)
        progress.show()
        val service = RetrofitClientInstance.retrofitInstance?.create(UserService::class.java)
        val call = service?.updateRadius("application/x-www-form-urlencoded", udb._userid, radius)
        call?.enqueue(object : Callback<DefaultRes> {
            override fun onFailure(call: Call<DefaultRes>, t: Throwable) {
                progress.dismiss()
            }

            override fun onResponse(call: Call<DefaultRes>, response: Response<DefaultRes>) {
                val body = response.body()
                val status = body?.response?.status
                val result = body?.data!!
                if (status.equals("Success", ignoreCase = true)) {
                    progress.dismiss()
                    toast(result)
                    udb.user_updateradius(radius)
                    km.text = "Within ${radius}Km radius"
                    dialog.dismiss()
                } else {
                    progress.dismiss();
                    toast(Temp.tempproblem)
                }
            }
        })
    }

    private fun getJobs() {
        val service = RetrofitClientInstance.retrofitInstance?.create(UserService::class.java)
        val call = service?.getJobsCount("application/x-www-form-urlencoded", udb._userid)
        call?.enqueue(object : Callback<JobsRes> {
            override fun onFailure(call: Call<JobsRes>, t: Throwable) {

            }

            override fun onResponse(call: Call<JobsRes>, response: Response<JobsRes>) {
                val body = response.body()
                val status = body?.response?.status
                val result = body?.data!!
                if (status.equals("Success", ignoreCase = true)) {
                    pendingLoader.visibility = View.GONE
                    confirmLoading.visibility = View.GONE
                    pendingjobcount.visibility = View.VISIBLE
                    upcomingjobcount.visibility = View.VISIBLE
                    pendingjobcount.text = result.pending.toString()
                    upcomingjobcount.text = result.confirmed.toString()
                } else {
                    toast(Temp.tempproblem)
                }
            }
        })
    }

    private fun uploadProfilePhoto(imagePath: String) {
        try {
            val myfile = File(imagePath)
            val reqFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), myfile)
            val imageBody = MultipartBody.Part.createFormData("driverpic", myfile.name, reqFile)
            val userId = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), udb._userid)
            val photoExist = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), "filled")
            val service = RetrofitClientInstance.retrofitInstance?.create(UserService::class.java)
            val call = service?.photoUpdate(userId, photoExist, imageBody)

            call?.enqueue(object : Callback<DefaultRes> {
                override fun onFailure(call: Call<DefaultRes>, t: Throwable) {
                    progress.dismiss()
                    toast("Sorry ! Unable to update , Please try later")
                }

                override fun onResponse(call: Call<DefaultRes>, response: Response<DefaultRes>) {
                    val body = response.body()
                    val status = body?.response?.status
                    if (status.equals("Success", ignoreCase = true)) {
                        driverpic.setImageBitmap(BitmapFactory.decodeFile(imagePath))
                        udb.user_imgsigupdate(Date(System.currentTimeMillis()).toString())
                        progress.dismiss()
                        toast("Updated")
                    } else {
                        progress.dismiss()
                        toast(Temp.tempproblem)
                    }
                }
            })
        } catch (e: Exception) {
            Log.d("rrrrr", Log.getStackTraceString(e))
        }
    }


//    private fun showDialog() {
//        val dialogWidth = resources.displayMetrics.widthPixels * 0.9F
//        val dialogHeight = resources.displayMetrics.heightPixels * 0.9F
//
//        imageDialog = Dialog(this)
//        imageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        imageDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        imageDialog.setCancelable(false)
//        imageDialog.setContentView(R.layout.dialog_image_upload)
//        imageDialog.window?.setLayout(dialogWidth.toInt(), dialogHeight.toInt())
//        imageDialog.show()
//
//        val frontImg = imageDialog.findViewById<ImageView>(R.id.frontImage)
//        val backImg = imageDialog.findViewById<ImageView>(R.id.backImage)
//        val rightImg = imageDialog.findViewById<ImageView>(R.id.sideRightImage)
//        val leftImg = imageDialog.findViewById<ImageView>(R.id.sideLeftImage)
//        val topImg = imageDialog.findViewById<ImageView>(R.id.topImage)
//
//        frontImg.setOnClickListener {
//            checkImgPermission(FRONT_IMG_TITLE, 1)
//        }
//
//        backImg.setOnClickListener {
//            checkImgPermission(BACK_IMG_TITLE, 2)
//        }
//
//        rightImg.setOnClickListener {
//            checkImgPermission(RIGHT_IMG_TITLE, 3)
//        }
//
//        leftImg.setOnClickListener {
//            checkImgPermission(LEFT_IMG_TITLE, 4)
//        }
//
//        topImg.setOnClickListener {
//            checkImgPermission(TOP_IMG_TITLE, 5)
//        }
//
//    }

//    fun checkImgPermission(title: String, type: Int) {
//        STORAGEPERMISSION = type
//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGEPERMISSION)
//        } else {
//            val folder =
//                    File(Environment.getExternalStorageDirectory().toString() + "/CarWashImages")
//            if (!folder.exists()) {
//                folder.mkdir()
//                val f1 =
//                        File(Environment.getExternalStorageDirectory().toString() + "/CarWashImages" + "/" + ".nomedia")
//                try {
//                    f1.createNewFile()
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//            }
//
//            selectImage(title)
//        }
//    }

//    fun selectImage(title: String) {
//        val options = arrayOf<CharSequence>("Remove Photo", "Take Photo", "Choose from Gallery", "Cancel")
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle(title)
//        builder.setItems(options) { dialog, item ->
//            if (options[item] == "Take Photo") {
//                when(title) {
//                    FRONT_IMG_TITLE -> { EasyImage.openCamera(this, FRONT_IMG_INT) }
//                    BACK_IMG_TITLE -> { EasyImage.openCamera(this, BACK_IMG_INT) }
//                    RIGHT_IMG_TITLE -> { EasyImage.openCamera(this, RIGHT_IMG_INT) }
//                    LEFT_IMG_TITLE -> { EasyImage.openCamera(this, LEFT_IMG_INT) }
//                    TOP_IMG_TITLE -> { EasyImage.openCamera(this, TOP_IMG_INT) }
//                }
//
//            } else if (options[item] == "Choose from Gallery") {
//
//                when(title) {
//                    FRONT_IMG_TITLE -> { EasyImage.openGallery(this, FRONT_IMG_INT) }
//                    BACK_IMG_TITLE -> { EasyImage.openGallery(this, BACK_IMG_INT) }
//                    RIGHT_IMG_TITLE -> { EasyImage.openGallery(this, RIGHT_IMG_INT) }
//                    LEFT_IMG_TITLE -> { EasyImage.openGallery(this, LEFT_IMG_INT) }
//                    TOP_IMG_TITLE -> { EasyImage.openGallery(this, TOP_IMG_INT) }
//                }
//            } else if (options[item] == "Cancel") {
//                dialog.dismiss()
//            } else if (options[item] == "Remove Photo") {
////                progress.setMessage("Removing Image...")
////                progress.setCancelable(false)
////                progress.show()
//
//                val frontImg = imageDialog.findViewById<ImageView>(R.id.frontImage)
//                val backImg = imageDialog.findViewById<ImageView>(R.id.backImage)
//                val rightImg = imageDialog.findViewById<ImageView>(R.id.sideRightImage)
//                val leftImg = imageDialog.findViewById<ImageView>(R.id.sideLeftImage)
//                val topImg = imageDialog.findViewById<ImageView>(R.id.topImage)
//
//                when(title) {
//                    FRONT_IMG_TITLE -> {
//                        frontImg.setImageBitmap(null)
//                    }
//
//                    BACK_IMG_TITLE -> {
//                        backImg.setImageBitmap(null)
//                    }
//
//                    RIGHT_IMG_TITLE -> {
//                        rightImg.setImageBitmap(null)
//                    }
//
//                    LEFT_IMG_TITLE -> {
//                        leftImg.setImageBitmap(null)
//                    }
//
//                    TOP_IMG_TITLE -> {
//                        topImg.setImageBitmap(null)
//                    }
//                }
//
////                deletepic()
//            }
//        }
//        builder.show()
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
//
//            override fun onImagePickerError(e: Exception?, source: EasyImage.ImageSource?, type: Int) {}
//
//            override fun onImagePicked(imageFile: File, source: EasyImage.ImageSource, type: Int) {
//
//                when(type) {
//                    1 -> {
//                        imgName = "FR.jpg"
//                    }
//
//                    2 -> {
//                        imgName = "BA.jpg"
//                    }
//
//                    3 -> {
//                        imgName = "RS.jpg"
//                    }
//
//                    4 -> {
//                        imgName = "LS.jpg"
//                    }
//
//                    5 -> {
//                        imgName = "TP.jpg"
//                    }
//                }
//
//                val f: File? =
//                        File(Environment.getExternalStorageDirectory().toString() + "/CarWashImages" + "/" + imgName)
//                try {
//                    f?.createNewFile()
//                } catch (ex: IOException) {
//
//                }
//                if (f != null) {
//                    try {
//                        val uri = Uri.fromFile(f)
//                        val options = UCrop.Options()
//                        options.setToolbarColor(resources.getColor(R.color.gradientCenter))
//                        options.setStatusBarColor(resources.getColor(R.color.gradientCenter))
//                        options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
//                        options.setCompressionQuality(80)
//                        options.setToolbarTitle("Crop Image")
//                        UCrop.of(Uri.fromFile(imageFile), uri)
//                                .withOptions(options)
//                                .withAspectRatio(4f, 3f)
//                                .start(this@MainActivity)
//                    } catch (a: Exception) {
//                        Toast.makeText(applicationContext, Log.getStackTraceString(a), Toast.LENGTH_LONG).show();
//                    }
//                }
//            }
//        })
//
//
//        when (requestCode) {
//            UCrop.REQUEST_CROP -> {
//                try {
//                    val imagePath = UCrop.getOutput(data!!)!!.path
//                    Log.d("ooooo", "$imagePath ::::: $imgName")
//                    progress.setMessage("Uploading image...")
//                    progress.setCancelable(false)
//                    progress.show()
//                    //update image to server
//
//
//                    // set image to view
//                    val frontImg = imageDialog.findViewById<ImageView>(R.id.frontImage)
//                    val backImg = imageDialog.findViewById<ImageView>(R.id.backImage)
//                    val rightImg = imageDialog.findViewById<ImageView>(R.id.sideRightImage)
//                    val leftImg = imageDialog.findViewById<ImageView>(R.id.sideLeftImage)
//                    val topImg = imageDialog.findViewById<ImageView>(R.id.topImage)
//
//                    when(imgName) {
//
//                        "FR.jpg" -> {
//                            frontImg.setImageBitmap(BitmapFactory.decodeFile(imagePath))
//                            progress.dismiss()
//                        }
//
//                        "BA.jpg" -> {
//                            backImg.setImageBitmap(BitmapFactory.decodeFile(imagePath))
//                            progress.dismiss()
//                        }
//
//                        "RS.jpg" -> {
//                            rightImg.setImageBitmap(BitmapFactory.decodeFile(imagePath))
//                            progress.dismiss()
//                        }
//
//                        "LS.jpg" -> {
//                            leftImg.setImageBitmap(BitmapFactory.decodeFile(imagePath))
//                            progress.dismiss()
//                        }
//
//                        "TP.jpg" -> {
//                            topImg.setImageBitmap(BitmapFactory.decodeFile(imagePath))
//                            progress.dismiss()
//                        }
//
//                    }
//
//                } catch (a: Exception) {
//
//                }
//            }
//        }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        when (requestCode) {
//            STORAGEPERMISSION -> {
//                val folder = File(Environment.getExternalStorageDirectory().toString() + "/CarWashImages")
//                if (!folder.exists()) {
//                    folder.mkdir()
//                    val f1 =
//                            File(Environment.getExternalStorageDirectory().toString() + "/CarWashImages" + "/" + ".nomedia")
//                    try {
//                        f1.createNewFile()
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                }
//
//                when (requestCode) {
//                    1 -> {
//                        selectImage(FRONT_IMG_TITLE)
//                    }
//
//                    2 -> {
//                        selectImage(BACK_IMG_TITLE)
//                    }
//
//                    3 -> {
//                        selectImage(RIGHT_IMG_TITLE)
//                    }
//
//                    4 -> {
//                        selectImage(LEFT_IMG_TITLE)
//                    }
//
//                    5 -> {
//                        selectImage(TOP_IMG_TITLE)
//                    }
//                }
//            }
//        }
//
//    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGEPERMISSION -> {
                val folder = File(Environment.getExternalStorageDirectory().toString() + "/driverProfile")
                if (!folder.exists()) {
                    folder.mkdir()
                    val f1 =
                            File(Environment.getExternalStorageDirectory().toString() + "/driverProfile" + "/" + ".nomedia")
                    try {
                        f1.createNewFile()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

                selectImage("Profile Photo")
            }

            REQUEST_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.isNotEmpty()) {
                    startLocation(this)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {

            override fun onImagePickerError(e: Exception?, source: EasyImage.ImageSource?, type: Int) {}

            override fun onImagePicked(imageFile: File, source: EasyImage.ImageSource, type: Int) {

                val f: File? =
                        File(Environment.getExternalStorageDirectory().toString() + "/driverProfile" + "/" + "profile.jpg")
                try {
                    f?.createNewFile()
                } catch (ex: IOException) {

                }
                if (f != null) {
                    try {
                        val uri = Uri.fromFile(f)
                        val options = UCrop.Options()
                        options.setToolbarColor(resources.getColor(R.color.gradientCenter))
                        options.setStatusBarColor(resources.getColor(R.color.gradientCenter))
                        options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
                        options.setCompressionQuality(80)
                        options.setToolbarTitle("Crop Image")
                        UCrop.of(Uri.fromFile(imageFile), uri)
                                .withOptions(options)
                                .withAspectRatio(4f, 3f)
                                .start(this@MainActivity)
                    } catch (a: Exception) {
                        Toast.makeText(applicationContext, Log.getStackTraceString(a), Toast.LENGTH_LONG).show();
                    }
                }
            }
        })


        when (requestCode) {
            UCrop.REQUEST_CROP -> {
                try {
                    val imagePath = UCrop.getOutput(data!!)!!.path
                    progress.setMessage("Uploading image...")
                    progress.setCancelable(false)
                    progress.show()
                    //update image to server
                    uploadProfilePhoto(imagePath!!)
                } catch (a: Exception) {

                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        (findViewById<View>(R.id.drawer_layout) as DrawerLayout).closeDrawer(GravityCompat.START)
        return true
    }

    override fun onStop() {
        super.onStop()
        stopLocation(this)
    }

    override fun onResume() {
        super.onResume()
        //setting driver image
        val rep = RequestOptions().placeholder(R.drawable.placeholder)
        Glide.with(this)
                .load("${Temp.weblink}driversmall/${udb._userid}.jpg")
                .apply(rep)
                .signature(ObjectKey(udb._userimgsig))
                .into(driverpic)

        //get jobs count
        getJobs()
    }

}
