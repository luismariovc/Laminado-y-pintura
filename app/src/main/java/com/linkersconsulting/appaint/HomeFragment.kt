package com.linkersconsulting.appaint

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.linkersconsulting.appaint.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var quoteRepository: QuoteRepository
    private var allQuotes: List<Cotizacion> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        quoteRepository = QuoteRepository(requireContext())
        
        setupHeader()
        setupRecyclerView()
        setupSearch()
    }

    override fun onResume() {
        super.onResume()
        loadQuotes()
    }

    private fun setupHeader() {
        val user = FirebaseAuth.getInstance().currentUser
        val name = user?.displayName ?: "Usuario"
        binding.tvWelcomeTitle.text = "Bienvenido, $name"
    }

    private fun setupRecyclerView() {
        binding.rvQuotes.layoutManager = LinearLayoutManager(context)
        // Initial empty adapter
        binding.rvQuotes.adapter = QuotesAdapter(emptyList()) { quote ->
            val intent = Intent(requireContext(), CotizacionActivity::class.java)
            intent.putExtra("COTIZACION_DATA", quote)
            startActivity(intent)
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterQuotes(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun filterQuotes(query: String) {
        if (query.isEmpty()) {
            updateAdapter(allQuotes)
            return
        }

        val filteredList = allQuotes.filter { quote ->
            quote.folio.contains(query, ignoreCase = true) ||
            quote.cliente.nombre.contains(query, ignoreCase = true) ||
            quote.vehiculo.marca.contains(query, ignoreCase = true) ||
            quote.vehiculo.modelo.contains(query, ignoreCase = true) ||
            quote.vehiculo.placas.contains(query, ignoreCase = true)
        }
        
        updateAdapter(filteredList)
    }

    private fun updateAdapter(list: List<Cotizacion>) {
        if (list.isEmpty()) {
            binding.rvQuotes.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
        } else {
            binding.rvQuotes.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
            (binding.rvQuotes.adapter as QuotesAdapter).updateList(list)
        }
    }

    private fun loadQuotes() {
        allQuotes = quoteRepository.getQuotes()
        // Apply current filter if any text exists, otherwise show all
        val currentQuery = binding.etSearch.text.toString()
        filterQuotes(currentQuery)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
