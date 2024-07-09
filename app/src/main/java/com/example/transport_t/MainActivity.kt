package com.example.transport_t

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Set initial fragment
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, FragmentOption1()).commit()

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.navigation_option1 -> selectedFragment = FragmentOption1()
                R.id.navigation_option2 -> selectedFragment = FragmentOption2()
                R.id.navigation_option3 -> selectedFragment = FragmentOption3()
            }
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, selectedFragment).commit()
            }
            true
        }
    }
}
