package com.solucionesmejia.almatrack

import android.content.Intent

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView

class ReportActivity : AppCompatActivity() {

    private lateinit var inventoryId: String
    private lateinit var inventoryName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        val toolbar = findViewById<Toolbar>(R.id.toolbarReport)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Obtener datos del inventario
        inventoryId = intent.getStringExtra("inventoryId") ?: ""
        inventoryName = intent.getStringExtra("inventoryName") ?: "Inventario"
        supportActionBar?.title = "Reportes $inventoryName"

        val cardView: CardView = findViewById(R.id.crvTotalValueInvested)
        cardView.setOnClickListener {
            val intent = Intent(this, ReportProductProfitabilityActivity::class.java)
            intent.putExtra("inventoryId", inventoryId)
            intent.putExtra("inventoryName", inventoryName)
            startActivity(intent)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}



