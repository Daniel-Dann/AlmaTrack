package com.solucionesmejia.almatrack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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


        //Aquí le decimos al botón:
        //“Cuando te presionen, haz todo esto que te voy a decir”.
        btGetInto.setOnClickListener {
            val email = etEmail.text.toString().trim()


            //Si el usuario no escribió su correo, mostramos un mensaje de error y paramos la función.
            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa tu correo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Revisamos que lo que escribió sí tenga forma de correo electrónico válido, usando una herramienta de Android (Patterns.EMAIL_ADDRESS).
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Enviamos el correo de recuperación
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //Mostramos un dialogo de confirmación en un recuadro en ves del Toast
                        AlertDialog.Builder(this)
                            .setTitle("¡Correo enviado!\"")
                            .setMessage("Te hemos enviado un correo para restablecer tu contraseña. Revisa tu bandeja de entrada o tu carpeta de spam.")
                            .setPositiveButton("Entendido") { dialog, _ ->
                                dialog.dismiss()
                                finish() // Cerrar esta pantalla y volver al login
                            }
                            .setCancelable(false).show() // Evita que se cierre tocando fuera del cuadro
                    }
                }
        }

        ivArrowBack.setOnClickListener {
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
        }
    }
}