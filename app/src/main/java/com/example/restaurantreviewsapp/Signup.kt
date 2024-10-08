package com.example.restaurantreviewsapp

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Signup : AppCompatActivity(){
    private var mAuth = FirebaseAuth.getInstance()
    private var currentUser = mAuth.currentUser
    private val db = Firebase.firestore

    private lateinit var emailText : EditText
    private lateinit var pwText: EditText
    private lateinit var nameText : EditText
    private lateinit var registerButton : Button
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        emailText = findViewById(R.id.signupEmailAddress)
        pwText = findViewById(R.id.signupPassword)
        nameText = findViewById(R.id.signupUsername)
        registerButton = findViewById(R.id.registerButton)

        registerButton.setOnClickListener{ _ -> registerClick() }



    }
    private fun registerClick() {
        if(TextUtils.isEmpty(emailText.text) || TextUtils.isEmpty(pwText.text) || TextUtils.isEmpty(nameText.text) ) {
            if(TextUtils.isEmpty(emailText.text)) {
                emailText.error = getString(R.string.signup_empty_email)
            }
            if(TextUtils.isEmpty(pwText.text)) {
                pwText.error = getString(R.string.signup_empty_password)
            }
            if(TextUtils.isEmpty(nameText.text)) {
                nameText.error = getString(R.string.signup_empty_username)
            }
            displayMessage(registerButton, getString(R.string.signup_empty_field))
            return
        }
        if(currentUser == null){
            mAuth.createUserWithEmailAndPassword(
                emailText.text.toString(),
                pwText.text.toString(),
            ).addOnCompleteListener(this) {task ->
                if(task.isSuccessful){
                    val s = getString(R.string.sign_up_success) + " " + emailText.text.toString()
                    displayMessage(registerButton, s)
                    createUser()
                    finish()
                }
            }
                .addOnFailureListener(this) { _ ->
                    val s = getString(R.string.sign_up_failure)
                    displayMessage(registerButton, s)
                }
        }
    }

    private fun createUser() {
        val usersRef = db.collection(getString(R.string.collection_ref_users))

        val currentEmail = mAuth.currentUser?.email
        val data = hashMapOf(
            getString(R.string.document_ref_email) to currentEmail,
            getString(R.string.document_ref_username) to nameText.text.toString(),
            getString(R.string.document_ref_profilepicture) to getString(R.string.default_profile_location)
        )
        usersRef.document().set(data)

    }


    override fun finish() {
        setResult(Activity.RESULT_OK)
        super.finish()
    }

}