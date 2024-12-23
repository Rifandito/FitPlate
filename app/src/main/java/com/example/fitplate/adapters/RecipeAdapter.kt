package com.example.fitplate.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitplate.R
import com.example.fitplate.Recipe

class RecipeAdapter(private val recipes: List<Recipe>) :
    RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        val textTime: TextView = itemView.findViewById(R.id.textTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.imageView.setImageResource(recipe.imageResId)
        holder.textTitle.text = recipe.title
        holder.textTime.text = recipe.time
    }

    override fun getItemCount(): Int = recipes.size
}
