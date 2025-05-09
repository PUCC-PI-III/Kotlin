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

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cadastro = findViewById<TextView>(R.id.textSemLogin)
        val risco = findViewById<Button>(R.id.loginBtn)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val senhaInput = findViewById<EditText>(R.id.senhaInput)

        cadastro.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }

        risco.setOnClickListener {
            val email = emailInput.text.toString()
            val senha = senhaInput.text.toString()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                loginUser(email, senha)
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, senha: String) {
        val url = "http://192.168.0.10:8080/users"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Erro na conexão: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val responseBody = response.body?.string()

                        // Verifica se a resposta é um array JSON
                        val usersArray = JSONArray(responseBody)

                        var userFound = false
                        for (i in 0 until usersArray.length()) {
                            val user = usersArray.getJSONObject(i)
                            if (user.getString("email") == email &&
                                user.getString("senha") == senha) {
                                userFound = true
                                break
                            }
                        }

                        if (userFound) {
                            Toast.makeText(this@MainActivity, "Login realizado com sucesso", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                        } else {
                            Toast.makeText(this@MainActivity, "Email ou senha incorretos", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Erro ao processar resposta do servidor", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}

