package com.example.qurandaily

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Drawer + nav view
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        navigationView.setNavigationItemSelectedListener(this)

        // Toolbar menu icon → open drawer
        val menuIcon = findViewById<ImageView>(R.id.menu_icon)
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // "Start Reading Quran" button → QuranReaderActivity
        val btnStartReading = findViewById<Button>(R.id.btn_start_reading)
        btnStartReading.setOnClickListener {
            openQuranReader()
        }

        // --- Header handling (wrapped in a safe check) ---
        if (navigationView.headerCount > 0) {
            val headerView = navigationView.getHeaderView(0)

            val closeIcon = headerView.findViewById<ImageView>(R.id.nav_close_icon)
            closeIcon.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
            }

            // nav_home is a TextView inside the CardView
            val navHomeText = headerView.findViewById<TextView>(R.id.nav_home)
            navHomeText.setOnClickListener {
                // Already on home, just close the drawer
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
    }

    private fun openQuranReader() {
        val intent = Intent(this, QuranReaderActivity::class.java)
        startActivity(intent)
    }

    private fun openHabitTracker() {
        val intent = Intent(this, HabitTrackerActivity::class.java)
        startActivity(intent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_quran_reader -> {
                openQuranReader()
            }
            R.id.nav_habits_tracker -> {
                openHabitTracker()
            }
            R.id.nav_ayah_day -> {
                // TODO later
            }
            R.id.nav_facts_miracles -> {
                // TODO later
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
