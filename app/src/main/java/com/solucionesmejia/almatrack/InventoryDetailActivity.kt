package com.solucionesmejia.almatrack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class InventoryDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_detail)

        val toolbar = findViewById<Toolbar>(R.id.toolbarDetail1)
        setSupportActionBar(toolbar)

        // Obtener nombre e ID del inventario
        val inventoryName = intent.getStringExtra("inventory_name")
        val inventoryId = intent.getStringExtra("inventory_id")

        supportActionBar?.title = inventoryName ?: "Inventario"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Aquí puedes cargar los productos de ese inventario usando inventoryId
    }

    // Para que la flecha de "back" funcione
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}