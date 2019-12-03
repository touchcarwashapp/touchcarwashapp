package com.touchcarwash_driver

import Helpers.DecodePolyline
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.beust.klaxon.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.URL


class MapActivity : AppCompatActivity() {

    // declare bounds object to fit whole route in screen
    val LatLongB = LatLngBounds.Builder()
    private var isMarkerRotating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val confirmMap = supportFragmentManager.findFragmentById(R.id.confirm_map) as SupportMapFragment
        confirmMap.getMapAsync { mMap ->
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.grey_map))
            val sydney = LatLng(-33.852, 151.211)
            val opera = LatLng(-33.9320447, 151.1597271)
            val options = MarkerOptions()
                    .position(sydney)
                    .title("You")
                    .snippet("You are here")
//                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car))
            val options2 = MarkerOptions()
                    .position(opera)
                    .title("Customer")
                    .snippet("Your customer is here")
//                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_driver))
            mMap.addMarker(options)
            mMap.addMarker(options2)

            val polyOptions = PolylineOptions().let {
                it.color(Color.BLACK)
                it.width(10f)
            }

            val url = getMapUrl(sydney, opera)

            doAsync {


                val result = URL(url).readText()


                uiThread {
                    val parser = Parser()
                    val stringBuilder = StringBuilder(result)
                    val json = parser.parse(stringBuilder) as JsonObject
                    Log.d("ttttt", "$json")
                    val routes = json.array<JsonObject>("routes")!!
                    val legs = routes[0]["legs"] as JsonArray<JsonObject>
                    val points = legs[0]["steps"] as JsonArray<JsonObject>
                    val polypts = points.flatMap { DecodePolyline().decodePoly(it.obj("polyline")?.string("points")!!) }

                    polyOptions.add(sydney)
                    LatLongB.include(sydney)
                    for (point in polypts) {
                        polyOptions.add(point)
                        LatLongB.include(point)
                    }
                    polyOptions.add(opera)
                    LatLongB.include(opera)
                    val bounds = LatLongB.build()
                    mMap.addPolyline(polyOptions)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))


                }
            }
        }

    }

    private fun getMapUrl(from: LatLng, to: LatLng): String {
        val origin = "origin=${from.latitude},${from.longitude}"
        val dest = "destination=${to.latitude},${to.longitude}"
        val sensor = "sensor=false"
        val params = "$origin&$dest&$sensor&key=${getString(R.string.mapKey)}"
        return "https://maps.googleapis.com/maps/api/directions/json?$params"
    }


}
