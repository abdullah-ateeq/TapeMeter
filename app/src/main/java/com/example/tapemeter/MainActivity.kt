package com.example.tapemeter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.tapemeter.data.datastore.PreferencesManager
import com.example.tapemeter.data.repository.TapeMeterRepository
import com.example.tapemeter.ui.TapeMeterApp
import com.example.tapemeter.ui.viewmodel.TapeMeterViewModel
import com.example.tapemeter.ui.viewmodel.TapeMeterViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: TapeMeterViewModel by viewModels {
        val preferencesManager = PreferencesManager(applicationContext)
        val repository = TapeMeterRepository(preferencesManager)
        TapeMeterViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TapeMeterApp(viewModel = viewModel)
        }
    }
}