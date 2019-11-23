package com.touchcarwash_driver

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.stepstone.apprating.AppRatingDialog
import com.stepstone.apprating.listener.RatingDialogListener
import kotlinx.android.synthetic.main.activity_payment.*
import java.util.*

class PaymentActivity : AppCompatActivity(), RatingDialogListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        payment.setOnClickListener {
            launchRateAndReview()
        }

    }

    override fun onNegativeButtonClicked() {
        Toast.makeText(this, "negative", Toast.LENGTH_SHORT).show()
    }

    override fun onNeutralButtonClicked() {
        Toast.makeText(this, "neutral", Toast.LENGTH_SHORT).show()
    }

    override fun onPositiveButtonClicked(rate: Int, comment: String) {
        Toast.makeText(this, "positive", Toast.LENGTH_SHORT).show()
    }




    private fun launchRateAndReview() {
        AppRatingDialog.Builder()
                .setPositiveButtonText("RATE")
                .setNegativeButtonText("CANCEL")
                .setNeutralButtonText("LATER")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                .setDefaultRating(3)
                .setTitle("Rate this application")
                .setDescription("Please select some stars and give your feedback")
                .setCommentInputEnabled(true)
                .setDefaultComment("This app is pretty cool !")
                .setStarColor(R.color.gradientStart)
                .setNoteDescriptionTextColor(R.color.gradientCenter)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.subHeads)
                .setHint("Please write your comment here ...")
                .setHintTextColor(R.color.mediumGrey)
                .setCommentTextColor(R.color.subHeads)
                .setCommentBackgroundColor(R.color.lightBlueBg)
                .setWindowAnimation(R.style.MyDialogFadeAnimation)
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .create(this@PaymentActivity)
                .show()
    }

}
