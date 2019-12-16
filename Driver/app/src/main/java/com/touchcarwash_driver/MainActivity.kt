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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.touchcarwash_driver.db.UserDatabaseHandler
import com.touchcarwash_driver.dto.res.DefaultRes
import com.touchcarwash_driver.dto.res.JobsRes
import com.touchcarwash_driver.utils.UserHelper
import com.touchcarwash_driver.utils.tryToConnect
import com.yalantis.ucrop.UCrop
import com.zemose.network.RetrofitClientInstance
import com.zemose.network.UserService
import io.nlopez.smartlocation.SmartLocation
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
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
        const val PENDING_ACTION = "PendingOrderAction"
        const val CONFIRM_ACTION = "ConfirmOrderAction"
        const val STORAGEPERMISSION = 79
    }

    private val PERMISSIONS =
            arrayOf(Manifest.permission.CALL_PHONE)
    private val REQUEST_CALL_CODE = 2050
    lateinit var progress: ProgressDialog
    private var udb = UserDatabaseHandler(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)

        progress = ProgressDialog(this)

        if (udb.getscreenwidth().equals("", ignoreCase = true)) {
            val width = resources.displayMetrics.widthPixels
            udb.addscreenwidth(width.toString() + "")
        }

        if (udb._userid.equals("", ignoreCase = true)) {
            startActivity(Intent(applicationContext, RegistrationActivity::class.java))
            finish()
            return
        }

        val face = Typeface.createFromAsset(assets, "proxibold.otf")

        //setting up toolbar
        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)

        if (udb._isonline == "") {
            udb.addonlinestatus("0");
        }
        //setting data from the udb
        if (udb._isonline == "" || udb._isonline == "0") {
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

        if (udb._radiouskm.isNullOrEmpty()) {
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

        driverpic.setOnClickListener { checkDriverImgPermission("Profile Photo") }

        //setting isOnline status
        status.setOnToggledListener { _, isOn ->
            val isOnline: String = if (isOn) {
                "1"
            } else {
                "0"
            }
            tryToConnect {
                //update is online within server
                updateIsOnline(isOnline)
            }
        }

        editaddress.setOnClickListener { checkLocation() }

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
                        tryToConnect {
                            updateRadius(radius.text.toString(), dialog)
                        }
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
                tryToConnect {
                    //delete pic server
                    deletePic()
                }
            }
        }
        builder.show()
    }

    private fun deletePic() {
        val service = RetrofitClientInstance.retrofitInstance?.create(UserService::class.java)
        val call = service?.removeProfilePhoto("application/x-www-form-urlencoded", "removed", udb._userid)
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
                    removeProfilePic("/driverProfile/")
                    driverpic.setImageDrawable(resources.getDrawable(R.drawable.placeholder))
                    udb.user_imgsigupdate(System.currentTimeMillis().toString())
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
                            tryToConnect {
                                updateLocation("${p0.latitude},${p0.longitude}", address)
                            }
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

            REQUEST_CALL_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.isNotEmpty()) {
                    makePhoneCall("+917998999996")
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
                    tryToConnect {
                        //update image to server
                        uploadProfilePhoto(imagePath!!)
                    }
                } catch (a: Exception) {

                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        (findViewById<View>(R.id.drawer_layout) as DrawerLayout).closeDrawer(GravityCompat.START)

        when (item.itemId) {

            R.id.myprofile -> {
                startActivity(intentFor<MainActivity>()
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }

            R.id.myjobs -> {
                startActivity(intentFor<CommonOrder>().setAction(CONFIRM_ACTION))
            }

            R.id.managegallery -> {
                startActivity(intentFor<GalleryActivity>())
            }

            R.id.accounts -> {
                startActivity(intentFor<AccountActivity>())
            }

            R.id.rpt_update -> {
                appUpdate()
            }

            R.id.admin -> {
                makePhoneCall("+917998999996")
            }

            R.id.logout -> {
                udb.deleteuser()
                startActivity(intentFor<RegistrationActivity>()
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
        return true
    }

    private fun makePhoneCall(phone: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CALL_CODE)
        } else {
            AlertDialog.Builder(this)
                    .setTitle("Are you sure you want to call administrator ?")
                    .setPositiveButton("yes") { _, _ ->
                        val callIntent = Intent(Intent.ACTION_CALL)
                        callIntent.data = Uri.parse("tel:$phone")
                        startActivity(callIntent)
                    }
                    .setNegativeButton("cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
        }
    }

    private fun appUpdate() {
        val appPackageName = this.packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")));
        } catch (e: android.content.ActivityNotFoundException) {
            startActivity(Intent (Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")));
        }
    }

    private fun removeProfilePic(directoryPath:String) {
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

        tryToConnect {
            //get jobs count
            getJobs()
        }
    }

}
