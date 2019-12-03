package com.touchcarwash_driver

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.navigation.NavigationView
import com.touchcarwash_driver.dto.req.RegReq
import com.yalantis.ucrop.UCrop
import com.zemose.network.RetrofitClientInstance
import com.zemose.network.UserService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.intentFor
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        var STORAGEPERMISSION = 1
        lateinit var progress: ProgressDialog
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

    private var imgName = ""
    private var db = DatabaseHandler(this)
    private var udb = UserDatabaseHandler(this)
    lateinit var imageDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progress = ProgressDialog(this)

        if (db.getscreenwidth().equals("", ignoreCase = true)) {
            val width = resources.displayMetrics.widthPixels
            db.addscreenwidth(width.toString() + "")
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

        //setting data from the db
        address.text = db._address
        km.text = "Within " + db._radiouskm + " Km Work radius"
        if (db._address.equals("", ignoreCase = true)) {


        }

        Glide.with(this).load(Temp.weblink + "drivers/" + udb._userid + ".jpg")
                .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.placeholder).signature(ObjectKey(udb._userimgsig)))
                .transition(DrawableTransitionOptions.withCrossFade()).into(driverpic)

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

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        (findViewById<View>(R.id.drawer_layout) as DrawerLayout).closeDrawer(GravityCompat.START)
        return true
    }

    private fun showDialog() {
        val dialogWidth = resources.displayMetrics.widthPixels * 0.9F
        val dialogHeight = resources.displayMetrics.heightPixels * 0.9F

        imageDialog = Dialog(this)
        imageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        imageDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        imageDialog.setCancelable(false)
        imageDialog.setContentView(R.layout.dialog_image_upload)
        imageDialog.window?.setLayout(dialogWidth.toInt(), dialogHeight.toInt())
        imageDialog.show()

        val frontImg = imageDialog.findViewById<ImageView>(R.id.frontImage)
        val backImg = imageDialog.findViewById<ImageView>(R.id.backImage)
        val rightImg = imageDialog.findViewById<ImageView>(R.id.sideRightImage)
        val leftImg = imageDialog.findViewById<ImageView>(R.id.sideLeftImage)
        val topImg = imageDialog.findViewById<ImageView>(R.id.topImage)

        frontImg.setOnClickListener {
            checkImgPermission(FRONT_IMG_TITLE, 1)
        }

        backImg.setOnClickListener {
            checkImgPermission(BACK_IMG_TITLE, 2)
        }

        rightImg.setOnClickListener {
            checkImgPermission(RIGHT_IMG_TITLE, 3)
        }

        leftImg.setOnClickListener {
            checkImgPermission(LEFT_IMG_TITLE, 4)
        }

        topImg.setOnClickListener {
            checkImgPermission(TOP_IMG_TITLE, 5)
        }

    }

    fun checkImgPermission(title: String, type: Int) {
        STORAGEPERMISSION = type
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGEPERMISSION)
        } else {
            val folder =
                    File(Environment.getExternalStorageDirectory().toString() + "/CarWashImages")
            if (!folder.exists()) {
                folder.mkdir()
                val f1 =
                        File(Environment.getExternalStorageDirectory().toString() + "/CarWashImages" + "/" + ".nomedia")
                try {
                    f1.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            selectImage(title)
        }
    }

    fun selectImage(title: String) {
        val options = arrayOf<CharSequence>("Remove Photo", "Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setItems(options) { dialog, item ->
            if (options[item] == "Take Photo") {
                when(title) {
                    FRONT_IMG_TITLE -> { EasyImage.openCamera(this, FRONT_IMG_INT) }
                    BACK_IMG_TITLE -> { EasyImage.openCamera(this, BACK_IMG_INT) }
                    RIGHT_IMG_TITLE -> { EasyImage.openCamera(this, RIGHT_IMG_INT) }
                    LEFT_IMG_TITLE -> { EasyImage.openCamera(this, LEFT_IMG_INT) }
                    TOP_IMG_TITLE -> { EasyImage.openCamera(this, TOP_IMG_INT) }
                }

            } else if (options[item] == "Choose from Gallery") {

                when(title) {
                    FRONT_IMG_TITLE -> { EasyImage.openGallery(this, FRONT_IMG_INT) }
                    BACK_IMG_TITLE -> { EasyImage.openGallery(this, BACK_IMG_INT) }
                    RIGHT_IMG_TITLE -> { EasyImage.openGallery(this, RIGHT_IMG_INT) }
                    LEFT_IMG_TITLE -> { EasyImage.openGallery(this, LEFT_IMG_INT) }
                    TOP_IMG_TITLE -> { EasyImage.openGallery(this, TOP_IMG_INT) }
                }
            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            } else if (options[item] == "Remove Photo") {
//                progress.setMessage("Removing Image...")
//                progress.setCancelable(false)
//                progress.show()

                val frontImg = imageDialog.findViewById<ImageView>(R.id.frontImage)
                val backImg = imageDialog.findViewById<ImageView>(R.id.backImage)
                val rightImg = imageDialog.findViewById<ImageView>(R.id.sideRightImage)
                val leftImg = imageDialog.findViewById<ImageView>(R.id.sideLeftImage)
                val topImg = imageDialog.findViewById<ImageView>(R.id.topImage)

                when(title) {
                    FRONT_IMG_TITLE -> {
                        frontImg.setImageBitmap(null)
                    }

                    BACK_IMG_TITLE -> {
                        backImg.setImageBitmap(null)
                    }

                    RIGHT_IMG_TITLE -> {
                        rightImg.setImageBitmap(null)
                    }

                    LEFT_IMG_TITLE -> {
                        leftImg.setImageBitmap(null)
                    }

                    TOP_IMG_TITLE -> {
                        topImg.setImageBitmap(null)
                    }
                }

//                deletepic()
            }
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {

            override fun onImagePickerError(e: Exception?, source: EasyImage.ImageSource?, type: Int) {}

            override fun onImagePicked(imageFile: File, source: EasyImage.ImageSource, type: Int) {

                when(type) {
                    1 -> {
                        imgName = "FR.jpg"
                    }

                    2 -> {
                        imgName = "BA.jpg"
                    }

                    3 -> {
                        imgName = "RS.jpg"
                    }

                    4 -> {
                        imgName = "LS.jpg"
                    }

                    5 -> {
                        imgName = "TP.jpg"
                    }
                }

                val f: File? =
                        File(Environment.getExternalStorageDirectory().toString() + "/CarWashImages" + "/" + imgName)
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
                    Log.d("ooooo", "$imagePath ::::: $imgName")
                    progress.setMessage("Uploading image...")
                    progress.setCancelable(false)
                    progress.show()
                    //update image to server


                    // set image to view
                    val frontImg = imageDialog.findViewById<ImageView>(R.id.frontImage)
                    val backImg = imageDialog.findViewById<ImageView>(R.id.backImage)
                    val rightImg = imageDialog.findViewById<ImageView>(R.id.sideRightImage)
                    val leftImg = imageDialog.findViewById<ImageView>(R.id.sideLeftImage)
                    val topImg = imageDialog.findViewById<ImageView>(R.id.topImage)

                    when(imgName) {

                        "FR.jpg" -> {
                            frontImg.setImageBitmap(BitmapFactory.decodeFile(imagePath))
                            progress.dismiss()
                        }

                        "BA.jpg" -> {
                            backImg.setImageBitmap(BitmapFactory.decodeFile(imagePath))
                            progress.dismiss()
                        }

                        "RS.jpg" -> {
                            rightImg.setImageBitmap(BitmapFactory.decodeFile(imagePath))
                            progress.dismiss()
                        }

                        "LS.jpg" -> {
                            leftImg.setImageBitmap(BitmapFactory.decodeFile(imagePath))
                            progress.dismiss()
                        }

                        "TP.jpg" -> {
                            topImg.setImageBitmap(BitmapFactory.decodeFile(imagePath))
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
            STORAGEPERMISSION -> {
                val folder = File(Environment.getExternalStorageDirectory().toString() + "/CarWashImages")
                if (!folder.exists()) {
                    folder.mkdir()
                    val f1 =
                            File(Environment.getExternalStorageDirectory().toString() + "/CarWashImages" + "/" + ".nomedia")
                    try {
                        f1.createNewFile()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

                when (requestCode) {
                    1 -> {
                        selectImage(FRONT_IMG_TITLE)
                    }

                    2 -> {
                        selectImage(BACK_IMG_TITLE)
                    }

                    3 -> {
                        selectImage(RIGHT_IMG_TITLE)
                    }

                    4 -> {
                        selectImage(LEFT_IMG_TITLE)
                    }

                    5 -> {
                        selectImage(TOP_IMG_TITLE)
                    }
                }
            }
        }

    }



}
