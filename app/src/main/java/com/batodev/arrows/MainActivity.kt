package com.batodev.arrows

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.batodev.arrows.navigation.RootNode
import com.batodev.arrows.ui.AppViewModel
import com.batodev.arrows.ui.DebugViewModel
import com.batodev.arrows.ui.theme.ArrowsTheme
import com.bumble.appyx.core.integration.NodeHost
import com.bumble.appyx.core.integrationpoint.ActivityIntegrationPoint
import com.bumble.appyx.core.integrationpoint.IntegrationPointProvider
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity(), IntegrationPointProvider {

    override lateinit var appyxV1IntegrationPoint: ActivityIntegrationPoint
        private set

    private val appViewModel: AppViewModel by viewModel()
    private val debugViewModel: DebugViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appyxV1IntegrationPoint = ActivityIntegrationPoint(this, savedInstanceState)
        enableEdgeToEdge()

        val application = applicationContext as ArrowsApplication
        val consentManager = application.consentManager()

        consentManager.gatherConsent(this) {
            if (consentManager.canRequestAds) {
                application.initializeAds()
            }
        }

        if (consentManager.canRequestAds) {
            application.initializeAds()
        }

        setContent {
            val currentTheme by appViewModel.theme.collectAsState()
            ArrowsTheme(themeName = currentTheme) {
                NodeHost(integrationPoint = appyxV1IntegrationPoint) { buildContext ->
                    RootNode(
                        buildContext = buildContext,
                        appViewModel = appViewModel,
                        debugViewModel = debugViewModel
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        appyxV1IntegrationPoint.onSaveInstanceState(outState)
    }
}
