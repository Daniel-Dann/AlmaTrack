package com.solucionesmejia.almatrack

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import java.text.NumberFormat
import java.util.Locale
import com.google.firebase.firestore.FirebaseFirestore
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

class ReportActivity : AppCompatActivity() {

    private lateinit var inventoryId: String
    private lateinit var inventoryName: String
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        val toolbar = findViewById<Toolbar>(R.id.toolbarReport)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Obtener datos del inventario
        inventoryId = intent.getStringExtra("inventoryId") ?: ""
        inventoryName = intent.getStringExtra("inventoryName") ?: "Inventario"
        supportActionBar?.title = "Reporte $inventoryName"

        // Detectar clic en el CardView
        val cardView = findViewById<CardView>(R.id.crvTotalValueInvested)
        cardView.setOnClickListener {
            generateInventoryValueReport()
        }
    }

    // Regresar al presionar la flecha
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    // Cálculo del valor total invertido
    private fun generateInventoryValueReport() {
        firestore.collection("inventories")
            .document(inventoryId)
            .collection("products")
            .get()
            .addOnSuccessListener { documents ->
                var totalValue = 0.0
                var totalProducts = 0
                val currency = if (documents.isEmpty) {
                    documents.first().getString("currency") ?: "MXN"
                } else {
                    "MXN"
                }

                val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
                    minimumFractionDigits = 2
                    maximumFractionDigits = 2
                }

                val builder = StringBuilder()

                for (doc in documents) {
                    val name = doc.getString("name") ?: "Producto sin nombre"
                    val price = doc.getDouble("price") ?: 0.0
                    val stock = doc.getLong("stock")?.toInt() ?: 0
                    val subtotal = price * stock
                    totalValue += subtotal
                    totalProducts += stock

                    val priceFormatted = numberFormat.format(price)
                    val subtotalFormatted = numberFormat.format(subtotal)

                    builder.append("• $name\n")
                    builder.append("  $stock x $$priceFormatted = $$subtotalFormatted $currency\n\n")
                }

                val totalFormatted = numberFormat.format(totalValue)

                builder.append("──────────────\n")
                builder.append("Total de productos: $totalProducts\n")
                builder.append("Total invertido: $$totalFormatted $currency")

                AlertDialog.Builder(this)
                    .setTitle("Detalle de inversión")
                    .setMessage(builder.toString())
                    .setPositiveButton("OK", null)
                    .setNegativeButton("Exportar PDF") { _, _ ->
                        exportReportToPDF(toString())
                    }
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "❌ Error al calcular el reporte", Toast.LENGTH_SHORT).show()
            }
    }

    private fun exportReportToPDF(reportContent: String) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

        val logo = BitmapFactory.decodeResource(resources, R.drawable.logo_almatrack)
        val scaledLogo = Bitmap.createScaledBitmap(logo, 100, 100, false)
        val colorNaranja = Color.parseColor("#FF9B1F")

        val productsRef = firestore.collection("inventories")
            .document(inventoryId)
            .collection("products")

        productsRef.get().addOnSuccessListener { documents ->
            var pageNumber = 1
            var total = 0.0
            var totalProducts = 0
            var rowIndex = 0

            val allPages = mutableListOf<PdfDocument.Page>()
            var y = 0f
            lateinit var currentPage: PdfDocument.Page
            lateinit var canvas: Canvas
            lateinit var paint: Paint

            fun drawFooter(pageNum: Int) {
                paint.color = Color.DKGRAY
                paint.textSize = 10f
                paint.isFakeBoldText = false

                // Marca izquierda
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText("© 2025 Alma Track. Todos los derechos reservados.", 40f, 820f, paint)

                // Página derecha
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText("Página $pageNum", 550f, 820f, paint)
            }

            fun startNewPage() {
                currentPage = document.startPage(pageInfo)
                canvas = currentPage.canvas
                paint = Paint()

                if (pageNumber == 1) {
                    canvas.drawBitmap(scaledLogo, 40f, 40f, paint)
                    paint.textSize = 18f
                    paint.isFakeBoldText = true
                    canvas.drawText("Reporte de Inventario: $inventoryName", 160f, 80f, paint)

                    paint.textSize = 12f
                    paint.isFakeBoldText = false
                    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                    canvas.drawText("Fecha: $date", 160f, 105f, paint)

                    y = 160f
                } else {
                    y = 60f
                }

                // Encabezado tabla
                paint.color = colorNaranja
                canvas.drawRect(40f, y - 18f, 550f, y + 6f, paint)

                paint.color = Color.BLACK
                paint.textSize = 14f
                paint.isFakeBoldText = true
                canvas.drawText("Producto", 40f, y, paint)
                canvas.drawText("Precio", 250f, y, paint)
                canvas.drawText("Stock", 350f, y, paint)
                canvas.drawText("Subtotal", 450f, y, paint)

                y += 25f
            }

            startNewPage()

            for (doc in documents) {
                val name = doc.getString("name") ?: "Producto"
                val price = doc.getDouble("price") ?: 0.0
                val stock = doc.getLong("stock")?.toInt() ?: 0
                val subtotal = price * stock

                total += subtotal
                totalProducts += stock

                if (rowIndex % 2 == 1) {
                    paint.color = Color.LTGRAY
                    canvas.drawRect(40f, y - 15f, 550f, y + 8f, paint)
                }

                paint.color = Color.BLACK
                paint.textSize = 13f
                paint.isFakeBoldText = false
                canvas.drawText(name.take(20), 40f, y, paint)
                canvas.drawText("$${numberFormat.format(price)}", 250f, y, paint)
                canvas.drawText(stock.toString(), 350f, y, paint)
                canvas.drawText("$${numberFormat.format(subtotal)}", 450f, y, paint)

                y += 20f
                rowIndex++

                if (y > 750f) {
                    drawFooter(pageNumber)
                    document.finishPage(currentPage)
                    pageNumber++
                    startNewPage()
                }
            }

            // Totales
            y += 20f
            paint.color = Color.BLACK
            paint.textSize = 13f
            paint.isFakeBoldText = true
            // Línea divisoria antes de los totales
            paint.strokeWidth = 2f
            canvas.drawLine(40f, y, 550f, y, paint)
            y += 15f
            canvas.drawText("Total de productos: $totalProducts", 40f, y, paint)
            y += 20f
            canvas.drawText("Total invertido: $${numberFormat.format(total)}", 40f, y, paint)

            drawFooter(pageNumber)
            document.finishPage(currentPage)

            // Guardar
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val file = File(downloadsDir, "Reporte_$inventoryName.pdf")

            try {
                document.writeTo(FileOutputStream(file))
                document.close()

                val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)

                AlertDialog.Builder(this)
                    .setTitle("PDF generado")
                    .setItems(arrayOf("Ver PDF", "Compartir")) { _, which ->
                        val intent = when (which) {
                            0 -> Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            1 -> Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            else -> null
                        }
                        intent?.let { startActivity(Intent.createChooser(it, "Selecciona una app")) }
                    }
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al guardar PDF: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }



}
