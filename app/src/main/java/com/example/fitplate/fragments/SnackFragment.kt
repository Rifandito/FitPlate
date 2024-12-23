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
import com.example.fitplate.activities.DetailActivity
import com.example.fitplate.adapters.SnackAdapter
import com.example.fitplate.Recipe

class SnackFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dataList: ArrayList<Recipe>
    private lateinit var myAdapter: SnackAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.item_snack, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        // Inisialisasi data
        dataList = arrayListOf()
        getData()

        // Atur adapter
        myAdapter = SnackAdapter(dataList)
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
            R.drawable.nergy_balls,
            R.drawable.smoothie_bowl_pisang_berry,
            R.drawable.keripik_kale_panggang,
            R.drawable.greek_yogurt,
            R.drawable.edame_rebus_dengan_garam_laut
        )

        val titles = arrayOf(
            getString(R.string.energyBalls_title),
            getString(R.string.smoothieBowlPisang_title),
            getString(R.string.keripik_title),
            getString(R.string.greek_title),
            getString(R.string.edamame_title)
        )

        val times = arrayOf(
            getString(R.string.energyBalls_time),
            getString(R.string.smoothieBowlPisang_time),
            getString(R.string.keripik_time),
            getString(R.string.greek_time),
            getString(R.string.edamame_time)
        )

        val nutrisiList = arrayOf(
            getString(R.string.nutrisiEnergyBalls),
            getString(R.string.nutrisiSmoothieBowlPisang),
            getString(R.string.nutrisiKeripik),
            getString(R.string.nutrisiGreek),
            getString(R.string.nutrisiEdamame)
        )

        val manfaatList = arrayOf(
            getString(R.string.manfaatEnergyBalls),
            getString(R.string.manfaatSmoothieBowlPisang),
            getString(R.string.manfaatKeripik),
            getString(R.string.manfaatGreek),
            getString(R.string.manfaatEdamame)
        )

        val bahanList = arrayOf(
            getString(R.string.bahanEnergyBalls),
            getString(R.string.bahanSmoothieBowlPisang),
            getString(R.string.bahanKeripik),
            getString(R.string.bahanGreek),
            getString(R.string.bahanEdamame)
        )

        val pembuatanList = arrayOf(
            getString(R.string.pembuatanEnergyBalls),
            getString(R.string.pembuatanSmoothieBowlPisang),
            getString(R.string.pembuatanKeripik),
            getString(R.string.pembuatanGreek),
            getString(R.string.pembuatanEdamame)
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
