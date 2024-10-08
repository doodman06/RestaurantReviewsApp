package com.example.restaurantreviewsapp

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class MyReviews : AppCompatActivity() {
    private var mAuth = FirebaseAuth.getInstance()
    private var currentUser = mAuth.currentUser
    private val db = Firebase.firestore

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            recreate()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_reviews)



        val currentEmail = currentUser!!.email


        val list = ArrayList<MyReviewModel>()
        val idList = ArrayList<String>()
        val reviewsRef = db.collection(getString(R.string.collection_ref_reviews))
        val query = reviewsRef.whereEqualTo(getString(R.string.document_ref_email), currentEmail)

        query.get()
            .addOnSuccessListener {documents ->
                for(document in documents) {
                    idList.add(document.id)
                    list.add(document.toObject<MyReviewModel>())
                }
                val emptyText = findViewById<TextView>(R.id.my_reviews_empty)
                if(list.isEmpty()){
                    emptyText.visibility = View.VISIBLE
                } else {
                    emptyText.visibility = View.GONE
                }
                val recyclerView = findViewById<View>(R.id.my_reviews_recycler_view) as RecyclerView
                val layoutManager = LinearLayoutManager(this)
                recyclerView.layoutManager = layoutManager
                val mAdapter = MyReviewsAdapter(list, idList, startForResult)
                recyclerView.adapter = mAdapter
            }
            .addOnFailureListener { exception ->
                displayMessage(findViewById<View>(R.id.my_reviews_recycler_view), exception.toString())
            }


    }

}