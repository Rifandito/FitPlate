package com.example.fitplate.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitplate.R
import com.example.fitplate.adapters.RecipeAdapter
import com.example.fitplate.Recipe

class RecipeFragment : Fragment() {

    private var category: String? = null

    companion object {
        private const val ARG_CATEGORY = "category"

        fun newInstance(category: String): RecipeFragment {
            val fragment = RecipeFragment()
            val bundle = Bundle().apply {
                putString(ARG_CATEGORY, category)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = arguments?.getString(ARG_CATEGORY)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_recipe, container, false)

        // Configure RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = RecipeAdapter(getRecipesForCategory())

        return view
    }

    private fun getRecipesForCategory(): List<Recipe> {
        return when (category) {
            "Breakfast" -> listOf(
                Recipe(R.drawable.salad, "Overnight Oats dengan Chia dan Buah-Buahan", "15 menit", "Nutrisi info", "Manfaat info", "Bahan info", "Pembuatan info"),
                Recipe(R.drawable.salad, "Pancake Protein dengan Pisang", "15 menit", "Nutrisi info", "Manfaat info", "Bahan info", "Pembuatan info"),
                Recipe(R.drawable.salad, "Toast Alpukat dengan Telur Rebus", "15 menit", "Nutrisi info", "Manfaat info", "Bahan info", "Pembuatan info"),
                Recipe(R.drawable.omelet_sayur, "Omelet Sayur dan Avokad", "20 menit", "Nutrisi info", "Manfaat info", "Bahan info", "Pembuatan info"),
                Recipe(R.drawable.smoothie_bowl, "Smoothie Bowl Protein Tinggi", "20 menit", "Nutrisi info", "Manfaat info", "Bahan info", "Pembuatan info")
            )
            "Lunch" -> listOf(
                Recipe(R.drawable.salad_ayam_panggang, "Salad Ayam Panggang dengan Quinoa dan Sayuran", "15 menit", "Nutrisi info", "Manfaat info", "Bahan info", "Pembuatan info"),
                Recipe(R.drawable.salmon_panggang, "Tumis Tofu dengan Brokoli dan Kacang Almond", "15 menit", "Nutrisi info", "Manfaat info", "Bahan info", "Pembuatan info"),
                Recipe(R.drawable.salad, "Sup Ayam Sayuran", "15 menit", "Nutrisi info", "Manfaat info", "Bahan info", "Pembuatan info"),
                Recipe(R.drawable.salad, "Nasi Cauliflower (Kembang Kol) dengan Daging Sapi dan Sayuran", "15 menit", "Nutrisi info", "Manfaat info", "Bahan info", "Pembuatan info"),
                Recipe(R.drawable.salad, "Sup Sayuran dengan Tempe dan Miso", "15 menit", "Nutrisi info", "Manfaat info", "Bahan info", "Pembuatan info")
            )
            "Dinner" -> listOf(
                Recipe(R.drawable.salad, "Salmon Panggang dengan Sayuran Kukus", "15 menit", "Nutrisi info", "Manfaat info", "Bahan info", "Pembuatan info"),
                Recipe(R.drawable.salad, "Zucchini Noodles dengan Daging Sapi Tumis", "15 menit", "Nutrisi info", "Manfaat info", "Bahan info", "Pembuatan info"),
                Recipe(R.drawable.salad, "Sup Ayam Sayuran", "15 menit", "Nutrisi info", "Manfaat info", "Bahan info", "Pembuatan info"),
                Recipe(R.drawable.salad, "Tofu dan Brokoli Tumis dengan Saus Kedelai", "15 menit", "Nutrisi info", "Manfaat info", "Bahan info", "Pembuatan info"),
                Recipe(R.drawable.salad, "Ayam Panggang dengan Salad Avokad dan Tomat", "15 menit", "Nutrisi info", "Manfaat info", "Bahan info", "Pembuatan info")
            )
            "Snack" -> listOf(
                Recipe(R.drawable.salad, "Energy Balls Coklat dan Almond", "15 menit", "Nutrisi info", "Manfaat info", "Bahan info", "Pembuatan info"),
                Recipe(R.drawable.salad, "Smoothie Bowl Pisang dan Berry", "15 menit", "Nutrisi info", "Manfaat info", "Bahan info", "Pembuatan info"),
                Recipe(R.drawable.salad, "Keripik Kale Panggang", "15 menit", "Nutrisi info", "Manfaat info", "Bahan info", "Pembuatan info"),
                Recipe(R.drawable.salad, "Greek Yogurt dengan Madu dan Kacang-Kacangan", "15 menit", "Nutrisi info", "Manfaat info", "Bahan info", "Pembuatan info"),
                Recipe(R.drawable.salad, "Edamame Rebus dengan Garam Laut", "15 menit", "Nutrisi info", "Manfaat info", "Bahan info", "Pembuatan info")
            )
            else -> emptyList()
        }
    }

}
