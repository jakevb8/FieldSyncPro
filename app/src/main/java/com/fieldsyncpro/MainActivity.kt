package com.fieldsyncpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fieldsyncpro.presentation.ui.FieldSyncNavGraph
import com.fieldsyncpro.presentation.ui.theme.FieldSyncProTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FieldSyncProTheme {
                FieldSyncNavGraph()
            }
        }
    }
}
