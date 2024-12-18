package com.example.fitplate.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.fitplate.R

class HomeActivity : AppCompatActivity() {

    private lateinit var cardViewKonsumsiAir: CardView
    private lateinit var cardViewMengukurTubuh: CardView
    private lateinit var cardViewFoodTracker: CardView
    private lateinit var cardViewMedalGuide: CardView // Tambahkan Medal Guide CardView
    private lateinit var profileButton: ImageButton
    private lateinit var leaderboardButton: ImageButton // Tambahkan ImageButton untuk leaderboard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        // Inisialisasi view
        initializeViews()

        // Atur listener
        //setupClickListeners()
    }

    private fun initializeViews() {
        cardViewKonsumsiAir = findViewById(R.id.cardViewKonsumsiAir)
        cardViewMengukurTubuh = findViewById(R.id.cardViewMengukurTubuh)
        cardViewFoodTracker = findViewById(R.id.cardViewFoodTracker)
        cardViewMedalGuide = findViewById(R.id.cardViewMedalGuide)
        profileButton = findViewById(R.id.profil_button)
        leaderboardButton = findViewById(R.id.leaderboard_button)
    }

//    private fun setupClickListeners() {
//        cardViewKonsumsiAir.setOnClickListener {
//            navigateToActivity(KonsumsiAirActivity::class.java)
//        }
//
//        cardViewMengukurTubuh.setOnClickListener {
//            navigateToActivity(MengukurTubuhActivity::class.java)
//        }
//
//        cardViewFoodTracker.setOnClickListener {
//            navigateToActivity(FoodTrackerActivity::class.java)
//        }
//
//        profileButton.setOnClickListener {
//            navigateToActivity(ProfilActivity::class.java)
//        }
//
//        cardViewMedalGuide.setOnClickListener {
//            navigateToActivity(GuideActivity::class.java)
//        }
//
//        leaderboardButton.setOnClickListener {
//            goToLeaderboard()
//        }
//    }

//    private fun <T> navigateToActivity(activityClass: Class<T>) {
//        val intent = Intent(this, activityClass)
//        startActivity(intent)
//    }
//
//    // Fungsi untuk berpindah ke LeaderboardActivity
//    private fun goToLeaderboard() {
//        val intent = Intent(this, LeaderboardActivity::class.java)
//        startActivity(intent)
//    }
}
