package com.linkersconsulting.appaint

import java.io.Serializable

data class Cotizacion(
    val folio: String = "003",
    val fecha: String,
    val cliente: Cliente,
    val vehiculo: Vehiculo,
    val servicios: List<String>,
    val hojalateria: List<ItemCosto>,
    val pintura: List<ItemPintura>,
    val repuestos: List<ItemCosto>,
    val anticipo: Double,
    val tiempoEstimado: String,
    val piezasIntervenir: Int,
    val datosTaller: DatosTaller
) : Serializable {
    val totalHojalateria: Double
        get() = hojalateria.sumOf { it.precio }

    val totalPintura: Double
        get() = pintura.sumOf { it.precio }

    val totalRepuestos: Double
        get() = repuestos.sumOf { it.precio }

    val totalGeneral: Double
        get() = totalHojalateria + totalPintura + totalRepuestos

    val saldoPendiente: Double
        get() = totalGeneral - anticipo
}

data class Cliente(
    val nombre: String,
    val telefono: String,
    val email: String,
    val domicilio: String
) : Serializable

data class Vehiculo(
    val marca: String,
    val modelo: String,
    val color: String,
    val placas: String,
    val codigoPintura: String
) : Serializable

data class ItemCosto(
    val descripcion: String,
    val precio: Double
) : Serializable

data class ItemPintura(
    val pieza: String,
    val cantidad: Int,
    val unidad: String,
    val precio: Double
) : Serializable

data class DatosTaller(
    val nombre: String = "Premium Laminado y Pintura",
    val encargado: String = "Juan Pérez",
    val telefono: String = "55 1234 5678",
    val email: String = "contacto@taller.com",
    val direccion: String = "Calle y Número, Ciudad",
    val logoBase64: String? = null
) : Serializable