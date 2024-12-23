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
import com.example.fitplate.adapters.DinnerAdapter
import com.example.fitplate.activities.DetailActivity
import com.example.fitplate_dienams.Recipe

class DinnerFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dataList: ArrayList<Recipe>
    private lateinit var myAdapter: DinnerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.item_dinner, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        // Inisialisasi data
        dataList = arrayListOf()
        getData()

        // Atur adapter
        myAdapter = DinnerAdapter(dataList)
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
            R.drawable.salad_ayam_panggang,
            R.drawable.tofu_brokoli_tumis,
            R.drawable.zucchini_noodles,
            R.drawable.sup_ayam_sayuran,
            R.drawable.ayam_panggang_dengan_salad_avokad
        )

        val titles = arrayOf(
            getString(R.string.ayamPanggangSalad_title),
            getString(R.string.tumisTofu_title),
            getString(R.string.zucchini_title),
            getString(R.string.supAyamSayuran_title),
            getString(R.string.tofuBrokoli_title)
        )

        val times = arrayOf(
            getString(R.string.ayamPanggangSalad_time),
            getString(R.string.tumisTofu_time),
            getString(R.string.zucchini_time),
            getString(R.string.supAyamSayuran_time),
            getString(R.string.tofuBrokoli_time)
        )

        val nutrisiList = arrayOf(
            getString(R.string.nutrisiAyamPanggangSalad),
            getString(R.string.nutrisiTumisTofu),
            getString(R.string.nutrisiZucchini),
            getString(R.string.nutrisiSupAyamSayuran),
            getString(R.string.nutrisiTofuBrokoli)
        )

        val manfaatList = arrayOf(
            getString(R.string.manfaatAyamPanggangSalad),
            getString(R.string.manfaatTumisTofu),
            getString(R.string.manfaatZucchini),
            getString(R.string.manfaatSupAyamSayuran),
            getString(R.string.manfaatTofuBrokoli)
        )

        val bahanList = arrayOf(
            getString(R.string.bahanAyamPanggangSalad),
            getString(R.string.bahanTumisTofu),
            getString(R.string.bahanZucchini),
            getString(R.string.bahanSupAyamSayuran),
            getString(R.string.bahanTofuBrokoli)
        )

        val pembuatanList = arrayOf(
            getString(R.string.pembuatanAyamPanggangSalad),
            getString(R.string.pembuatanTumisTofu),
            getString(R.string.pembuatanZucchini),
            getString(R.string.pembuatanSupAyamSayuran),
            getString(R.string.pembuatanTofuBrokoli)
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
