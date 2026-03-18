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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.healthcodex.HealthCodexApp
import com.example.healthcodex.ui.theme.GlassHighlight
import com.example.healthcodex.ui.theme.GlassTextPrimary
import com.example.healthcodex.ui.theme.GlassTextSecondary
import com.example.healthcodex.ui.theme.LiquidGlassSurface
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSecurityRoute(navController: NavController, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val app = application as HealthCodexApp
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.factory(application))
    val state by viewModel.securityState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val currentBaseUrl by app.prefsRepository.baseUrl.collectAsStateWithLifecycle(initialValue = null)
    val loggedInUser by app.prefsRepository.loggedInUserName.collectAsStateWithLifecycle(initialValue = null)
    var serverUrlInput by rememberSaveable(currentBaseUrl) {
        mutableStateOf(currentBaseUrl ?: "")
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = GlassTextPrimary,
        unfocusedTextColor = GlassTextPrimary,
        focusedBorderColor = GlassHighlight,
        unfocusedBorderColor = GlassTextSecondary.copy(alpha = 0.5f),
        cursorColor = GlassHighlight,
        focusedLabelColor = GlassHighlight,
        unfocusedLabelColor = GlassTextSecondary
    )

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
                .verticalScroll(rememberScrollState())
        ) {
            // Server connection settings
            LiquidGlassSurface(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Сервер", style = MaterialTheme.typography.titleMedium, color = GlassTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Адрес бекенда Health Backend",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = serverUrlInput,
                        onValueChange = { serverUrlInput = it },
                        label = { Text("URL сервера") },
                        placeholder = { Text("http://192.168.0.10:8000", color = GlassTextSecondary.copy(alpha = 0.4f)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                app.prefsRepository.setBaseUrl(serverUrlInput.trim())
                            }
                        },
                        enabled = serverUrlInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlassHighlight.copy(alpha = 0.8f)
                        )
                    ) {
                        Text("Сохранить URL", color = GlassTextPrimary)
                    }
                    loggedInUser?.let { name ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Вошли как: $name",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    app.authRepository.logout()
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        ) {
                            Text("Выйти из аккаунта", color = GlassTextPrimary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

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
