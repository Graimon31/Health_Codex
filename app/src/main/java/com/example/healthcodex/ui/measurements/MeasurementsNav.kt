// app/src/main/java/com/example/healthcodex/ui/measurements/MeasurementsNav.kt
package com.example.healthcodex.ui.measurements

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

/**
 * Navigation graph for the measurements feature.
 */
object MeasurementsNav {
    const val home = "measurements/home"
    const val detail = "measurements/detail/{id}"
    fun detail(id: Long) = "measurements/detail/$id"
}

fun NavGraphBuilder.measurementsNavGraph(
    navController: NavController,
    paddingValues: PaddingValues
) {
    navigation(startDestination = MeasurementsNav.home, route = "measurements") {
        composable(MeasurementsNav.home) {
            MeasurementsRoute(navController = navController, paddingValues = paddingValues)
        }
        composable(MeasurementsNav.detail) { backStack ->
            val id = backStack.arguments?.getString("id")?.toLongOrNull() ?: return@composable
            MeasurementDetailRoute(
                measurementId = id,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
