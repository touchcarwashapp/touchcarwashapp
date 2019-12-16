package com.touchcarwash_driver

import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.touchcarwash_driver.adapters.PendingOrderAdapter
import com.touchcarwash_driver.dto.res.DirectionRes
import com.touchcarwash_driver.network.DirectionInterface
import com.touchcarwash_driver.utils.DecodePolyline
import com.touchcarwash_driver.utils.UserHelper
import com.touchcarwash_driver.utils.tryToConnect
import com.zemose.network.GmapRetrofitInstance
import io.nlopez.smartlocation.SmartLocation
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.toast
import java.util.*


class MapActivity : AppCompatActivity() {

    companion object {
        const val MAP_REQUEST_LOCATION = 3782
    }

    private lateinit var mMap: GoogleMap
    private lateinit var polyOptions: PolylineOptions
    private lateinit var blackPolyOptions: PolylineOptions
    private lateinit var blackPolyline: Polyline
    private lateinit var greyPolyline: Polyline
    private lateinit var polyLineList: List<LatLng>
    private lateinit var marker: Marker
    private lateinit var driverLoc: LatLng
    private lateinit var customerLoc: LatLng
    val pattern_polyline_dotted_black = Arrays.asList(Gap(10f), Dash(20f))
    val pattern_polyline_dotted_grey = Arrays.asList(Gap(5f), Dash(10f))


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        checkLocation()
    }


    private fun checkLocation() {
        if (UserHelper.hasPermissions(this, MainActivity.PERMISSIONS)) {
            tryToConnect {
                startLocation(this)
            }
        } else {
            ActivityCompat.requestPermissions(this, MainActivity.PERMISSIONS, MainActivity.REQUEST_LOCATION)
        }

    }

    private fun startLocation(context: Context) {
        if (SmartLocation.with(context).location().state().locationServicesEnabled()) {
            if (SmartLocation.with(context).location().state().isAnyProviderAvailable) {
                SmartLocation.with(context)
                        .location()
                        .oneFix()
                        .start { p0 ->
                            driverLoc = LatLng(p0.latitude, p0.longitude)
                            val custLat = intent?.getStringExtra(PendingOrderAdapter.CUST_MAP_LAT)?.toDouble()
                            val custLng = intent?.getStringExtra(PendingOrderAdapter.CUST_MAP_LNG)?.toDouble()
                            if (custLat != null && custLng != null) {
                                customerLoc = LatLng(custLat, custLng)
                                initialiseMap()
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


    private fun initialiseMap() {

        val confirmMap = supportFragmentManager.findFragmentById(R.id.confirm_map) as SupportMapFragment
        confirmMap.getMapAsync { it ->
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
            mMap.moveCamera(CameraUpdateFactory.newLatLng(driverLoc))
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
                    "${driverLoc.latitude},${driverLoc.longitude}",
                    "${customerLoc.latitude},${customerLoc.longitude}",
                    getString(R.string.mapKey)
            )
            call?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe(object : SingleObserver<DirectionRes> {

                        override fun onSuccess(t: DirectionRes) {
                            for (route in t.routes) {
                                val points = route.overviewPolyline.points
                                polyLineList = DecodePolyline.decodePoly(points)
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
        //adjusting bounds
        val builder = LatLngBounds.builder() // declaring builder
        for (latLng in polyLineList)
            builder.include(latLng) // attaching list of latlng into builder object

        val bounds = builder.build() // building the builder object as bounds
        mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                        bounds,
                        100
                )
        ) // setting bounds and animating camera


        polyOptions = PolylineOptions() // initialising polyline options and setting attributes
        polyOptions.color(Color.GRAY)
        polyOptions.width(10f)
        polyOptions.startCap(SquareCap())
        polyOptions.endCap(SquareCap())
        polyOptions.jointType(2)
        polyOptions.pattern(pattern_polyline_dotted_grey)
        polyOptions.addAll(polyLineList) // adding all polylines to the polyOptions
        greyPolyline = mMap.addPolyline(polyOptions) // setting polyline to variable

        blackPolyOptions = PolylineOptions()
        blackPolyOptions.color(Color.BLACK)
        blackPolyOptions.width(10f)
        blackPolyOptions.startCap(SquareCap())
        blackPolyOptions.endCap(SquareCap())
        blackPolyOptions.jointType(2)
        blackPolyOptions.pattern(pattern_polyline_dotted_black)
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
        polyLineAnimator.repeatCount = ValueAnimator.INFINITE
        polyLineAnimator.repeatMode = ValueAnimator.RESTART
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
            blackPolyline.points = p //set the blackpolyline points as the new sublist
        }
        polyLineAnimator.start() //start the animator

        marker = mMap.addMarker( //setting marker with current location as position and icon
                MarkerOptions()
                        .position(driverLoc)
                        .title("You")
                        .snippet("Driver located here..")
                        .flat(true)
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_driver))
        )
    }


    override fun onStop() {
        super.onStop()
        stopLocation(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MAP_REQUEST_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.isNotEmpty()) {
                    tryToConnect {
                        startLocation(this)
                    }
                }
            }
        }
    }

}
