package com.example.treerecord

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.transition.Visibility
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class Registar : AppCompatActivity() {

    public override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        if (currentUser != null) {

            val intent = Intent(this@Registar, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    lateinit var editTextEmail: TextInputEditText
    lateinit var editTextPassword: TextInputEditText
    lateinit var buttonReg: Button
    lateinit var mAuth: FirebaseAuth
    lateinit var progressBar: ProgressBar
    lateinit var textView:  TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_registar)
        mAuth = FirebaseAuth.getInstance()
        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        buttonReg = findViewById(R.id.btn_register)
        progressBar = findViewById(R.id.progressBar)
        textView = findViewById(R.id.loginNow)


        buttonReg.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            var email = editTextEmail.text.toString()
            var password = editTextPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                editTextEmail.error = "Preencha o email"
                editTextPassword.error = "Preencha a palavra-passe"
                return@setOnClickListener
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener() {
                        task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {

                        Toast.makeText(
                            this@Registar,
                            "Account created",
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {

                        Toast.makeText(
                            this@Registar,
                            "Authentication failed",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }

        textView.setOnClickListener{
            val intent = Intent(this@Registar, Login::class.java)
            startActivity(intent)
            finish()
        }
    }
}