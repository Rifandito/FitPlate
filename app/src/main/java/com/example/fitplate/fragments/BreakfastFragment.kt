package com.example.fitplate.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitplate.R
import com.example.fitplate.adapters.BreakfastAdapter
import com.example.fitplate.activities.DetailActivity
import com.example.fitplate_dienams.Recipe

class BreakfastFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dataList: ArrayList<Recipe>
    private lateinit var myAdapter: BreakfastAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.item_breakfast, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        // Inisialisasi data
        dataList = arrayListOf()
        getData()

        // Atur adapter
        myAdapter = BreakfastAdapter(dataList)
        recyclerView.adapter = myAdapter

        // Klik item untuk membuka DetailActivity
        myAdapter.onItemClick = {
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra("imageRes", it.imageResId)
                putExtra("title", it.title)
                putExtra("time", it.time)
                putExtra("nutrisi", it.nutrisi)
                putExtra("manfaat", it.manfaat)
                putExtra("bahan", it.bahan)
                putExtra("pembuatan", it.pembuatan)
            }
            startActivity(intent)
        }

        return view
    }


    private fun getData() {
        val images = arrayOf(
            R.drawable.smoothie_bowl,
            R.drawable.omelet_sayur,
            R.drawable.salad,
            R.drawable.pancake
        )

        val titles = arrayOf(
            getString(R.string.smoothie_title),
            getString(R.string.omeletSayurAvocado_title),
            getString(R.string.saladTuna_title),
            getString(R.string.pancake_title)
        )

        val times = arrayOf(
            getString(R.string.smoothie_time),
            getString(R.string.omeletSayurAvocado_time),
            getString(R.string.saladTuna_time),
            getString(R.string.pancake_time)
        )

        val nutrisiList = arrayOf(
            getString(R.string.nutrisiSmoothieBowl),
            getString(R.string.nutrisiOmeletSayurAvocado),
            getString(R.string.nutrisiSaladTuna),
            getString(R.string.nutrisiPancake)
        )

        val manfaatList = arrayOf(
            getString(R.string.manfaatSmoothieBowl),
            getString(R.string.manfaatOmeletSayurAvocado),
            getString(R.string.manfaatSaladTuna),
            getString(R.string.manfaatPancake)
        )

        val bahanList = arrayOf(
            getString(R.string.bahanSmoothieBowl),
            getString(R.string.bahanOmeletSayurAvocado),
            getString(R.string.bahanSaladTuna),
            getString(R.string.bahanPancake)
        )

        val pembuatanList = arrayOf(
            getString(R.string.pembuatanSmoothieBowl),
            getString(R.string.pembuatanOmeletSayurAvocado),
            getString(R.string.pembuatanSaladTuna),
            getString(R.string.pembuatanPancake)
        )

        for (i in images.indices) {
            val data = Recipe(
                images[i], titles[i], times[i],
                nutrisiList[i], manfaatList[i], bahanList[i], pembuatanList[i]
            )
            dataList.add(data)
        }
    }
}
