package com.touchcarwash_driver

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var db = DatabaseHandler(this)
    private var udb = UserDatabaseHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (db.getscreenwidth().equals("", ignoreCase = true)) {
            val width = resources.displayMetrics.widthPixels
            db.addscreenwidth(width.toString() + "")
        }

        if (udb._userid.equals("", ignoreCase = true)) {
            startActivity(Intent(applicationContext, Registration::class.java))
            finish()
            return
        }

        val face = Typeface.createFromAsset(assets, "proxibold.otf")

        //setting up toolbar
        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)

        //setting data from the db
        address.text = db._address
        km.text = "Within " + db._radiouskm + " Km Work radius"
        if (db._address.equals("", ignoreCase = true)) {

        }

        Glide.with(this).load(Temp.weblink + "drivers/" + udb._userid + ".jpg")
                .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.placeholder).signature(ObjectKey(udb._userimgsig)))
                .transition(DrawableTransitionOptions.withCrossFade()).into(driverpic)

        drivername.text = "Haneefa"


        //setting up hamburger button and drawer transitions
        val actionBarToggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(actionBarToggle)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        }
        actionBarToggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.navigview)
        (navigationView.getHeaderView(0).findViewById<View>(R.id.txthelp) as TextView).setTypeface(face)
        navigationView.setNavigationItemSelectedListener(this)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        (findViewById<View>(R.id.drawer_layout) as DrawerLayout).closeDrawer(GravityCompat.START)
        return true
    }
}
