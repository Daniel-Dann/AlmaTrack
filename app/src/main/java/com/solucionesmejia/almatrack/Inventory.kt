package com.solucionesmejia.almatrack

data class Inventory(
    var name:String,
    var currency: String,
    var imageResId: Int = R.drawable.almatrak_logo,
    var productCount: Int = 0
)