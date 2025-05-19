package com.solucionesmejia.almatrack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.solucionesmejia.almatrack.R
import com.solucionesmejia.almatrack.adapter.InventoryAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var inventoryAdapter: InventoryAdapter
    private val inventoryList = mutableListOf<Inventory>()
    private val firestore = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Inventario"*/

        // RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.rvInventory)
        inventoryAdapter = InventoryAdapter(
            inventoryList,
            onEditClick = { inventory -> showEditInventoryDialog(inventory) },
            onDeleteClick = { inventory -> confirmDeleteInventory(inventory) },
            onItemClick = { inventory -> openInventoryDetail(inventory) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = inventoryAdapter

        // FAB
        val fab = findViewById<FloatingActionButton>(R.id.fabAddInventory)
        fab.setOnClickListener {
            showAddInventoryDialog()
        }


        // Buscador
        val etSearch = findViewById<EditText>(R.id.etSearchInventory)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                inventoryAdapter.filterList(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Cargar datos desde Firestore
        loadInventories()
    }

    private fun showAddInventoryDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_inventory, null)
        val etName = dialogView.findViewById<EditText>(R.id.etInventoryName)
        val spinner = dialogView.findViewById<Spinner>(R.id.scSelectCurrency)
        val btnSave = dialogView.findViewById<Button>(R.id.btCreateInventory)

        // Configurar Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.currencies,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val currency = spinner.selectedItem.toString()

            if (name.isEmpty()) {
                etName.error = "Nombre requerido"
                return@setOnClickListener
            }

            val id = firestore.collection("inventories").document().id
            val inventory = Inventory(id, name, currency)

            firestore.collection("inventories").document(id)
                .set(inventory)
                .addOnSuccessListener {
                    Toast.makeText(this, "Inventario guardado", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()
    }

    private fun loadInventories() {
        firestore.collection("inventories")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val loadedInventories = snapshot?.documents?.mapNotNull {
                    it.toObject(Inventory::class.java)
                } ?: emptyList()

                inventoryAdapter.updateList(loadedInventories)
            }
    }

    private fun showEditInventoryDialog(inventory: Inventory) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_inventory, null)
        val etName = dialogView.findViewById<EditText>(R.id.etInventoryName)
        val spinner = dialogView.findViewById<Spinner>(R.id.scSelectCurrency)
        val btnSave = dialogView.findViewById<Button>(R.id.btCreateInventory)

        // Rellenar datos actuales
        etName.setText(inventory.name)

        // Configurar Spinner
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.currencies,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val position = adapter.getPosition(inventory.currency)
        spinner.setSelection(position)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnSave.setText("Actualizar")

        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newCurrency = spinner.selectedItem.toString()

            if (newName.isEmpty()) {
                etName.error = "Nombre requerido"
                return@setOnClickListener
            }

            val updatedInventory = inventory.copy(name = newName, currency = newCurrency)

            firestore.collection("inventories").document(inventory.id)
                .set(updatedInventory)
                .addOnSuccessListener {
                    Toast.makeText(this, "Inventario actualizado", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()
    }

    private fun confirmDeleteInventory(inventory: Inventory) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar inventario")
            .setMessage("¿Seguro que deseas eliminar el inventario \"${inventory.name}\"?")
            .setPositiveButton("Eliminar") { dialog, _ ->
                firestore.collection("inventories").document(inventory.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Inventario eliminado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun openInventoryDetail(inventory: Inventory) {
        val intent = Intent(this, InventoryDetailActivity::class.java)
        intent.putExtra("inventory_id", inventory.id)
        intent.putExtra("inventory_name", inventory.name)
        startActivity(intent)
    }


}