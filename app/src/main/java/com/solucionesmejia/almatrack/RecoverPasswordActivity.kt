package com.solucionesmejia.almatrack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class RecoverPasswordActivity : AppCompatActivity() {

    // Declaramos una variable llamada auth que va a ser la encargada de hablar con Firebase para autenticar usuarios.
    // Usamos lateinit porque la vamos a inicializar más tarde, en onCreate.
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_password)

        // Aquí inicializamos la conexión con Firebase Authentication.
        // Es como prender la radio para poder hablar con Firebase: 📡
        auth = FirebaseAuth.getInstance()

        // Declaramos nuestras variables
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btGetInto = findViewById<Button>(R.id.btGetInto)
        val ivArrowBack = findViewById<ImageView>(R.id.ivArrowBack)

        btGetInto.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa tu correo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Enviamos el correo de recuperación
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Correo enviado. Revisa tu bandeja o spam.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish() // opcional: cerrar pantalla y volver al login
                    } else {
                        Toast.makeText(
                            this,
                            "Error: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        ivArrowBack.setOnClickListener{
            val i = Intent(this,LoginActivity::class.java)
            startActivity(i)
        }
    }
}