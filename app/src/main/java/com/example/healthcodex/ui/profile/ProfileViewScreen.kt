// app/src/main/java/com/example/healthcodex/ui/profile/ProfileViewScreen.kt
package com.example.healthcodex.ui.profile

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Share
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.healthcodex.feature.profile.ProfileExportImport
import com.example.healthcodex.ui.theme.GlassButton
import com.example.healthcodex.ui.theme.GlassHighlight
import com.example.healthcodex.ui.theme.GlassOutlinedButton
import com.example.healthcodex.ui.theme.GlassTextPrimary
import com.example.healthcodex.ui.theme.GlassTextSecondary
import com.example.healthcodex.ui.theme.LiquidGlassSurface
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
        when (effect) {
            is ProfileEvent.ProfileSaved -> {
                snackbarHostState.showSnackbar("Профиль сохранён")
                viewModel.clearEffects()
            }
            is ProfileEvent.DoctorLinked -> {
                snackbarHostState.showSnackbar("Врач привязан: ${(effect as ProfileEvent.DoctorLinked).doctorName}")
                viewModel.clearEffects()
            }
            is ProfileEvent.DoctorLinkError -> {
                snackbarHostState.showSnackbar((effect as ProfileEvent.DoctorLinkError).message)
                viewModel.clearEffects()
            }
            else -> {}
        }
    }

    // Fetch profile from server on first load
    LaunchedEffect(Unit) {
        viewModel.fetchProfileFromServer()
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
        topBar = {
            TopAppBar(
                title = { Text("Профиль", color = GlassTextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
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
                DoctorLinkCard(
                    doctorName = profile.doctorName,
                    isLinking = effect is ProfileEvent.DoctorLinking,
                    onLink = { code -> viewModel.linkDoctor(code) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                DeviceCard(profile, onChange = { showBleDialog = true })
                Spacer(modifier = Modifier.height(12.dp))
                PrivacyCard(profile)
                Spacer(modifier = Modifier.height(20.dp))
                // Action buttons
                GlassButton(
                    onClick = { navController.navigate(ProfileNav.edit) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Редактировать", color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
                GlassOutlinedButton(
                    onClick = { navController.navigate(ProfileNav.security) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Безопасность", color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    GlassOutlinedButton(
                        onClick = { ProfileExportImport.shareIce(context, profile) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ICE", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    GlassOutlinedButton(
                        onClick = { ProfileExportImport.exportProfile(context, profile) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Экспорт", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    GlassOutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Импорт", color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
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
    LiquidGlassSurface(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Профиль не заполнен",
                style = MaterialTheme.typography.titleMedium,
                color = GlassTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Добавьте данные для персонализированного мониторинга",
                color = GlassTextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            GlassButton(onClick = onFill) {
                Text("Заполнить профиль", color = Color.White)
            }
        }
    }
}

@Composable
private fun BiometricLockCard(onUnlock: () -> Unit) {
    LiquidGlassSurface(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Профиль защищён",
                style = MaterialTheme.typography.titleMedium,
                color = GlassTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Для просмотра требуется биометрия. Нажмите, чтобы выполнить stub-аутентификацию.",
                color = GlassTextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            GlassButton(onClick = onUnlock) {
                Text("Разблокировать", color = Color.White)
            }
        }
    }
}

@Composable
private fun ProfileSummaryCard(state: ProfileViewState) {
    val profile = state.profile ?: return
    LiquidGlassSurface(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = profile.fullName.ifBlank { "Без имени" },
                style = MaterialTheme.typography.titleMedium,
                color = GlassTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Возраст: ${state.age ?: "—"}", color = GlassTextSecondary)
            Text("Пол: ${profile.sex}", color = GlassTextSecondary)
            Text("Рост: ${profile.heightCm?.let { "$it см" } ?: "—"}", color = GlassTextSecondary)
            Text("Вес: ${profile.weightKg?.let { String.format("%.1f кг", it) } ?: "—"}", color = GlassTextSecondary)
            Text("BMI: ${state.bmi}", color = GlassTextSecondary)
            profile.birthDate?.let {
                Text("Дата рождения: ${Formatters.formatDate(it)}", color = GlassTextSecondary)
            }
        }
    }
}

@Composable
private fun MedicalCard(profile: com.example.healthcodex.data.profile.UserProfile) {
    LiquidGlassSurface(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Медицинский статус", style = MaterialTheme.typography.titleMedium, color = GlassTextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Диагнозы: ${profile.conditions.joinToString().ifBlank { "нет" }}", color = GlassTextSecondary)
            Text("Аллергии: ${profile.allergies.joinToString().ifBlank { "нет" }}", color = GlassTextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Медикаменты:", fontWeight = FontWeight.Medium, color = GlassTextPrimary)
            if (profile.medications.isEmpty()) {
                Text("Не указаны", color = GlassTextSecondary)
            } else {
                profile.medications.forEach { med ->
                    Text("• ${med.name} — ${med.dose} (${med.scheduleNote})", color = GlassTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun BaselineCard(profile: com.example.healthcodex.data.profile.UserProfile) {
    LiquidGlassSurface(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Нормы и пороги", style = MaterialTheme.typography.titleMedium, color = GlassTextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Пульс в покое: ${profile.restingHr ?: "—"}", color = GlassTextSecondary)
            Text("САД базовое: ${profile.bpBaselineSystolic ?: "—"}", color = GlassTextSecondary)
            Text("ДАД базовое: ${profile.bpBaselineDiastolic ?: "—"}", color = GlassTextSecondary)
            Text("Порог HR: ${profile.hrHigh ?: "—"}", color = GlassTextSecondary)
            Text("Порог САД: ${profile.bpSysHigh ?: "—"}", color = GlassTextSecondary)
            Text("Порог ДАД: ${profile.bpDiaHigh ?: "—"}", color = GlassTextSecondary)
        }
    }
}

@Composable
private fun ContactsCard(profile: com.example.healthcodex.data.profile.UserProfile) {
    LiquidGlassSurface(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Контакты", style = MaterialTheme.typography.titleMedium, color = GlassTextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("ICE: ${profile.emergencyName.orEmpty()} ${profile.emergencyPhone.orEmpty()}", color = GlassTextSecondary)
            Text("Врач: ${profile.doctorName.orEmpty()} ${profile.doctorPhone.orEmpty()}", color = GlassTextSecondary)
        }
    }
}

@Composable
private fun DeviceCard(profile: com.example.healthcodex.data.profile.UserProfile, onChange: () -> Unit) {
    LiquidGlassSurface(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("BLE устройство", style = MaterialTheme.typography.titleMedium, color = GlassTextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Имя: ${profile.bleDeviceName ?: "Не привязано"}", color = GlassTextSecondary)
            Text("Адрес: ${profile.bleDeviceAddress ?: "—"}", color = GlassTextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            GlassOutlinedButton(onClick = onChange) {
                Icon(Icons.Default.Link, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Изменить привязку", color = Color.White)
            }
        }
    }
}

@Composable
private fun PrivacyCard(profile: com.example.healthcodex.data.profile.UserProfile) {
    LiquidGlassSurface(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Конфиденциальность", style = MaterialTheme.typography.titleMedium, color = GlassTextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Поделиться с врачом: ${if (profile.shareWithDoctor) "Да" else "Нет"}", color = GlassTextSecondary)
            Text("Согласие: ${if (profile.consentAccepted) "Принято" else "Не принято"}", color = GlassTextSecondary)
            if (profile.consentAccepted) {
                Text("Версия: ${profile.consentVersion}", color = GlassTextSecondary)
                Text("Дата: ${profile.consentTimestamp?.toString() ?: "—"}", color = GlassTextSecondary)
            }
        }
    }
}

@Composable
private fun DoctorLinkCard(
    doctorName: String?,
    isLinking: Boolean,
    onLink: (String) -> Unit
) {
    var doctorCode by remember { mutableStateOf("") }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = GlassTextPrimary,
        unfocusedTextColor = GlassTextPrimary,
        focusedBorderColor = GlassHighlight,
        unfocusedBorderColor = GlassTextSecondary.copy(alpha = 0.5f),
        cursorColor = GlassHighlight,
        focusedLabelColor = GlassHighlight,
        unfocusedLabelColor = GlassTextSecondary
    )

    LiquidGlassSurface(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Лечащий врач",
                style = MaterialTheme.typography.titleMedium,
                color = GlassTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (!doctorName.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = GlassHighlight)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(doctorName, color = GlassTextPrimary, fontWeight = FontWeight.Medium)
                }
            } else {
                Text("Врач не привязан", color = GlassTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = doctorCode,
                    onValueChange = { doctorCode = it.uppercase().take(6) },
                    label = { Text("Код врача") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                GlassButton(
                    onClick = {
                        if (doctorCode.isNotBlank()) onLink(doctorCode)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLinking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.Link, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Привязать врача", color = Color.White)
                    }
                }
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
