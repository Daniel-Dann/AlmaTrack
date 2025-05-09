package com.solucionesmejia.almatrack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    // Declaramos una variable llamada auth que va a ser la encargada de hablar con Firebase para autenticar usuarios.
    // Usamos lateinit porque la vamos a inicializar más tarde, en onCreate.
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Aquí inicializamos la conexión con Firebase Authentication.
        // Es como prender la radio para poder hablar con Firebase: 📡
        auth = FirebaseAuth.getInstance()

        // Declaramos nuestras variables que usaremos para el login
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btGetInto = findViewById<Button>(R.id.btGetInto)
        val tvRecoverPaswword = findViewById<TextView>(R.id.tvRecoverPaswword)

        //Le decimos al botón:
        // 🔘 “Cuando alguien te presione, haz esto…”
        btGetInto.setOnClickListener {

            //Tomamos lo que el usuario escribió en los campos y lo guardamos en dos variables: email y password.
            //Es como decir:
            //✏️ “Guarda lo que escribió el usuario en estas cajitas”
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            //“👮Verifica que el correo y contraseña no esté vacía"
            if (email.isEmpty() && password.isEmpty()) {
                Toast.makeText(
                    this,
                    "¡Ups! Parece que olvidaste ingresar tu [Correo y Contraseña]",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            //“👮Verifica que el campo de email no esté vacío"
            if (email.isEmpty()) {
                Toast.makeText(
                    this,
                    "¡Ups! Parece que olvidaste ingresar tu [Correo]",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            //“👮Verifica que la contraseña no esté vacía"
            if (password.isEmpty()) {
                Toast.makeText(
                    this,
                    "¡Ups! Parece que olvidaste ingresar tu [Contraseña]",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            //“👮Verifica que tenga al menos 6 caracteres (Firebase lo exige)"
            if (password.length < 6) {
                Toast.makeText(
                    this,
                    "¡Ups! La contraseña debe tener al menos 6 caracteres",
                    Toast.LENGTH_SHORT
                ).show()
            }

            //“👮Verifica que el correo tenga formato correcto (contiene @ y .)"
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "¡Ups! Parece que el [Correo] no es valido", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            //Aquí le decimos a Firebase:
            //“👮Verifica si todo está bien, intentamos iniciar sesión”.
            auth.signInWithEmailAndPassword(email, password)

                //Si el task fue exitoso nos permitira el inicio de sesio:
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        //Mandamos al usuario a la pantalla MainActivity, como quien dice:
                        // 🚪 “¡Bienvenido! Puedes entrar a la app”.
                        //finish() cierra
                        Toast.makeText(this, "!Sesion Exitosa¡", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        //Mensaje de error de cualquier error que pueda tener firebase
                        Toast.makeText(
                            this,
                            "[Correo o Contraeña] incorrectas",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        tvRecoverPaswword.setOnClickListener{
            val i = Intent(this, RecoverPasswordActivity::class.java)
            startActivity(i)
        }


    }

    /*//Verifica si el usuario ya está autenticado (sesión iniciada antes).
    //Si es así, lo mandamos directo a MainActivity, sin pedirle login otra vez.
    override fun onStart(){
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }*/
}


