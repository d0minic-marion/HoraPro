package com.example.projet.horapro

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

// Credential Manager + GIS
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.horapro.R
import com.example.horapro.RegisterActivity
import com.google.android.gms.common.SignInButton
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    private lateinit var email: TextInputEditText
    private lateinit var pwd: TextInputEditText
    private lateinit var btnLogin: Button
    private var progress: ProgressBar? = null
    private var btnGoogle: SignInButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        email = findViewById(R.id.etEmail)
        pwd   = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progress = findViewById(R.id.progress)
        btnGoogle = findViewById(R.id.btnGoogle)

        // Inscription
        findViewById<TextView?>(R.id.tvSignup)?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Reset password
        findViewById<TextView?>(R.id.tvForgot)?.setOnClickListener {
            val e = email.text?.toString()?.trim().orEmpty()
            if (e.isBlank()) {
                toast("Entre d’abord ton email.")
            } else {
                setLoading(true)
                auth.sendPasswordResetEmail(e)
                    .addOnSuccessListener { setLoading(false); toast("Email de réinitialisation envoyé.") }
                    .addOnFailureListener { ex -> setLoading(false); toast(ex.message ?: "Erreur d’envoi.") }
            }
        }

        // Email / mot de passe
        btnLogin.setOnClickListener { onLoginClick() }

        // Google via Credential Manager
        btnGoogle?.setOnClickListener { signInWithGoogle() }
    }

    // -------- Email / Password --------
    private fun onLoginClick() {
        val e = email.text?.toString()?.trim().orEmpty()
        val p = pwd.text?.toString()?.trim().orEmpty()
        if (e.isBlank() || p.isBlank()) {
            toast("Email et mot de passe requis"); return
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

    // -------- Google (Credential Manager + GIS) --------
    private fun signInWithGoogle() {
        // IMPORTANT : ajoute R.string.server_client_id dans strings.xml (voir plus bas)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getString(R.string.server_client_id))
            .setFilterByAuthorizedAccounts(false) // true = comptes déjà autorisés seulement
            .setAutoSelectEnabled(false)          // true = auto-select s’il n’y a qu’un choix
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        setLoading(true)
        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(this@LoginActivity, request)
                val cred = result.credential

                if (cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleCred = GoogleIdTokenCredential.createFrom(cred.data)
                    val idToken = googleCred.idToken
                    val firebaseCred = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCred)
                        .addOnSuccessListener {
                            setLoading(false)
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { ex ->
                            setLoading(false)
                            toast(messageFromAuthException(ex))
                        }
                } else {
                    setLoading(false)
                    toast("Type d’identifiant non supporté.")
                }
            } catch (e: GetCredentialException) {
                setLoading(false)
                // Annulation par l’utilisateur, pas d’Internet, etc.
                toast(e.message ?: "Connexion Google annulée/échouée.")
            } catch (e: Exception) {
                setLoading(false)
                toast(e.message ?: "Erreur inattendue.")
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        btnLogin.isEnabled = !loading
        btnGoogle?.isEnabled = !loading
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

// IMPORTANT test

