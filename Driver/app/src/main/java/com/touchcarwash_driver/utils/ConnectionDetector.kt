package com.touchcarwash_driver.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import androidx.fragment.app.Fragment
import android.view.View
import android.view.Window
import com.touchcarwash_driver.R


fun Activity.tryToConnect(retryUrl: () -> Unit) {

    if (isConnected(this)) {
        retryUrl()
    } else {
        showErrorDialog(this, retryUrl)
    }
}


fun Fragment.tryToConnectFragment(retryUrl: () -> Unit){
    if(isConnected(this@tryToConnectFragment.requireContext())) {
        retryUrl()
    }else {
        showErrorDialog(this@tryToConnectFragment.requireContext(), retryUrl)
    }
}

private fun isConnected(context: Context): Boolean {
    val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val info = connectivity.activeNetworkInfo
    return info != null && info.isConnected
}

private fun showErrorDialog(context: Context, retryUrl: () -> Unit) {
    val dialog: Dialog
    dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.setCancelable(false)
    dialog.setContentView(R.layout.internet_connectivity_error)
    dialog.show()

    val resId = R.id.networkRetry
    val retryBtn = dialog.findViewById<View>(resId) as View
    retryBtn.setOnClickListener {
        if (isConnected(context)) {
            retryUrl()
            dialog.dismiss()
        }
    }
}
