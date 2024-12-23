package com.example.fitplate.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.fitplate.fragments.RecipeFragment

class RecipePagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val categories = listOf("Breakfast", "Lunch", "Dinner", "Snack")

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
        return RecipeFragment.newInstance(categories[position])
    }
}
