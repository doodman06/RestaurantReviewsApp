package com.example.restaurantreviewsapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

class AddReview : AppCompatActivity() {
    private var mAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val storageRef = storage.reference
    private lateinit var restName : EditText
    private lateinit var foodRatingBar: RatingBar
    private lateinit var serviceRatingBar: RatingBar
    private lateinit var reviewContent: EditText
    private lateinit var overallRatingBar: RatingBar
    private lateinit var submitButton: Button
    private lateinit var includeImageSwitch: MaterialSwitch
    private lateinit var foodName : EditText
    private lateinit var uploadImageButton: Button
    private lateinit var includeLocationSwitch: MaterialSwitch
    private  var imageUri : Uri? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var point: GeoPoint? = null
    private val cancellationTokenSource = CancellationTokenSource()
    private var extras : Bundle? = null

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK) {
                 imageUri  = result.data?.data!!
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_review)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        extras = intent.extras

        var name : String? = null
        if(extras != null) {
            name = extras!!.getString(getString(R.string.extras_restname))
        }



        restName = findViewById(R.id.add_review_rest_name)
        foodRatingBar = findViewById(R.id.add_review_food_rating_bar)
        serviceRatingBar = findViewById(R.id.add_review_service_rating_bar)
        overallRatingBar = findViewById(R.id.add_review_overall_rating_bar)
        reviewContent = findViewById(R.id.add_review_content)
        submitButton = findViewById(R.id.add_review_submit)
        includeImageSwitch = findViewById(R.id.include_image_switch)
        foodName = findViewById(R.id.add_review_food_name)
        uploadImageButton = findViewById((R.id.add_review_upload_image))
        includeLocationSwitch= findViewById(R.id.add_review_location_switch)

        if(name != null){
            restName.setText(name)
            restName.isEnabled = false
        } else {
            restName.isEnabled = true
        }

        includeImageSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                foodName.visibility = View.VISIBLE
                uploadImageButton.visibility = View.VISIBLE

            } else {
                foodName.visibility = View.GONE
                uploadImageButton.visibility = View.GONE
            }
        }

        includeLocationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_CODE
                    )
                } else {
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationTokenSource.token)
                        .addOnCompleteListener { task: Task<Location> ->
                        val location = task.result
                        if(location == null) {
                            //cry
                        } else {
                            val lat : Double = (location.latitude )
                            val long: Double = (location.longitude )

                            point = GeoPoint(lat, long)
                        }
                    }

                }

            }
        }


        uploadImageButton.setOnClickListener { _ ->
            val photoIntent = Intent(Intent.ACTION_PICK)
            photoIntent.type = getString(R.string.implicit_image_intent)
            startForResult.launch(photoIntent)
        }

        submitButton.setOnClickListener{ _ -> submitClick()}
    }

    private fun submitClick() {
        //Basically just submit everything to database
        val reviewsRef = db.collection(getString(R.string.collection_ref_reviews))

        val currentEmail = mAuth.currentUser?.email
        val sdf = SimpleDateFormat(getString(R.string.date_format))
        val date = sdf.format(Date())

        val uniqueId = UUID.randomUUID().toString()
        var path : String? = getString(
            R.string.reviews_storage_new_image_reference,
            restName.text.toString(),
            uniqueId
        )
        val imageStorageRef  = path.let { storageRef.child(it!!) }
        var food : String? = foodName.text.toString()
        if(includeImageSwitch.isChecked && imageUri != null){
            //upload the image
            var uploadTask = imageUri?.let { imageStorageRef.putFile(it) }
        } else {
            path = null
            food = null
        }
        if(!includeLocationSwitch.isChecked) {
            point = null
        }

        var id : String? = null
        if(extras != null) {
            id = extras!!.getString(getString(R.string.extras_doc_id))
        }

       val data : HashMap<String, Any?>
        if(id == null){
            data = hashMapOf(
                getString(R.string.document_ref_email) to currentEmail,
                getString(R.string.document_ref_restaurantname) to restName.text.toString(),
                getString(R.string.document_ref_content) to reviewContent.text.toString(),
                getString(R.string.document_ref_date) to date.toString(),
                getString(R.string.document_ref_food) to foodRatingBar.rating,
                getString(R.string.document_ref_service) to serviceRatingBar.rating,
                getString(R.string.document_ref_overall) to overallRatingBar.rating,
                getString(R.string.document_ref_picture) to path,
                getString(R.string.document_ref_foodname) to food,
                getString(R.string.document_ref_geopoint) to point,
                getString(R.string.document_ref_likes) to 0,
                getString(R.string.document_ref_likedby) to arrayListOf<String>()
            )
        } else {
             data = hashMapOf(
                 getString(R.string.document_ref_email) to currentEmail,
                 getString(R.string.document_ref_restaurantname) to restName.text.toString(),
                 getString(R.string.document_ref_content) to reviewContent.text.toString(),
                 getString(R.string.document_ref_date) to date.toString(),
                 getString(R.string.document_ref_food) to foodRatingBar.rating,
                 getString(R.string.document_ref_service) to serviceRatingBar.rating,
                 getString(R.string.document_ref_overall) to overallRatingBar.rating,
                 getString(R.string.document_ref_picture) to path,
                 getString(R.string.document_ref_foodname) to food,
                 getString(R.string.document_ref_geopoint) to point,
            )

        }
        if(id == null){
            reviewsRef.document().set(data)
        } else {
            reviewsRef.document(id).update(data as Map<String, Any>)
        }

        finish()
    }

    override fun finish() {
        setResult(Activity.RESULT_OK)
        super.finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == LOCATION_PERMISSION_CODE) {
            if(!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                displayMessage(includeLocationSwitch,
                    getString(R.string.review_denied_location_permission))
                includeLocationSwitch.isChecked = false
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_CODE = 42
    }
}