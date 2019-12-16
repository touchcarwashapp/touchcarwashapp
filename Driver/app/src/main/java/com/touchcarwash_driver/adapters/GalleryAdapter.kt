package com.touchcarwash_driver.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.touchcarwash_driver.R
import com.touchcarwash_driver.utils.UserHelper
import kotlinx.android.synthetic.main.gallery_dialog.view.*
import kotlinx.android.synthetic.main.gallery_static_card.view.*
import org.jetbrains.anko.find

class GalleryAdapter(val images: ArrayList<String>) :
        RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val layout = LayoutInflater.from(p0.context).inflate(R.layout.gallery_static_card, p0, false)
        return ViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bind(images[p1], p1)
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: String, pos: Int) {
            //setting vehicle image
            val rep = RequestOptions().placeholder(R.drawable.placeholder)
            Glide.with(view.context)
                    .load(item)
                    .apply(rep)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(view.galleryImg)

            view.galleryImg.setOnClickListener {
                val dialog = UserHelper.createDialog(it.context,0.9f,0.9f,R.layout.gallery_dialog)
                val imageView = dialog.find<ImageView>(R.id.galleryBig)

                val reps = RequestOptions().placeholder(R.drawable.placeholder)
                Glide.with(view.context)
                        .load(item)
                        .apply(reps)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageView)


                val closeBtn = dialog.find<ImageView>(R.id.closeBtn)
                closeBtn.setOnClickListener { dialog.dismiss() }
            }
        }

    }

}