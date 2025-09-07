package com.example.treerecord

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    lateinit var editTextEmail: TextInputEditText
    lateinit var editTextPassword: TextInputEditText
    lateinit var buttonLog: Button
    lateinit var mAuth: FirebaseAuth
    lateinit var progressBar: ProgressBar
    lateinit var textView: TextView

    public override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        if (currentUser != null) {

            val intent = Intent(this@Login, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()
        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        buttonLog = findViewById(R.id.btn_login)
        progressBar = findViewById(R.id.progressBar)
        textView = findViewById(R.id.registerNow)

        buttonLog.setOnClickListener(){

            progressBar.visibility = View.VISIBLE
            var email = editTextEmail.text.toString()
            var password = editTextPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                editTextEmail.error = "Preencha o email"
                editTextPassword.error = "Preencha a palavra-passe"
                return@setOnClickListener
            }

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener() { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext,"Login sucessful",Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@Login, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {


                        Toast.makeText(
                            this@Login,
                            "Authentication failed",
                            Toast.LENGTH_SHORT,
                        ).show()

                    }
                }
        }

        textView.setOnClickListener{
            val intent = Intent(this@Login, Registar::class.java)
            startActivity(intent)
            finish()
        }

    }
}