package com.touchcarwash_driver

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class StartTrackingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_tracking)

        val trackMap = supportFragmentManager.findFragmentById(R.id.trackMap) as SupportMapFragment
        trackMap.getMapAsync {
            val sydney = LatLng(-33.852, 151.211)
            val options = MarkerOptions().position(sydney).title("This is Sydney")
            it.addMarker(options)
            it.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        }
    }
}
