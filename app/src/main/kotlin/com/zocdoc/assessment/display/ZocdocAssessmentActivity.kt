package com.zocdoc.assessment.display

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zocdoc.assessment.display.composables.ZocdocAssessmentHomeScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ZocdocAssessmentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZocdocAssessmentHomeScreen()
        }
    }
}