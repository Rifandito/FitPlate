package com.example.fitplate.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitplate.R
import com.example.fitplate.dataclasses.Makanan

class FoodAdapter(
    private val foods: List<Makanan>,
    private val onItemClick: (Makanan) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodIcon: ImageView = itemView.findViewById(R.id.foodIcon)
        val foodName: TextView = itemView.findViewById(R.id.foodName)
        val mealTime: TextView = itemView.findViewById(R.id.mealTime)
        val detailText: TextView = itemView.findViewById(R.id.detailText)
        val chevronIcon: ImageView = itemView.findViewById(R.id.chevronIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val makanan = foods[position]

        // Bind the food data to the views
        holder.foodName.text = makanan.namaMakanan
        holder.mealTime.text = makanan.waktuMakan

        // Handle the item click with the appropriate intent
//        holder.itemView.setOnClickListener {
//            val intent = Intent(holder.itemView.context, DetailFoodActivity::class.java).apply {
//                putExtra("food_id", food.id)
//                putExtra("food_name", food.name)
//                putExtra("meal_time", food.mealTime)
//                putExtra("calories", food.calories)
//                putExtra("carbs", food.carbs)
//                putExtra("protein", food.protein)
//                putExtra("fat", food.fat)
//            }
//            holder.itemView.context.startActivity(intent)
//        }

        // Handle the item click with the provided lambda
        holder.itemView.setOnClickListener {
            onItemClick(makanan)
        }
    }

    override fun getItemCount() = foods.size
}


