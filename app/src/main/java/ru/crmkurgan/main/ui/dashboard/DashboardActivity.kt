package ru.crmkurgan.main.ui.dashboard

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import player.Jzvd
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R


class DashboardActivity : AppCompatActivity() {
    private val navController by lazy { findNavController(R.id.dashboard_nav_host_fragment) }
    var sharedPref: SharedPreferences? = null
    var first = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window1 = window
        window1.statusBarColor = ContextCompat.getColor(this, R.color.variantSecondary)
        setContentView(R.layout.activity_dashboard)
        sharedPref = getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navigation_dashboard)
        val navController1 = navController
        if(first) {
            first = false
            sharedPref?.let {
                val uid = it.getInt(Constants.UID, 0)
                if (uid == 0) {
                    bottomNavigationView.menu.clear()
                    bottomNavigationView.inflateMenu(R.menu.menu_dashboard1)
                }
                bottomNavigationView.setupWithNavController(navController1)
            }
        }
        menuInflater.inflate(R.menu.menu_header_other, menu)
        sharedPref?.let {
            val uid = it.getInt(Constants.UID, 0)
            if (uid == 0) {
                menu.getItem(0).isVisible = false
            }
            val navDestination = navController1.currentDestination
            navDestination?.let { it1 ->
                when (it1.label) {
                    "mapFragment" -> {
                        menu.getItem(0).isVisible = false
                    }
                    "panoramaFragment" -> {
                        menu.getItem(0).isVisible = false
                    }
                    "sliderFragment" -> {
                        menu.getItem(0).isVisible = false
                        menu.getItem(1).isVisible = true
                        menu.getItem(2).isVisible = true
                    }
                }
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_notification -> {
                findNavController(this, R.id.dashboard_nav_host_fragment)
                    .navigate(R.id.goToNotification)
                return true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (Jzvd.backPress()) {
            return
        }
        Jzvd.releaseAllVideos()
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        Jzvd.releaseAllVideos()
    }
}