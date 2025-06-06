package com.solucionesmejia.almatrack

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.solucionesmejia.almatrack.adapters.ProductProfitabilityAdapter
import androidx.core.widget.NestedScrollView
import androidx.core.widget.NestedScrollView.OnScrollChangeListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ReportProductProfitabilityActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductProfitabilityAdapter
    private lateinit var searchEditText: TextInputEditText
    private lateinit var layoutNoResults: LinearLayout
    private lateinit var currency: String
    private var allProducts: List<Product> = listOf()
    private var inventoryId: String = ""
    private var inventoryName: String = ""
    private lateinit var fabExportPdf: FloatingActionButton
    private var isFabVisible = true
    private lateinit var btnExportPdf: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_product_profitability)

        btnExportPdf = findViewById(R.id.btnExportPdf)

        inventoryId = intent.getStringExtra("inventoryId") ?: ""
        inventoryName = intent.getStringExtra("inventoryName") ?: ""
        currency = intent.getStringExtra("inventoryCurrency") ?: "MXN"

        val toolbar = findViewById<Toolbar>(R.id.toolbarProductProfitability)
        toolbar.title = "Rentabilidad de Productos $inventoryName"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerViewProducts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        searchEditText = findViewById(R.id.etSearchProductsProfits)
        layoutNoResults = findViewById(R.id.layoutNoResults)
        layoutNoResults.visibility = View.GONE


        loadProducts()
        setupSearch()
        setupScrollBehavior()
    }

    private fun setupScrollBehavior() {
        val scrollView = findViewById<NestedScrollView>(R.id.nestedScrollView) // Necesitarás agregar este ID a tu layout

        scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            when {
                // Scroll hacia abajo
                // Scroll hacia abajo
                scrollY > oldScrollY + 5 -> {
                    btnExportPdf.animate().translationY(btnExportPdf.height.toFloat()).setDuration(300).start()
                }
                // Scroll hacia arriba
                scrollY < oldScrollY - 5 -> {
                    btnExportPdf.animate().translationY(0f).setDuration(300).start()
                }
            }
        })
    }

    private fun loadProducts() {
        if (inventoryId.isEmpty()) {
            Toast.makeText(this, "ID del inventario no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("inventories")
            .document(inventoryId)
            .collection("products")
            .get()
            .addOnSuccessListener { snapshot ->
                allProducts = snapshot.documents
                    .mapNotNull { it.toObject(Product::class.java) }
                    .sortedBy { it.name.lowercase() }

                adapter = ProductProfitabilityAdapter(allProducts)
                recyclerView.adapter = adapter

                // Si no hay productos, mostrar mensaje
                if (allProducts.isEmpty()) {
                    layoutNoResults.visibility = View.VISIBLE
                } else {
                    layoutNoResults.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar productos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupSearch() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(query: CharSequence?, start: Int, before: Int, count: Int) {
                val filtered = allProducts.filter {
                    it.name.contains(query.toString(), ignoreCase = true)
                }.sortedBy { it.name.lowercase() }

                adapter = ProductProfitabilityAdapter(filtered)
                recyclerView.adapter = adapter

                if (filtered.isEmpty()) {
                    if (layoutNoResults.visibility != View.VISIBLE) {
                        layoutNoResults.startAnimation(fadeIn)
                        layoutNoResults.visibility = View.VISIBLE
                    }
                } else {
                    layoutNoResults.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}




