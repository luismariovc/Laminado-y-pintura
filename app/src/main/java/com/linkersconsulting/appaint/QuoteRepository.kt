package com.linkersconsulting.appaint

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class QuoteRepository(private val context: Context) {

    private val gson = Gson()
    private val fileName = "quotes.json"

    fun saveQuote(cotizacion: Cotizacion) {
        val quotes = getQuotes().toMutableList()
        // Add to top of list
        quotes.add(0, cotizacion)
        saveList(quotes)
    }

    fun getQuotes(): List<Cotizacion> {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return emptyList()

        return try {
            val jsonString = file.readText()
            val type = object : TypeToken<List<Cotizacion>>() {}.type
            gson.fromJson(jsonString, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getNextFolio(): String {
        val quotes = getQuotes()
        if (quotes.isEmpty()) return "001"

        val maxFolio = quotes.maxOfOrNull { it.folio.toIntOrNull() ?: 0 } ?: 0
        return String.format("%03d", maxFolio + 1)
    }

    private fun saveList(quotes: List<Cotizacion>) {
        try {
            val jsonString = gson.toJson(quotes)
            val file = File(context.filesDir, fileName)
            file.writeText(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
