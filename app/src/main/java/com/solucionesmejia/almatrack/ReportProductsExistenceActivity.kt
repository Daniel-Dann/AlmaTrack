package com.solucionesmejia.almatrack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar

class ReportProductsExistenceActivity : AppCompatActivity() {

    private var inventoryId: String = ""
    private var inventoryName: String = ""
    private lateinit var currency: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_products_existence)

        inventoryId = intent.getStringExtra("inventoryId") ?: ""
        inventoryName = intent.getStringExtra("inventoryName") ?: ""
        currency = intent.getStringExtra("inventoryCurrency") ?: "MXN"

        val toolbar = findViewById<Toolbar>(R.id.toolbarProductExistences)
        toolbar.title = "Existencia de Productos $inventoryName"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }
}