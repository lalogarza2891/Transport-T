package com.example.transport_t

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.*

class FragmentOption1 : Fragment() {

    private lateinit var searchAutoCompleteTextView: AutoCompleteTextView
    private lateinit var locationApiHelper: LocationApiHelper
    private val apiKey = "AIzaSyCiom4P0R8ghymtiW4DM7uyeUhzNfMTelA" // Reemplaza con tu API Key
    private var searchJob: Job? = null

    private val commonCities = listOf(
        "New York", "Los Angeles", "Chicago", "Houston", "Phoenix",
        "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose"
    )


    private val savedPlaces = mutableListOf<Pair<String, String>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_option1, container, false)
        searchAutoCompleteTextView = view.findViewById(R.id.searchAutoCompleteTextView)

        locationApiHelper = LocationApiHelper(apiKey, requireContext())

        loadSavedPlacesFromCSV()
        setupAutoCompleteTextView()
        setupSavedPlacesContainer(view)

        return view
    }

    private fun setupAutoCompleteTextView() {
        var itemSelected = false

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!itemSelected) {
                    val query = s.toString()
                    if (query.isNotEmpty()) {
                        showInstantSuggestions(query)
                        debouncedFetchLocations(query, searchAutoCompleteTextView)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        searchAutoCompleteTextView.addTextChangedListener(textWatcher)

        val savedPlaceNames = savedPlaces.map { it.first }
        val savedPlacesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, savedPlaceNames)
        searchAutoCompleteTextView.setAdapter(savedPlacesAdapter)

        searchAutoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedPlace = parent.getItemAtPosition(position) as String
            searchAutoCompleteTextView.setText(selectedPlace)
            searchAutoCompleteTextView.dismissDropDown()
            itemSelected = true
            searchAutoCompleteTextView.setSelection(selectedPlace.length)
        }

        searchAutoCompleteTextView.setOnDismissListener {
            if (itemSelected) {
                itemSelected = false
            }
        }
    }

    private fun setupSavedPlacesContainer(view: View) {
        val savedPlacesContainer = view.findViewById<LinearLayout>(R.id.savedPlacesContainer)
        savedPlacesContainer.removeAllViews() // Limpia las vistas existentes

        if (savedPlaces.isEmpty()) {
            addNoPlacesTextView(savedPlacesContainer)
        } else {
            savedPlaces.forEach { place ->
                val placeView = createPlaceView(place.first, place.second)
                savedPlacesContainer.addView(placeView)
            }
        }

        val addPlaceButton = Button(requireContext())
        addPlaceButton.text = "Agregar lugar"
        addPlaceButton.setOnClickListener {
            showAddPlaceDialog()
        }
        savedPlacesContainer.addView(addPlaceButton)
    }

    private fun addNoPlacesTextView(container: ViewGroup) {
        val noPlacesTextView = TextView(requireContext()).apply {
            text = "No hay lugares guardados"
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            id = View.generateViewId() // Asigna un ID único
            tag = "no_places_text" // Agrega una etiqueta para fácil identificación
        }
        container.addView(noPlacesTextView)
    }
    private fun createPlaceView(placeName: String, placeAddress: String): View {
        val placeView = LayoutInflater.from(requireContext()).inflate(R.layout.saved_place_item, null)
        placeView.findViewById<TextView>(R.id.placeNameTextView).text = placeName
        placeView.findViewById<TextView>(R.id.placeAddressTextView).text = placeAddress
        placeView.setOnClickListener {
            searchAddressForPlace(placeName, placeAddress)
        }

        placeView.findViewById<ImageButton>(R.id.deletePlaceButton).setOnClickListener {
            showDeleteConfirmationDialog(placeName, placeAddress)
        }

        return placeView
    }
    private fun showDeleteConfirmationDialog(placeName: String, placeAddress: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar lugar")
            .setMessage("¿Estás seguro de que quieres eliminar este lugar?")
            .setPositiveButton("Sí") { _, _ ->
                deletePlace(placeName, placeAddress)
            }
            .setNegativeButton("No", null)
            .show()
    }
    private fun deletePlace(placeName: String, placeAddress: String) {
        savedPlaces.removeAll { it.first == placeName && it.second == placeAddress }
        saveSavedPlacesToCSV()

        view?.findViewById<LinearLayout>(R.id.savedPlacesContainer)?.let { container ->
            // Elimina la vista del lugar
            val placeViewToRemove = container.children.find {
                it.findViewById<TextView>(R.id.placeNameTextView)?.text == placeName &&
                        it.findViewById<TextView>(R.id.placeAddressTextView)?.text == placeAddress
            }
            placeViewToRemove?.let { container.removeView(it) }

            // Si no quedan lugares, agrega el texto "No hay lugares guardados"
            if (savedPlaces.isEmpty()) {
                addNoPlacesTextView(container)
            }
        }

        // Actualizar las sugerencias del buscador principal
        val allSuggestions = (savedPlaces.map { it.first } + commonCities).distinct()
        updateAdapter(allSuggestions, searchAutoCompleteTextView)

        Toast.makeText(requireContext(), "Lugar eliminado", Toast.LENGTH_SHORT).show()
    }
    private fun showAddPlaceDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_place, null)
        val placeNameInput = dialogView.findViewById<EditText>(R.id.placeNameInput)
        val placeAddressInput = dialogView.findViewById<AutoCompleteTextView>(R.id.placeAddressInput)

        var itemSelected = false

        placeAddressInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!itemSelected) {
                    val query = s.toString()
                    if (query.isNotEmpty()) {
                        debouncedFetchLocations(query, placeAddressInput)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        placeAddressInput.setOnItemClickListener { parent, _, position, _ ->
            val selectedPlace = parent.getItemAtPosition(position) as String
            placeAddressInput.setText(selectedPlace)
            placeAddressInput.dismissDropDown()
            itemSelected = true
            placeAddressInput.setSelection(selectedPlace.length)
        }

        placeAddressInput.setOnDismissListener {
            if (itemSelected) {
                itemSelected = false
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Agregar nuevo lugar")
            .setView(dialogView)
            .setPositiveButton("Agregar") { dialog, _ ->


                val newPlaceName = placeNameInput.text.toString().trim()
                val newPlaceAddress = placeAddressInput.text.toString().trim()
                if (newPlaceName.isNotEmpty() && newPlaceAddress.isNotEmpty()) {
                    addNewPlace(newPlaceName, newPlaceAddress)
                } else {
                    Toast.makeText(requireContext(), "Ingrese un nombre y una dirección válidos", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }

    private fun addNewPlace(placeName: String, placeAddress: String) {
        savedPlaces.add(Pair(placeName, placeAddress))
        val savedPlacesContainer = view?.findViewById<LinearLayout>(R.id.savedPlacesContainer)
        savedPlacesContainer?.let { container ->
            // Elimina el texto "No hay lugares guardados" si existe
            container.findViewWithTag<TextView>("no_places_text")?.let {
                container.removeView(it)
            }
            val newPlaceView = createPlaceView(placeName, placeAddress)
            container.addView(newPlaceView, container.childCount - 1) // Inserta antes del botón "Agregar lugar"
        }
        saveSavedPlacesToCSV()

        // Actualizar las sugerencias del buscador principal
        val allSuggestions = (savedPlaces.map { it.first } + commonCities).distinct()
        updateAdapter(allSuggestions, searchAutoCompleteTextView)
    }

    private fun searchAddressForPlace(placeName: String, placeAddress: String) {
        Toast.makeText(requireContext(), "Lugar guardado: $placeName, Dirección: $placeAddress", Toast.LENGTH_SHORT).show()
    }

    private fun showInstantSuggestions(query: String) {
        val instantSuggestions = commonCities.filter { it.startsWith(query, ignoreCase = true) }
        updateAdapter(instantSuggestions, searchAutoCompleteTextView)
    }

    private fun debouncedFetchLocations(query: String, autoCompleteTextView: AutoCompleteTextView) {
        searchJob?.cancel()
        searchJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val locations = locationApiHelper.fetchLocations(query)
                if (locations.isNotEmpty()) {
                    updateAdapter(locations, autoCompleteTextView)
                } else {
                    val savedPlaceNames = savedPlaces.map { it.first }
                    updateAdapter(savedPlaceNames, autoCompleteTextView)
                }
            } catch (e: Exception) {
                val savedPlaceNames = savedPlaces.map { it.first }
                updateAdapter(savedPlaceNames, autoCompleteTextView)
            }
        }
    }

    private fun updateAdapter(locations: List<String>, autoCompleteTextView: AutoCompleteTextView) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, locations)
        autoCompleteTextView.setAdapter(adapter)
        adapter.notifyDataSetChanged()
        if (locations.isNotEmpty()) {
            autoCompleteTextView.showDropDown()
        }
    }

    private fun saveSavedPlacesToCSV() {
        val csvFile = File(requireContext().filesDir, "saved_places.csv")
        try {
            val writer = FileWriter(csvFile)
            savedPlaces.forEach { place ->
                writer.append("${place.first},${place.second}\n")
            }
            writer.flush()
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadSavedPlacesFromCSV() {
        val csvFile = File(requireContext().filesDir, "saved_places.csv")
        try {
            if (csvFile.exists()) {
                val reader = BufferedReader(FileReader(csvFile))
                var line: String?
                savedPlaces.clear() // Limpia la lista antes de cargar
                while (reader.readLine().also { line = it } != null) {
                    val tokens = line!!.split(",")
                    if (tokens.size >= 2) {
                        savedPlaces.add(Pair(tokens[0], tokens[1]))
                    }
                }
                reader.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("savedPlaces", ArrayList(savedPlaces))
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            @Suppress("UNCHECKED_CAST")
            val restoredPlaces = it.getSerializable("savedPlaces") as? ArrayList<Pair<String, String>>
            restoredPlaces?.let { places ->
                savedPlaces.clear()
                savedPlaces.addAll(places)
                view?.let { view -> setupSavedPlacesContainer(view) }
            }
        }
    }
}