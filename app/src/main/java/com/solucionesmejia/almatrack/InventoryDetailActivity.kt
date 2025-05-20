package com.solucionesmejia.almatrack

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.solucionesmejia.almatrack.adapter.ProductAdapter

class InventoryDetailActivity : AppCompatActivity() {

    private lateinit var inventoryId: String
    private lateinit var inventoryName: String
    private lateinit var inventoryCurrency: String
    private val firestore = FirebaseFirestore.getInstance()

    private val productList = mutableListOf<Product>()
    private lateinit var productAdapter: ProductAdapter
    private lateinit var rvProducts: RecyclerView
    private lateinit var etSearch: EditText  // NUEVO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_detail)

        // Configurar Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarDetail1)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Obtener datos del inventario
        inventoryId = intent.getStringExtra("inventory_id") ?: ""
        inventoryName = intent.getStringExtra("inventory_name") ?: "Inventario"
        inventoryCurrency = intent.getStringExtra("inventory_currency") ?: "MXN"
        supportActionBar?.title = inventoryName

        // Referencias UI
        etSearch = findViewById(R.id.etSearchProduct)  // NUEVO
        rvProducts = findViewById(R.id.rvProducts)
        rvProducts.layoutManager = LinearLayoutManager(this)
        productAdapter = ProductAdapter(emptyList())
        rvProducts.adapter = productAdapter

        // Buscar productos al escribir
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase().trim()
                val filteredList = productList.filter {
                    it.name.lowercase().contains(query) || it.description.lowercase().contains(query)
                }
                productAdapter.updateList(filteredList, query)
            }
        })

        // Botón para agregar producto
        val fabAddProduct = findViewById<FloatingActionButton>(R.id.fabAddProduct)
        fabAddProduct.setOnClickListener {
            showAddProductDialog()
        }

        // Escuchar productos en tiempo real
        firestore.collection("inventories")
            .document(inventoryId)
            .collection("products")
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) {
                    Toast.makeText(this, "Error al cargar productos", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                productList.clear()
                for (document in snapshots) {
                    val product = document.toObject(Product::class.java)
                    product.currency = inventoryCurrency // Asegura que tenga la moneda
                    productList.add(product)
                }

                // Actualizar lista completa (puede estar filtrada si el usuario está buscando)
                val query = etSearch.text.toString().lowercase().trim()
                val filtered = if (query.isEmpty()) productList else productList.filter {
                    it.name.lowercase().contains(query) || it.description.lowercase().contains(query)
                }
                productAdapter.updateList(filtered, query)
            }
    }

    private fun showAddProductDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etProductName)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etProductDescription)
        val etPrice = dialogView.findViewById<TextInputEditText>(R.id.etProductPrice)
        val etStock = dialogView.findViewById<TextInputEditText>(R.id.etProductStock)
        val btnCreate = dialogView.findViewById<Button>(R.id.btnCreateProduct)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnCreate.setOnClickListener {
            val name = etName.text.toString().trim()
            val rawDescription = etDescription.text.toString().trim()
            val description = if (rawDescription.isEmpty()) "Sin descripción" else rawDescription
            val price = etPrice.text.toString().trim().toDoubleOrNull()
            val stock = etStock.text.toString().trim().toIntOrNull()

            if (name.isEmpty() || price == null || stock == null) {
                Snackbar.make(dialogView, "Por favor completa todos los campos", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newProduct = Product(
                name = name,
                description = description,
                price = price,
                stock = stock
            )

            firestore.collection("inventories")
                .document(inventoryId)
                .collection("products")
                .add(newProduct)
                .addOnSuccessListener {
                    dialog.dismiss()
                    Snackbar.make(findViewById(android.R.id.content), "✅ Producto \"$name\" agregado correctamente", Snackbar.LENGTH_LONG)
                        .setAnchorView(findViewById(R.id.fabAddProduct))
                        .show()
                }
                .addOnFailureListener {
                    Snackbar.make(findViewById(android.R.id.content), "❌ Error al agregar producto", Snackbar.LENGTH_LONG)
                        .show()
                }
        }

        dialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
