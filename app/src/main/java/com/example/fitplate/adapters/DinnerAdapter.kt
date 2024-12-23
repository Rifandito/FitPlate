package com.example.fitplate.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitplate.R
import com.example.fitplate.Recipe

class DinnerAdapter(
    private val items: List<Recipe>
) : RecyclerView.Adapter<DinnerAdapter.ViewHolder>() {

    var onItemClick: ((Recipe) -> Unit)? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageViewDinner)
        val title: TextView = view.findViewById(R.id.textTitleDinner)
        val time: TextView = view.findViewById(R.id.textTimeDinner)

        init {
            view.setOnClickListener {
                onItemClick?.invoke(items[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_dinner, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.imageView.setImageResource(item.imageResId)
        holder.title.text = item.title
        holder.time.text = item.time
    }

    override fun getItemCount(): Int = items.size
}
