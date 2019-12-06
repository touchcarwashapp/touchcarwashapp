package com.touchcarwash_driver

import Helpers.DecodePolyline
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.beust.klaxon.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.touchcarwash_driver.adapters.PendingOrderAdapter
import com.touchcarwash_driver.utils.UserHelper
import io.nlopez.smartlocation.SmartLocation
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.net.URL
import java.util.*


class MapActivity : AppCompatActivity() {

    companion object {
        const val MAP_REQUEST_LOCATION = 3782
    }

    // declare bounds object to fit whole route in screen
    private lateinit var polyOptions: PolylineOptions
    private lateinit var blackPolyOptions: PolylineOptions
    private lateinit var blackPolyline: Polyline
    private lateinit var greyPolyline: Polyline
    private lateinit var polyLineList: List<LatLng>
    private lateinit var marker: Marker
    lateinit var confirmMap: SupportMapFragment
    val pattern_polyline_dotted_black = Arrays.asList(Gap(10f), Dash(20f))
    val pattern_polyline_dotted_grey = Arrays.asList(Gap(5f), Dash(10f))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)


        //get present location of driver
        checkLocation()
        //get customer location through intents


        confirmMap = supportFragmentManager.findFragmentById(R.id.confirm_map) as SupportMapFragment


    }

    private fun getMapUrl(from: LatLng, to: LatLng): String {
        val origin = "origin=${from.latitude},${from.longitude}"
        val dest = "destination=${to.latitude},${to.longitude}"
        val sensor = "sensor=false"
        val params = "$origin&$dest&$sensor&key=${getString(R.string.mapKey)}"
        return "https://maps.googleapis.com/maps/api/directions/json?$params"
    }

    private fun checkLocation() {
        if (UserHelper.hasPermissions(this, MainActivity.PERMISSIONS)) {
            startLocation(this)
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
                            val driverLoc = LatLng(p0.latitude, p0.longitude)
                            val custLat = intent?.getStringExtra(PendingOrderAdapter.CUST_MAP_LAT)?.toDouble()
                            val custLng = intent?.getStringExtra(PendingOrderAdapter.CUST_MAP_LNG)?.toDouble()
                            if (custLat != null && custLng != null) {
                                val customerLoc = LatLng(custLat, custLng)
                                initialiseMap(confirmMap, driverLoc, customerLoc)
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

    private fun initialiseMap(confirmMap: SupportMapFragment, driverLoc: LatLng, customerLoc: LatLng) {
        confirmMap.getMapAsync { mMap ->

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

            // get the google api direction ur for getting polyline objects
            val url = getMapUrl(driverLoc, customerLoc)

            doAsync {
                val result = URL(url).readText()
                uiThread {
                    val parser = Parser()
                    val stringBuilder = StringBuilder(result)
                    val json = parser.parse(stringBuilder) as JsonObject
                    val routes = json.array<JsonObject>("routes")!!
                    val legs = routes[0]["legs"] as JsonArray<JsonObject>
                    val points = legs[0]["steps"] as JsonArray<JsonObject>
                    polyLineList = points.flatMap { DecodePolyline().decodePoly(it.obj("polyline")?.string("points")!!) }

                    drawPolyLineAndAnimateCar(mMap, driverLoc)
                }
            }
        }
    }

    private fun drawPolyLineAndAnimateCar(mMap: GoogleMap, driverLoc: LatLng) {
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
                        .anchor(0.5f,0.5f)
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

        marker = mMap.addMarker( //setting another marker with current location as position and icon
                MarkerOptions()
                        .position(driverLoc)
                        .title("You")
                        .snippet("Driver located here..")
                        .flat(true)
                        .anchor(0.5f,0.5f)
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
                    startLocation(this)
                }
            }
        }
    }

}
