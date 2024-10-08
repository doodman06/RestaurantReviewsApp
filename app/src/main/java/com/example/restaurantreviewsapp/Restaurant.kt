package com.example.restaurantreviewsapp

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.AggregateField
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import java.math.BigDecimal
import java.math.RoundingMode

class Restaurant : AppCompatActivity() {
    private val db = Firebase.firestore
    private lateinit var reviewsRef: CollectionReference
    private var extras : Bundle? = null
    private var restName : String? = null
    private lateinit var restTitle : TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        reviewsRef = db.collection(getString(R.string.collection_ref_reviews))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.restaurant)
        extras = intent.extras
        restTitle = findViewById(R.id.restaurant_rest_name)
        if (extras != null) {
            restName = extras!!.getString(getString(R.string.extras_restname))
            restTitle.text = restName
        }

        val q = reviewsRef.whereEqualTo(getString(R.string.document_ref_restaurantname), restName)
        val restQuery = q.aggregate(AggregateField.count(), AggregateField.average(getString(R.string.document_ref_overall))
        )
        restQuery.get(AggregateSource.SERVER).addOnCompleteListener { task ->
            if(task.isSuccessful) {
                val snapshot = task.result
                val reviews = snapshot.count
                val score = snapshot.get(AggregateField.average(getString(R.string.document_ref_overall)))

                val AverageScore = findViewById<TextView>(R.id.restaurant_avg_reviews)
                if(score == null){
                    AverageScore.text = getString(R.string.n_a)
                } else {
                    val roundedScore = BigDecimal(score).setScale(2, RoundingMode.HALF_EVEN)
                    AverageScore.text = getString(R.string.user_avg, roundedScore.toString())
                }
                val TotalReviews = findViewById<TextView>(R.id.restaurant_total_reviews)
                TotalReviews.text = getString(R.string.user_total, reviews.toString())
            }
        }


        val list = ArrayList<MyReviewModel>()
        val idList = ArrayList<String>()
        val query = reviewsRef.whereEqualTo(getString(R.string.document_ref_restaurantname), restName)
        query.get().addOnSuccessListener { documents ->
            for (document in documents) {
                list.add(document.toObject<MyReviewModel>())
                idList.add(document.id)
            }
            val recyclerView = findViewById<View>(R.id.restaurant_recycler_view) as RecyclerView
            val layoutManager = LinearLayoutManager(this)
            recyclerView.layoutManager = layoutManager
            val mAdapter = RestaurantAdapter(list, idList)
            recyclerView.adapter = mAdapter

        }
    }
}