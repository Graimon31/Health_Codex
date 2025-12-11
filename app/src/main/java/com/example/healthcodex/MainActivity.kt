// app/src/main/java/com/example/healthcodex/MainActivity.kt
package com.example.healthcodex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.healthcodex.R
import com.example.healthcodex.feature.forecast.ForecastRoute
import com.example.healthcodex.ui.ProfileNavItem
import com.example.healthcodex.ui.measurements.MeasurementsNav
import com.example.healthcodex.ui.measurements.measurementsNavGraph
import com.example.healthcodex.ui.profile.ProfileNav
import com.example.healthcodex.ui.profile.ProfileNavGraph
import com.example.healthcodex.ui.theme.HealthCodexTheme

/**
 * Main activity that hosts the application navigation graph.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge to avoid doubled insets and white bars around the content.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            HealthCodexTheme {
                AppScaffold()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val measurementHome = MeasurementsNav.home
    val profileStart = ProfileNav.view
    val items = listOf(
        ProfileNavItem(
            route = "forecast",
            labelRes = R.string.forecast_tab,
            icon = Icons.Default.Home
        ),
        ProfileNavItem(
            route = measurementHome,
            labelRes = R.string.measurements_tab,
            icon = Icons.Default.Favorite
        ),
        ProfileNavItem(
            route = profileStart,
            labelRes = R.string.profile_tab,
            icon = Icons.Default.Person
        )
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    val isSelected = when (item.route) {
                        "forecast" -> currentRoute == item.route
                        measurementHome -> currentRoute?.startsWith(MeasurementsNav.home) == true
                        profileStart -> currentRoute?.startsWith("profile") == true
                        else -> currentRoute == item.route
                    }
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = stringResource(id = item.labelRes)) },
                        label = { Text(stringResource(id = item.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "forecast",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable("forecast") {
                ForecastRoute(
                    paddingValues = innerPadding,
                    onFillProfile = { navController.navigate(ProfileNav.edit) }
                )
            }
            measurementsNavGraph(navController, innerPadding)
            ProfileNavGraph(navController, innerPadding)
        }
    }
}
