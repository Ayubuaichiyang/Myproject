package com.xuyang.a202305100227.Myproject

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottom_navigation)

        if (supportFragmentManager.fragments.isEmpty()) {
            replaceFragment(EatWhatFragment())
            bottomNavigation.selectedItemId = R.id.nav_eat
        }

//        bottomNavigation.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.nav_eat -> {
//                    replaceFragment(EatWhatFragment())
//                    showBottomNavigation()
//                    true
//                }
//                R.id.nav_diary -> {
//                    replaceFragment(FoodDiaryFragment())
//                    showBottomNavigation()
//                    true
//                }
//                else -> false
//            }
//        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun hideBottomNavigation() {
        bottomNavigation.visibility = android.view.View.GONE
    }

    fun showBottomNavigation() {
        bottomNavigation.visibility = android.view.View.VISIBLE
    }
}