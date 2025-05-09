package com.solucionesmejia.almatrack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
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

            // ✅ Validación 1: Campo vacío
            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa tu correo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ Validación 2: Formato incorrecto
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ Solo si pasa las validaciones, mostramos la animación
            val loadingDialog = showLoadingDialog()

            // Enviamos el correo de recuperación
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    loadingDialog.dismiss() // Cerramos el diálogo de carga
                    if (task.isSuccessful) {
                        //Mostramos un dialogo de confirmación en un recuadro en ves del Toast
                        AlertDialog.Builder(this)
                            .setTitle("¡Correo enviado!\"")
                            .setMessage("Te hemos enviado un correo para restablecer tu contraseña. Revisa tu bandeja de entrada o tu carpeta de spam.")
                            .setIcon(R.drawable.vector_email)
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

    //Fución que permite mandar una ventana de carga de que se esta enviado el correo
    private fun showLoadingDialog(): AlertDialog {
        // LinearLayout Agrupa el círculo y el texto uno debajo del otro.
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 50, 50, 50)
        }

        //ProgressBar El círculo animado de carga.
        val progressBar = ProgressBar(this).apply {
            isIndeterminate = true
        }

        ////TextView Muestra el texto “Enviando correo…”.
        val message = TextView(this).apply { // TextView
            text = "Enviando correo..."
            textSize = 16f
            setPadding(0, 20, 0, 0)
            gravity = Gravity.CENTER
        }

        layout.addView(progressBar)
        layout.addView(message)

        val dialog = AlertDialog.Builder(this)
            .setView(layout)
            .setCancelable(false) // .setCancelable(false) El usuario no puede cerrar el diálogo mientras se está enviando.
            .create()

        dialog.show()
        return dialog
    }
}