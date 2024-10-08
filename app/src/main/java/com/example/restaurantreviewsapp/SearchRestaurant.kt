package com.example.restaurantreviewsapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SearchRestaurant : AppCompatActivity() {
    private val db = Firebase.firestore
    private lateinit var reviewsRef : CollectionReference



    private lateinit var searchButton : Button
    private lateinit var searchRestName : EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        reviewsRef = db.collection(getString(R.string.collection_ref_reviews))

        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_rest)

        searchButton = findViewById(R.id.search_rest_button)
        searchRestName = findViewById(R.id.search_rest_name)
        searchButton.setOnClickListener { _ ->
            searchRest()
        }
    }

    private fun searchRest(){
        val restName = searchRestName.text.toString()
        val list = ArrayList<String>()

        val query = reviewsRef.whereEqualTo(getString(R.string.document_ref_restaurantname), restName)
        query.get()
            .addOnSuccessListener { documents ->
                for(document in documents) {
                    list.add(document.get(getString(R.string.document_ref_restaurantname)).toString())
                }

                val recyclerView = findViewById<View>(R.id.search_rest_recycler_view) as RecyclerView
                val layoutManager = LinearLayoutManager(this)
                recyclerView.layoutManager = layoutManager
                val mAdapter = RestAdapter(list)
                recyclerView.adapter = mAdapter

            }.addOnFailureListener{ task ->
                displayMessage(searchButton, task.toString())
            }

    }
}