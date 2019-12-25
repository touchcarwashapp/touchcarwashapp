package helper

import android.app.Activity
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.zemose.supplier.OTP_verify
import kotlinx.android.synthetic.main.activity_otp_verify.*

class SMSBroadcastReceiver(cntx: OTP_verify) : BroadcastReceiver() {

    var ov: OTP_verify = cntx
    var listener: SmsListener? = null

    override fun onReceive(context: Context, intent: Intent) {

        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            val status = extras!!.get(SmsRetriever.EXTRA_STATUS) as Status

            when (status.statusCode) {
                CommonStatusCodes.SUCCESS ->
                    try {
                        val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String
                        if (message.contains("Zemose Supplier")) {
                            val otp = message.substring(message.indexOf(":") + 2, message.indexOf(":") + 8)

                            if (listener != null)
                                listener?.onSmsRecieved(otp)
                            ov.CheckOTP()
                        }
                    } catch (a: Exception) {
                        Log.w("a", Log.getStackTraceString(a));
                    }

                CommonStatusCodes.TIMEOUT -> {
                    //
                }
            }// Get SMS message contents
            // Extract one-time code from the message and complete verification
            // by sending the code back to your server.
            // Waiting for SMS timed out (5 minutes)
            // // Handle the error ...
        }
    }

    interface SmsListener {
        fun onSmsRecieved(otp: String);
    }

    fun setSmsListener(listener: SmsListener) {
        this.listener = listener
    }
}