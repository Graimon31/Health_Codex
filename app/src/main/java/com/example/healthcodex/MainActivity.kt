// app/src/main/java/com/example/healthcodex/MainActivity.kt
package com.example.healthcodex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import android.app.Activity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.healthcodex.R
import com.example.healthcodex.feature.forecast.ForecastRoute
import com.example.healthcodex.ui.measurements.MeasurementsNav
import com.example.healthcodex.ui.measurements.measurementsNavGraph
import com.example.healthcodex.ui.profile.ProfileNav
import com.example.healthcodex.ui.profile.ProfileNavGraph
import com.example.healthcodex.ui.theme.GlassNavItem
import com.example.healthcodex.ui.theme.GlassBottomBar
import com.example.healthcodex.ui.theme.LiquidGlassBackdrop
import com.example.healthcodex.ui.theme.HealthCodexTheme
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Main activity that hosts the application navigation graph.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    val view = LocalView.current

    // Always use dark icons=false for the cosmic dark background
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false
    }

    val measurementHome = MeasurementsNav.home
    val profileStart = ProfileNav.view

    val items = listOf(
        GlassNavItem(
            route = "forecast",
            icon = Icons.Default.Home,
            label = stringResource(R.string.forecast_tab)
        ),
        GlassNavItem(
            route = measurementHome,
            icon = Icons.Default.Favorite,
            label = stringResource(R.string.measurements_tab)
        ),
        GlassNavItem(
            route = profileStart,
            icon = Icons.Default.Person,
            label = stringResource(R.string.profile_tab)
        )
    )

    // Wrap everything in the cosmic gradient backdrop
    LiquidGlassBackdrop {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            containerColor = Color.Transparent,
            bottomBar = {
                GlassBottomBar(
                    items = items,
                    currentRoute = currentRoute,
                    onItemSelected = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                        }
                    },
                    isSelected = { item ->
                        when (item.route) {
                            "forecast" -> currentRoute == item.route
                            measurementHome -> currentRoute?.startsWith(MeasurementsNav.home) == true
                            profileStart -> currentRoute?.startsWith("profile") == true
                            else -> currentRoute == item.route
                        }
                    }
                )
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
}
