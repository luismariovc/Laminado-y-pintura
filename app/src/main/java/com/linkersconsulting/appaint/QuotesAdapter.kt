package com.linkersconsulting.appaint

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QuotesAdapter(
    private var quotes: List<Cotizacion>,
    private val onQuoteClick: (Cotizacion) -> Unit
) : RecyclerView.Adapter<QuotesAdapter.QuoteViewHolder>() {

    class QuoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFolio: TextView = view.findViewById(R.id.tvCardFolio)
        val tvDate: TextView = view.findViewById(R.id.tvCardDate)
        val tvCliente: TextView = view.findViewById(R.id.tvCardCliente)
        val tvVehiculo: TextView = view.findViewById(R.id.tvCardVehiculo)
        val tvTotal: TextView = view.findViewById(R.id.tvCardTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quote_card, parent, false)
        return QuoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        val quote = quotes[position]
        
        holder.tvFolio.text = "FOLIO #${quote.folio}"
        // Only show date part if possible, or simple substring
        holder.tvDate.text = quote.fecha.substringBefore(" ")
        holder.tvCliente.text = quote.cliente.nombre
        holder.tvVehiculo.text = "${quote.vehiculo.marca} ${quote.vehiculo.modelo}"
        holder.tvTotal.text = "$${String.format("%.2f", quote.totalGeneral)}"

        holder.itemView.setOnClickListener { onQuoteClick(quote) }
    }

    override fun getItemCount() = quotes.size

    fun updateList(newQuotes: List<Cotizacion>) {
        quotes = newQuotes
        notifyDataSetChanged()
    }
}
