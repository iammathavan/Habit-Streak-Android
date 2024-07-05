package com.example.habitapp

import DataStoreManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate


class SignUpPage : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonSignup: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        buttonSignup = findViewById(R.id.buttonSignup)
        editTextName = findViewById(R.id.editTextName)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        dataStoreManager = DataStoreManager(this)

        buttonSignup.setOnClickListener {
            val name = editTextName.text.toString()
            val email = editTextEmail.text.toString()
            val passwordOne = editTextPassword.text.toString()
            val passwordTwo = editTextConfirmPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || passwordOne.isEmpty() || passwordTwo.isEmpty()){
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordOne != passwordTwo){
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, passwordOne)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = auth.currentUser
                        val userId = user?.uid

                        if (userId != null) {
                            CoroutineScope(Dispatchers.IO).launch {
                                updateUserInfo(name, email)
                                dataStoreManager.setLoggedIn(true)
                                dataStoreManager.setUserId(userId)
                            }.invokeOnCompletion {
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }


                    } else {
                        // If sign in fails, display a message to the user.
                        Snackbar.make(buttonSignup, "Sign-up failed: ${task.exception?.message}", Snackbar.LENGTH_LONG).show()
                        //Toast.makeText(this, "Sign-up failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

    }

    private fun updateUserInfo(name: String, email: String) {
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid

        currentUserID?.let {uid->
            val userInfoRef = database.child(uid).child("userinfo")
            val userInfoID = userInfoRef.push().key

            val user = User(userInfoID, name, email, LocalDate.now())
            userInfoID?.let {
                userInfoRef.child(it).setValue(user)
                    .addOnSuccessListener {
                        Toast.makeText(this, "User information added successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener{
                        Toast.makeText(this, "Failed to add user info: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}