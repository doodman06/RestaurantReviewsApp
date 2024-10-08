package com.example.restaurantreviewsapp

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class RestaurantAdapter(private val imageModelArrayList: MutableList<MyReviewModel>, private  val idList: MutableList<String>) : RecyclerView.Adapter<RestaurantAdapter.ViewHolder>() {
    private val storage = Firebase.storage
    private val storageRef = storage.reference
    private var mAuth = FirebaseAuth.getInstance()
    /*
   * Inflate our views using the layout defined in row_layout.xml
   */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.restaurant_row_layout, parent, false)

        return ViewHolder(v)
    }

    /*
     * Bind the data to the child views of the ViewHolder
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var info = imageModelArrayList[position]
        val db = Firebase.firestore
        val id = idList[position]
        val reviewsCollectionRef = db.collection(holder.itemView.context.getString(R.string.collection_ref_reviews))
        val reviewRef = reviewsCollectionRef.document(id)


        holder.restTitle.text = holder.itemView.context.getString(R.string.review_title, info.RestaurantName.toString())
        holder.date.text = holder.itemView.context.getString(R.string.review_date, info.Date.toString())
        holder.overall.text = holder.itemView.context.getString(R.string.review_overall, info.Overall.toString())
        holder.service.text = holder.itemView.context.getString(R.string.review_service, info.Service.toString())
        holder.food.text = holder.itemView.context.getString(R.string.review_food, info.Food.toString())
        holder.review.text = holder.itemView.context.getString(R.string.review_content, info.Content.toString())
        holder.likes.text = info.Likes.toString()


        if(mAuth.currentUser != null) {
            if(info.LikedBy?.contains(mAuth.currentUser!!.email) == true) {
                val greenThumbsUp = ContextCompat.getDrawable(holder.itemView.context, R.drawable.green_round_thumb_up_24)
                holder.likeButton.setImageDrawable(greenThumbsUp)
            }
        }
        holder.likeButton.setOnClickListener { _ ->
            if(mAuth.currentUser != null) {
                if(info.LikedBy?.contains(mAuth.currentUser!!.email) == true) {
                    reviewRef.update(holder.itemView.context.getString(R.string.document_ref_likedby), FieldValue.arrayRemove(mAuth.currentUser!!.email))
                    reviewRef.update(holder.itemView.context.getString(R.string.document_ref_likes), FieldValue.increment(-1))
                    val greyThumbsUp = ContextCompat.getDrawable(holder.itemView.context, R.drawable.grey_round_thumb_up_24)
                    holder.likeButton.setImageDrawable(greyThumbsUp)

                } else {
                    reviewRef.update(holder.itemView.context.getString(R.string.document_ref_likedby), FieldValue.arrayUnion(mAuth.currentUser!!.email))
                    reviewRef.update(holder.itemView.context.getString(R.string.document_ref_likes), FieldValue.increment(1))
                    val greenThumbsUp = ContextCompat.getDrawable(holder.itemView.context, R.drawable.green_round_thumb_up_24)
                    holder.likeButton.setImageDrawable(greenThumbsUp)
                }
                reviewRef.get()
                    .addOnSuccessListener { document ->
                        if(document != null){
                            info = document.toObject<MyReviewModel>()!!
                            holder.likes.text = info.Likes.toString()
                        }
                    }
            } else {
                displayMessage(holder.likeButton,holder.itemView.context.getString(R.string.snackbar_not_logged_in_msg) )
            }
        }


        if(info.Picture != null) {
            holder.foodName.visibility = View.VISIBLE
            holder.foodImage.visibility = View.VISIBLE
            holder.foodName.text = info.FoodName.toString()
            val foodImageUri = info.Picture?.let { storageRef.child(it) }


            //load image
            Glide.with(holder.itemView.context)
                .load(foodImageUri)
                .placeholder(R.drawable.restaurant_24)
                .into(holder.foodImage)
        }

        if(info.Geopoint != null){
            holder.mapButton.visibility = View.VISIBLE
            holder.mapButton.setOnClickListener { _ ->
                val location = Uri.parse(holder.itemView.context.getString(
                    R.string.lat_long_from_geopoint,
                    info.Geopoint!!.latitude.toString(),
                    info.Geopoint!!.longitude.toString()
                ))
                val mapIntent = Intent(Intent.ACTION_VIEW, location)
                startActivity(holder.itemView.context, mapIntent, null)
            }
        }

        val usersRef = db.collection(holder.itemView.context.getString(R.string.collection_ref_users))
        val query = usersRef.whereEqualTo(holder.itemView.context.getString(R.string.document_ref_email), info.Email)
        query.get()
            .addOnSuccessListener { documents ->
                val document = documents.documents[0]
                val profilePicturePath = document.get(holder.itemView.context.getString(R.string.document_ref_profilepicture))
                val imageUri = storageRef.child(profilePicturePath.toString())
                val userName = document.get(holder.itemView.context.getString(R.string.document_ref_username))
                holder.author.text = holder.itemView.context.getString(R.string.author_name, userName)

                Glide.with(holder.itemView.context)
                    .load(imageUri)
                    .placeholder(R.drawable.person_24)
                    .into(holder.imgView)

            }



    }

    /*
     * Get the maximum size of the
     */
    override fun getItemCount(): Int {
        return imageModelArrayList.size
    }

    /*
     * The parent class that handles layout inflation and child view use
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{

        var imgView = itemView.findViewById<View>(R.id.profilePicture) as ImageView
        var restTitle = itemView.findViewById<View>(R.id.rest_title) as TextView
        var author = itemView.findViewById<View>(R.id.rest_author) as TextView
        var date = itemView.findViewById<View>(R.id.rest_post_date) as TextView
        var overall = itemView.findViewById<View>(R.id.rest_overall) as TextView
        var service = itemView.findViewById<View>(R.id.rest_service) as TextView
        var food = itemView.findViewById<View>(R.id.rest_food) as TextView
        var review = itemView.findViewById<View>(R.id.rest_review) as TextView
        var foodImage = itemView.findViewById<View>(R.id.rest_food_image) as ImageView
        var foodName = itemView.findViewById<View>(R.id.rest_food_name) as TextView
        var mapButton = itemView.findViewById<View>(R.id.rest_map_button) as ImageButton
        var likeButton = itemView.findViewById<View>(R.id.review_like_button) as ImageButton
        var likes = itemView.findViewById<View>(R.id.review_likes) as TextView

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {


        }
    }
}
