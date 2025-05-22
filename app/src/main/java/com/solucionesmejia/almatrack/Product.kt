package com.solucionesmejia.almatrack

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    var salePrice: Double = 0.0,
    var currency: String = "MXN" // "MXN" o "USD"

)