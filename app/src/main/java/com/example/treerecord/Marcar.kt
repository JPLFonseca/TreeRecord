package com.example.treerecord

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class Marcar : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var googleMap: GoogleMap
    private var selectedImageUri: Uri? = null
    private lateinit var imgPreview: ImageView

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    // Localizaocao do telemovel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Permissao de localizacao
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                enableMyLocation()
            } else {
                Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_LONG).show()
            }
        }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                if (::imgPreview.isInitialized) {
                    imgPreview.setImageURI(uri)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marcar)

        // Iniciar firebase
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://tree-record-db-default-rtdb.europe-west1.firebasedatabase.app/")
        storage = FirebaseStorage.getInstance()


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)


    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.setOnMapClickListener(this)
        val defaultLocation = LatLng(38.7223, -9.1393)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 7f))

        // Tentativa de ativar a localização logo ao iniciar, se a permissão já existir
        checkLocationPermissionAndEnableMyLocation()
    }

    private fun checkLocationPermissionAndEnableMyLocation() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                enableMyLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Opcional: Mostrar uma explicação ao utilizador
                Toast.makeText(this, "A permissão de localização é necessária para mostrar a sua posição.", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                }
            }
        }
    }

    override fun onMapClick(latLng: LatLng) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_tree_record, null)
        val editName = dialogView.findViewById<EditText>(R.id.editTreeName)
        val editDesc = dialogView.findViewById<EditText>(R.id.editTreeDesc)
        val btnSelectPhoto = dialogView.findViewById<Button>(R.id.btnSelectPhoto)
        val txtCoordinates = dialogView.findViewById<TextView>(R.id.txtCoordinates)
        imgPreview = dialogView.findViewById(R.id.imgPreview)

        txtCoordinates.text = "Latitude: %.4f\nLongitude: %.4f".format(latLng.latitude, latLng.longitude)

        btnSelectPhoto.setOnClickListener {
            pickImageFromGallery()
        }

        val dialog = AlertDialog.Builder(this, R.style.DialogAnimation)
            .setTitle("Nova Árvore")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val name = editName.text.toString().trim()
                val desc = editDesc.text.toString().trim()

                if (name.isNotEmpty() && selectedImageUri != null) {
                    saveTreeToFirebase(name, desc, latLng)
                } else {
                    Toast.makeText(this, "O nome e a foto são obrigatórios.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.show()
    }

    private fun saveTreeToFirebase(name: String, desc: String, latLng: LatLng) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        Toast.makeText(this, "A guardar...", Toast.LENGTH_SHORT).show()
        val imageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")

        selectedImageUri?.let { uri ->
            imageRef.putFile(uri).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val localizacao = Localizacao(latLng.latitude, latLng.longitude)
                    val timestamp = System.currentTimeMillis()
                    val gostos = mapOf(userId to true)
                    val arvore = Arvore(name, desc, userId, timestamp, downloadUrl.toString(), localizacao, gostos)
                    database.getReference("arvores").push().setValue(arvore).addOnSuccessListener {
                        Toast.makeText(this, "Árvore guardada com sucesso!", Toast.LENGTH_LONG).show()
                        googleMap.addMarker(MarkerOptions().position(latLng).title(name))
                    }
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Falha ao carregar a imagem: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }
}
