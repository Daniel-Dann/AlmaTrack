package com.solucionesmejia.almatrack.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.solucionesmejia.almatrack.Product
import com.solucionesmejia.almatrack.R
import java.util.*
import kotlin.collections.ArrayList

class ProductProfitabilityAdapter(
    private val productList: List<Product>
) : RecyclerView.Adapter<ProductProfitabilityAdapter.ViewHolder>(), Filterable {

    private var filteredList = ArrayList<Product>()
    private var currency = "MXN"

    init {
        filteredList.addAll(productList)
        if (productList.isNotEmpty()) {
            currency = productList[0].currency
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvProducto: TextView = itemView.findViewById(R.id.tvProducto)
        val tvStock: TextView = itemView.findViewById(R.id.tvStock)
        val tvCost: TextView = itemView.findViewById(R.id.tvCost)
        val tvVenta: TextView = itemView.findViewById(R.id.tvVenta)
        val tvMargin: TextView = itemView.findViewById(R.id.tvMargin)
        val tvUnitEarnings: TextView = itemView.findViewById(R.id.tvUnitEarnings)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_profitability, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = filteredList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = filteredList[position]
        holder.tvProducto.text = product.name
        holder.tvStock.text = "${product.stock} unidades"

        val locale = Locale("es", "MX")
        val formattedCost = String.format(locale, "%,.2f", product.price) + " ${product.currency}"
        val formattedSale = String.format(locale, "%,.2f", product.salePrice) + " ${product.currency}"
        val unitProfit = product.salePrice - product.price
        val formattedUnitProfit = String.format(locale, "%,.2f", unitProfit) + " ${product.currency}"

        val margin = if (product.salePrice > 0) {
            ((product.salePrice - product.price) / product.salePrice) * 100
        } else 0.0

        val formattedMargin = String.format(Locale.getDefault(), "%.2f%%", margin)

        holder.tvCost.text = ("$$formattedCost")
        holder.tvVenta.text = ("$$formattedSale")
        holder.tvMargin.text = formattedMargin
        holder.tvUnitEarnings.text = "($$formattedUnitProfit por unidad)"
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val query = charSequence.toString().lowercase(Locale.getDefault())
                val results = if (query.isEmpty()) {
                    productList
                } else {
                    productList.filter {
                        it.name.lowercase(Locale.getDefault()).contains(query)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = results
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filteredList.clear()
                @Suppress("UNCHECKED_CAST")
                filteredList.addAll(filterResults.values as List<Product>)
                notifyDataSetChanged()
            }
        }
    }
}







