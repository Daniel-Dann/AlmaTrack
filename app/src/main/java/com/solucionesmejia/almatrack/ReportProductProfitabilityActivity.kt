package com.solucionesmejia.almatrack

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.solucionesmejia.almatrack.adapters.ProductProfitabilityAdapter
import androidx.core.widget.NestedScrollView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.property.UnitValue
import java.io.File
import java.util.Locale
import android.graphics.BitmapFactory
import androidx.appcompat.app.AlertDialog
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.layout.element.Image
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.events.Event
import com.itextpdf.kernel.events.IEventHandler
import com.itextpdf.kernel.events.PdfDocumentEvent
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.VerticalAlignment
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import org.apache.poi.ss.usermodel.VerticalAlignment as PoiVerticalAlignment





class ReportProductProfitabilityActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductProfitabilityAdapter
    private lateinit var searchEditText: TextInputEditText
    private lateinit var layoutNoResults: LinearLayout
    private lateinit var currency: String
    private var allProducts: List<Product> = listOf()
    private var inventoryId: String = ""
    private var inventoryName: String = ""
    private lateinit var fabExportPdf: FloatingActionButton
    private var isFabVisible = true
    private lateinit var btnExportPdf: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_product_profitability)

        btnExportPdf = findViewById(R.id.btnExportPdf)

        inventoryId = intent.getStringExtra("inventoryId") ?: ""
        inventoryName = intent.getStringExtra("inventoryName") ?: ""
        currency = intent.getStringExtra("inventoryCurrency") ?: "MXN"

        val toolbar = findViewById<Toolbar>(R.id.toolbarProductProfitability)
        toolbar.title = "Rentabilidad de Productos $inventoryName"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerViewProducts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        searchEditText = findViewById(R.id.etSearchProductsProfits)
        layoutNoResults = findViewById(R.id.layoutNoResults)
        layoutNoResults.visibility = View.GONE

        findViewById<MaterialButton>(R.id.btnExportExcel).setOnClickListener {
            exportToExcel()
        }

        btnExportPdf.setOnClickListener {
            exportToPdf()
        }



        loadProducts()
        setupSearch()
        setupScrollBehavior()
    }

    private fun setupScrollBehavior() {
        val scrollView =
            findViewById<NestedScrollView>(R.id.nestedScrollView) // Necesitarás agregar este ID a tu layout

        scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            when {
                // Scroll hacia abajo
                // Scroll hacia abajo
                scrollY > oldScrollY + 5 -> {
                    btnExportPdf.animate().translationY(btnExportPdf.height.toFloat())
                        .setDuration(300).start()
                }
                // Scroll hacia arriba
                scrollY < oldScrollY - 5 -> {
                    btnExportPdf.animate().translationY(0f).setDuration(300).start()
                }
            }
        })
    }

    private fun exportToExcel() {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Rentabilidad")
            val creationHelper = workbook.creationHelper

            // 1. Insertar imagen (logo)
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.logo_almatrack)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val pictureIdx = workbook.addPicture(stream.toByteArray(), Workbook.PICTURE_TYPE_PNG)
            stream.close()

            val drawing = sheet.createDrawingPatriarch()
            val anchor = creationHelper.createClientAnchor().apply {
                setCol1(0)
                setRow1(0)
                setCol2(2)
                setRow2(4)
            }
            drawing.createPicture(anchor, pictureIdx)

            // 2. Encabezado mejorado
            val titleFont = workbook.createFont().apply {
                bold = true
                fontHeightInPoints = 16
                color = IndexedColors.DARK_BLUE.index
            }

            val subtitleFont = workbook.createFont().apply {
                bold = true
                fontHeightInPoints = 12
            }

            // Título principal
            sheet.createRow(0).createCell(3).apply {
                setCellValue("REPORTE DE RENTABILIDAD DE PRODUCTOS")
                cellStyle = workbook.createCellStyle().apply {
                    setFont(titleFont)
                    alignment = HorizontalAlignment.CENTER
                }
            }
            sheet.addMergedRegion(CellRangeAddress(0, 0, 3, 5))

            // Subtítulos
            listOf(
                "Inventario: $inventoryName",
                "Moneda: $currency",
                "Fecha de generación: ${getCurrentFormattedDate()}"
            ).forEachIndexed { index, value ->
                sheet.createRow(index + 1).createCell(3).apply {
                    setCellValue(value)
                    cellStyle = workbook.createCellStyle().apply { setFont(subtitleFont) }
                }
            }

            // 3. Estilos mejorados
            val headerStyle = workbook.createCellStyle().apply {
                fillForegroundColor = IndexedColors.DARK_BLUE.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                setFont(workbook.createFont().apply {
                    bold = true
                    color = IndexedColors.WHITE.index
                    fontHeightInPoints = 12
                })
                alignment = HorizontalAlignment.CENTER
                verticalAlignment = PoiVerticalAlignment.CENTER
                borderBottom = BorderStyle.MEDIUM
                borderTop = BorderStyle.MEDIUM
                borderLeft = BorderStyle.MEDIUM
                borderRight = BorderStyle.MEDIUM
            }

            val currencyStyle = workbook.createCellStyle().apply {
                dataFormat = creationHelper.createDataFormat().getFormat("\$#,##0.00")
                alignment = HorizontalAlignment.RIGHT
            }

            val percentageStyle = workbook.createCellStyle().apply {
                dataFormat = creationHelper.createDataFormat().getFormat("0.00%")
                alignment = HorizontalAlignment.RIGHT
            }

            // 4. Encabezado de tabla
            val headers = listOf("Producto", "Stock", "Costo", "Venta", "Ganancia/U", "Margen")
            val tableHeaderRow = sheet.createRow(5).apply {
                heightInPoints = 20f
            }

            headers.forEachIndexed { i, title ->
                tableHeaderRow.createCell(i).apply {
                    setCellValue(title)
                    cellStyle = headerStyle
                }
            }

            // 5. Datos con formato
            allProducts.forEachIndexed { index, product ->
                val row = sheet.createRow(index + 6)
                val unitProfit = product.salePrice - product.price
                val margin = if (product.salePrice > 0) (unitProfit / product.salePrice) else 0.0

                // Estilo base para la fila
                val rowStyle = workbook.createCellStyle().apply {
                    if (index % 2 == 0) {
                        fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
                        fillPattern = FillPatternType.SOLID_FOREGROUND
                    }
                    borderBottom = BorderStyle.THIN
                    borderTop = BorderStyle.THIN
                    borderLeft = BorderStyle.THIN
                    borderRight = BorderStyle.THIN
                }

                // Producto
                row.createCell(0).apply {
                    setCellValue(product.name)
                    cellStyle = rowStyle
                }

                // Stock
                row.createCell(1).apply {
                    setCellValue(product.stock.toDouble())
                    cellStyle = rowStyle
                }

                // Costo
                row.createCell(2).apply {
                    setCellValue(product.price)
                    cellStyle = workbook.createCellStyle().apply {
                        cloneStyleFrom(rowStyle)
                        cloneStyleFrom(currencyStyle)
                    }
                }

                // Venta
                row.createCell(3).apply {
                    setCellValue(product.salePrice)
                    cellStyle = workbook.createCellStyle().apply {
                        cloneStyleFrom(rowStyle)
                        cloneStyleFrom(currencyStyle)
                    }
                }

                // Ganancia/U
                row.createCell(4).apply {
                    setCellValue(unitProfit)
                    cellStyle = workbook.createCellStyle().apply {
                        cloneStyleFrom(rowStyle)
                        cloneStyleFrom(currencyStyle)
                    }
                }

                // Margen
                row.createCell(5).apply {
                    setCellValue(margin)
                    cellStyle = workbook.createCellStyle().apply {
                        cloneStyleFrom(rowStyle)
                        cloneStyleFrom(percentageStyle)
                    }
                }
            }

            // 6. Ajustar anchos de columna (versión segura para Android)
            val columnWidths = intArrayOf(12000, 4000, 5000, 5000, 5000, 5000) // Valores en unidades 1/256
            headers.indices.forEach { i ->
                sheet.setColumnWidth(i, columnWidths[i])
            }

            // 7. Congelar encabezados
            sheet.createFreezePane(0, 6)

            // 8. Guardar archivo
            val fileName = "Reporte_Rentabilidad_${inventoryName}_${getCurrentTimestamp()}.xlsx"
            val file = File(getExternalFilesDir(null), fileName)
            FileOutputStream(file).use { out ->
                workbook.write(out)
            }
            workbook.close()

            showExcelOptionsDialog(file)

        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Error al exportar Excel: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
            e.printStackTrace()
        }
    }

    private fun showExcelOptionsDialog(file: File) {
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Excel generado")
        builder.setMessage("¿Qué deseas hacer con el archivo?")

        builder.setPositiveButton("Ver") { _, _ ->
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "No se encontró app para abrir Excel", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Compartir") { _, _ ->
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(Intent.createChooser(shareIntent, "Compartir Excel con..."))
        }

        builder.setNeutralButton("Cancelar", null)
        builder.show()
    }

    private fun exportToPdf() {
        try {
            // Cargar logo desde recursos
            val logoBitmap = BitmapFactory.decodeResource(resources, R.drawable.logo_almatrack)
            val stream = java.io.ByteArrayOutputStream()
            logoBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val imageData = ImageDataFactory.create(stream.toByteArray())
            val logo = Image(imageData).scaleToFit(70f, 70f)

            // Crear una tabla de 2 columnas para logo + texto
            val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 4f)))
            headerTable.setWidth(UnitValue.createPercentValue(100f))

            // Celda con logo alineado a la izquierda
            val logoCell = Cell().add(logo)
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
            headerTable.addCell(logoCell)

            // Celda con texto alineado a la izquierda
            val textCell = Cell()
                .add(Paragraph("Reporte de Rentabilidad de Productos").setBold().setFontSize(16f))
                .add(Paragraph("Inventario: $inventoryName").setFontSize(13f))
                .add(
                    Paragraph("Fecha de generación: ${getCurrentFormattedDate()}").setFontSize(11f)
                        .setItalic()
                )
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            headerTable.addCell(textCell)


            val fileName = "Reporte_Rentabilidad_${inventoryName}_${getCurrentTimestamp()}.pdf"
            val filePath = getExternalFilesDir(null)?.absolutePath + File.separator + fileName
            val file = File(filePath)
            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, FooterPageEventHandler(this))
            val document = Document(pdfDocument)
            document.setMargins(36f, 36f, 60f, 36f)

            document.add(headerTable)
            document.add(Paragraph("\n")) // Espacio debajo del encabezado


            val columnWidths = floatArrayOf(3f, 2f, 2f, 2f, 2f)
            val table = Table(columnWidths)
            table.setWidth(UnitValue.createPercentValue(100f))

            // Encabezado color naranja
            val orangeColor = DeviceRgb(255, 152, 0)
            val headers = listOf("Producto", "Costo", "Venta", "Utilidad", "Margen")
            for (header in headers) {
                val headerCell = Cell().add(Paragraph(header).setBold())
                    .setBackgroundColor(orangeColor)
                    .setFontColor(ColorConstants.BLACK)
                    .setTextAlignment(TextAlignment.CENTER)
                table.addHeaderCell(headerCell)
            }

            // Zebra Striping para filas
            for ((index, product) in allProducts.withIndex()) {
                val unitProfit = product.salePrice - product.price
                val margin =
                    if (product.salePrice > 0) ((unitProfit / product.salePrice) * 100) else 0.0

                val rowData = listOf(
                    product.name,
                    "$${formatCurrency(product.price)} $currency",
                    "$${formatCurrency(product.salePrice)} $currency",
                    "$${formatCurrency(unitProfit)} $currency",
                    String.format("%.2f%%", margin)
                )


                for (cellText in rowData) {
                    val cell = Cell().add(Paragraph(cellText))
                        .setTextAlignment(TextAlignment.LEFT)
                    if (index % 2 == 0) {
                        cell.setBackgroundColor(DeviceRgb(245, 245, 245)) // Gris claro
                    }
                    table.addCell(cell)
                }
            }


            document.add(table)
            document.close()
            pdfWriter.close()

            // Mostrar opciones al usuario
            showPdfOptionsDialog(file)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al generar PDF", Toast.LENGTH_SHORT).show()
        }
    }

    class FooterPageEventHandler(private val activity: AppCompatActivity) : IEventHandler {
        override fun handleEvent(event: Event) {
            val pdfEvent = event as PdfDocumentEvent
            val pdfDoc = pdfEvent.document
            val page = pdfEvent.page
            val pageNumber = pdfDoc.getPageNumber(page)

            val canvas = PdfCanvas(page)
            val pageSize = page.pageSize
            val font = PdfFontFactory.createFont(StandardFonts.HELVETICA)

            // Texto: Página X
            val x = pageSize.width / 2
            val y = pageSize.bottom + 20

            canvas.beginText()
            canvas.setFontAndSize(font, 10f) // ✔ correcto
            canvas.moveText((pageSize.right - 80).toDouble(), y.toDouble())
            canvas.showText("Página $pageNumber")
            canvas.endText()

            canvas.beginText()
            canvas.setFontAndSize(font, 9f)
            canvas.moveText(pageSize.left + 36.0, y.toDouble())
            canvas.showText("© 2025 Alma Track. Todos los derechos reservados.")
            canvas.endText()

        }
    }

    private fun formatCurrency(value: Double): String {
        val formatter = java.text.NumberFormat.getNumberInstance(Locale("es", "MX"))
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter.format(value)
    }


    private fun showPdfOptionsDialog(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            file
        )

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("PDF generado")
        builder.setMessage("¿Qué deseas hacer con el archivo?")

        builder.setPositiveButton("Abrir") { _, _ ->
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "No se encontró app para abrir PDF", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Compartir") { _, _ ->
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "application/pdf"
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(Intent.createChooser(shareIntent, "Compartir PDF con..."))
        }

        builder.setNeutralButton("Cancelar", null)
        builder.show()
    }

    private fun getCurrentFormattedDate(): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy h:mm a", Locale("es", "MX"))
        val raw = sdf.format(java.util.Date())

        // Convertir "AM"/"PM" a formato con puntos
        return raw.replace("AM", "a.m.").replace("PM", "p.m.")
    }

    private fun getCurrentTimestamp(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    private fun loadProducts() {
        if (inventoryId.isEmpty()) {
            Toast.makeText(this, "ID del inventario no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("inventories")
            .document(inventoryId)
            .collection("products")
            .get()
            .addOnSuccessListener { snapshot ->
                allProducts = snapshot.documents
                    .mapNotNull { it.toObject(Product::class.java) }
                    .sortedBy { it.name.lowercase() }

                adapter = ProductProfitabilityAdapter(allProducts)
                recyclerView.adapter = adapter

                // Si no hay productos, mostrar mensaje
                if (allProducts.isEmpty()) {
                    layoutNoResults.visibility = View.VISIBLE
                } else {
                    layoutNoResults.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar productos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupSearch() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(query: CharSequence?, start: Int, before: Int, count: Int) {
                val filtered = allProducts.filter {
                    it.name.contains(query.toString(), ignoreCase = true)
                }.sortedBy { it.name.lowercase() }

                adapter = ProductProfitabilityAdapter(filtered)
                recyclerView.adapter = adapter

                if (filtered.isEmpty()) {
                    if (layoutNoResults.visibility != View.VISIBLE) {
                        layoutNoResults.startAnimation(fadeIn)
                        layoutNoResults.visibility = View.VISIBLE
                    }
                } else {
                    layoutNoResults.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}




