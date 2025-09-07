package com.example.treerecord

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Settings : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var emailTextView: TextView
    private lateinit var treeCountTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var premiumButton: Button // NEW: Premium button
    private lateinit var preferences: SharedPreferences
    private lateinit var languageSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        preferences = getSharedPreferences("settings", Context.MODE_PRIVATE)

        // Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://tree-record-db-default-rtdb.europe-west1.firebasedatabase.app/")

        // UI
        emailTextView = findViewById(R.id.txt_user_email)
        treeCountTextView = findViewById(R.id.txt_tree_count)
        logoutButton = findViewById(R.id.btn_logout)
        premiumButton = findViewById(R.id.btn_go_premium)
        languageSpinner = findViewById(R.id.spinner_language)

        val user = auth.currentUser
        if (user == null) {
            // Se não houver utilizador, volta para o login
            startActivity(Intent(this, Login::class.java))
            finish()
            return // Termina a execução do onCreate
        }

        // Mostrar email do utilizador
        emailTextView.text = user.email ?: "Não autenticado"

        // Carregar dados e configurar UI
        loadTreeCount(user.uid)
        checkPremiumStatus(user.uid) // NEW: Check premium status on load

        // Listener para o botão Premium
        premiumButton.setOnClickListener {
            upgradeToPremium(user.uid)
        }

        // Listener para o botão de Logout
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        // Lógica do seletor de idioma
        setupLanguageSpinner()
    }

    private fun loadTreeCount(userId: String) {
        val arvoresRef = database.getReference("arvores")
        val query = arvoresRef.orderByChild("userId").equalTo(userId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                treeCountTextView.text = snapshot.childrenCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {
                treeCountTextView.text = "Erro"
            }
        })
    }

    private fun checkPremiumStatus(userId: String) {
        val premiumRef = database.getReference("premium_users").child(userId)
        premiumRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.value == true) {
                    // Se o utilizador já for premium, atualiza a UI
                    updatePremiumUI()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Não faz nada em caso de erro, o botão continua normal
            }
        })
    }

    private fun upgradeToPremium(userId: String) {
        val premiumRef = database.getReference("premium_users").child(userId)
        premiumRef.setValue(true)
            .addOnSuccessListener {
                Toast.makeText(this, "Parabéns! Agora é um utilizador Premium.", Toast.LENGTH_LONG).show()
                updatePremiumUI()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ocorreu um erro. Tente novamente.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updatePremiumUI() {
        premiumButton.text = "É um Utilizador Premium"
        premiumButton.isEnabled = false // Desativa o botão
        premiumButton.background.setTint(Color.parseColor("#B8860B"))
    }

    private fun setupLanguageSpinner() {
        val languages = arrayOf("Português", "English", "Español")
        val languageCodes = arrayOf("pt", "en", "es")
        languageSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)

        val savedLang = preferences.getString("language", "pt") ?: "pt"
        val langIndex = languageCodes.indexOf(savedLang)
        languageSpinner.setSelection(if (langIndex >= 0) langIndex else 0)

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val selectedLang = languageCodes[position]
                preferences.edit().putString("language", selectedLang).apply()
                val appLocale = LocaleListCompat.forLanguageTags(selectedLang)
                AppCompatDelegate.setApplicationLocales(appLocale)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}