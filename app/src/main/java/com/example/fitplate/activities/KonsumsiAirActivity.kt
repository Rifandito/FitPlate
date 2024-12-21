package com.example.fitplate.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.fitplate.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class KonsumsiAirActivity : AppCompatActivity() {

    // Pindahkan DayAxisValueFormatter sebagai inner class
    private inner class DayAxisValueFormatter : com.github.mikephil.charting.formatter.ValueFormatter() {
        private val days = arrayOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

        override fun getFormattedValue(value: Float): String {
            return if (value >= 0 && value < days.size) days[value.toInt()] else ""
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.konsumsi_air_activity)

        // Inisialisasi BarChart
        val barChart: BarChart = findViewById(R.id.waterBarChart)

        // Membuat data untuk BarChart
        val dataSet = BarDataSet(createBarEntries(), "Konsumsi Air Harian (ml)")
        val biru = ContextCompat.getColor(this, R.color.biru)
        dataSet.color = biru

        val data = BarData(dataSet)
        data.setValueTextSize(12f)
        barChart.data = data

        // Konfigurasi X-Axis
        val xAxis = barChart.xAxis
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = DayAxisValueFormatter()

        // Konfigurasi Y-Axis
        barChart.axisRight.isEnabled = false
        val leftAxis = barChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 3000f
        leftAxis.setLabelCount(7, true)
        leftAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }
        leftAxis.setDrawGridLines(true)
        leftAxis.granularity = 500f

        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.setDrawValueAboveBar(true)
        barChart.setPinchZoom(false)
        barChart.setScaleEnabled(false)
        barChart.invalidate()

        // Tombol kembali
        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Menangani klik pada CardView artikelKonsumsiCard
        val artikelKonsumsiCard: CardView = findViewById(R.id.artikelKonsumsiCard)
        artikelKonsumsiCard.setOnClickListener {
            val intent = Intent(this@KonsumsiAirActivity, ArtikelAirActivity::class.java)
            startActivity(intent)
        }

        // Menangani klik pada CardView artikelKeseimbanganCard
        val artikelKeseimbanganCard: CardView = findViewById(R.id.artikelKeseimbanganCard)
        artikelKeseimbanganCard.setOnClickListener {
            val intent = Intent(this@KonsumsiAirActivity, ArtikelKonsumsiActivity::class.java)
            startActivity(intent)
        }
    }

    // Fungsi untuk membuat data BarEntry
    private fun createBarEntries(): List<BarEntry> {
        return listOf(
            BarEntry(0f, 2200f),
            BarEntry(1f, 2500f),
            BarEntry(2f, 1800f),
            BarEntry(3f, 2000f),
            BarEntry(4f, 2700f),
            BarEntry(5f, 3000f),
            BarEntry(6f, 2300f)
        )
    }
}
