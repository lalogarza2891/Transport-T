package com.example.transport_t



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LocationAdapter(private var locations: List<String>) :
    RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    fun updateLocations(newLocations: List<String>) {
        locations = newLocations
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_location, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(locations[position])
    }

    override fun getItemCount(): Int = locations.size

    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)

        fun bind(location: String) {
            locationTextView.text = location
        }
    }
}
