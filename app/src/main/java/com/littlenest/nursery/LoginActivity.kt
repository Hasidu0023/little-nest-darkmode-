package com.littlenest.nursery

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.littlenest.nursery.model.LoginRequest
import com.littlenest.nursery.model.LoginResponse
import com.littlenest.nursery.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import android.util.Patterns
import android.content.Context

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    private var isLoginInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.editTextUsername)
        passwordEditText = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.buttonLogin)

        loginButton.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        if (isLoginInProgress) {
            return
        }

        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

//        val username = "janani"
//        val password = "janani"
//        val username = "nursery1_hannah"
//        val password = "nursery1_hannah"
//        val username = "admin"
//        val password = "AdTech123$"
//        val username = "newnew123"
//        val password = "newteacher"


//        val username ="Student_nursery2"
//        val password = "Student_nursery2"
//        val username = "teacher_jennifer_nur1"
//        val password = "teacher_jennifer_nur1"

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        isLoginInProgress = true

        val request = LoginRequest(email, password)

        RetrofitClient.instance.loginUser(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                isLoginInProgress = false

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()
                    val token = loginResponse?.token
                    val userRole = loginResponse?.user?.role
                    val nurseryId = loginResponse?.user?.nurseryId
                    val userId = loginResponse?.user?.id
                    val profileId = loginResponse?.user?.profileId
                    val username = loginResponse?.user?.username


                    //Log token
                    Log.d("loginResponse", "resp: $loginResponse")

                    // Save token and other data in SharedPreferences
                    val sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("auth_token", token)
                        putString("user_role", userRole)
                        putInt("nursery_id", nurseryId ?: -1) // store -1 if null
                        putString("api_key", "your-very-secret-key") // your static API key
                        putInt("user_id", userId ?: -1)
                        putInt("profile_id", profileId ?: -1)
                        putString("username", username)
                        apply()
                    }

                    Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()

                    // Proceed to next screen
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                isLoginInProgress = false
                Toast.makeText(this@LoginActivity, "Network error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}