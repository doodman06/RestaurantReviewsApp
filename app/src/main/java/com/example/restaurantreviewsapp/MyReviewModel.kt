package com.example.restaurantreviewsapp

import com.google.firebase.firestore.GeoPoint

data class MyReviewModel(
     val Content: String? = null,
     val Date: String? = null,
     val Email: String? = null,
     val Food: Int? = null,
     val Overall: Int? = null,
     val Service: Int? = null,
     val RestaurantName: String? = null,
     val FoodName: String? = null,
     val Picture: String? = null,
     val Geopoint: GeoPoint? = null,
     val Likes: Int? = null,
     val LikedBy: ArrayList<String>? = null
     )

