package com.example.restaurantreviewsapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateField.average
import com.google.firebase.firestore.AggregateField.count
import com.google.firebase.firestore.AggregateField.sum
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.math.BigDecimal
import java.math.RoundingMode

class Account : AppCompatActivity() {
    private var mAuth = FirebaseAuth.getInstance()
    private var currentUser = mAuth.currentUser
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val storageRef = storage.reference
    private lateinit var  usersStorageRef : StorageReference
    private lateinit var profileButton : ImageButton

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK) {
                val imageUri : Uri? = result.data?.data
                val usersRef = db.collection(getString(R.string.collection_ref_users))
                val currentEmail = mAuth.currentUser?.email
                val query = usersRef.whereEqualTo(getString(R.string.document_ref_email), currentEmail)
                query.get()
                    .addOnSuccessListener { documents ->
                        val document = documents.documents[0]
                        val profilePath = document.get(getString(R.string.document_ref_profilepicture))
                        if(profilePath == getString(R.string.default_profile_location)) {

                            val pictureRef = usersStorageRef.child(
                                getString(
                                    R.string.users_storage_profile_picture_reference,
                                    currentEmail,
                                    getString(R.string.profile_picture_filename)
                                ))
                            var uploadTask = imageUri?.let { pictureRef.putFile(it) }

                        } else {
                            val pictureRef = usersStorageRef.child(getString(
                                R.string.users_storage_profile_picture_reference,
                                currentEmail,
                                getString(R.string.profile_picture_filename)
                            ))
                            pictureRef.delete()
                            var uploadTask = imageUri?.let { pictureRef.putFile(it) }
                        }
                        val newProfilePath = getString(
                            R.string.storage_user_profile_picture_new_reference,
                            currentEmail,
                            getString(R.string.profile_picture_filename)
                        )
                        document.reference.update(getString(R.string.document_ref_profilepicture), newProfilePath)
                        updateUI()
                    }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account)
        usersStorageRef = storageRef.child(getString(R.string.storage_ref_users))

        val signOutButton = findViewById<Button>(R.id.signOutButton)
        signOutButton.setOnClickListener{ _ ->
            mAuth.signOut()
            finish()
        }
        profileButton = findViewById(R.id.account_profile_picture_button)
        profileButton.setOnClickListener{ _ ->
            val photoIntent = Intent(Intent.ACTION_PICK)
            photoIntent.type = getString(R.string.implicit_image_intent)
            startForResult.launch(photoIntent)


        }
        updateUI()



        //populate activity with user details
        if(currentUser != null) {
            val currentEmail = currentUser!!.email
            val usersRef = db.collection(getString(R.string.collection_ref_users))

            val query = usersRef.whereEqualTo(getString(R.string.document_ref_email), currentEmail)
            query.get()
                .addOnSuccessListener { documents ->
                    val document = documents.documents[0]
                    val Username = findViewById<TextView>(R.id.UserName)
                    Username.text = document.get(getString(R.string.document_ref_username)).toString()
                }
                .addOnFailureListener { exception ->
                    displayMessage(profileButton, exception.toString())
                }
        }
    }
    private fun updateUI() {
        val usersRef = db.collection(getString(R.string.collection_ref_users))
        val reviewsRef = db.collection(getString(R.string.collection_ref_reviews))
        val query = usersRef.whereEqualTo(getString(R.string.document_ref_email), mAuth.currentUser?.email)
        query.get()
            .addOnSuccessListener { documents ->
                val document = documents.documents[0]
                val profilePicturePath = document.get(getString(R.string.document_ref_profilepicture))
                val imageUri = storageRef.child(profilePicturePath.toString())
                val imageView = findViewById<ImageView>(R.id.profilePicture)
                Glide.with(this)
                    .load(imageUri)
                    .into(imageView)

            }
        val q = reviewsRef.whereEqualTo(getString(R.string.document_ref_email), mAuth.currentUser?.email)
        val reviewsQuery = q.aggregate(count(), sum(getString(R.string.document_ref_likes)),
            average(getString(R.string.document_ref_overall)))
        reviewsQuery.get(AggregateSource.SERVER).addOnCompleteListener { task ->
            if(task.isSuccessful) {
                val snapshot = task.result
                val reviews = snapshot.count
                val likes = snapshot.get(sum(getString(R.string.document_ref_likes)))
                val score = snapshot.get(average(getString(R.string.document_ref_overall)))

                val TotalLikes = findViewById<TextView>(R.id.user_total_likes)
                TotalLikes.text = getString(R.string.user_total_likes, likes.toString())
                val AverageScore = findViewById<TextView>(R.id.user_avg_reviews)
                if(score == null){
                    AverageScore.text = getString(R.string.n_a)
                } else {
                    val roundedScore = BigDecimal(score).setScale(2, RoundingMode.HALF_EVEN)
                    AverageScore.text = getString(R.string.user_avg, roundedScore.toString())
                }
                val TotalReviews = findViewById<TextView>(R.id.user_total_reviews)
                TotalReviews.text = getString(R.string.user_total, reviews.toString())
            }
        }


    }

    override fun finish() {
        setResult(Activity.RESULT_OK)
        super.finish()
    }

}
