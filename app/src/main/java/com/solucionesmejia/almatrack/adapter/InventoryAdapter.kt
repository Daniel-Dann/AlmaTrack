package com.solucionesmejia.almatrack.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.solucionesmejia.almatrack.Inventory
import com.solucionesmejia.almatrack.R

class InventoryAdapter(
    private var inventoryList: List<Inventory>,
    private val onEditClick: (Inventory) -> Unit,
    private val onDeleteClick: (Inventory) -> Unit,
    private val onItemClick: (Inventory) -> Unit // <- ¡AQUÍ FALTABA LA COMA!
) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    private var originalList: List<Inventory> = ArrayList(inventoryList)

    class InventoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.ivInventoryIcon)
        val name: TextView = itemView.findViewById(R.id.tvInventoryName)
        val currency: TextView = itemView.findViewById(R.id.tvInventoryCurrency)
        val options: ImageView = itemView.findViewById(R.id.ivOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventory, parent, false)
        return InventoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val inventory = inventoryList[position]
        holder.name.text = inventory.name
        holder.currency.text = inventory.currency

        // Click sobre toda la tarjeta
        holder.itemView.setOnClickListener {
            onItemClick(inventory)
        }

        holder.options.setOnClickListener {
            val popup = PopupMenu(holder.itemView.context, it)
            popup.inflate(R.menu.menu_inventory_item)

            // Forzar mostrar iconos en el PopupMenu
            try {
                val fields = popup.javaClass.declaredFields
                for (field in fields) {
                    if ("mPopup" == field.name) {
                        field.isAccessible = true
                        val menuPopupHelper = field.get(popup)
                        val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                        val setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon",
                            Boolean::class.javaPrimitiveType
                        )
                        setForceIcons.invoke(menuPopupHelper, true)
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        onEditClick(inventory)
                        true
                    }
                    R.id.action_delete -> {
                        onDeleteClick(inventory)
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }
    }

    override fun getItemCount(): Int = inventoryList.size

    fun updateList(newList: List<Inventory>) {
        originalList = ArrayList(newList)
        inventoryList = newList
        notifyDataSetChanged()
    }

    private fun normalize(text: String): String {
        val normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }

    fun filterList(query: String) {
        val normalizedQuery = normalize(query.trim().lowercase())
        inventoryList = if (normalizedQuery.isEmpty()) {
            originalList
        } else {
            originalList.filter {
                normalize(it.name.lowercase()).contains(normalizedQuery)
            }
        }
        notifyDataSetChanged()
    }
}

