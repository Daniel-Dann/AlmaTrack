package com.solucionesmejia.almatrack

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.solucionesmejia.almatrack.adapter.ProductAdapter

class InventoryDetailActivity : AppCompatActivity() {

    private lateinit var inventoryId: String
    private lateinit var inventoryName: String
    private lateinit var inventoryCurrency: String
    private val firestore = FirebaseFirestore.getInstance()

    private val productList = mutableListOf<Product>()
    private lateinit var productAdapter: ProductAdapter
    private lateinit var rvProducts: RecyclerView
    private lateinit var etSearch: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_detail)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarDetail1)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Inventario
        inventoryId = intent.getStringExtra("inventory_id") ?: ""
        inventoryName = intent.getStringExtra("inventory_name") ?: "Inventario"
        inventoryCurrency = intent.getStringExtra("inventory_currency") ?: "MXN"
        supportActionBar?.title = inventoryName

        // RecyclerView y buscador
        etSearch = findViewById(R.id.etSearchProduct)
        rvProducts = findViewById(R.id.rvProducts)
        rvProducts.layoutManager = LinearLayoutManager(this)
        productAdapter = ProductAdapter(emptyList(),
            onEditClick = { product ->
                //Funcion editar
                showEditProductDialog(product)
            },
            onDeleteClick = { product -> confirmDeleteInventory(product)}
                // Aquí puedes eliminar el producto desde Firestore

        )

        rvProducts.adapter = productAdapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase().trim()
                val filtered = getFilteredList(query).sortedBy { it.name.lowercase() }
                productAdapter.updateList(filtered, query)
            }
        })

        findViewById<FloatingActionButton>(R.id.fabAddProduct).setOnClickListener {
            showAddProductDialog()
        }

        // Escuchar cambios
        firestore.collection("inventories")
            .document(inventoryId)
            .collection("products")
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) {
                    Toast.makeText(this, "Error al cargar productos", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                productList.clear()
                for (doc in snapshots) {
                    val product = doc.toObject(Product::class.java)
                    product.id = doc.id // Esta es la línea clave que debes agregar
                    product.currency = inventoryCurrency
                    productList.add(product)
                }

                val query = etSearch.text.toString().lowercase().trim()
                val filtered = getFilteredList(query).sortedBy { it.name.lowercase() }
                productAdapter.updateList(filtered, query)
            }
    }

    private fun getFilteredList(query: String): List<Product> {
        return if (query.isEmpty()) {
            productList
        } else {
            productList.filter {
                it.name.lowercase().contains(query) || it.description.lowercase().contains(query)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_inventory_detail, menu)
        return true
    }

    private fun showEditProductDialog(product: Product) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null)

        val etName = dialogView.findViewById<TextInputEditText>(R.id.etProductName)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etProductDescription)
        val etPrice = dialogView.findViewById<TextInputEditText>(R.id.etProductPrice)
        val etSalePrice = dialogView.findViewById<TextInputEditText>(R.id.etProductSalePrice)
        val etStock = dialogView.findViewById<TextInputEditText>(R.id.etProductStock)
        val btnCreate = dialogView.findViewById<Button>(R.id.btnCreateProduct)

        // Rellenar campos existentes
        etName.setText(product.name)
        etDescription.setText(product.description)
        etPrice.setText(product.price.toString())
        etSalePrice.setText(product.salePrice.toString())
        etStock.setText(product.stock.toString())
        etStock.isEnabled = false // No editable

        btnCreate.text = "Actualizar"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnCreate.setOnClickListener {
            val name = etName.text.toString().trim()
            val rawDescription = etDescription.text.toString().trim()
            val description = if (rawDescription.isEmpty()) "Sin descripción" else rawDescription
            val price = etPrice.text.toString().trim().toDoubleOrNull()
            val salePrice = etSalePrice.text.toString().trim().toDoubleOrNull()

            if (name.isEmpty() || price == null || salePrice == null) {
                Snackbar.make(dialogView, "Por favor completa todos los campos", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedProduct = hashMapOf(
                "name" to name,
                "description" to description,
                "price" to price,
                "salePrice" to salePrice
            )

            // Buscar documento y actualizar
            firestore.collection("inventories")
                .document(inventoryId)
                .collection("products")
                .whereEqualTo("name", product.name)
                .limit(1)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        snapshot.documents[0].reference.update(updatedProduct as Map<String, Any>)
                        Snackbar.make(findViewById(android.R.id.content), "✅   Producto \"$name\" actualizado", Snackbar.LENGTH_LONG)
                            .setAnchorView(findViewById(R.id.fabAddProduct))
                            .show()
                    }
                    dialog.dismiss()
                }
                .addOnFailureListener {
                    Snackbar.make(findViewById(android.R.id.content), "❌   Error al actualizar el producto \"$name\"", Snackbar.LENGTH_LONG).show()
                }
        }

        dialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val query = etSearch.text.toString().lowercase().trim()
        val filtered = getFilteredList(query)

        when (item.itemId) {
            R.id.sort_name -> {
                val sorted = filtered.sortedBy { it.name.lowercase() }
                productAdapter.updateList(sorted, query)
                return true
            }
            R.id.sort_price_costo -> {
                val sorted = filtered.sortedBy { it.price }
                productAdapter.updateList(sorted, query)
                return true
            }
            R.id.sort_price -> {
                val sorted = filtered.sortedBy { it.salePrice }
                productAdapter.updateList(sorted, query)
                return true
            }
            R.id.sort_stock -> {
                val sorted = filtered.sortedByDescending { it.stock }
                productAdapter.updateList(sorted, query)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAddProductDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etProductName)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etProductDescription)
        val etPrice = dialogView.findViewById<TextInputEditText>(R.id.etProductPrice)
        val etSalePrice = dialogView.findViewById<TextInputEditText>(R.id.etProductSalePrice)
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
            val salePrice = etSalePrice.text.toString().trim().toDoubleOrNull()
            val stock = etStock.text.toString().trim().toIntOrNull()

            if (name.isEmpty() || price == null || salePrice == null || stock == null) {
                etName.error = "Nombre requerido"
                etPrice.error = "Precio costo requerido"
                etSalePrice.error = "Precio venta requerido"
                etStock.error = "Cantidad de Stock requerido"
                return@setOnClickListener
            }

            val newProduct = Product(
                name = name,
                description = description,
                price = price,
                salePrice = salePrice,
                stock = stock
            )

            firestore.collection("inventories")
                .document(inventoryId)
                .collection("products")
                .add(newProduct)
                .addOnSuccessListener {
                    dialog.dismiss()
                    Snackbar.make(findViewById(android.R.id.content), "✅   Producto \"$name\" agregado correctamente", Snackbar.LENGTH_LONG)
                        .setAnchorView(findViewById(R.id.fabAddProduct))
                        .show()
                }
                .addOnFailureListener {
                    Snackbar.make(findViewById(android.R.id.content), "❌   Error al agregar el producto \"$name\"", Snackbar.LENGTH_LONG)
                        .show()
                }
        }

        dialog.show()
    }

    private fun confirmDeleteInventory(product: Product) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Eliminar producto")
            .setMessage("¿Seguro que deseas eliminar el producto \"${product.name}\"?")
            .setPositiveButton("Eliminar") { dialog, _ ->
                // Corrección: Usar la ruta correcta del documento
                firestore.collection("inventories")
                    .document(inventoryId)
                    .collection("products")
                    .document(product.id) // Asegúrate que product.id esté correctamente asignado
                    .delete()
                    .addOnSuccessListener {
                        // Eliminar también de la lista local
                        productList.removeAll { it.id == product.id }
                        // Actualizar el adaptador
                        val query = etSearch.text.toString().lowercase().trim()
                        val filtered = getFilteredList(query).sortedBy { it.name.lowercase() }
                        productAdapter.updateList(filtered, query)

                        Snackbar.make(findViewById(android.R.id.content), "✅   Producto \"${product.name}\" eliminado", Snackbar.LENGTH_SHORT).show()

                    }
                    .addOnFailureListener {
                        Snackbar.make(findViewById(android.R.id.content), "❌   Error al eliminar: ${it.message}", Snackbar.LENGTH_SHORT).show()
                    }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

}




