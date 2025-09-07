package com.example.treerecord

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MyTrees : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var treesRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var layout: LinearLayout
    private var myTreesValueEventListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alltrees) // You can reuse the same layout file

        // Iniciar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://tree-record-db-default-rtdb.europe-west1.firebasedatabase.app")
        treesRef = database.getReference("arvores")

        layout = findViewById(R.id.trees_container)

        // Verificação de login
        if (auth.currentUser != null) {
            loadCurrentUserTrees()
        } else {
            Toast.makeText(this, "You need to be logged in to see your trees.", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadCurrentUserTrees() {
        val currentUserID = auth.currentUser?.uid ?: return

        // Filtro para só mostrar as árvores com o userid com login feito
        // This query filters the trees to only get the ones where 'userId' matches the current user's ID.
        val query = treesRef.orderByChild("userId").equalTo(currentUserID)


        myTreesValueEventListener?.let { query.removeEventListener(it) }

        myTreesValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                layout.removeAllViews()

                if (snapshot.exists()) {
                    for (treeSnapshot in snapshot.children) {
                        val treeId = treeSnapshot.key
                        val nome = treeSnapshot.child("nome").getValue(String::class.java) ?: "No name"
                        val descricao = treeSnapshot.child("descricao").getValue(String::class.java) ?: "No description"
                        val urlFoto = treeSnapshot.child("urlFoto").getValue(String::class.java)

                        val localizacaoSnapshot = treeSnapshot.child("localizacao")
                        val latitude = localizacaoSnapshot.child("latitude").getValue(Double::class.java)
                        val longitude = localizacaoSnapshot.child("longitude").getValue(Double::class.java)


                        val treeImageView = ImageView(this@MyTrees).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                600
                            ).apply {
                                setMargins(0, 20, 0, 20)
                            }
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                        layout.addView(treeImageView)

                        // Carregar a imagem
                        if (!urlFoto.isNullOrEmpty()) {
                            Glide.with(this@MyTrees)
                                .load(urlFoto)
                                .placeholder(R.drawable.placeholder_image)
                                .error(R.drawable.error_image)
                                .into(treeImageView)
                        } else {
                            treeImageView.setImageResource(R.drawable.no_image_available)
                        }


                        val textView = TextView(this@MyTrees).apply {
                            text = "Name: $nome\nLocation: ${latitude?.toString() ?: "N/A"}, ${longitude?.toString() ?: "N/A"}\nDescription: $descricao"
                            textSize = 16f
                            setPadding(0, 10, 0, 20)
                        }
                        layout.addView(textView)
                    }
                } else {
                    // Messagem mostrada ao utilizador se este não tiver árvores guardadas
                    Toast.makeText(this@MyTrees, "You have not uploaded any trees yet.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MyTrees, "Error loading your trees: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        query.addValueEventListener(myTreesValueEventListener!!)
    }

    override fun onPause() {
        super.onPause()

        myTreesValueEventListener?.let { treesRef.removeEventListener(it) }
    }
}