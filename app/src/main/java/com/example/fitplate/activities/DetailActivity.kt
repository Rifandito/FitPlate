package com.example.fitplate.activities
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.fitplate.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data dari Intent
        val imageRes = intent.getIntExtra("imageRes", 0)
        val title = intent.getStringExtra("title")
        val time = intent.getStringExtra("time")
        val nutrisi = intent.getStringExtra("nutrisi")
        val manfaat = intent.getStringExtra("manfaat")
        val bahan = intent.getStringExtra("bahan")
        val pembuatan = intent.getStringExtra("pembuatan")

        Log.d("DetailActivity", "Data received: imageRes=$imageRes, title=$title, time=$time, nutrisi=$nutrisi, manfaat=$manfaat, bahan=$bahan, pembuatan=$pembuatan")
        // Set data ke UI
        binding.imageView.setImageResource(imageRes)
        binding.textTitle.text = title
        binding.textTime.text = time
        binding.tNutrisi.text = nutrisi
        binding.tManfaat.text = manfaat
        binding.tBahan.text = bahan
        binding.tPembuatan.text = pembuatan
    }
}
