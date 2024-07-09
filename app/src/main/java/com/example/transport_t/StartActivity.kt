package com.example.transport_t

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start)

        // Delay for 2 seconds and then start MainActivity
        Handler().postDelayed({
            val intent = Intent(this, Instr::class.java)
            startActivity(intent)
            finish()
        }, 2000) // 2000 milliseconds = 2 seconds
    }
}
