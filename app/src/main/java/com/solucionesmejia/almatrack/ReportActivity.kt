package com.solucionesmejia.almatrack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar

class ReportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)


        val toolbar = findViewById<Toolbar>(R.id.toolbarReport)
        setSupportActionBar(toolbar)
        // Habilita el botón de regreso (opcional)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Obtener el nombre del inventario del intent
        val inventoryName = intent.getStringExtra("inventoryName") ?: "Inventario"
        // Título personalizado
        supportActionBar?.title = "Reportes: Inventario $inventoryName"

    }

    // Hacer que el botón de regreso funcione
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}