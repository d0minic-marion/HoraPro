package com.example.horapro


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText


class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        val email = findViewById<TextInputEditText>(R.id.etEmail)
        val pwd = findViewById<TextInputEditText>(R.id.etPassword)
        val terms = findViewById<CheckBox>(R.id.cbTerms)
        val btn = findViewById<Button>(R.id.btnContinue)
        val goLogin = findViewById<TextView>(R.id.tvSignin)


        btn.setOnClickListener {
            if (!terms.isChecked) {
                Toast.makeText(this, "Veuillez accepter les conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.text.isNullOrBlank() || pwd.text.isNullOrBlank()) {
                Toast.makeText(this, "Email et mot de passe requis", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
// Next step: call Firebase Auth here
            startActivity(Intent(this, MainActivity::class.java))
        }


        goLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}