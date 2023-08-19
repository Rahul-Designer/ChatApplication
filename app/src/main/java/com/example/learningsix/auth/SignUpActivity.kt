package com.example.learningsix.auth

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.example.learningsix.HomeActivity
import com.example.learningsix.R
import com.example.learningsix.databinding.ActivitySignUpBinding
import com.example.learningsix.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storageRef: FirebaseStorage
    private lateinit var database: FirebaseDatabase
    private var uri: Uri? = null
    var click: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storageRef = FirebaseStorage.getInstance()

        binding.signIn.setOnClickListener{
            startActivity(Intent(this , SignInActivity::class.java))
        }

        binding.signUp.setOnClickListener {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Please Wait..")
            progressDialog.setMessage("Application is loading, please wait")
            progressDialog.show()

            val inputName = binding.nameTextField.editText?.text.toString()
            val inputEmail = binding.emailTextField.editText?.text.toString()
            val inputPhone = binding.phoneTextField.editText?.text.toString()
            val inputPassword = binding.passwordTextField.editText?.text.toString()

            firebaseAuth.createUserWithEmailAndPassword(
                inputEmail,
                inputPassword
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.uid.toString()
                    val pref = getSharedPreferences("login", MODE_PRIVATE)
                    val editor = pref.edit()
                    editor.putBoolean("flag", true)
                    editor.apply()
                    database.reference.child("users").child(userId)
                        .setValue(User(userId, inputEmail,inputName, inputPhone))
                    progressDialog.dismiss()
                    startActivity(Intent(this,HomeActivity::class.java))
                    finish()
                }
                else {
                    Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}