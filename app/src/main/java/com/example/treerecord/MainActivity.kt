package com.example.treerecord

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userDetailsText: TextView
    private lateinit var userID: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        auth = FirebaseAuth.getInstance()
        userDetailsText = findViewById(R.id.user_details)
        userID = auth.currentUser?.uid.toString()


        val user: FirebaseUser? = auth.currentUser
        if (user == null) {
            startActivity(Intent(this, Login::class.java))
            finish()
        } else {
            userDetailsText.text = "Email: ${user.email}"
        }



        val btnAbout = findViewById<ImageButton>(R.id.iconBottomLeft)
        btnAbout.setOnClickListener {
            val intent = Intent(this, About::class.java)
            startActivity(intent)
        }

        val btnInfo = findViewById<ImageButton>(R.id.iconTopLeft)
        btnInfo.setOnClickListener {
            val intent = Intent(this, Info::class.java)
            startActivity(intent)
        }

        val btnSettings = findViewById<ImageButton>(R.id.iconTopRight)
        btnSettings.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }

        val btnAllTrees = findViewById<Button>(R.id.button3)
        btnAllTrees.setOnClickListener {
            val intent = Intent(this, AllTrees::class.java)
            startActivity(intent)
        }

        val btnMarcar = findViewById<Button>(R.id.button1)
        btnMarcar.setOnClickListener {
            val intent = Intent(this, Marcar::class.java)
            startActivity(intent)
        }

        val btnArvoresGuardadas = findViewById<Button>(R.id.button2)
        btnArvoresGuardadas.setOnClickListener {
            val intent = Intent(this, MyTrees::class.java)
            startActivity(intent)
        }
    }
}
