// app/src/main/java/com/example/healthcodex/ui/profile/ProfileViewScreen.kt
package com.example.healthcodex.ui.profile

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.healthcodex.feature.profile.ProfileExportImport
import com.example.healthcodex.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileViewRoute(navController: NavController, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.factory(application))
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val effect by viewModel.effects.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var showBleDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(effect) {
        if (effect is ProfileEvent.ProfileSaved) {
            snackbarHostState.showSnackbar("Профиль сохранён")
            viewModel.clearEffects()
        }
    }

    val profile = state.profile
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            ProfileExportImport.importProfile(context, it) { imported ->
                imported?.let { profileResult ->
                    viewModel.updateField { _ -> ProfileForm.fromProfile(profileResult) }
                    viewModel.saveProfile()
                }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Профиль") }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = "Профиль",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (state.biometricLockEnabled && !state.isAuthenticated) {
                BiometricLockCard(onUnlock = viewModel::unlockProfile)
            } else if (profile == null) {
                EmptyProfileCard(onFill = { navController.navigate(ProfileNav.edit) })
            } else {
                ProfileSummaryCard(state)
                Spacer(modifier = Modifier.height(12.dp))
                MedicalCard(profile)
                Spacer(modifier = Modifier.height(12.dp))
                BaselineCard(profile)
                Spacer(modifier = Modifier.height(12.dp))
                ContactsCard(profile)
                Spacer(modifier = Modifier.height(12.dp))
                DeviceCard(profile, onChange = { showBleDialog = true })
                Spacer(modifier = Modifier.height(12.dp))
                PrivacyCard(profile)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate(ProfileNav.edit) }) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Редактировать")
                }
                Spacer(modifier = Modifier.height(8.dp))
                ElevatedButton(onClick = { navController.navigate(ProfileNav.security) }) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Безопасность")
                }
                Spacer(modifier = Modifier.height(8.dp))
                ElevatedButton(onClick = { ProfileExportImport.shareIce(context, profile) }) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Поделиться ICE")
                }
                Spacer(modifier = Modifier.height(8.dp))
                ElevatedButton(onClick = { ProfileExportImport.exportProfile(context, profile) }) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Экспорт в JSON")
                }
                Spacer(modifier = Modifier.height(8.dp))
                ElevatedButton(onClick = { importLauncher.launch(arrayOf("application/json")) }) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Импорт из JSON")
                }
            }
        }
    }

    if (showBleDialog) {
        BleDeviceDialog(
            onDismiss = { showBleDialog = false },
            onSelect = { name, address ->
                viewModel.linkBleDevice(name, address)
                showBleDialog = false
            }
        )
    }
}

@Composable
private fun EmptyProfileCard(onFill: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Профиль не заполнен", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Добавьте данные для персонализированного мониторинга")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onFill) {
                Text("Заполнить")
            }
        }
    }
}

@Composable
private fun BiometricLockCard(onUnlock: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Профиль защищён",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Для просмотра требуется биометрия. Нажмите, чтобы выполнить stub-аутентификацию.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onUnlock) {
                Text("Разблокировать")
            }
        }
    }
}

@Composable
private fun ProfileSummaryCard(state: ProfileViewState) {
    val profile = state.profile ?: return
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = profile.fullName.ifBlank { "Без имени" }, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Возраст: ${state.age ?: "—"}")
            Text("Пол: ${profile.sex}")
            Text("Рост: ${profile.heightCm?.let { "$it см" } ?: "—"}")
            Text("Вес: ${profile.weightKg?.let { String.format("%.1f кг", it) } ?: "—"}")
            Text("BMI: ${state.bmi}")
            profile.birthDate?.let {
                Text("Дата рождения: ${Formatters.formatDate(it)}")
            }
        }
    }
}

@Composable
private fun MedicalCard(profile: com.example.healthcodex.data.profile.UserProfile) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Медицинский статус", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Диагнозы: ${profile.conditions.joinToString().ifBlank { "нет" }}")
            Text("Аллергии: ${profile.allergies.joinToString().ifBlank { "нет" }}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Медикаменты:", fontWeight = FontWeight.Medium)
            if (profile.medications.isEmpty()) {
                Text("Не указаны")
            } else {
                profile.medications.forEach { med ->
                    Text("• ${med.name} — ${med.dose} (${med.scheduleNote})")
                }
            }
        }
    }
}

@Composable
private fun BaselineCard(profile: com.example.healthcodex.data.profile.UserProfile) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Нормы и пороги", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Пульс в покое: ${profile.restingHr ?: "—"}")
            Text("САД базовое: ${profile.bpBaselineSystolic ?: "—"}")
            Text("ДАД базовое: ${profile.bpBaselineDiastolic ?: "—"}")
            Text("Порог HR: ${profile.hrHigh ?: "—"}")
            Text("Порог САД: ${profile.bpSysHigh ?: "—"}")
            Text("Порог ДАД: ${profile.bpDiaHigh ?: "—"}")
        }
    }
}

@Composable
private fun ContactsCard(profile: com.example.healthcodex.data.profile.UserProfile) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Контакты", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("ICE: ${profile.emergencyName.orEmpty()} ${profile.emergencyPhone.orEmpty()}")
            Text("Врач: ${profile.doctorName.orEmpty()} ${profile.doctorPhone.orEmpty()}")
        }
    }
}

@Composable
private fun DeviceCard(profile: com.example.healthcodex.data.profile.UserProfile, onChange: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("BLE устройство", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Имя: ${profile.bleDeviceName ?: "Не привязано"}")
            Text("Адрес: ${profile.bleDeviceAddress ?: "—"}")
            Spacer(modifier = Modifier.height(8.dp))
            ElevatedButton(onClick = onChange) {
                Icon(Icons.Default.Link, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Изменить привязку")
            }
        }
    }
}

@Composable
private fun PrivacyCard(profile: com.example.healthcodex.data.profile.UserProfile) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Конфиденциальность", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Поделиться с врачом: ${if (profile.shareWithDoctor) "Да" else "Нет"}")
            Text("Согласие: ${if (profile.consentAccepted) "Принято" else "Не принято"}")
            if (profile.consentAccepted) {
                Text("Версия: ${profile.consentVersion}")
                Text("Дата: ${profile.consentTimestamp?.toString() ?: "—"}")
            }
        }
    }
}

@Composable
private fun BleDeviceDialog(onDismiss: () -> Unit, onSelect: (String, String) -> Unit) {
    val devices = listOf(
        "HealthBand" to "AA:BB:CC:DD:01",
        "PulseTracker" to "AA:BB:CC:DD:02"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите устройство") },
        text = {
            Column {
                devices.forEach { (name, address) ->
                    TextButton(onClick = { onSelect(name, address) }) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(name, fontWeight = FontWeight.Medium)
                            Text(address, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}
