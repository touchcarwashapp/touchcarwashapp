package com.touchcarwash_driver

import android.animation.ValueAnimator
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.jakewharton.rxrelay2.PublishRelay
import com.touchcarwash_driver.adapters.ConfirmedOrderAdapter
import com.touchcarwash_driver.db.UserDatabaseHandler
import com.touchcarwash_driver.dto.res.CommonJobsRes
import com.touchcarwash_driver.dto.res.DefaultRes
import com.touchcarwash_driver.dto.res.DirectionRes
import com.touchcarwash_driver.network.DirectionInterface
import com.touchcarwash_driver.utils.DecodePolyline
import com.touchcarwash_driver.utils.Update_Vehicle_Location
import com.touchcarwash_driver.utils.UserHelper
import com.touchcarwash_driver.utils.tryToConnect
import com.zemose.network.GmapRetrofitInstance
import com.zemose.network.RetrofitClientInstance
import com.zemose.network.UserService
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationAccuracy
import io.nlopez.smartlocation.location.config.LocationParams
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_start_tracking.*
import org.jetbrains.anko.find
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StartTrackingActivity : AppCompatActivity() {

    companion object {
        const val FRAG_MAP_REQUEST_LOCATION = 3782
        const val TRACKING_INTERVAL = 1000 * 2
        const val TRACKING_DISTANCE = 20
        const val ACTION_IMG_UPLOAD = "BeforeWash"
    }

    lateinit var origin: LatLng
    lateinit var destination: LatLng
    lateinit var progress: ProgressDialog
    private val TRACKING_ACCURACY = LocationAccuracy.HIGH
    private lateinit var marker: Marker
    private lateinit var mMap: GoogleMap
    private lateinit var polyOptions: PolylineOptions
    private lateinit var blackPolyOptions: PolylineOptions
    private lateinit var blackPolyline: Polyline
    private lateinit var greyPolyline: Polyline
    private lateinit var polyLineList: List<LatLng>
    private var LatLngPublishRelay = PublishRelay.create<LatLng>()
    private var emission = 0
    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private var isMarkerRotating = false
    lateinit var disposable: Disposable
    lateinit var totalDuration: String
    lateinit var udb: UserDatabaseHandler
    lateinit var orderId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_tracking)

        progress = ProgressDialog(this)
        udb = UserDatabaseHandler(this)

        orderId = intent.getStringExtra(ConfirmedOrderAdapter.ORDER_ID)!!
        //initiate location
        checkLocation()

        reached.setOnClickListener {
            val time = totalDuration.split(" ")
            val duration = (time[0].toInt() * 60) + time[2].toInt()
            tryToConnect {
                reachedDestination(orderId, duration.toString())
            }
        }

    }


    private fun reachedDestination(orderId: String, totalDuration: String) {
        val service = RetrofitClientInstance.retrofitInstance?.create(UserService::class.java)
        val call = service?.reachedDestination("application/x-www-form-urlencoded", orderId, totalDuration)
        call?.enqueue(object : Callback<DefaultRes> {
            override fun onFailure(call: Call<DefaultRes>, t: Throwable) {
                progress.dismiss()
            }

            override fun onResponse(call: Call<DefaultRes>, response: Response<DefaultRes>) {
                val body = response.body()
                val status = body?.response?.status
                if (status.equals("Success", ignoreCase = true)) {
                    progress.dismiss()
                    //show start wash dialog
                    initiateStartWash(orderId)
                } else {
                    progress.dismiss()
                    toast(Temp.tempproblem)
                }
            }
        })
    }

    private fun startWash(orderId: String,
                          vehicleName: String,
                          vehicleWash: String,
                          vehicleAccess: String,
                          vehicleAmount: String,
                          vehicleImage: String,
                          vehicleImageSign: String,
                          washTypesList: ArrayList<CommonJobsRes.Data.Washtype>,
                          accessTypesList: ArrayList<CommonJobsRes.Data.Accessory>) {
        val service = RetrofitClientInstance.retrofitInstance?.create(UserService::class.java)
        val call = service?.startWash("application/x-www-form-urlencoded", orderId)
        call?.enqueue(object : Callback<DefaultRes> {
            override fun onFailure(call: Call<DefaultRes>, t: Throwable) {
                progress.dismiss()
            }

            override fun onResponse(call: Call<DefaultRes>, response: Response<DefaultRes>) {
                val body = response.body()
                val status = body?.response?.status
                if (status.equals("Success", ignoreCase = true)) {
                    progress.dismiss()
                    startActivity(intentFor<UploadImagesActivity>(
                            ConfirmedOrderAdapter.VEHICLE_NAME to vehicleName,
                            ConfirmedOrderAdapter.VEHICLE_WASH to vehicleWash,
                            ConfirmedOrderAdapter.VEHICLE_ACCESS to vehicleAccess,
                            ConfirmedOrderAdapter.VEHICLE_AMOUNT to vehicleAmount,
                            ConfirmedOrderAdapter.VEHICLE_IMAGE to vehicleImage,
                            ConfirmedOrderAdapter.VEHICLE_IMAGE_SIGN to vehicleImageSign,
                            ConfirmedOrderAdapter.VEHICLE_WASH_LIST to washTypesList,
                            ConfirmedOrderAdapter.VEHICLE_ACCESS_LIST to accessTypesList,
                            ConfirmedOrderAdapter.ORDER_ID to orderId)
                            .setAction(ACTION_IMG_UPLOAD))

                } else {
                    progress.dismiss()
                    toast(Temp.tempproblem)
                }
            }
        })
    }

    private fun initiateStartWash(orderId: String) {
        val dialog = UserHelper.createDialog(this, 0.9f, 0.5f, R.layout.static_start_wash_card)
        val name = dialog.find<TextView>(R.id.carName)
        val image = dialog.find<ImageView>(R.id.carImage)
        val wash = dialog.find<TextView>(R.id.washType)
        val accessory = dialog.find<TextView>(R.id.accessories)
        val amount = dialog.find<TextView>(R.id.price)
        val startWashBtn = dialog.find<Button>(R.id.startWashBtn)


        val vehicleName = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_NAME)!!
        val vehicleWash = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_WASH)!!
        val vehicleAccess = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_ACCESS)!!
        val vehicleAmount = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_AMOUNT)!!
        val vehicleImage = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_IMAGE)!!
        val vehicleImageSign = intent.getStringExtra(ConfirmedOrderAdapter.VEHICLE_IMAGE_SIGN)!!
        val washTypesList = intent.getSerializableExtra(ConfirmedOrderAdapter.VEHICLE_WASH_LIST) as ArrayList<CommonJobsRes.Data.Washtype>
        val accessTypesList = intent.getSerializableExtra(ConfirmedOrderAdapter.VEHICLE_ACCESS_LIST) as ArrayList<CommonJobsRes.Data.Accessory>

        //setting vehicle image
        val rep = RequestOptions().placeholder(R.drawable.placeholder)
        Glide.with(this)
                .load(vehicleImage)
                .apply(rep)
                .transition(DrawableTransitionOptions.withCrossFade())
                .signature(ObjectKey(vehicleImageSign))
                .into(image)
        name.text = vehicleName
        wash.text = vehicleWash
        accessory.text = vehicleAccess
        amount.text = UserHelper.convertToPrice(this, vehicleAmount.toDouble())

        startWashBtn.setOnClickListener {
            tryToConnect {
                startWash(orderId, vehicleName, vehicleWash, vehicleAccess, vehicleAmount, vehicleImage, vehicleImageSign, washTypesList, accessTypesList)
            }
        }
    }

    private fun checkLocation() {
        if (UserHelper.hasPermissions(this, MainActivity.PERMISSIONS)) {
            tryToConnect {
                startLocation(this)
            }
        } else {
            ActivityCompat.requestPermissions(this, MainActivity.PERMISSIONS, FRAG_MAP_REQUEST_LOCATION)
        }
    }

    private fun startLocation(context: Context) {
        Toast.makeText(this, "location Tracking Started", Toast.LENGTH_SHORT).show()
        if (SmartLocation.with(context).location().state().locationServicesEnabled()) {
            if (SmartLocation.with(context).location().state().isAnyProviderAvailable) {

                val builder = LocationParams.Builder()
                        .setAccuracy(TRACKING_ACCURACY)
                        .setDistance(TRACKING_DISTANCE.toFloat())
                        .setInterval(TRACKING_INTERVAL.toLong())

                val provider = LocationGooglePlayServicesProvider()
                provider.setCheckLocationSettings(true)

                SmartLocation.with(context)
                        .location(provider)
                        .continuous()
                        .config(builder.build())
                        .start { p0 ->
                            toast("location changed.")
                            origin = LatLng(p0.latitude, p0.longitude)
                            val customerLoc = intent?.getStringExtra(ConfirmedOrderAdapter.VEHICLE_LOCATION)!!.split(",")
                            val Lat = customerLoc[0]
                            val Lng = customerLoc[1]
                            destination = LatLng(Lat.toDouble(), Lng.toDouble())

                            //set lat and lng to user db and call server sync
                            udb.addvehcilelocation(udb._vehicleid, p0.latitude.toString(), p0.longitude.toString(), orderId)
                            Update_Vehicle_Location(this).locationupdate()

                            if (!(::mMap.isInitialized)) {
                                tryToConnect {
                                    initiateMap()
                                }
                            }
                            LatLngPublishRelay.accept(LatLng(p0.latitude, p0.longitude))
                        }
            }
        } else {
            Log.d("tttttt", "no location services enabled")
        }
    }

    private fun stopLocation(context: Context) {
        SmartLocation.with(context).location().stop()
    }

    private fun initiateMap() {
        //map code here
        val fragMap = supportFragmentManager.findFragmentById(R.id.trackMap) as SupportMapFragment
        fragMap.getMapAsync {
            mMap = it
            //setting up options
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.uber_map_style))
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            mMap.isTrafficEnabled = false
            mMap.isIndoorEnabled = false
            mMap.isBuildingsEnabled = false
            mMap.uiSettings.isZoomControlsEnabled = true
            mMap.uiSettings.isZoomGesturesEnabled = true
            mMap.uiSettings.setAllGesturesEnabled(true)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(origin))
            mMap.moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                    .target(mMap.cameraPosition.target)
                                    .zoom(17f)
                                    .bearing(30f)
                                    .tilt(45f)
                                    .build()
                    )
            )

            // fetch the latlanglist from the direction api

            val service =
                    GmapRetrofitInstance.retrofitInstance?.create(DirectionInterface::class.java)
            val call = service?.getDirection(
                    "driving",
                    "${origin.latitude},${origin.longitude}",
                    "${destination.latitude},${destination.longitude}",
                    getString(R.string.mapKey)
            )
            call?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe(object : SingleObserver<DirectionRes> {

                        override fun onSuccess(t: DirectionRes) {
                            for (route in t.routes) {
                                val points = route.overviewPolyline.points
                                polyLineList = DecodePolyline.decodePoly(points)
                                for (leg in route.legs) {
                                    totalDuration = leg.duration.text
                                }
                                drawPolyLineAndAnimateCar()
                            }
                        }

                        override fun onSubscribe(d: Disposable) {
                            // data of polyline here too
                        }

                        override fun onError(e: Throwable) {
                            // error message here
                        }
                    })
        }
    }


    private fun drawPolyLineAndAnimateCar() {

        //set the total duration
        timeRem.text = "Remaining $totalDuration"

        //adjusting bounds
        val builder = LatLngBounds.builder() // declaring builder
        for (latLng in polyLineList)
            builder.include(latLng) // attaching list of latlng into builder object

        val bounds = builder.build() // building the builder object as bounds
        mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                        bounds,
                        2
                )
        ) // setting bounds and animating camera


        polyOptions = PolylineOptions() // initialising polyline options and setting attributes
        polyOptions.color(Color.GRAY)
        polyOptions.width(5f)
        polyOptions.startCap(SquareCap())
        polyOptions.endCap(SquareCap())
        polyOptions.jointType(2)
        polyOptions.addAll(polyLineList) // adding all polylines to the polyOptions
        greyPolyline = mMap.addPolyline(polyOptions) // setting polyline to variable

        blackPolyOptions = PolylineOptions()
        blackPolyOptions.color(Color.BLACK)
        blackPolyOptions.width(5f)
        blackPolyOptions.startCap(SquareCap())
        blackPolyOptions.endCap(SquareCap())
        blackPolyOptions.jointType(2)
        blackPolyline = mMap.addPolyline(blackPolyOptions)

        // adding customer marker ie, the last of polyline point
        mMap.addMarker(
                MarkerOptions()
                        .position(polyLineList.get(polyLineList.size - 1))
                        .title("Customer")
                        .snippet("Customer located here..")
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car))
        )

        val polyLineAnimator = ValueAnimator.ofInt(0, 100)
        polyLineAnimator.duration = 4000
        polyLineAnimator.interpolator = LinearInterpolator()
        polyLineAnimator.addUpdateListener {
            val points = greyPolyline.points //get all the points
            val animatedValue = it.animatedValue as Int // get the animated value in each update
            val size = points.size // get total point size
            val newPoints =
                    (size * (animatedValue / 100.0f)) // create a new point with animated value and size
            val p = points.subList(
                    0,
                    newPoints.toInt()
            ) as List<LatLng> //create a sublist with from index 0 and to index as the new point and convert it to list of latlng
            blackPolyline.points = p //set the black polyline points as the new sublist
        }
        polyLineAnimator.start() //start the animator

        marker = mMap.addMarker( //setting marker with current location as position and icon
                MarkerOptions()
                        .position(origin)
                        .title("Your location")
                        .snippet("You started from this point")
                        .flat(true)
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_driver))
        )
    }


    private fun animateCarOnMap(latLangList: List<LatLng>) {
        //adjusting bounds
        val builder = LatLngBounds.builder() // declaring builder
        for (latLng in latLangList)
            builder.include(latLng) // attaching list of latlng into builder object

        val bounds = builder.build()

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 2)) // setting bounds and animating camera

        if (emission == 1) {
            marker = mMap.addMarker(
                    MarkerOptions().position(latLangList.get(0))
                            .flat(true)
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_vehicle))
            )
        }

        marker.position = latLangList.get(0)

        val valueAnimator = ValueAnimator.ofInt(0, 1)
        valueAnimator.duration = 2000
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener {
            val v = it.animatedFraction
            lng = v * latLangList.get(1).longitude + (1 - v) * latLangList.get(0).longitude
            lat = v * latLangList.get(1).latitude + (1 - v) * latLangList.get(0).latitude
            val newPos = LatLng(lat, lng)

            marker.position = newPos
            marker.setAnchor(0.5f, 0.5f)
            marker.rotation = getBearing(latLangList.get(0), newPos)
            rotateMarker(marker, getBearing(latLangList.get(0), newPos))
            mMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                    .target(newPos)
                                    .zoom(15.5f)
                                    .build()
                    )
            )
        }
        valueAnimator.start()
    }

    private fun getBearing(begin: LatLng, end: LatLng): Float {
        val lat = Math.abs(begin.latitude - end.latitude)
        val lng = Math.abs(begin.longitude - end.longitude)

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return Math.toDegrees(Math.atan(lng / lat)).toFloat()
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (90 - Math.toDegrees(Math.atan(lng / lat)) + 90).toFloat()
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (Math.toDegrees(Math.atan(lng / lat)) + 180).toFloat()
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (90 - Math.toDegrees(Math.atan(lng / lat)) + 270).toFloat()
        return -1f
    }

    private fun rotateMarker(marker: Marker, toRotation: Float) {
        if (!isMarkerRotating) {
            val handler = Handler()
            val start = SystemClock.uptimeMillis()
            val startRotation = marker.rotation
            val duration: Long = 1000

            val interpolator = LinearInterpolator()

            handler.post(object : Runnable {
                override fun run() {
                    isMarkerRotating = true
                    val elapsed = SystemClock.uptimeMillis() - start
                    val t = interpolator.getInterpolation(elapsed.toFloat() / duration)
                    val rot = t * toRotation + (1 - t) * startRotation
                    marker.rotation = if (-rot > 180) rot / 2 else rot
                    if (t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16)
                    } else {
                        isMarkerRotating = false
                    }
                }
            })
        }
    }


    override fun onStop() {
        super.onStop()
        stopLocation(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            FRAG_MAP_REQUEST_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.isNotEmpty()) {
                    tryToConnect {
                        startLocation(this)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        disposable = LatLngPublishRelay
                .buffer(2)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    toast("$it")
                    emission++
                    animateCarOnMap(it)

                }
    }
}
