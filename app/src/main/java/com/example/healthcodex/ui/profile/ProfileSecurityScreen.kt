// app/src/main/java/com/example/healthcodex/ui/profile/ProfileSecurityScreen.kt
package com.example.healthcodex.ui.profile

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.healthcodex.ui.theme.GlassTextPrimary
import com.example.healthcodex.ui.theme.GlassTextSecondary
import com.example.healthcodex.ui.theme.LiquidGlassSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSecurityRoute(navController: NavController, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.factory(application))
    val state by viewModel.securityState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Безопасность профиля", color = GlassTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = GlassTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.padding(paddingValues)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize()
        ) {
            LiquidGlassSurface(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Защита профиля", style = MaterialTheme.typography.titleMedium, color = GlassTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    SecurityToggle(
                        title = "Биометрический замок",
                        description = "Включите для требования биометрии при открытии профиля",
                        checked = state.biometricLockEnabled,
                        onCheckedChange = viewModel::setBiometricLock
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LiquidGlassSurface(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Сеть", style = MaterialTheme.typography.titleMedium, color = GlassTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    SecurityToggle(
                        title = "Разрешить HTTP",
                        description = "Использовать небезопасные соединения для отладки",
                        checked = state.allowHttp,
                        onCheckedChange = viewModel::setAllowHttp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LiquidGlassSurface(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Демо режим", style = MaterialTheme.typography.titleMedium, color = GlassTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    SecurityToggle(
                        title = "Демо измерения",
                        description = "Генерировать синтетические данные для демонстрации",
                        checked = state.demoMode,
                        onCheckedChange = viewModel::setDemoMode
                    )
                }
            }
        }
    }
}

@Composable
private fun SecurityToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = GlassTextPrimary)
        Text(description, style = MaterialTheme.typography.bodySmall, color = GlassTextSecondary)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
