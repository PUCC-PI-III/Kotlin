package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.ByteArrayOutputStream
import java.io.IOException

class AdicionarActivity : AppCompatActivity() {

    private lateinit var clickedImage: ImageView
    private lateinit var tituloInput: EditText
    private lateinit var obsInput: EditText
    private lateinit var adicionarBtn: Button
    private var imagemBitmap: Bitmap? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double? = null
    private var longitude: Double? = null

    private val LOCATION_REQUEST_CODE = 100

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val photo = result.data?.extras?.get("data") as? Bitmap
        photo?.let {
            imagemBitmap = it
            clickedImage.setImageBitmap(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.criar_risco)

        clickedImage = findViewById(R.id.imagemRiscoView)
        tituloInput = findViewById(R.id.tituloInput)
        obsInput = findViewById(R.id.descricaoInput)
        adicionarBtn = findViewById(R.id.AdicionarBtn)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        solicitarPermissaoLocalizacao()

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(cameraIntent)

        adicionarBtn.setOnClickListener {
            if (imagemBitmap != null && tituloInput.text.isNotEmpty() && obsInput.text.isNotEmpty()) {
                if (latitude != null && longitude != null) {
                    enviarParaServidor(
                        tituloInput.text.toString(),
                        obsInput.text.toString(),
                        "$latitude,$longitude",
                        imagemBitmap!!
                    )
                } else {
                    Toast.makeText(this, "Localização ainda não disponível", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos e tire uma foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun solicitarPermissaoLocalizacao() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
        } else {
            obterLocalizacao()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obterLocalizacao()
        } else {
            Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun obterLocalizacao() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
            }
        }
    }

    private fun enviarParaServidor(titulo: String, obs: String, localizacao: String, bitmap: Bitmap) {
        val client = OkHttpClient()
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val imageBytes = stream.toByteArray()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("titulo", titulo)
            .addFormDataPart("obs", obs)
            .addFormDataPart("localizacao", localizacao)
            .addFormDataPart(
                "imagem",
                "foto.jpg",
                RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageBytes)
            )
            .build()

        val request = Request.Builder()
            .url("http://10.0.2.2:3000/riscos")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@AdicionarActivity, "Erro ao enviar", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AdicionarActivity, "Risco enviado com sucesso", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@AdicionarActivity, "Erro no servidor", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}

