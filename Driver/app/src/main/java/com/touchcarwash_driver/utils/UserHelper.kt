package com.touchcarwash_driver.utils

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.view.Window
import androidx.core.app.ActivityCompat
import java.util.*

object UserHelper {

    fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (context != null) {
            for(permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    //geo coder
    fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double, context: Context?): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                val returnedAddress = addresses[0]
                val strReturnedAddress = StringBuilder("")
                strReturnedAddress.append(returnedAddress.getAddressLine(0)).append("\n")
                return strReturnedAddress.toString()
            } else {
                return ""
            }
        } catch (e: Exception) {
            return ""
        }

    }

    fun createDialog(context: Context, dialogWidth: Float, dialogHeight: Float, layout: Int): Dialog {
        val width = context.resources.displayMetrics.widthPixels * dialogWidth
        val height = context.resources.displayMetrics.heightPixels * dialogHeight
        return Dialog(context).also {
            it.requestWindowFeature(Window.FEATURE_NO_TITLE)
            it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setCancelable(false)
            it.setContentView(layout)
            it.window?.setLayout(width.toInt(), height.toInt())
            it.show()
        }

    }

}