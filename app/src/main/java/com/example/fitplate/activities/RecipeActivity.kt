package com.example.fitplate.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.fitplate.fragments.*
import com.example.fitplate.databinding.ActivityRecipeBinding
import com.google.android.material.tabs.TabLayoutMediator

class RecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTabs()
    }

    private fun setupTabs() {
        val fragments = listOf(
            BreakfastFragment(),
            LunchFragment(),
            DinnerFragment(),
            SnackFragment()
        )

        val titles = listOf("Breakfast", "Lunch", "Dinner", "Snack")

        binding.viewPager.adapter = RecipePagerAdapter(this, fragments)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    private class RecipePagerAdapter(
        activity: AppCompatActivity,
        private val fragments: List<Fragment>
    ) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment = fragments[position]
    }
}