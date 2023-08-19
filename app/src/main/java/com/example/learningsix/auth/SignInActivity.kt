package com.example.learningsix.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.learningsix.HomeActivity
import com.example.learningsix.R
import com.example.learningsix.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding : ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_sign_in)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.signUp.setOnClickListener {
            startActivity(Intent(this,SignUpActivity::class.java))
            finish()
        }
        // Shared Preference Code
        val pref = getSharedPreferences("login", MODE_PRIVATE)
        val check = pref.getBoolean("flag", false)
        if (check) {
            // for true Login (User already login )
            startActivity(Intent(this@SignInActivity, HomeActivity::class.java))
            finish()
        }

        // Sign with Email I'd and Password
        binding.signInBtn.setOnClickListener {
            val email = binding.emailEdt.text.toString()
            val password = binding.passwordEdt.text.toString()
            if (email.isEmpty()) binding.emailEdt.error = "Please enter email"
            if (password.isEmpty()) binding.passwordEdt.error = "Please enter password"
            else {
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        // Shared Preference
                        val pref = getSharedPreferences("login", MODE_PRIVATE)
                        val editor = pref.edit()
                        editor.putBoolean("flag", true)
                        editor.apply()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }

            }
            binding.emailEdt.text = null
            binding.passwordEdt.text = null
        }


    }
}