package com.example.treerecord

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AllTrees : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var treesRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var treesContainerLayout: LinearLayout
    private lateinit var filterButtonsContainer: HorizontalScrollView
    private lateinit var userID: String

    private var allTreesList = listOf<DataSnapshot>() // Cache para todas as árvores

    // Limites das regiões
    private val northBounds = LatLngBounds(40.422636, 41.968557, -8.812804, -6.162193)
    private val centerBounds = LatLngBounds(38.715324, 40.422636, -8.812804, -7.200913)
    private val southBounds = LatLngBounds(36.994945, 38.715324, -8.967550, -7.200913)
    private val azoresBounds = LatLngBounds(36.823214, 39.974563, -31.522465, -24.89659)
    private val madeiraBounds = LatLngBounds(32.533302, 33.209779, -17.637543, -16.132033)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alltrees)

        // Iniciar Firebase
        database = FirebaseDatabase.getInstance("https://tree-record-db-default-rtdb.europe-west1.firebasedatabase.app")
        treesRef = database.getReference("arvores")
        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser?.uid ?: ""

        // Iniciar UI
        treesContainerLayout = findViewById(R.id.trees_container)
        filterButtonsContainer = findViewById(R.id.filter_buttons_container_scroll)

        checkIfUserIsPremium()
        setupFilterButtons()
    }

    private fun checkIfUserIsPremium() {
        if (userID.isEmpty()) {
            filterButtonsContainer.visibility = View.GONE
            loadAllTreesFromFirebase()
            return
        }

        val premiumRef = database.getReference("premium_users").child(userID)
        premiumRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.value == true) {
                    filterButtonsContainer.visibility = View.VISIBLE
                } else {
                    filterButtonsContainer.visibility = View.GONE
                }
                loadAllTreesFromFirebase()
            }

            override fun onCancelled(error: DatabaseError) {
                filterButtonsContainer.visibility = View.GONE
                loadAllTreesFromFirebase()
            }
        })
    }

    private fun setupFilterButtons() {
        findViewById<Button>(R.id.btn_filter_all).setOnClickListener { displayTrees(allTreesList) }
        findViewById<Button>(R.id.btn_filter_north).setOnClickListener { filterTreesByRegion(northBounds) }
        findViewById<Button>(R.id.btn_filter_center).setOnClickListener { filterTreesByRegion(centerBounds) }
        findViewById<Button>(R.id.btn_filter_south).setOnClickListener { filterTreesByRegion(southBounds) }
        findViewById<Button>(R.id.btn_filter_azores).setOnClickListener { filterTreesByRegion(azoresBounds) }
        findViewById<Button>(R.id.btn_filter_madeira).setOnClickListener { filterTreesByRegion(madeiraBounds) }
    }

    private fun loadAllTreesFromFirebase() {
        treesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    allTreesList = snapshot.children.toList()
                    displayTrees(allTreesList)
                } else {
                    Toast.makeText(this@AllTrees, "No trees found in the database.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AllTrees, "Error loading trees: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterTreesByRegion(bounds: LatLngBounds) {
        val filteredList = allTreesList.filter { treeSnapshot ->
            val lat = treeSnapshot.child("localizacao/latitude").getValue(Double::class.java)
            val lon = treeSnapshot.child("localizacao/longitude").getValue(Double::class.java)
            lat != null && lon != null && bounds.contains(lat, lon)
        }
        displayTrees(filteredList)
    }

    private fun displayTrees(treesToDisplay: List<DataSnapshot>) {
        treesContainerLayout.removeAllViews()

        if (treesToDisplay.isEmpty()) {
            Toast.makeText(this, "Nenhuma árvore encontrada para este filtro.", Toast.LENGTH_SHORT).show()
            return
        }

        for (treeSnapshot in treesToDisplay) {

            val treeId = treeSnapshot.key
            val nome = treeSnapshot.child("nome").getValue(String::class.java) ?: "Sem nome"
            val descricao = treeSnapshot.child("descricao").getValue(String::class.java) ?: "Sem descrição"
            val urlFoto = treeSnapshot.child("urlFoto").getValue(String::class.java)
            val lat = treeSnapshot.child("localizacao/latitude").getValue(Double::class.java)
            val lon = treeSnapshot.child("localizacao/longitude").getValue(Double::class.java)

            val gostosMap = treeSnapshot.child("gostos").children.associate { it.key!! to it.getValue(Boolean::class.java)!! }
            val jaGostou = gostosMap.containsKey(userID)

            // ImageView para a foto
            val treeImageView = ImageView(this@AllTrees).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 600).apply {
                    setMargins(0, 20, 0, 20)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            treesContainerLayout.addView(treeImageView)

            if (!urlFoto.isNullOrEmpty()) {
                Glide.with(this@AllTrees).load(urlFoto).placeholder(R.drawable.placeholder_image).error(R.drawable.error_image).into(treeImageView)
            } else {
                treeImageView.setImageResource(R.drawable.no_image_available)
            }

            // TextView para os detalhes
            val textView = TextView(this@AllTrees).apply {
                text = "Nome: $nome\nLocalização: ${lat ?: "N/A"}, ${lon ?: "N/A"}\nDescrição: $descricao"
                textSize = 16f
                setPadding(0, 10, 0, 20)
            }
            treesContainerLayout.addView(textView)

            // Layout horizontal para o botão de gosto
            val horizontalLayout = LinearLayout(this@AllTrees).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 0, 0, 30)
            }

            val likesCount = gostosMap.size
            val likeButton = Button(this@AllTrees).apply {
                text = if (jaGostou) " Gostou ($likesCount)" else " Gosto ($likesCount)"
                setOnClickListener {
                    if (treeId != null) {
                        toggleLike(treeId, jaGostou)
                    }
                }
            }
            horizontalLayout.addView(likeButton)
            treesContainerLayout.addView(horizontalLayout)
        }
    }

    private fun toggleLike(treeId: String, currentlyLiked: Boolean) {
        if (userID.isEmpty()){
            Toast.makeText(this, "Precisa de estar autenticado para gostar.", Toast.LENGTH_SHORT).show()
            return
        }
        val treeLikesRef = treesRef.child(treeId).child("gostos").child(userID)

        if (currentlyLiked) {
            treeLikesRef.removeValue()
        } else {
            treeLikesRef.setValue(true)
        }
    }

    data class LatLngBounds(val minLat: Double, val maxLat: Double, val minLon: Double, val maxLon: Double) {
        fun contains(lat: Double, lon: Double): Boolean {
            return lat in minLat..maxLat && lon in minLon..maxLon
        }
    }
}
