package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.home)

        val registrar = findViewById<Button>(R.id.CameraBtn)

        registrar.setOnClickListener {
            val intent = Intent(this, AdicionarActivity::class.java)
            startActivity(intent)
        }
    }
}

