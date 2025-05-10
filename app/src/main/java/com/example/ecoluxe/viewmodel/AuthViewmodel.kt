package com.example.ecoluxe.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var authState by mutableStateOf<FirebaseUser?>(null)
        private set

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = try {
                auth.signInWithEmailAndPassword(email, password).await().user
            } catch (e: Exception) {
                null
            }
            authState = user
            onResult(user != null)
        }
    }

    fun signup(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = try {
                auth.createUserWithEmailAndPassword(email, password).await().user
            } catch (e: Exception) {
                null
            }
            authState = user
            onResult(user != null)
        }
    }
}
