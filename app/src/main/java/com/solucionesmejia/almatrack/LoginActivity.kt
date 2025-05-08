package com.solucionesmejia.almatrack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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

        //Le decimos al botón:
        // 🔘 “Cuando alguien te presione, haz esto…”
        btGetInto.setOnClickListener {

            //Tomamos lo que el usuario escribió en los campos y lo guardamos en dos variables: email y password.
            //Es como decir:
            //✏️ “Guarda lo que escribió el usuario en estas cajitas”
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            //Revisamos que los campos no estén vacíos.
            if (email.isNotEmpty() && password.isNotEmpty()) {

                //Aquí le decimos a Firebase:
                //👮 “Verifica si este correo y contraseña existen y son correctos”.
                auth.signInWithEmailAndPassword(email, password)

                    //Cuando Firebase termina de revisar, nos responde.
                    //Si el task fue exitoso (es decir, sí existe ese usuario y la contraseña es correcta):
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            //Mandamos al usuario a la pantalla MainActivity, como quien dice:
                            //🚪 “¡Bienvenido! Puedes entrar a la app”.
                            //finish() cierra
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            //Si Firebase responde con un error (por ejemplo, mal correo o contraseña), mostramos un mensaje emergente (un “toast”).
                            Toast.makeText(
                                this, "¡Ups! Parece que tu [Conrreo/Contraseña] son Incorrectas", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                //Si los campos estaban vacíos, mostramos un mensaje para que el usuario sepa que falta algo.
                Toast.makeText(this, "¡Ups! Parece que olvidaste ingresar tu [Conrreo/Contraseña]", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Verifica si el usuario ya está autenticado (sesión iniciada antes).
    //Si es así, lo mandamos directo a MainActivity, sin pedirle login otra vez.
    /*override fun onStart(){
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }*/
}


