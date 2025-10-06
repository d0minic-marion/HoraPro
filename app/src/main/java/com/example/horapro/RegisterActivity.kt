package com.example.horapro

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.horapro.model.User
import com.example.projet.horapro.LoginActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var btn: Button
    private var progress: ProgressBar? = null
    private lateinit var email: TextInputEditText
    private lateinit var pwd: TextInputEditText
    private lateinit var terms: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        email = findViewById(R.id.etEmail)
        pwd   = findViewById(R.id.etPassword)
        terms = findViewById(R.id.cbTerms)
        btn   = findViewById(R.id.btnContinue)
        progress = findViewById(R.id.progress)

        findViewById<TextView>(R.id.tvSignin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btn.setOnClickListener { onRegisterClick() }
    }

    private fun onRegisterClick() {
        val e = email.text?.toString()?.trim().orEmpty()
        val p = pwd.text?.toString()?.trim().orEmpty()

        if (!terms.isChecked) { toast("Veuillez accepter les conditions"); return }
        if (e.isBlank() || p.isBlank()) { toast("Email et mot de passe requis"); return }
        if (p.length < 6) { toast("Mot de passe trop court (min. 6 caractères)"); return }

        setLoading(true)

        auth.createUserWithEmailAndPassword(e, p)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid.isNullOrBlank()) {
                    setLoading(false)
                    toast("Erreur: UID introuvable")
                    return@addOnSuccessListener
                }

                val userDoc = User(
                    uid = uid,
                    email = e,
                    termsAccepted = terms.isChecked
                )

                db.collection("users").document(uid)
                    .set(userDoc)
                    .addOnSuccessListener {
                        setLoading(false)
                        toast("Compte créé avec succès !")
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { ex ->
                        auth.currentUser?.delete()
                        setLoading(false)
                        toast("Erreur BD: ${ex.localizedMessage}")
                    }
            }
            .addOnFailureListener { ex ->
                setLoading(false)
                toast(messageFromAuthException(ex))
            }

    }

    private fun setLoading(loading: Boolean) {
        btn.isEnabled = !loading
        progress?.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    private fun messageFromAuthException(ex: Exception): String {
        val raw = ex.message ?: return "Échec de l'inscription"
        return when {
            raw.contains("email address is badly formatted", true) -> "Email invalide"
            raw.contains("email address is already in use", true) -> "Email déjà utilisé"
            raw.contains("WEAK_PASSWORD", true) -> "Mot de passe trop faible"
            raw.contains("operation is not allowed", true) -> "Active le provider Email/Password dans Firebase Auth."
            else -> raw
        }
    }
}
