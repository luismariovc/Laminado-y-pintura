package com.example.laminadoypintura

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan

class MainActivity : AppCompatActivity() {

    // Views - Header
    private lateinit var tvHeaderTime: TextView
    private lateinit var tvHeaderDate: TextView

    // Views - Perfil Taller
    private lateinit var etTallerNombre: EditText
    private lateinit var etTallerEncargado: EditText
    private lateinit var etTallerTelefono: EditText
    private lateinit var etTallerEmail: EditText
    private lateinit var etTallerDireccion: EditText
    private lateinit var btnSubirLogo: View

    // Views - Cliente
    private lateinit var etClienteNombre: EditText
    private lateinit var etClienteTelefono: EditText
    private lateinit var etClienteEmail: EditText
    private lateinit var etClienteDomicilio: EditText

    // Views - Vehículo
    private lateinit var etVehiculoMarca: EditText
    private lateinit var etVehiculoModelo: EditText
    private lateinit var etVehiculoColor: EditText
    private lateinit var etVehiculoPlacas: EditText
    private lateinit var etCodigoPintura: EditText

    // Contenedores de Listas Dinámicas
    private lateinit var containerHojalateria: LinearLayout
    private lateinit var containerPintura: LinearLayout
    private lateinit var containerRepuestos: LinearLayout

    // Botones Agregar
    private lateinit var btnAgregarHojalateria: Button
    private lateinit var btnAgregarPintura: Button
    private lateinit var btnAgregarRepuesto: Button

    // Text Views Totales Header de cada Card
    private lateinit var tvTotalHojalateria: TextView
    private lateinit var tvTotalPintura: TextView
    private lateinit var tvTotalRepuestos: TextView

    // Footer Financiero
    private lateinit var tvGranTotal: TextView
    private lateinit var etAnticipo: EditText
    private lateinit var tvSaldoPendiente: TextView

    // Grid Servicios
    private lateinit var gridServicios: GridLayout
    private val selectedServices = mutableSetOf<String>()

    // Global Params
    private lateinit var etTiempoEstimado: EditText
    private lateinit var etPiezasIntervenir: EditText
    private lateinit var btnGenerar: MaterialButton

    // Tracking lists for dynamic views
    private val hojalateriaViews = mutableListOf<View>()
    private val pinturaViews = mutableListOf<View>()
    private val repuestoViews = mutableListOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        loadSavedLogo()
        setupListeners()
        setupServicesGrid()
        // Estado inicial
        addHojalateriaRow()
        addPinturaRow()
        addRepuestoRow()
    }

    // Real-time Clock Logic
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val timeRunnable = object : Runnable {
        override fun run() {
            updateHeaderData()
            handler.postDelayed(this, 1000) // Update every second
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(timeRunnable) // Start updates
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(timeRunnable) // Stop updates to save battery
    }

    private fun initViews() {
        // Header
        tvHeaderTime = findViewById(R.id.tvHeaderTime)
        tvHeaderDate = findViewById(R.id.tvHeaderDate)

        // Perfil Taller
        etTallerNombre = findViewById(R.id.etTallerNombre)
        etTallerEncargado = findViewById(R.id.etTallerEncargado)
        etTallerTelefono = findViewById(R.id.etTallerTelefono)
        etTallerEmail = findViewById(R.id.etTallerEmail)
        etTallerDireccion = findViewById(R.id.etTallerDireccion)
        btnSubirLogo = findViewById(R.id.btnSubirLogo)

        // Cliente
        etClienteNombre = findViewById(R.id.etClienteNombre)
        etClienteTelefono = findViewById(R.id.etClienteTelefono)
        etClienteEmail = findViewById(R.id.etClienteEmail)
        etClienteDomicilio = findViewById(R.id.etClienteDomicilio)

        // Vehículo
        etVehiculoMarca = findViewById(R.id.etVehiculoMarca)
        etVehiculoModelo = findViewById(R.id.etVehiculoModelo)
        etVehiculoColor = findViewById(R.id.etVehiculoColor)
        etVehiculoPlacas = findViewById(R.id.etVehiculoPlacas)
        etCodigoPintura = findViewById(R.id.etCodigoPintura)

        // Nuevos Bindings Dinámicos
        containerHojalateria = findViewById(R.id.containerHojalateria)
        containerPintura = findViewById(R.id.containerPintura)
        containerRepuestos = findViewById(R.id.containerRepuestos)

        btnAgregarHojalateria = findViewById(R.id.btnAgregarHojalateria)
        btnAgregarPintura = findViewById(R.id.btnAgregarPintura)
        btnAgregarRepuesto = findViewById(R.id.btnAgregarRepuesto)

        tvTotalHojalateria = findViewById(R.id.tvTotalHojalateria)
        tvTotalPintura = findViewById(R.id.tvTotalPintura)
        tvTotalRepuestos = findViewById(R.id.tvTotalRepuestos)

        tvGranTotal = findViewById(R.id.tvGranTotal)
        etAnticipo = findViewById(R.id.etAnticipo)
        tvSaldoPendiente = findViewById(R.id.tvSaldoPendiente)

        gridServicios = findViewById(R.id.gridServicios)

        etTiempoEstimado = findViewById(R.id.etTiempoEstimado)
        etPiezasIntervenir = findViewById(R.id.etPiezasIntervenir)

        btnGenerar = findViewById(R.id.btnGenerar)

        // Valores por defecto
        etTiempoEstimado.setText("2 a 3 Días Hábiles")
        etPiezasIntervenir.setText("1")
    }


    private fun updateHeaderData() {
        val currentDate = Date()
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, d 'DE' MMMM 'DE' yyyy", Locale("es", "ES"))

        val timeString = timeFormat.format(currentDate).lowercase()
        val spannableTime = SpannableString(timeString)
        
        // Find the start of am/pm (usually after a space)
        val spaceIndex = timeString.lastIndexOf(' ')
        if (spaceIndex != -1 && spaceIndex < timeString.length - 1) {
            spannableTime.setSpan(
                RelativeSizeSpan(0.6f),
                spaceIndex,
                timeString.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        tvHeaderTime.text = spannableTime
        tvHeaderDate.text = dateFormat.format(currentDate).uppercase()
    }

    // Image Picker
    private var base64Logo: String? = null
    private val pickImage = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@registerForActivityResult
        
        try {
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Toast.makeText(this, "Error: No se pudo acceder a la imagen", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            inputStream.use { stream ->
                val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                if (bitmap == null) {
                    Toast.makeText(this, "Error: No se pudo decodificar la imagen", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }

                // Redimensionar si es muy grande (max 800px ancho)
                val scaledBitmap = if (bitmap.width > 800) {
                    val ratio = 800.0 / bitmap.width
                    android.graphics.Bitmap.createScaledBitmap(bitmap, 800, (bitmap.height * ratio).toInt(), true)
                } else bitmap

                // Comprimir a JPEG
                val outputStream = java.io.ByteArrayOutputStream()
                scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
                val byteArray = outputStream.toByteArray()
                
                base64Logo = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
                
                // Save to SharedPreferences
                getSharedPreferences("AppPrefs", MODE_PRIVATE).edit()
                    .putString("logo_base64", base64Logo)
                    .apply()

                // Actualizar UI
                val imgLogoPreview = findViewById<android.widget.ImageView>(R.id.imgLogoPreview)
                val btnRemoveLogo = findViewById<android.view.View>(R.id.btnRemoveLogo)
                val lblSubirLogo = findViewById<android.view.View>(R.id.lblSubirLogo)

                if (imgLogoPreview != null) {
                    imgLogoPreview.imageTintList = null // Clear the red tint from XML
                    imgLogoPreview.clearColorFilter() // Clear manual filter set by delete action
                    imgLogoPreview.setImageBitmap(scaledBitmap)
                    imgLogoPreview.setPadding(0,0,0,0) // Remove padding to use full space
                }
                
                btnRemoveLogo?.visibility = android.view.View.VISIBLE
                lblSubirLogo?.visibility = android.view.View.INVISIBLE // Hide text when image is present
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupListeners() {
        btnAgregarHojalateria.setOnClickListener { addHojalateriaRow() }
        btnAgregarPintura.setOnClickListener { addPinturaRow() }
        btnAgregarRepuesto.setOnClickListener { addRepuestoRow() }

        etAnticipo.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) = calculateTotals()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Btn Subir Logo Listener (Container click)
        findViewById<android.view.View>(R.id.btnSubirLogo).setOnClickListener { 
            pickImage.launch("image/*")
        }

        // Btn Remove Logo Listener
        findViewById<android.view.View>(R.id.btnRemoveLogo).setOnClickListener {
             // Remove from SharedPreferences
             getSharedPreferences("AppPrefs", MODE_PRIVATE).edit()
                 .remove("logo_base64")
                 .apply()
                 
             base64Logo = null
             val imgLogoPreview = findViewById<android.widget.ImageView>(R.id.imgLogoPreview)
             val btnRemoveLogo = findViewById<android.view.View>(R.id.btnRemoveLogo)
             val lblSubirLogo = findViewById<android.view.View>(R.id.lblSubirLogo)

             // Reset UI
             imgLogoPreview.setImageResource(R.drawable.ic_image_upload)
             imgLogoPreview.setColorFilter(getColor(R.color.premium_red))
             val paddingDp = (8 * resources.displayMetrics.density).toInt()
             imgLogoPreview.setPadding(paddingDp, paddingDp, paddingDp, paddingDp)
             
             btnRemoveLogo.visibility = android.view.View.GONE
             lblSubirLogo.visibility = android.view.View.VISIBLE
        }

        btnGenerar.setOnClickListener {
            if (validarFormulario()) {
                generarCotizacion()
            }
        }
    }

    // ... (Add Rows functions same as before) ...
    // Skipping addHojalateriaRow, addPinturaRow, addRepuestoRow to save tokens/lines if not changing.
    // Wait, replace_file_content needs me to replace chunks.
    // I can stick to modifying setupListeners and generarCotizacion.

    private fun generarCotizacion() {
        // Crear cliente
        val cliente = Cliente(
            nombre = etClienteNombre.text.toString(),
            telefono = etClienteTelefono.text.toString(),
            email = etClienteEmail.text.toString(),
            domicilio = etClienteDomicilio.text.toString()
        )

        // Crear vehículo
        val vehiculo = Vehiculo(
            marca = etVehiculoMarca.text.toString(),
            modelo = etVehiculoModelo.text.toString(),
            color = etVehiculoColor.text.toString(),
            placas = etVehiculoPlacas.text.toString(),
            codigoPintura = etCodigoPintura.text.toString()
        )
        
        // Datos Taller
        val datosTaller = DatosTaller(
            nombre = etTallerNombre.text.toString().ifEmpty { "Premium Laminado y Pintura" },
            encargado = etTallerEncargado.text.toString().ifEmpty { "Juan Pérez" },
            telefono = etTallerTelefono.text.toString().ifEmpty { "55 1234 5678" },
            email = etTallerEmail.text.toString().ifEmpty { "contacto@taller.com" },
            direccion = etTallerDireccion.text.toString().ifEmpty { "Calle y Número, Ciudad" },
            logoBase64 = base64Logo
        )

        // Servicios
        val serviciosList = selectedServices.toList()

        // Hojalateria Dinamica
        val hojalateria = mutableListOf<ItemCosto>()
        for (view in hojalateriaViews) {
            val desc = view.findViewById<EditText>(R.id.etDescripcion).text.toString()
            val precio = view.findViewById<EditText>(R.id.etPrecio).text.toString().toDoubleOrNull() ?: 0.0
            if (desc.isNotEmpty()) hojalateria.add(ItemCosto(desc, precio))
        }

        // Pintura Dinamica
        val pintura = mutableListOf<ItemPintura>()
        for (view in pinturaViews) {
            val pieza = view.findViewById<EditText>(R.id.etPieza).text.toString()
            val cant = view.findViewById<EditText>(R.id.etCantidad).text.toString().toIntOrNull() ?: 1
            val unit = view.findViewById<EditText>(R.id.etUnidad).text.toString()
            val precio = view.findViewById<EditText>(R.id.etPrecio).text.toString().toDoubleOrNull() ?: 0.0
            if (pieza.isNotEmpty()) pintura.add(ItemPintura(pieza, cant, unit, precio))
        }

        // Repuestos Dinamicos
        val repuestos = mutableListOf<ItemCosto>()
        for (view in repuestoViews) {
            val desc = view.findViewById<EditText>(R.id.etDescripcion).text.toString()
            val precio = view.findViewById<EditText>(R.id.etPrecio).text.toString().toDoubleOrNull() ?: 0.0
            if (desc.isNotEmpty()) repuestos.add(ItemCosto(desc, precio))
        }

        // Fecha actual
        val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        // Crear cotización
        val cotizacion = Cotizacion(
            fecha = fechaActual,
            cliente = cliente,
            vehiculo = vehiculo,
            servicios = serviciosList,
            hojalateria = hojalateria,
            pintura = pintura,
            repuestos = repuestos,
            anticipo = etAnticipo.text.toString().toDoubleOrNull() ?: 0.0,
            tiempoEstimado = etTiempoEstimado.text.toString(),
            piezasIntervenir = etPiezasIntervenir.text.toString().toIntOrNull() ?: 1,
            datosTaller = datosTaller
        )

        // Ir a pantalla de cotización
        val intent = Intent(this, CotizacionActivity::class.java)
        intent.putExtra("COTIZACION_DATA", cotizacion)
        startActivity(intent)
    }

    private fun setupServicesGrid() {
        val servicios = listOf(
            "Golpe / Colisión", "Baño de Pintura", "Repintado (Piezas)",
            "Varillaje (Granizo)", "Reparación Fascias", "Pulido / Detailing",
            "Restauración Faros", "Banco / Chasis", "Cambio de Cristal", "OTRO SERVICIO..."
        )

        gridServicios.rowCount = (servicios.size + 1) / 2

        for (servicio in servicios) {
            val btn = Button(this).apply {
                text = servicio
                textSize = 10f
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }
                setBackgroundColor(getColor(android.R.color.white)) // Default state
                setTextColor(getColor(R.color.text_grey))
                setOnClickListener { toggleService(this, servicio) }
            }
            gridServicios.addView(btn)
        }
    }

    private fun toggleService(btn: Button, service: String) {
        if (selectedServices.contains(service)) {
            selectedServices.remove(service)
            btn.setBackgroundColor(getColor(android.R.color.white))
            btn.setTextColor(getColor(R.color.text_grey))
        } else {
            selectedServices.add(service)
            btn.setBackgroundColor(getColor(R.color.premium_red))
            btn.setTextColor(getColor(android.R.color.white))
        }
    }

    private fun addHojalateriaRow() {
        val view = layoutInflater.inflate(R.layout.item_costo_simple, null)
        val etPrecio = view.findViewById<EditText>(R.id.etPrecio)
        val btnEliminar = view.findViewById<View>(R.id.btnEliminar)

        etPrecio.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) = calculateTotals()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnEliminar.setOnClickListener {
            containerHojalateria.removeView(view)
            hojalateriaViews.remove(view)
            calculateTotals()
        }

        containerHojalateria.addView(view)
        hojalateriaViews.add(view)
    }

    private fun addPinturaRow() {
        val view = layoutInflater.inflate(R.layout.item_costo_pintura, null)
        val etPrecio = view.findViewById<EditText>(R.id.etPrecio)
        val btnEliminar = view.findViewById<View>(R.id.btnEliminar)

        etPrecio.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) = calculateTotals()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnEliminar.setOnClickListener {
            containerPintura.removeView(view)
            pinturaViews.remove(view)
            calculateTotals()
        }

        containerPintura.addView(view)
        pinturaViews.add(view)
    }

    private fun addRepuestoRow() {
        val view = layoutInflater.inflate(R.layout.item_costo_simple, null)
        val etPrecio = view.findViewById<EditText>(R.id.etPrecio)
        val btnEliminar = view.findViewById<View>(R.id.btnEliminar)

        etPrecio.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) = calculateTotals()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnEliminar.setOnClickListener {
            containerRepuestos.removeView(view)
            repuestoViews.remove(view)
            calculateTotals()
        }

        containerRepuestos.addView(view)
        repuestoViews.add(view)
    }

    private fun calculateTotals() {
        // Hojalateria
        var totalHoj = 0.0
        for (view in hojalateriaViews) {
            val etPrecio = view.findViewById<EditText>(R.id.etPrecio)
            totalHoj += etPrecio.text.toString().toDoubleOrNull() ?: 0.0
        }
        tvTotalHojalateria.text = "$${String.format("%.2f", totalHoj)}"

        // Pintura
        var totalPin = 0.0
        for (view in pinturaViews) {
            val etPrecio = view.findViewById<EditText>(R.id.etPrecio)
            totalPin += etPrecio.text.toString().toDoubleOrNull() ?: 0.0
        }
        tvTotalPintura.text = "$${String.format("%.2f", totalPin)}"

        // Repuestos
        var totalRep = 0.0
        for (view in repuestoViews) {
            val etPrecio = view.findViewById<EditText>(R.id.etPrecio)
            totalRep += etPrecio.text.toString().toDoubleOrNull() ?: 0.0
        }
        tvTotalRepuestos.text = "$${String.format("%.2f", totalRep)}"

        // Gran Total
        val granTotal = totalHoj + totalPin + totalRep
        tvGranTotal.text = "$${String.format("%.2f", granTotal)}"

        // Saldo
        val anticipo = etAnticipo.text.toString().toDoubleOrNull() ?: 0.0
        val saldo = granTotal - anticipo
        tvSaldoPendiente.text = "$${String.format("%.2f", saldo)}"
    }

    private fun validarFormulario(): Boolean {
        if (etClienteNombre.text.toString().isEmpty()) {
            Toast.makeText(this, "Ingresa el nombre del cliente", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etVehiculoMarca.text.toString().isEmpty()) {
            Toast.makeText(this, "Ingresa la marca del vehículo", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }



    private fun loadSavedLogo() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val savedBase64 = prefs.getString("logo_base64", null)
        
        if (savedBase64 != null) {
            base64Logo = savedBase64
            try {
                val decodedBytes = android.util.Base64.decode(savedBase64, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                
                val imgLogoPreview = findViewById<android.widget.ImageView>(R.id.imgLogoPreview)
                val btnRemoveLogo = findViewById<android.view.View>(R.id.btnRemoveLogo)
                val lblSubirLogo = findViewById<android.view.View>(R.id.lblSubirLogo)

                if (imgLogoPreview != null) {
                    imgLogoPreview.imageTintList = null
                    imgLogoPreview.clearColorFilter()
                    imgLogoPreview.setImageBitmap(bitmap)
                    imgLogoPreview.setPadding(0, 0, 0, 0)
                }
                
                btnRemoveLogo?.visibility = android.view.View.VISIBLE
                lblSubirLogo?.visibility = android.view.View.INVISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}