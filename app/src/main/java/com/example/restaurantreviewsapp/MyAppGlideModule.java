package com.example.restaurantreviewsapp;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;

//This Block of code is from https://github.com/firebase/snippets-android/blob/cadf308896bd0238c7214c8e68c6e02160fddc30/storage/app/src/main/java/com/google/firebase/referencecode/storage/MyAppGlideModule.java
//Used by compiler to register a Firebase Image loader for FirebaseUI
@GlideModule
public class MyAppGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        // Register FirebaseImageLoader to handle StorageReference
        registry.append(StorageReference.class, InputStream.class,
                new FirebaseImageLoader.Factory());
    }

}
