package com.example.transport_t

import ImageAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class Instr : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var buttonNext: Button
    private lateinit var images: List<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.instr)

        viewPager = findViewById(R.id.viewPager)
        buttonNext = findViewById(R.id.button_next)

        // Asegúrate de tener las imágenes en la carpeta res/drawable
        images = listOf(
            R.drawable.image1, // image1.png en res/drawable
            R.drawable.image2, // image2.png en res/drawable
            R.drawable.image3  // image3.png en res/drawable
        )

        val adapter = ImageAdapter(images)
        viewPager.isUserInputEnabled = false
        viewPager.adapter = adapter
        viewPager.setPageTransformer { _, _ -> }
        buttonNext.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < images.size - 1) {
                viewPager.setCurrentItem(currentItem + 1, false)
            } else {
                val intent = Intent(this@Instr, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                buttonNext.text = if (position == images.size - 1) "Iniciar" else "Siguiente"
            }
        })
    }
}
