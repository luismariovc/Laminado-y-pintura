package com.linkersconsulting.appaint

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class QuoteFragment : Fragment() {

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
    private lateinit var spTiempoEstimado: Spinner
    private lateinit var etPiezasIntervenir: EditText
    private lateinit var btnGenerar: MaterialButton

    // Tracking lists for dynamic views
    private val hojalateriaViews = mutableListOf<View>()
    private val pinturaViews = mutableListOf<View>()
    private val repuestoViews = mutableListOf<View>()

    private var base64Logo: String? = null

    // Image Picker
    private val pickImage = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@registerForActivityResult
        
        try {
            val context = requireContext()
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Toast.makeText(context, "Error: No se pudo acceder a la imagen", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            inputStream.use { stream ->
                val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                if (bitmap == null) {
                    Toast.makeText(context, "Error: No se pudo decodificar la imagen", Toast.LENGTH_SHORT).show()
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
                context.getSharedPreferences("AppPrefs", MODE_PRIVATE).edit()
                    .putString("logo_base64", base64Logo)
                    .apply()

                // Actualizar UI
                view?.let {
                    val imgLogoPreview = it.findViewById<android.widget.ImageView>(R.id.imgLogoPreview)
                    val btnRemoveLogo = it.findViewById<android.view.View>(R.id.btnRemoveLogo)
                    
                    imgLogoPreview.setImageBitmap(scaledBitmap)
                    imgLogoPreview.imageTintList = null 
                    imgLogoPreview.setPadding(0,0,0,0)
                    imgLogoPreview.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                    
                    btnRemoveLogo?.visibility = android.view.View.VISIBLE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Toast handled safely?
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quote, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initHeader(view)
        loadSavedLogo(view)
        setupListeners(view)
        setupServicesGrid(view)
        
        // Restore dynamic rows or init defaults
        // For simplicity in this migration, just init defaults
        addHojalateriaRow()
        addPinturaRow()
        addRepuestoRow()
    }

    private fun initViews(view: View) {
        // Perfil Taller
        etTallerNombre = view.findViewById(R.id.etTallerNombre)
        etTallerEncargado = view.findViewById(R.id.etTallerEncargado)
        etTallerTelefono = view.findViewById(R.id.etTallerTelefono)
        etTallerEmail = view.findViewById(R.id.etTallerEmail)
        etTallerDireccion = view.findViewById(R.id.etTallerDireccion)
        btnSubirLogo = view.findViewById(R.id.btnSubirLogo)

        // Cliente
        etClienteNombre = view.findViewById(R.id.etClienteNombre)
        etClienteTelefono = view.findViewById(R.id.etClienteTelefono)
        etClienteEmail = view.findViewById(R.id.etClienteEmail)
        etClienteDomicilio = view.findViewById(R.id.etClienteDomicilio)

        // Vehículo
        etVehiculoMarca = view.findViewById(R.id.etVehiculoMarca)
        etVehiculoModelo = view.findViewById(R.id.etVehiculoModelo)
        etVehiculoColor = view.findViewById(R.id.etVehiculoColor)
        etVehiculoPlacas = view.findViewById(R.id.etVehiculoPlacas)
        etCodigoPintura = view.findViewById(R.id.etCodigoPintura)

        // Binding
        containerHojalateria = view.findViewById(R.id.containerHojalateria)
        containerPintura = view.findViewById(R.id.containerPintura)
        containerRepuestos = view.findViewById(R.id.containerRepuestos)

        btnAgregarHojalateria = view.findViewById(R.id.btnAgregarHojalateria)
        btnAgregarPintura = view.findViewById(R.id.btnAgregarPintura)
        btnAgregarRepuesto = view.findViewById(R.id.btnAgregarRepuesto)

        tvTotalHojalateria = view.findViewById(R.id.tvTotalHojalateria)
        tvTotalPintura = view.findViewById(R.id.tvTotalPintura)
        tvTotalRepuestos = view.findViewById(R.id.tvTotalRepuestos)

        tvGranTotal = view.findViewById(R.id.tvGranTotal)
        etAnticipo = view.findViewById(R.id.etAnticipo)
        tvSaldoPendiente = view.findViewById(R.id.tvSaldoPendiente)

        gridServicios = view.findViewById(R.id.gridServicios)

        spTiempoEstimado = view.findViewById(R.id.spTiempoEstimado)
        etPiezasIntervenir = view.findViewById(R.id.etPiezasIntervenir)

        btnGenerar = view.findViewById(R.id.btnGenerar)

        // Setup Spinner
        val adapter = android.widget.ArrayAdapter.createFromResource(
            requireContext(),
            R.array.tiempo_estimado_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTiempoEstimado.adapter = adapter

        etPiezasIntervenir.setText("1")
    }

    // Views - Header
    private lateinit var tvHeaderTime: TextView
    private lateinit var tvHeaderDate: TextView

    private fun initHeader(view: View) {
        tvHeaderTime = view.findViewById(R.id.tvHeaderTime)
        tvHeaderDate = view.findViewById(R.id.tvHeaderDate)
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

    private fun updateHeaderData() {
        val currentDate = Date()
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, d 'DE' MMMM 'DE' yyyy", Locale("es", "ES"))

        val timeString = timeFormat.format(currentDate).lowercase()
        val spannableTime = android.text.SpannableString(timeString)
        
        // Find the start of am/pm (usually after a space)
        val spaceIndex = timeString.lastIndexOf(' ')
        if (spaceIndex != -1 && spaceIndex < timeString.length - 1) {
            spannableTime.setSpan(
                android.text.style.RelativeSizeSpan(0.6f),
                spaceIndex,
                timeString.length,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        tvHeaderTime.text = spannableTime
        tvHeaderDate.text = dateFormat.format(currentDate).uppercase()
    }

    private fun setupListeners(view: View) {
        btnAgregarHojalateria.setOnClickListener { addHojalateriaRow() }
        btnAgregarPintura.setOnClickListener { addPinturaRow() }
        btnAgregarRepuesto.setOnClickListener { addRepuestoRow() }

        etAnticipo.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) = calculateTotals()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnSubirLogo.setOnClickListener { 
            try {
                pickImage.launch("image/*")
            } catch (e: Exception) {
                Toast.makeText(context, "Error al abrir galería: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<View>(R.id.btnRemoveLogo).setOnClickListener {
             requireContext().getSharedPreferences("AppPrefs", MODE_PRIVATE).edit()
                 .remove("logo_base64")
                 .apply()
                 
             base64Logo = null
             val imgLogoPreview = view.findViewById<android.widget.ImageView>(R.id.imgLogoPreview)
             val btnRemoveLogo = view.findViewById<android.view.View>(R.id.btnRemoveLogo)
             
             imgLogoPreview.setImageResource(R.drawable.ic_image_upload)
             imgLogoPreview.imageTintList = android.content.res.ColorStateList.valueOf(resources.getColor(R.color.premium_red, null))
             val padding = (8 * resources.displayMetrics.density).toInt()
             imgLogoPreview.setPadding(padding, padding, padding, padding)
             imgLogoPreview.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
             
             btnRemoveLogo.visibility = android.view.View.GONE
        }

        btnGenerar.setOnClickListener {
            if (validarFormulario()) {
                checkPermissionAndGenerate()
            }
        }
    }

    private fun checkPermissionAndGenerate() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_CONTACTS) 
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            
            requestPermissions(
                arrayOf(android.Manifest.permission.WRITE_CONTACTS, android.Manifest.permission.READ_CONTACTS),
                1001
            )
        } else {
            saveContactAndGenerate()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                saveContactAndGenerate()
            } else {
                Toast.makeText(context, "Permiso denegado. El cliente no se guardará automáticamente.", Toast.LENGTH_SHORT).show()
                generarCotizacion()
            }
        }
    }

    private fun saveContactAndGenerate() {
        val name = etClienteNombre.text.toString()
        val phone = etClienteTelefono.text.toString().filter { it.isDigit() }
        
        if (name.isNotEmpty() && phone.isNotEmpty()) {
            saveContactIfNew(name, phone)
        }
        generarCotizacion()
    }

    private fun saveContactIfNew(name: String, phone: String) {
        val context = requireContext()
        val uri = android.net.Uri.withAppendedPath(android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI, android.net.Uri.encode(phone))
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        
        val exists = if (cursor != null && cursor.moveToFirst()) {
            cursor.close()
            true
        } else {
            cursor?.close()
            false
        }

        if (!exists) {
            try {
                val ops = ArrayList<android.content.ContentProviderOperation>()
                
                ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(android.provider.ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(android.provider.ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build())

                ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(android.provider.ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build())

                ops.add(android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                    .withValue(android.provider.ContactsContract.CommonDataKinds.Phone.TYPE, android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build())

                context.contentResolver.applyBatch(android.provider.ContactsContract.AUTHORITY, ops)
                Toast.makeText(context, "Cliente guardado en contactos", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun generarCotizacion() {
        val cliente = Cliente(
            nombre = etClienteNombre.text.toString(),
            telefono = etClienteTelefono.text.toString(),
            email = etClienteEmail.text.toString(),
            domicilio = etClienteDomicilio.text.toString()
        )

        val vehiculo = Vehiculo(
            marca = etVehiculoMarca.text.toString(),
            modelo = etVehiculoModelo.text.toString(),
            color = etVehiculoColor.text.toString(),
            placas = etVehiculoPlacas.text.toString(),
            codigoPintura = etCodigoPintura.text.toString()
        )
        
        val datosTaller = DatosTaller(
            nombre = etTallerNombre.text.toString().ifEmpty { "Premium Laminado y Pintura" },
            encargado = etTallerEncargado.text.toString().ifEmpty { "Juan Pérez" },
            telefono = etTallerTelefono.text.toString().ifEmpty { "55 1234 5678" },
            email = etTallerEmail.text.toString().ifEmpty { "contacto@taller.com" },
            direccion = etTallerDireccion.text.toString().ifEmpty { "Calle y Número, Ciudad" },
            logoBase64 = base64Logo
        )

        val serviciosList = selectedServices.toList()

        val hojalateria = mutableListOf<ItemCosto>()
        for (view in hojalateriaViews) {
            val desc = view.findViewById<EditText>(R.id.etDescripcion).text.toString()
            val precio = view.findViewById<TextView>(R.id.tvTotalLinea).text.toString().replace("$", "").toDoubleOrNull() ?: 0.0
            if (desc.isNotEmpty()) hojalateria.add(ItemCosto(desc, precio))
        }

        val pintura = mutableListOf<ItemPintura>()
        for (view in pinturaViews) {
            val pieza = view.findViewById<EditText>(R.id.etDescripcion).text.toString()
            val cant = view.findViewById<EditText>(R.id.etCantidad).text.toString().toIntOrNull() ?: 1
            val unit = "Pieza" 
            val precio = view.findViewById<EditText>(R.id.etPrecio).text.toString().toDoubleOrNull() ?: 0.0
            if (pieza.isNotEmpty()) pintura.add(ItemPintura(pieza, cant, unit, precio))
        }

        val repuestos = mutableListOf<ItemCosto>()
        for (view in repuestoViews) {
            val desc = view.findViewById<EditText>(R.id.etDescripcion).text.toString()
            val precio = view.findViewById<TextView>(R.id.tvTotalLinea).text.toString().replace("$", "").toDoubleOrNull() ?: 0.0
            if (desc.isNotEmpty()) repuestos.add(ItemCosto(desc, precio))
        }

        val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        val cotizacion = Cotizacion(
            fecha = fechaActual,
            cliente = cliente,
            vehiculo = vehiculo,
            servicios = serviciosList,
            hojalateria = hojalateria,
            pintura = pintura,
            repuestos = repuestos,
            anticipo = etAnticipo.text.toString().toDoubleOrNull() ?: 0.0,
            tiempoEstimado = spTiempoEstimado.selectedItem.toString(),
            piezasIntervenir = etPiezasIntervenir.text.toString().toIntOrNull() ?: 1,
            datosTaller = datosTaller
        )

        // Save to Repository
        QuoteRepository(requireContext()).saveQuote(cotizacion)

        val intent = Intent(requireContext(), CotizacionActivity::class.java)
        intent.putExtra("COTIZACION_DATA", cotizacion)
        intent.putExtra("AUTO_SEND_WHATSAPP", true)
        startActivity(intent)
    }

    private fun setupServicesGrid(view: View) {
        val servicios = listOf(
            "Golpe / Colisión", "Baño de Pintura", "Repintado (Piezas)",
            "Varillaje (Granizo)", "Reparación Fascias", "Pulido / Detailing",
            "Restauración Faros", "Banco / Chasis", "Cambio de Cristal", "OTRO SERVICIO..."
        )

        gridServicios.rowCount = (servicios.size + 1) / 2

        for (servicio in servicios) {
            val btn = Button(requireContext()).apply {
                text = servicio
                textSize = 9f
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }
                setBackgroundColor(resources.getColor(android.R.color.white, null))
                setTextColor(resources.getColor(R.color.text_grey, null))
                setOnClickListener { toggleService(this, servicio) }
            }
            gridServicios.addView(btn)
        }
    }

    private fun toggleService(btn: Button, service: String) {
        if (selectedServices.contains(service)) {
            selectedServices.remove(service)
            btn.setBackgroundColor(resources.getColor(android.R.color.white, null))
            btn.setTextColor(resources.getColor(R.color.text_grey, null))
        } else {
            selectedServices.add(service)
            btn.setBackgroundColor(resources.getColor(R.color.premium_red, null))
            btn.setTextColor(resources.getColor(android.R.color.white, null))
        }
    }

    private fun setupRowListeners(view: View, container: LinearLayout, list: MutableList<View>) {
        val etCantidad = view.findViewById<EditText>(R.id.etCantidad)
        val etPrecio = view.findViewById<EditText>(R.id.etPrecio)
        val tvTotalLinea = view.findViewById<TextView>(R.id.tvTotalLinea)
        val btnEliminar = view.findViewById<View>(R.id.btnEliminar)

        val watcher = object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                val qty = etCantidad.text.toString().toIntOrNull() ?: 0
                val price = etPrecio.text.toString().toDoubleOrNull() ?: 0.0
                val total = qty * price
                tvTotalLinea.text = "$${String.format("%.2f", total)}"
                calculateTotals()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        etCantidad.addTextChangedListener(watcher)
        etPrecio.addTextChangedListener(watcher)

        btnEliminar.setOnClickListener {
            container.removeView(view)
            list.remove(view)
            calculateTotals()
        }
    }

    private fun addHojalateriaRow() {
        val view = layoutInflater.inflate(R.layout.item_costo_simple, null)
        setupRowListeners(view, containerHojalateria, hojalateriaViews)
        containerHojalateria.addView(view)
        hojalateriaViews.add(view)
    }

    private fun addPinturaRow() {
        val view = layoutInflater.inflate(R.layout.item_costo_pintura, null)
        setupRowListeners(view, containerPintura, pinturaViews)
        containerPintura.addView(view)
        pinturaViews.add(view)
    }

    private fun addRepuestoRow() {
        val view = layoutInflater.inflate(R.layout.item_costo_simple, null)
        setupRowListeners(view, containerRepuestos, repuestoViews)
        containerRepuestos.addView(view)
        repuestoViews.add(view)
    }

    private fun calculateTotals() {
        var totalHoj = 0.0
        for (view in hojalateriaViews) {
            val total = view.findViewById<TextView>(R.id.tvTotalLinea).text.toString().replace("$", "").toDoubleOrNull() ?: 0.0
            totalHoj += total
        }
        tvTotalHojalateria.text = "$${String.format("%.2f", totalHoj)}"

        var totalPin = 0.0
        for (view in pinturaViews) {
            val total = view.findViewById<TextView>(R.id.tvTotalLinea).text.toString().replace("$", "").toDoubleOrNull() ?: 0.0
            totalPin += total
        }
        tvTotalPintura.text = "$${String.format("%.2f", totalPin)}"

        var totalRep = 0.0
        for (view in repuestoViews) {
            val total = view.findViewById<TextView>(R.id.tvTotalLinea).text.toString().replace("$", "").toDoubleOrNull() ?: 0.0
            totalRep += total
        }
        tvTotalRepuestos.text = "$${String.format("%.2f", totalRep)}"

        val granTotal = totalHoj + totalPin + totalRep
        tvGranTotal.text = "$${String.format("%.2f", granTotal)}"

        val anticipo = etAnticipo.text.toString().toDoubleOrNull() ?: 0.0
        val saldo = granTotal - anticipo
        tvSaldoPendiente.text = "$${String.format("%.2f", saldo)}"
    }

    private fun validarFormulario(): Boolean {
        if (etClienteNombre.text.toString().isEmpty()) {
            Toast.makeText(context, "Ingresa el nombre del cliente", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etVehiculoMarca.text.toString().isEmpty()) {
            Toast.makeText(context, "Ingresa la marca del vehículo", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun loadSavedLogo(view: View) {
        val prefs = requireContext().getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val savedBase64 = prefs.getString("logo_base64", null)
        
        if (savedBase64 != null) {
            base64Logo = savedBase64
            try {
                val decodedBytes = android.util.Base64.decode(savedBase64, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                
                val imgLogoPreview = view.findViewById<android.widget.ImageView>(R.id.imgLogoPreview)
                val btnRemoveLogo = view.findViewById<android.view.View>(R.id.btnRemoveLogo)
                
                if (bitmap != null) {
                    imgLogoPreview.setImageBitmap(bitmap)
                    imgLogoPreview.imageTintList = null 
                    imgLogoPreview.setPadding(0,0,0,0)
                    imgLogoPreview.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                    btnRemoveLogo?.visibility = android.view.View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
