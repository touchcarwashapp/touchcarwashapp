package com.touchcarwash_driver

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.touchcarwash_driver.adapters.GalleryAdapter
import com.touchcarwash_driver.db.UserDatabaseHandler
import com.touchcarwash_driver.dto.res.GalleryRes
import com.zemose.network.RetrofitClientInstance
import com.zemose.network.UserService
import kotlinx.android.synthetic.main.activity_gallery.*
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GalleryActivity : AppCompatActivity() {

    lateinit var udb: UserDatabaseHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        udb = UserDatabaseHandler(this)
        backBtn.setOnClickListener { onBackPressed() }
    }

    private fun getGalleryImages(ctx: Context) {
        val service = RetrofitClientInstance.retrofitInstance?.create(UserService::class.java)
        val call = service?.getGallery("application/x-www-form-urlencoded", udb._userid)
        call?.enqueue(object : Callback<GalleryRes> {
            override fun onFailure(call: Call<GalleryRes>, t: Throwable) {
                toast("error : $t")
            }

            override fun onResponse(call: Call<GalleryRes>, response: Response<GalleryRes>) {
                val body = response.body()
                val status = body?.response?.status
                val result = body?.data!!

                if (status.equals("Success", ignoreCase = true)) {
                    galleryRecycler.layoutManager = GridLayoutManager(ctx, 3, RecyclerView.VERTICAL, false)
                    galleryRecycler.adapter = GalleryAdapter(result as ArrayList<String>)

                } else {
                    toast(Temp.tempproblem)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        getGalleryImages(this)
    }


}
