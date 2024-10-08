package com.example.restaurantreviewsapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {
    private var mAuth = FirebaseAuth.getInstance()
    private var currentUser = mAuth.currentUser

    private lateinit var emailText : EditText
    private lateinit var pwText: EditText
    private lateinit var signUpButton : Button
    private lateinit var signInButton: Button

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
           finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        mAuth.signOut()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        //set  UI elements
        emailText = findViewById(R.id.loginEmailAddress)
        pwText = findViewById(R.id.loginPassword)

        signUpButton = findViewById(R.id.signUpButton)
        signInButton = findViewById(R.id.signInButton)

        signUpButton.setOnClickListener{ _ -> signUpClick() }
        signInButton.setOnClickListener{ _ -> signInClick()}
    }

    private fun signUpClick() {
        val intent = Intent(this, Signup::class.java)
        startForResult.launch(intent)

    }
    private fun signInClick() {
        if(TextUtils.isEmpty(emailText.text) || TextUtils.isEmpty(pwText.text)) {
            if(TextUtils.isEmpty(emailText.text)) {
                emailText.error = getString(R.string.signup_empty_email)
            }
            if(TextUtils.isEmpty(pwText.text)) {
                pwText.error = getString(R.string.signup_empty_password)
            }
            displayMessage(signInButton, getString(R.string.signup_empty_field))
            return
        }
        if(currentUser == null){
            mAuth.signInWithEmailAndPassword(
                emailText.text.toString(),
                pwText.text.toString(),
            ).addOnCompleteListener(this) {task ->
                if(task.isSuccessful){
                    val s = getString(R.string.sign_in_success) + " " + emailText.text.toString()
                    displayMessage(signInButton, s)
                    finish()
                }
            }
        }
    }





    override fun finish() {
        setResult(Activity.RESULT_OK)
        super.finish()
    }
}