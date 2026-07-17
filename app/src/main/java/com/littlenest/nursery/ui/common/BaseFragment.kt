package com.littlenest.nursery.ui.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.littlenest.nursery.LoginActivity

open class BaseFragment(layoutId: Int) : Fragment(layoutId) {

    // SharedPreferences helper
    protected fun getSharedPrefs() =
        requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    // Get token
    protected fun getToken(): String? =
        getSharedPrefs().getString("auth_token", null)

    // Get API key
    protected fun getApiKey(): String =
        getSharedPrefs().getString("api_key", "") ?: ""

    // Get user role
    protected fun getUserRole(): String? =
        getSharedPrefs().getString("user_role", null)

    // Get nurseryId
    protected fun getNurseryId(): Int =
        getSharedPrefs().getInt("nursery_id", -1)

    // Get profileId (StudentId/TeacherId)
    protected fun getProfileId(): Int =
        getSharedPrefs().getInt("profile_id", -1)

    // Get userId
    protected fun userId(): Int =
        getSharedPrefs().getInt("user_id", -1)

    protected fun getBaseUrl(): String = AppConfig.BASE_URL
    protected fun apiBaseUrl(): String = AppConfig.API_BASE_URL
    protected fun uploadsBaseUrl(): String = AppConfig.UPLOADS_BASE_URL

    // Logout function
    protected fun handleLogout() {
        with(getSharedPrefs().edit()) {
            clear()
            apply()
        }
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    // You can override this in each fragment if you want to do custom setup
    open fun setupUI(view: View) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI(view)
    }
}
