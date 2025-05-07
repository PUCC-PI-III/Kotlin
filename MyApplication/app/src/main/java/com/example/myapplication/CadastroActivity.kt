package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray
import java.io.IOException

class CadastroActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cadastro)

        val login = findViewById<TextView>(R.id.textJaCadastro)
        val cadastrar = findViewById<Button>(R.id.CadastroBtn)

        val emailInput = findViewById<EditText>(R.id.emailInputCad)
        val senhaInput = findViewById<EditText>(R.id.senhaInputCad)

        login.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        cadastrar.setOnClickListener {
            val email = emailInput.text.toString()
            val senha = senhaInput.text.toString()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                cadastrarUser(email, senha)
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cadastrarUser(email: String, senha: String) {
        val url = "http://10.0.2.2:3000/user"

        val json = JSONObject()
        json.put("email", email)
        json.put("senha", senha)

        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CadastroActivity, "Erro na conexão", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CadastroActivity, "Cadastro realizado com sucesso", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@CadastroActivity, "Erro ao cadastrar.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}

