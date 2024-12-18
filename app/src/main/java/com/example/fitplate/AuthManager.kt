package com.example.fitplate

import android.content.Context

class AuthManager(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    // Fungsi untuk menyimpan user ID (login)
    fun login(userId: String) {
        val editor = sharedPreferences.edit()
        editor.putString("USER_ID", userId) // Simpan user ID ke SharedPreferences
        editor.apply()
    }

    // Fungsi untuk mengecek status login
    fun checkLoginStatus(): Boolean {
        val userId = sharedPreferences.getString("USER_ID", null)
        return userId != null // True jika user sudah login, false jika belum
    }

    // Fungsi untuk logout
    fun logout() {
        val editor = sharedPreferences.edit()
        editor.remove("USER_ID") // Hapus user ID dari SharedPreferences
        editor.apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString("USER_ID", null)
    }

}
