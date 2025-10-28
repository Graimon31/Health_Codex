// app/src/main/java/com/example/healthcodex/ui/profile/ProfileNav.kt
package com.example.healthcodex.ui.profile

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

/**
 * Navigation graph for the profile feature.
 */
object ProfileNav {
    const val view = "profile/view"
    const val edit = "profile/edit"
    const val security = "profile/security"
}

fun NavGraphBuilder.ProfileNavGraph(navController: NavController, paddingValues: PaddingValues) {
    navigation(startDestination = ProfileNav.view, route = "profile") {
        composable(ProfileNav.view) {
            ProfileViewRoute(navController = navController, paddingValues = paddingValues)
        }
        composable(ProfileNav.edit) {
            ProfileEditRoute(navController = navController, paddingValues = paddingValues)
        }
        composable(ProfileNav.security) {
            ProfileSecurityRoute(navController = navController, paddingValues = paddingValues)
        }
    }
}
