package com.example.horapro

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ⚠️ Utilisation de ton vrai layout au lieu de simple_list_item_1
        setContentView(R.layout.activity_main)
        title = "Bienvenue dans HoraPro"
    }
}
