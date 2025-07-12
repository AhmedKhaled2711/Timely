package com.lee.timely.features.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ActivationViewModel : ViewModel() {
    lateinit var context: Context

    fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }
} 