package com.littlenest.nursery.ui.settings

import android.app.AlertDialog
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.SwitchCompat
import android.widget.TextView
import android.widget.Button
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.littlenest.nursery.R
import com.littlenest.nursery.ui.common.BaseFragment
import androidx.navigation.fragment.findNavController

class SettingsFragment : BaseFragment(R.layout.fragment_family_settings) {

    private val viewModel: SettingsViewModel by viewModels()

    override fun setupUI(view: View) {
        val nurseryLogo = view.findViewById<ImageView>(R.id.nursery_logo)
        val nurseryName = view.findViewById<TextView>(R.id.nursery_name)
        val nurseryAddress = view.findViewById<TextView>(R.id.nursery_address)
        val nurseryDescription = view.findViewById<TextView>(R.id.nursery_description)
        val switchNotifications = view.findViewById<SwitchCompat>(R.id.switchNotifications)
        val layoutLanguage = view.findViewById<View>(R.id.layoutLanguage)
        val textLanguage = view.findViewById<TextView>(R.id.textLanguage)
        val layoutLogout = view.findViewById<View>(R.id.layoutLogout)
        val nurseryEmail = view.findViewById<TextView>(R.id.nursery_email)

        //val userId = userId()
        val role = getUserRole()
        val baseUrl = getBaseUrl()
        val nurseryId = getNurseryId()
        val token = getToken()
        val apiKey = getApiKey()

        //println("🧩 SettingsFragment Loaded — userId=$userId, nurseryId=$nurseryId, role=$role")

        // ✅ Fetch nursery info
        viewModel.fetchNursery("Bearer $token", apiKey, nurseryId)
        viewModel.nursery.observe(viewLifecycleOwner) { nursery ->
            if (nursery != null) {
                nurseryName.text = nursery.name
                nurseryAddress.text = nursery.address
                nurseryDescription.text = nursery.description
                textLanguage.text = nursery.language
                nurseryEmail.text= nursery.nursery_email

                val imageUrl = baseUrl + nursery.image
                Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_app_logo_placeholder)
                    .error(R.drawable.ic_app_logo_placeholder)
                    .into(nurseryLogo)
            } else {
                showToast("Failed to load nursery details")
            }
        }

        // ✅ Notifications - save the notification setting
        val isNotificationsEnabled = getSharedPrefs().getBoolean("notifications_enabled", true)
        switchNotifications.isChecked = isNotificationsEnabled

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            // Save in SharedPreferences
            with(getSharedPrefs().edit()) {
                putBoolean("notifications_enabled", isChecked)
                apply()
            }
            val message = if (isChecked) "Notifications enabled" else "Notifications disabled"
            showToast(message)
        }

        // ✅ Logout
        layoutLogout.setOnClickListener {
            showLogoutDialog()
        }

        val btnUpdateNursery = view.findViewById<Button>(R.id.btnSaveNursery)
        if (role == "admin") {
            btnUpdateNursery.visibility = View.VISIBLE
        } else {
            btnUpdateNursery.visibility = View.GONE
        }
        btnUpdateNursery.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_updateNursery)
        }
    }

    // ✅ Language selector
//    layoutLanguage.setOnClickListener {
//        showLanguageDialog(textLanguage)
//    }
//    private fun showLanguageDialog(textLanguage: TextView) {
//        val languages = arrayOf("English", "සිංහල", "தமிழ்")
//        val currentLang = textLanguage.text.toString()
//
//        AlertDialog.Builder(requireContext())
//            .setTitle("Select Language")
//            .setSingleChoiceItems(languages, languages.indexOf(currentLang)) { dialog, which ->
//                textLanguage.text = languages[which]
//                dialog.dismiss()
//                showToast("Language set to ${languages[which]}")
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ -> handleLogout() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
