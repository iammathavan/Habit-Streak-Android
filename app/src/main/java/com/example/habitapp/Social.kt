package com.example.habitapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import androidx.fragment.app.Fragment

class Social : AppCompatActivity() {

    private lateinit var btnHome: Button
    private lateinit var btnInfo: Button
    private lateinit var btnFriends: Button
    private lateinit var btnRequests: Button

    private lateinit var userId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_social)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.socialActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnHome = findViewById(R.id.btnHome)
        btnInfo = findViewById(R.id.btnInfo)
        btnFriends = findViewById(R.id.btnFriends)
        btnRequests = findViewById(R.id.btnRequests)

        userId = intent.getStringExtra("USER_ID") ?: ""

        if (userId.isNotEmpty()) {
            loadFragment(UserInfoFragment.newInstance(userId), btnInfo)
        }


        findViewById<Button>(R.id.btnFriends).setOnClickListener {
            loadFragment(FriendsFragment.newInstance(userId), btnFriends)
        }

        findViewById<Button>(R.id.btnInfo).setOnClickListener {
            recreate()
        }

        findViewById<Button>(R.id.btnRequests).setOnClickListener {
            loadFragment(RequestFragment.newInstance(userId), btnRequests)
        }

        findViewById<Button>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadFragment(fragment: Fragment, activeButton: Button) {
        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.fragmentContainer, fragment)
            .commit()

        updateButtonColor(activeButton)
    }

    private fun updateButtonColor(activeButton: Button) {
        val buttons = listOf(btnHome, btnInfo, btnFriends, btnRequests)

        buttons.forEach{ btn->
            btn.setBackgroundResource(R.drawable.roundstyle_btn)
        }

        activeButton.setBackgroundResource(R.drawable.roundstyle_btn_100)
    }
}