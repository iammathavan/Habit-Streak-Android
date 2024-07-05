package com.example.habitapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class ResetPassword : AppCompatActivity() {


    private lateinit var editTextEmail: EditText
    private lateinit var resetPasswordBtn: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reset_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        editTextEmail = findViewById(R.id.editTextEmail)
        resetPasswordBtn = findViewById(R.id.resetPasswordBtn)

        auth = FirebaseAuth.getInstance()


        resetPasswordBtn.setOnClickListener {
            val email = editTextEmail.text.toString()
            if (email.isNotEmpty()){
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener{task->
                        if(task.isSuccessful){
                            Toast.makeText(this, "Reset email sent", Toast.LENGTH_SHORT).show()
                            finish()
                        }else{
                            Toast.makeText(this, "Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }else{
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }
    }


}