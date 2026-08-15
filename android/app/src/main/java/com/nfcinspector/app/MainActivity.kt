package com.nfcinspector.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nfcinspector.app.data.model.NfcStatus
import com.nfcinspector.app.data.model.TagRecord
import com.nfcinspector.app.nfc.NfcManager
import com.nfcinspector.app.ui.screens.*
import com.nfcinspector.app.ui.theme.NFCInspectorTheme
import com.nfcinspector.app.ui.viewmodel.MainViewModel
import com.nfcinspector.app.ui.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as NfcInspectorApplication).repository)
    }

    private lateinit var nfcManager: NfcManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcManager = NfcManager(
            context = this,
            onTagScanned = { tagRecord ->
                runOnUiThread {
                    viewModel.onTagScanned(tagRecord)
                }
            },
            onError = { errorMsg ->
                runOnUiThread {
                    viewModel.onScanError(errorMsg)
                }
            }
        )

        setContent {
            NFCInspectorTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Automatic state check on resume / returning from system settings
        val status = nfcManager.checkNfcStatus()
        viewModel.updateNfcStatus(status)

        if (status is NfcStatus.ReadyWaiting || status is NfcStatus.TagDetected) {
            nfcManager.startReaderMode(this)
        }
    }

    override fun onPause() {
        super.onPause()
        nfcManager.stopReaderMode(this)
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Reader : Screen("reader", "Leitor", Icons.Outlined.Nfc, Icons.Filled.Nfc)
    object History : Screen("history", "Histórico", Icons.Outlined.History, Icons.Filled.History)
    object Compare : Screen("compare", "Comparar", Icons.Outlined.CompareArrows, Icons.Filled.CompareArrows)
    object About : Screen("about", "Sobre", Icons.Outlined.Info, Icons.Filled.Info)
}

@Composable
fun MainAppScaffold(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var selectedReportTag by remember { mutableStateOf<TagRecord?>(null) }

    val screens = listOf(
        Screen.Reader,
        Screen.History,
        Screen.Compare,
        Screen.About
    )

    if (selectedReportTag != null) {
        ReportScreen(
            tag = selectedReportTag!!,
            viewModel = viewModel,
            onBack = { selectedReportTag = null }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    screens.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selected) screen.selectedIcon else screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Reader.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(Screen.Reader.route) {
                    ReaderScreen(
                        viewModel = viewModel,
                        onNavigateToReport = { tag -> selectedReportTag = tag }
                    )
                }
                composable(Screen.History.route) {
                    HistoryScreen(
                        viewModel = viewModel,
                        onViewReport = { tag -> selectedReportTag = tag },
                        onNavigateToCompare = {
                            navController.navigate(Screen.Compare.route)
                        }
                    )
                }
                composable(Screen.Compare.route) {
                    CompareScreen(
                        viewModel = viewModel,
                        onSelectTag = { /* slot selection modal handled inside */ }
                    )
                }
                composable(Screen.About.route) {
                    AboutScreen()
                }
            }
        }
    }
}
