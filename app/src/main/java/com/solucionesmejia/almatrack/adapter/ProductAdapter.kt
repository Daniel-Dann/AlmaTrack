package com.solucionesmejia.almatrack.adapter

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.solucionesmejia.almatrack.Inventory
import com.solucionesmejia.almatrack.Product
import com.solucionesmejia.almatrack.R
import java.text.Normalizer
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private var productList: List<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    var searchQuery: String = ""
    private var originalList: List<Product> = ArrayList(productList)

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductDescription: TextView = itemView.findViewById(R.id.tvProductDescription)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val options: ImageView = itemView.findViewById(R.id.ivOptionsProduct)
        val tvProductStock: TextView = itemView.findViewById(R.id.tvProductStock)
        val salePrice: TextView = itemView.findViewById(R.id.tvProductSalePrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.tvProductName.text = highlightText(product.name, searchQuery)
        holder.tvProductDescription.text = highlightText(product.description, searchQuery)

        val formattedPrice = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(product.price)

        val formattedSalePrice = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(product.salePrice)

        holder.tvProductPrice.text = "$$formattedPrice ${product.currency}"
        holder.salePrice.text = "$$formattedSalePrice ${product.currency}"
        holder.tvProductStock.text = "Stock: ${product.stock}"

        holder.imgProduct.setImageResource(R.drawable.inventory_product)

        // Opciones del menú
        holder.options.setOnClickListener {
            val popup = PopupMenu(holder.itemView.context, it)
            popup.inflate(R.menu.menu_product_item)
            try {
                val fields = popup.javaClass.declaredFields
                for (field in fields) {
                    if ("mPopup" == field.name) {
                        field.isAccessible = true
                        val menuPopupHelper = field.get(popup)
                        val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                        val setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", Boolean::class.javaPrimitiveType
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
                    R.id.action_editProduct -> {
                        onEditClick(product)
                        true
                    }
                    R.id.action_deleteProduct -> {
                        onDeleteClick(product)
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }
    }

    override fun getItemCount(): Int = productList.size

    fun updateList(newList: List<Product>, query: String = "") {
        originalList = ArrayList(newList)
        productList = newList
        searchQuery = query
        notifyDataSetChanged()
    }

    fun filterList(query: String) {
        val normalizedQuery = normalize(query.trim().lowercase())
        productList = if (normalizedQuery.isEmpty()) {
            originalList
        } else {
            originalList.filter {
                normalize(it.name.lowercase()).contains(normalizedQuery)
            }
        }
        notifyDataSetChanged()
    }

    private fun highlightText(text: String, keyword: String): SpannableString {
        val spannable = SpannableString(text)
        if (keyword.isEmpty()) return spannable

        val lowerText = text.lowercase()
        val lowerKeyword = keyword.lowercase()

        var start = lowerText.indexOf(lowerKeyword)
        while (start >= 0) {
            val end = start + keyword.length
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            start = lowerText.indexOf(lowerKeyword, end)
        }

        return spannable
    }

    private fun normalize(input: String): String {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }


}

