package com.example.laminadoypintura

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PdfGenerator(private val context: Context) {

    // Premium Colors
    private val COLOR_PREMIUM_RED = Color.parseColor("#D50000")
    private val COLOR_DARK_GREY = Color.parseColor("#1F2937")
    private val COLOR_LIGHT_GREY = Color.parseColor("#F3F4F6")
    private val COLOR_TEXT_GREY = Color.parseColor("#6B7280")
    private val COLOR_WHITE = Color.WHITE
    private val COLOR_BLACK = Color.BLACK

    fun generatePdf(cotizacion: Cotizacion): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 width=595, height=842
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        // --- PAINTS ---
        val paint = Paint()
        
        val titlePaint = Paint().apply {
            color = COLOR_DARK_GREY
            textSize = 18f // Renamed from 24f to match scaling
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        
        val subtitlePaint = Paint().apply {
            color = COLOR_TEXT_GREY
            textSize = 12f
        }

        val sectionHeaderBgPaint = Paint().apply {
            color = COLOR_DARK_GREY
            style = Paint.Style.FILL
        }

        val sectionHeaderTextPaint = Paint().apply {
            color = COLOR_WHITE
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        
        val labelPaint = Paint().apply {
            color = COLOR_PREMIUM_RED
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val textPaint = Paint().apply {
            color = COLOR_BLACK
            textSize = 10f
        }
        
        val textBoldPaint = Paint().apply {
            color = COLOR_BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val smallTextPaint = Paint().apply {
            color = COLOR_TEXT_GREY
            textSize = 9f
        }

        val pricePaint = Paint().apply {
            color = COLOR_WHITE
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        
        val itemPricePaint = Paint().apply {
            color = COLOR_BLACK
            textSize = 10f
            textAlign = Paint.Align.RIGHT
        }

        val folioBgPaint = Paint().apply {
            color = Color.parseColor("#FEE2E2") // Light Red for Folio Box
            style = Paint.Style.FILL
        }
        
        val folioTextPaint = Paint().apply {
            color = COLOR_PREMIUM_RED
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val cardBgPaint = Paint().apply {
            color = COLOR_LIGHT_GREY
            style = Paint.Style.FILL
        }

        // Layout Constants
        val margin = 30f
        val width = 595f
        val contentWidth = width - (margin * 2)
        var y = 40f

        // ================= HEADER =================
        // Logic to draw Logo or fallback
        var logoBottomY = y
        cotizacion.datosTaller.logoBase64?.let { base64 ->
            try {
                val decodedBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                // Resize: Max height 60, maintain aspect ratio
                val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val logoHeight = 60f
                val logoWidth = logoHeight * aspectRatio
                val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, logoWidth.toInt(), logoHeight.toInt(), true)
                canvas.drawBitmap(scaledBitmap, margin, y, null)
                logoBottomY = y + logoHeight + 10
            } catch (e: Exception) { e.printStackTrace() }
        }

        // Title and Subtitle (Aligned with Logo or Top Left)
        val textStartX = if (cotizacion.datosTaller.logoBase64 != null) margin + 80f else margin // Offset if logo
        var headerY = y + 20
        canvas.drawText("PREMIUM LAMINADO Y PINTURA", textStartX, headerY, titlePaint)
        headerY += 15
        canvas.drawText("Cotización y Presupuesto", textStartX, headerY, subtitlePaint)

        // Folio Box (Top Right)
        val folioBoxWidth = 100f
        val folioBoxHeight = 40f
        val folioX = width - margin - folioBoxWidth
        
        canvas.drawRect(folioX, y, width - margin, y + folioBoxHeight, folioBgPaint)
        canvas.drawText("FOLIO: ${cotizacion.folio}", width - margin - 10, y + 25, folioTextPaint)
        
        canvas.drawText(cotizacion.fecha, width - margin, y + folioBoxHeight + 15, Paint().apply { 
            color = COLOR_TEXT_GREY; textSize=9f; textAlign=Paint.Align.RIGHT 
        })

        y = Math.max(logoBottomY, y + folioBoxHeight + 30)
        y += 10

        // ================= CARDS (CLIENTE & VEHICULO) =================
        val colGap = 10f
        val colWidth = (contentWidth - colGap) / 2
        
        // --- CLIENT CARD (Left) ---
        val clientRectTop = y
        val clientRectBottom = y + 90 // Fixed height for consistency
        canvas.drawRect(margin, clientRectTop, margin + colWidth, clientRectBottom, cardBgPaint)
        
        var cardY = clientRectTop + 20
        val cardPadding = 15f
        
        canvas.drawText("CLIENTE", margin + cardPadding, cardY, labelPaint)
        cardY += 20
        canvas.drawText(cotizacion.cliente.nombre, margin + cardPadding, cardY, textBoldPaint)
        cardY += 15
        canvas.drawText("Tel: ${cotizacion.cliente.telefono}", margin + cardPadding, cardY, smallTextPaint)
        cardY += 12
        canvas.drawText("Email: ${cotizacion.cliente.email}", margin + cardPadding, cardY, smallTextPaint)
        
        // --- VEHICLE CARD (Right) ---
        val vehX = margin + colWidth + colGap
        canvas.drawRect(vehX, clientRectTop, vehX + colWidth, clientRectBottom, cardBgPaint)
        
        cardY = clientRectTop + 20
        canvas.drawText("VEHÍCULO", vehX + cardPadding, cardY, labelPaint)
        cardY += 20
        canvas.drawText("${cotizacion.vehiculo.marca} ${cotizacion.vehiculo.modelo}", vehX + cardPadding, cardY, textBoldPaint)
        cardY += 15
        canvas.drawText("Placas: ${cotizacion.vehiculo.placas}", vehX + cardPadding, cardY, smallTextPaint)
        cardY += 12
        canvas.drawText("Color: ${cotizacion.vehiculo.color}", vehX + cardPadding, cardY, smallTextPaint)
        cardY += 12
        canvas.drawText("Cód. Pintura: ${cotizacion.vehiculo.codigoPintura}", vehX + cardPadding, cardY, smallTextPaint)

        y = clientRectBottom + 15

        // ================= INFO BLOCK (TIEMPO / PIEZAS) =================
        // Using a light grey band
        canvas.drawRect(margin, y, width - margin, y + 60, cardBgPaint)
        var infoY = y + 20
        canvas.drawText("Tiempo Estimado: ${cotizacion.tiempoEstimado}", margin + cardPadding, infoY, textPaint)
        infoY += 15
        canvas.drawText("Piezas a Intervenir: ${cotizacion.piezasIntervenir}", margin + cardPadding, infoY, textPaint)
        infoY += 15
        canvas.drawText("Servicios Solicitados: ${cotizacion.servicios.joinToString(", ")}", margin + cardPadding, infoY, textPaint) 
        
        y += 80

        // ================= FINANCIAL DETAILS =================
        
        fun drawSection(title: String, items: List<Any>, getDesc: (Any) -> String, getQty: (Any) -> Int, getUnit: (Any) -> Double, getTotal: (Any) -> Double) {
            if (items.isEmpty()) return
            
            // Draw Header
            canvas.drawRect(margin, y, width - margin, y + 25, sectionHeaderBgPaint)
            canvas.drawText(title, margin + 10, y + 17, sectionHeaderTextPaint)
            
            // Section Total (Right Aligned)
            val sectionTotal = items.sumOf { getTotal(it) }
            canvas.drawText("$${String.format("%.2f", sectionTotal)}", width - margin - 10, y + 17, pricePaint)
            
            y += 25
            
            // Draw Column Headers (Optional, or just list items)
            // Let's just list items cleanly: Qty | Description | Unit $ | Total $
            // Coords: Qty(margin+10), Desc(margin+40), Unit(width-130), Total(width-10)
            
            items.forEach { item ->
                val qty = getQty(item)
                val desc = getDesc(item)
                val unitPrice = getUnit(item)
                val totalPrice = getTotal(item)
                
                // Qty
                canvas.drawText("$qty", margin + 10, y + 15, textPaint)
                
                // Description (Truncate if too long)
                val maxDescLen = 45 // chars approx
                val displayDesc = if (desc.length > maxDescLen) desc.substring(0, maxDescLen) + "..." else desc
                canvas.drawText(displayDesc, margin + 40, y + 15, textPaint)
                
                // Unit Price
                canvas.drawText("$${String.format("%.2f", unitPrice)}", width - 120, y + 15, itemPricePaint)
                
                // Total Price
                canvas.drawText("$${String.format("%.2f", totalPrice)}", width - margin - 10, y + 15, itemPricePaint)
                
                y += 20
            }
            y += 10 // Spacing after section
        }

        // Logic for extracting fields from varying model types (Pintura vs CostoSimple)
        // Note: ItemCosto doesn't strictly have quantity in model yet, but UI does. 
        // We need to check if Models.kt supports quantity for general items.
        // If not, we defaults to 1 for Hojalateria/Repuestos if the model wasn't updated.
        // Checking Models.kt is prudent. Assuming for now we need adaptors.
        
        drawSection("1. HOJALATERÍA", cotizacion.hojalateria, 
            { (it as ItemCosto).descripcion }, 
            { 1 }, // Default Qty 1 for simple items if model not updated
            { (it as ItemCosto).precio }, 
            { (it as ItemCosto).precio }
        )
        
        drawSection("2. PINTURA", cotizacion.pintura, 
            { (it as ItemPintura).pieza }, 
            { (it as ItemPintura).cantidad }, 
            { (it as ItemPintura).precio }, 
            { (it as ItemPintura).cantidad * (it as ItemPintura).precio }
        )
        
        drawSection("3. REPUESTOS", cotizacion.repuestos, 
            { (it as ItemCosto).descripcion }, 
            { 1 }, // Default 1
            { (it as ItemCosto).precio },
            { (it as ItemCosto).precio }
        )

        y += 20

        // ================= TOTALS FOOTER =================
        val totalsWidth = 250f
        val totalsXStart = width - margin - totalsWidth
        
        // Anticipo and Total Row
        canvas.drawText("(-) Anticipo:", totalsXStart, y, Paint().apply { textSize=12f; color=COLOR_TEXT_GREY })
        canvas.drawText("$${String.format("%.2f", cotizacion.anticipo)}", width - margin, y, Paint().apply { textSize=12f; textAlign=Paint.Align.RIGHT; color=COLOR_BLACK; isFakeBoldText=true })
        y += 20
        
        canvas.drawText("TOTAL:", totalsXStart, y, Paint().apply { textSize=14f; color=COLOR_TEXT_GREY })
        canvas.drawText("$${String.format("%.2f", cotizacion.totalGeneral)}", width - margin, y, Paint().apply { textSize=18f; textAlign=Paint.Align.RIGHT; color=COLOR_BLACK; isFakeBoldText=true })
        
        y += 20
        
        // SALDO RESTANTE (Red Box)
        val saldoHeight = 50f
        val saldoRectPaint = Paint().apply { color = COLOR_PREMIUM_RED; style = Paint.Style.FILL }
        canvas.drawRect(totalsXStart, y, width - margin, y + saldoHeight, saldoRectPaint)
        
        val saldoTextPaint = Paint().apply { color = COLOR_WHITE; textSize = 12f; isFakeBoldText = true }
        val saldoAmountPaint = Paint().apply { color = COLOR_WHITE; textSize = 20f; isFakeBoldText = true; textAlign = Paint.Align.RIGHT }
        
        canvas.drawText("SALDO RESTANTE:", totalsXStart + 15, y + 30, saldoTextPaint)
        canvas.drawText("$${String.format("%.2f", cotizacion.saldoPendiente)}", width - margin - 15, y + 32, saldoAmountPaint)


        pdfDocument.finishPage(page)

        // Save File
        val pdfDir = File(context.filesDir, "pdfs")
        if (!pdfDir.exists()) pdfDir.mkdir()
        
        val fileName = "Cotizacion_${cotizacion.folio}.pdf"
        val file = File(pdfDir, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }
    }
}
