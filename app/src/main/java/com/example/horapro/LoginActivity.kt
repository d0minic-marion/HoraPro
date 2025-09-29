package com.example.horapro

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var email: TextInputEditText
    private lateinit var pwd: TextInputEditText
    private lateinit var btnLogin: Button
    private var progress: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        email = findViewById(R.id.etEmail)
        pwd   = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progress = findViewById(R.id.progress)

        findViewById<TextView?>(R.id.tvSignup)?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<TextView?>(R.id.tvForgot)?.setOnClickListener {
            val e = email.text?.toString()?.trim().orEmpty()
            if (e.isBlank()) {
                toast("Entre d’abord ton email.")
            } else {
                setLoading(true)
                auth.sendPasswordResetEmail(e)
                    .addOnSuccessListener {
                        setLoading(false)
                        toast("Email de réinitialisation envoyé.")
                    }
                    .addOnFailureListener { ex ->
                        setLoading(false)
                        toast(ex.message ?: "Erreur d’envoi.")
                    }
            }
        }

        btnLogin.setOnClickListener { onLoginClick() }

    }

    private fun onLoginClick() {
        val e = email.text?.toString()?.trim().orEmpty()
        val p = pwd.text?.toString()?.trim().orEmpty()

        if (e.isBlank() || p.isBlank()) {
            toast("Email et mot de passe requis")
            return
        }

        setLoading(true)

        auth.signInWithEmailAndPassword(e, p)
            .addOnSuccessListener {
                setLoading(false)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { ex ->
                setLoading(false)
                toast(messageFromAuthException(ex))
            }
    }

    private fun setLoading(loading: Boolean) {
        btnLogin.isEnabled = !loading
        progress?.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    private fun messageFromAuthException(ex: Exception): String {
        val raw = ex.message ?: return "Échec de la connexion"
        return when {
            raw.contains("password is invalid", true) -> "Mot de passe invalide"
            raw.contains("no user record", true) -> "Aucun compte associé à cet email"
            raw.contains("blocked all requests", true) -> "Trop de tentatives. Réessaie plus tard."
            raw.contains("network error", true) -> "Problème de réseau."
            raw.contains("WEAK_PASSWORD", true) -> "Mot de passe trop faible"
            raw.contains("email address is badly formatted", true) -> "Email invalide"
            else -> raw
        }
    }
}