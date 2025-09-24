package com.example.horapro


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        val email = findViewById<TextInputEditText>(R.id.etEmail)
        val pwd = findViewById<TextInputEditText>(R.id.etPassword)
        val btn = findViewById<Button>(R.id.btnLogin)
        val bottom = findViewById<TextView>(R.id.tvBottom)


        btn.setOnClickListener {
            val e = email.text?.toString().orEmpty()
            val p = pwd.text?.toString().orEmpty()
            if (e.isBlank() || p.isBlank()) {
                Toast.makeText(this, "Email et mot de passe requis", Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }


        bottom.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}