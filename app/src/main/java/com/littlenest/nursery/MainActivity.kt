package com.littlenest.nursery

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import android.content.Context
import android.widget.TextView
import android.widget.ImageView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.littlenest.nursery.databinding.ActivityMainBinding
import com.bumptech.glide.Glide

import com.littlenest.nursery.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.littlenest.nursery.ui.student.GetStudentResponse
import com.littlenest.nursery.ui.common.AppConfig
import com.littlenest.nursery.ui.teacher.SingleTeacherByIdResponse
import com.bumptech.glide.load.engine.DiskCacheStrategy


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve saved data
        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", null)
        val userRole = sharedPref.getString("user_role", null)
        //val nurseryId = sharedPref.getInt("nursery_id", -1)
        val userId = sharedPref.getInt("user_id", -1)  // read as Int
        val profileId = sharedPref.getInt("profile_id", -1)
        val username = sharedPref.getString("username", null)

        // If no token is found, redirect back to login
        if (token.isNullOrEmpty()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        // ✅ Update Drawer Header
        val headerView = navView.getHeaderView(0)
        val headerName = headerView.findViewById<TextView>(R.id.headerName)
        val headerImage = headerView.findViewById<ImageView>(R.id.headerImage)

        // Fetch profile data from SharedPreferences
        val profileName = sharedPref.getString("profile_name", "Guest")
        val profileImageUrl = sharedPref.getString("profile_image", null)
        val apiKey = "your-very-secret-key"

        // Set name
        headerName.text = profileName

        // Load profile picture (using Glide)
        if (!profileImageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(profileImageUrl)
                .placeholder(R.drawable.avatar_placeholder) // fallback icon
                .into(headerImage)
        } else {
            headerImage.setImageResource(R.drawable.avatar_placeholder)
        }


        // ✅ Load Navigation Graph based on Role
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Clear any existing menu first
        navView.menu.clear()
        when (userRole) {
            "student" -> {
                navController.setGraph(R.navigation.nav_graph_family)
                navView.inflateMenu(R.menu.nav_drawer_family)
            }
            "teacher" -> {
                navController.setGraph(R.navigation.nav_graph_educator)
                navView.inflateMenu(R.menu.nav_drawer_educator)
            }
            "admin" -> {
                navController.setGraph(R.navigation.nav_graph_manager)
                navView.inflateMenu(R.menu.nav_drawer_manager)
            }
            else -> {
                // Fallback - load a default graph
                navController.setGraph(R.navigation.mobile_navigation)
                navView.inflateMenu(R.menu.activity_main_drawer)
            }
        }

//        appBarConfiguration = AppBarConfiguration(
//            setOf(R.id.nav_home,
//                R.id.nav_gallery,
//                R.id.nav_slideshow,
//                R.id.nav_students,
//            ),
//            drawerLayout
//        )

        // ✅ Setup AppBar + Drawer
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // ✅ Logout handler
        navView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.nav_logout) {
                handleLogout()
                true
            } else {
                val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
                if (handled) {
                    drawerLayout.closeDrawers()
                }
                handled
            }
        }

        //update drawer profile pic and name
        if (profileId != -1 && userRole != null) {
            if (userRole == "student") {
                RetrofitClient.instance.getStudentById(
                    profileId,
                    "Bearer $token",
                    apiKey
                ).enqueue(object : Callback<GetStudentResponse> {
                        override fun onResponse(
                            call: Call<GetStudentResponse>,
                            response: Response<GetStudentResponse>
                        ) {
                            if (response.isSuccessful) {
                                val student = response.body()?.student
                                if (student != null) {
                                    updateDrawerHeader(
                                        student.extraData.fullName,
                                        "${AppConfig.UPLOADS_BASE_URL}${student.extraData.profilePicture}",
                                        "(Student)"
                                    )
                                }
                            } else {
                                Log.e("Error", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                            }
                        }

                        override fun onFailure(call: Call<GetStudentResponse>, t: Throwable) {
                            t.printStackTrace()
                        }
                    })
            } else if (userRole == "teacher") {
                RetrofitClient.instance.getTeacherById(
                    profileId,
                    "Bearer $token",
                    apiKey)
                    .enqueue(object : Callback<SingleTeacherByIdResponse> {
                        override fun onResponse(
                            call: Call<SingleTeacherByIdResponse>,
                            response: Response<SingleTeacherByIdResponse>
                        ) {
                            if (response.isSuccessful) {
                                val teacher = response.body()?.teacher
                                teacher?.let {
                                    updateDrawerHeader(
                                        it.extraData.name,
                                        "${AppConfig.UPLOADS_BASE_URL}${it.extraData.profilePicture}",
                                        "(Teacher)"
                                    )
                                }

                            } else {
                                Log.e("Error", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                            }
                        }

                        override fun onFailure(call: Call<SingleTeacherByIdResponse>, t: Throwable) {
                            t.printStackTrace()
                        }
                    })
            } else if (userRole == "admin") {
                updateDrawerHeader(
                     username,
                    "${AppConfig.UPLOADS_BASE_URL}",
                    "(Admin)"
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun handleLogout() {
        // Clear token and user data from SharedPreferences
        val sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Go back to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Step 3: Update Drawer Header
    private fun updateDrawerHeader(name: String?, profilePicUrl: String?, role: String?) {
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)

        val headerName: TextView = headerView.findViewById(R.id.headerName)
        val headerImage: ImageView = headerView.findViewById(R.id.headerImage)
        val headerRole: TextView = headerView.findViewById(R.id.headerRole)

        headerName.text = name ?: "User"
        headerRole.text = role ?: "No Role"

        if (role == "(Admin)") {
            // ✅ Hide avatar for admin
            headerImage.visibility = ImageView.GONE
        } else {
            // ✅ Show avatar for student / teacher
            headerImage.visibility = ImageView.VISIBLE

            Glide.with(this)
                .load(profilePicUrl)
                .transform(CircleCrop())
                .placeholder(R.drawable.avatar_placeholder)
                .into(headerImage)
        }
    }


    override fun onResume() {
        super.onResume()
        refreshDrawerHeader()
    }

    private fun refreshDrawerHeader() {
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val imageUrl = prefs.getString("profile_image", null) ?: return

        val navView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navView.getHeaderView(0)
        val imgProfileHeader = headerView.findViewById<ImageView>(R.id.headerImage)

        Glide.with(this)
            .load(imageUrl)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(R.drawable.avatar_placeholder)
            .circleCrop()
            .into(imgProfileHeader)
    }

}
