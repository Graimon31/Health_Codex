// app/src/main/java/com/example/healthcodex/ui/measurements/MeasurementsNav.kt
package com.example.healthcodex.ui.measurements

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/**
 * Navigation graph for the measurements feature.
 */
object MeasurementsNav {
    const val home = "measurements"
    const val detail = "measurements/detail/{id}"
    const val devices = "measurements/devices"
    fun detail(id: Long) = "measurements/detail/$id"
}

fun NavGraphBuilder.measurementsNavGraph(
    navController: NavController,
    paddingValues: PaddingValues
) {
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
    composable(MeasurementsNav.devices) {
        DeviceConnectRoute(
            onBack = { navController.popBackStack() },
            onConnected = { deviceName ->
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "measurements_device_linked",
                    deviceName
                )
                navController.popBackStack()
            }
        )
    }
}
