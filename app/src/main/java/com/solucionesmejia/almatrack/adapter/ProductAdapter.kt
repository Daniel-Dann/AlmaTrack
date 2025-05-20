package com.solucionesmejia.almatrack.adapter

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.solucionesmejia.almatrack.Product
import com.solucionesmejia.almatrack.R
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(private var productList: List<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    var searchQuery: String = ""

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductDescription: TextView = itemView.findViewById(R.id.tvProductDescription)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val tvProductStock: TextView = itemView.findViewById(R.id.tvProductStock)
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

        holder.tvProductPrice.text = "$$formattedPrice ${product.currency}"
        holder.tvProductStock.text = "Stock: ${product.stock}"

        holder.imgProduct.setImageResource(R.drawable.vector_box_product)
    }

    override fun getItemCount(): Int = productList.size

    fun updateList(newList: List<Product>, query: String = "") {
        this.productList = newList
        this.searchQuery = query
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
}