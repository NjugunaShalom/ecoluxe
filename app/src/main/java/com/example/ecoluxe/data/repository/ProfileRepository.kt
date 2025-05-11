//package com.example.ecoluxe.data.repositories
//
//import android.content.Context
//import android.net.Uri
//import com.example.ecoluxe.data.ProductCategory
//import com.example.ecoluxe.data.models.UserAchievement
//import java.util.*
//
//interface ProfileRepository {
//    suspend fun getUserProfile(): UserProfile
//    suspend fun updateUserProfile(name: String, bio: String, role: String, imageUrl: String)
//    suspend fun uploadProfileImage(imageUri: Uri, context: Context): String
//}