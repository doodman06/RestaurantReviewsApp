package com.example.restaurantreviewsapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private var mAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val accountButton = findViewById<View>(R.id.my_account_button) as ImageButton
        accountButton.setOnClickListener { _ ->
            val intent : Intent = if(mAuth.currentUser == null){
                Intent(this, Login::class.java)
            } else {
                Intent(this, Account::class.java)
            }
            startActivity(intent)
        }
        val myReviewsButton = findViewById<View>(R.id.my_reviews_button) as ImageButton
        myReviewsButton.setOnClickListener { _ ->
            val intent : Intent = if(mAuth.currentUser == null){
                Intent(this, Login::class.java)
            } else {
                Intent(this, MyReviews::class.java)
            }
            startActivity(intent)
        }
        val newReviewButton = findViewById<View>(R.id.add_review_button) as ImageButton
        newReviewButton.setOnClickListener { _ ->
            val intent : Intent = if(mAuth.currentUser == null){
                Intent(this, Login::class.java)
            } else {
                Intent(this, AddReview::class.java)
            }
            startActivity(intent)
        }
        val searchRestButton = findViewById<View>(R.id.search_restaurant_button) as ImageButton
        searchRestButton.setOnClickListener { _ ->
            val intent = Intent(this, SearchRestaurant::class.java)
            startActivity(intent)
        }
    }
}

fun displayMessage(view: View, msgTxt: String) {
    val sb = Snackbar.make(view, msgTxt, Snackbar.LENGTH_LONG)
    sb.show()
}