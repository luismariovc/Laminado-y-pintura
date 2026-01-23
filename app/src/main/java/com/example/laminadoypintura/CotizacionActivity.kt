package com.example.laminadoypintura

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class CotizacionActivity : AppCompatActivity() {

    private lateinit var tvFolio: TextView
    private lateinit var tvFecha: TextView

    private lateinit var tvClienteNombre: TextView
    private lateinit var tvClienteTelefono: TextView
    private lateinit var tvClienteEmail: TextView
    private lateinit var tvClienteDomicilio: TextView

    private lateinit var tvVehiculoInfo: TextView
    private lateinit var tvVehiculoPlacas: TextView
    private lateinit var tvVehiculoColor: TextView
    private lateinit var tvCodigoPintura: TextView

    private lateinit var tvServicios: TextView
    private lateinit var tvTiempoEstimado: TextView
    private lateinit var tvPiezas: TextView

    private lateinit var tvTotalHojalateria: TextView
    private lateinit var tvTotalPintura: TextView
    private lateinit var tvTotalRepuestos: TextView

    private lateinit var tvTotalGeneral: TextView
    private lateinit var tvAnticipo: TextView
    private lateinit var tvSaldoPendiente: TextView

    private lateinit var btnWhatsApp: MaterialButton
    private lateinit var btnEmail: MaterialButton
    private lateinit var btnImprimir: MaterialButton
    private lateinit var btnVolver: Button

    private lateinit var cotizacion: Cotizacion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cotizacion)

        initViews()
        loadCotizacion()
        displayCotizacion()
        setupListeners()
    }

    private fun initViews() {
        tvFolio = findViewById(R.id.tvFolio)
        tvFecha = findViewById(R.id.tvFecha)

        tvClienteNombre = findViewById(R.id.tvClienteNombre)
        tvClienteTelefono = findViewById(R.id.tvClienteTelefono)
        tvClienteEmail = findViewById(R.id.tvClienteEmail)
        tvClienteDomicilio = findViewById(R.id.tvClienteDomicilio)

        tvVehiculoInfo = findViewById(R.id.tvVehiculoInfo)
        tvVehiculoPlacas = findViewById(R.id.tvVehiculoPlacas)
        tvVehiculoColor = findViewById(R.id.tvVehiculoColor)
        tvCodigoPintura = findViewById(R.id.tvCodigoPintura)

        tvServicios = findViewById(R.id.tvServicios)
        tvTiempoEstimado = findViewById(R.id.tvTiempoEstimado)
        tvPiezas = findViewById(R.id.tvPiezas)

        tvTotalHojalateria = findViewById(R.id.tvTotalHojalateria)
        tvTotalPintura = findViewById(R.id.tvTotalPintura)
        tvTotalRepuestos = findViewById(R.id.tvTotalRepuestos)

        tvTotalGeneral = findViewById(R.id.tvTotalGeneral)
        tvAnticipo = findViewById(R.id.tvAnticipo)
        tvSaldoPendiente = findViewById(R.id.tvSaldoPendiente)

        btnWhatsApp = findViewById(R.id.btnWhatsApp)
        btnEmail = findViewById(R.id.btnEmail)
        btnImprimir = findViewById(R.id.btnImprimir)
        btnVolver = findViewById(R.id.btnVolver)
    }

    private fun loadCotizacion() {
        cotizacion = intent.getSerializableExtra("COTIZACION_DATA") as Cotizacion
    }

    private fun displayCotizacion() {
        // Header
        tvFolio.text = "FOLIO: ${cotizacion.folio}"
        tvFecha.text = cotizacion.fecha

        // Cliente
        tvClienteNombre.text = cotizacion.cliente.nombre
        tvClienteTelefono.text = "Tel: ${cotizacion.cliente.telefono}"
        tvClienteEmail.text = "Email: ${cotizacion.cliente.email}"
        tvClienteDomicilio.text = cotizacion.cliente.domicilio

        // Vehículo
        tvVehiculoInfo.text = "${cotizacion.vehiculo.marca} ${cotizacion.vehiculo.modelo}"
        tvVehiculoPlacas.text = "Placas: ${cotizacion.vehiculo.placas}"
        tvVehiculoColor.text = "Color: ${cotizacion.vehiculo.color}"
        tvCodigoPintura.text = "Cód. Pintura: ${cotizacion.vehiculo.codigoPintura}"

        // Servicios y tiempos
        tvServicios.text = cotizacion.servicios.joinToString(", ")
        tvTiempoEstimado.text = "Tiempo: ${cotizacion.tiempoEstimado}"
        tvPiezas.text = "Piezas: ${cotizacion.piezasIntervenir}"

        // Totales
        tvTotalHojalateria.text = "$${String.format("%.2f", cotizacion.totalHojalateria)}"
        tvTotalPintura.text = "$${String.format("%.2f", cotizacion.totalPintura)}"
        tvTotalRepuestos.text = "$${String.format("%.2f", cotizacion.totalRepuestos)}"

        tvTotalGeneral.text = "$${String.format("%.2f", cotizacion.totalGeneral)}"
        tvAnticipo.text = "$${String.format("%.2f", cotizacion.anticipo)}"
        tvSaldoPendiente.text = "$${String.format("%.2f", cotizacion.saldoPendiente)}"
    }

    private fun setupListeners() {
        btnWhatsApp.setOnClickListener {
            enviarPorWhatsApp(cotizacion)
        }

        btnEmail.setOnClickListener {
            enviarPorEmail(cotizacion)
        }

        btnImprimir.setOnClickListener {
            Toast.makeText(this, "Función de impresión en desarrollo", Toast.LENGTH_SHORT).show()
        }

        btnVolver.setOnClickListener {
            finish()
        }
    }

    // ============================================
    // FUNCIONES DE COMPARTIR
    // ============================================

    private fun enviarPorWhatsApp(cotizacion: Cotizacion) {
        val mensaje = """
            *COTIZACIÓN - FOLIO: ${cotizacion.folio}*
            _Premium Laminado y Pintura_
            
            *CLIENTE:* ${cotizacion.cliente.nombre}
            *VEHÍCULO:* ${cotizacion.vehiculo.marca} ${cotizacion.vehiculo.modelo}
            *PLACAS:* ${cotizacion.vehiculo.placas}
            
            *SERVICIOS SOLICITADOS:*
            ${cotizacion.servicios.joinToString("\n") { "• $it" }}
            
            *DETALLE DE COSTOS:*
            
            ${if (cotizacion.hojalateria.isNotEmpty()) "*1. HOJALATERÍA* - $${String.format("%.2f", cotizacion.totalHojalateria)}" else ""}
            ${cotizacion.hojalateria.joinToString("\n") { "  • ${it.descripcion}: $${String.format("%.2f", it.precio)}" }}
            
            ${if (cotizacion.pintura.isNotEmpty()) "\n*2. PINTURA* - $${String.format("%.2f", cotizacion.totalPintura)}" else ""}
            ${cotizacion.pintura.joinToString("\n") { "  • ${it.pieza} (${it.cantidad} ${it.unidad}): $${String.format("%.2f", it.precio)}" }}
            
            ${if (cotizacion.repuestos.isNotEmpty()) "\n*3. REPUESTOS* - $${String.format("%.2f", cotizacion.totalRepuestos)}" else ""}
            ${cotizacion.repuestos.joinToString("\n") { "  • ${it.descripcion}: $${String.format("%.2f", it.precio)}" }}
            
            ━━━━━━━━━━━━━━━━
            *TOTAL:* $${String.format("%.2f", cotizacion.totalGeneral)}
            *ANTICIPO:* -$${String.format("%.2f", cotizacion.anticipo)}
            *SALDO PENDIENTE:* $${String.format("%.2f", cotizacion.saldoPendiente)}
            ━━━━━━━━━━━━━━━━
            
            ⏱️ *Tiempo estimado:* ${cotizacion.tiempoEstimado}
            📍 *Piezas a intervenir:* ${cotizacion.piezasIntervenir}
            
            _Premium Laminado y Pintura_
            📞 55 1234 5678
        """.trimIndent()

        try {
            val telefono = cotizacion.cliente.telefono.replace(Regex("[^0-9]"), "")
            val url = "https://wa.me/$telefono?text=${Uri.encode(mensaje)}"

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al abrir WhatsApp: ${e.message}",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun enviarPorEmail(cotizacion: Cotizacion) {
        val asunto = "Cotización #${cotizacion.folio} - ${cotizacion.vehiculo.marca} ${cotizacion.vehiculo.modelo}"

        val cuerpo = """
            Estimado/a ${cotizacion.cliente.nombre},
            
            Adjunto encontrará la cotización solicitada para los servicios de laminado y pintura de su vehículo.
            
            DETALLES DEL VEHÍCULO:
            - Marca/Modelo: ${cotizacion.vehiculo.marca} ${cotizacion.vehiculo.modelo}
            - Placas: ${cotizacion.vehiculo.placas}
            - Color: ${cotizacion.vehiculo.color}
            
            SERVICIOS SOLICITADOS:
            ${cotizacion.servicios.joinToString("\n") { "- $it" }}
            
            RESUMEN DE COSTOS:
            
            1. HOJALATERÍA: $${String.format("%.2f", cotizacion.totalHojalateria)}
            ${cotizacion.hojalateria.joinToString("\n") { "   - ${it.descripcion}: $${String.format("%.2f", it.precio)}" }}
            
            2. PINTURA: $${String.format("%.2f", cotizacion.totalPintura)}
            ${cotizacion.pintura.joinToString("\n") { "   - ${it.pieza} (${it.cantidad} ${it.unidad}): $${String.format("%.2f", it.precio)}" }}
            
            3. REPUESTOS Y ACCESORIOS: $${String.format("%.2f", cotizacion.totalRepuestos)}
            ${cotizacion.repuestos.joinToString("\n") { "   - ${it.descripcion}: $${String.format("%.2f", it.precio)}" }}
            
            ────────────────────────────
            TOTAL PRESUPUESTO: $${String.format("%.2f", cotizacion.totalGeneral)}
            ANTICIPO SOLICITADO: $${String.format("%.2f", cotizacion.anticipo)}
            SALDO PENDIENTE: $${String.format("%.2f", cotizacion.saldoPendiente)}
            ────────────────────────────
            
            Tiempo estimado de entrega: ${cotizacion.tiempoEstimado}
            Piezas a intervenir: ${cotizacion.piezasIntervenir}
            
            Para cualquier duda o aclaración, no dude en contactarnos.
            
            Saludos cordiales,
            Premium Laminado y Pintura
            Tel: 55 1234 5678
            Email: contacto@taller.com
        """.trimIndent()

        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(cotizacion.cliente.email))
                putExtra(Intent.EXTRA_SUBJECT, asunto)
                putExtra(Intent.EXTRA_TEXT, cuerpo)
            }

            startActivity(Intent.createChooser(intent, "Enviar cotización por..."))
        } catch (e: Exception) {
            Toast.makeText(this, "Error al enviar email: ${e.message}",
                Toast.LENGTH_SHORT).show()
        }
    }
}