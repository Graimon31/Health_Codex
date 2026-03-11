// app/src/main/java/com/example/healthcodex/ui/measurements/DeviceConnectScreen.kt
package com.example.healthcodex.ui.measurements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthcodex.R
import com.example.healthcodex.data.measurements.ConnectedDevice
import com.example.healthcodex.data.measurements.MeasurementDeviceType
import com.example.healthcodex.util.Formatters

/**
 * Entry point composable that wires navigation callbacks with the device connection UI.
 */
@Composable
fun DeviceConnectRoute(
    onBack: () -> Unit,
    onConnected: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: DeviceConnectViewModel = viewModel(factory = DeviceConnectViewModel.factory(context))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DeviceConnectEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                is DeviceConnectEvent.DeviceLinked -> onConnected(event.name)
            }
        }
    }

    DeviceConnectScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRefresh = { viewModel.refreshDiscovery() },
        onConnect = { device -> viewModel.connect(device) },
        onActivate = { device -> viewModel.activate(device) },
        onForget = { device -> viewModel.forget(device) },
        onAddManual = { name, address, type -> viewModel.addManual(name, address, type) }
    )
}

/**
 * High level screen layout for device discovery and management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceConnectScreen(
    state: DeviceConnectUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onConnect: (DiscoveredDevice) -> Unit,
    onActivate: (ConnectedDevice) -> Unit,
    onForget: (ConnectedDevice) -> Unit,
    onAddManual: (String, String?, MeasurementDeviceType) -> Unit
) {
    val manualDialogVisible = rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.measure_devices_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !state.isScanning) {
                        Icon(imageVector = Icons.Default.Bluetooth, contentDescription = stringResource(id = R.string.measure_devices_refresh))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { manualDialogVisible.value = true },
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                text = { Text(text = stringResource(id = R.string.measure_devices_add_manual)) },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.measure_devices_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                SectionCard(
                    title = stringResource(id = R.string.measure_devices_discovered),
                    isLoading = state.isScanning,
                    isEmpty = state.discovered.isEmpty(),
                    emptyMessage = stringResource(id = R.string.measure_devices_discovered_empty)
                ) {
                    state.discovered.forEach { device ->
                        DeviceRow(
                            name = device.name,
                            subtitle = device.address ?: stringResource(id = R.string.measure_devices_address_unknown),
                            icon = if (device.type == MeasurementDeviceType.WEARABLE) Icons.Default.Watch else Icons.Default.DevicesOther,
                            statusColor = MaterialTheme.colorScheme.primary,
                            trailing = {
                                Button(
                                    onClick = { onConnect(device) },
                                    enabled = !state.isProcessing,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(text = stringResource(id = R.string.measure_devices_connect))
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
            item {
                SectionCard(
                    title = stringResource(id = R.string.measure_devices_connected),
                    isLoading = false,
                    isEmpty = state.connected.isEmpty(),
                    emptyMessage = stringResource(id = R.string.measure_devices_connected_empty)
                ) {
                    state.connected.forEach { device ->
                        DeviceRow(
                            name = device.name,
                            subtitle = device.address ?: stringResource(id = R.string.measure_devices_address_unknown),
                            icon = if (device.type == MeasurementDeviceType.WEARABLE) Icons.Default.Watch else Icons.Default.DevicesOther,
                            statusColor = if (device.status.isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            secondaryText = device.lastSync?.let {
                                stringResource(id = R.string.measure_sheet_bt_last_sync, Formatters.formatInstant(it))
                            }
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onActivate(device) },
                                    enabled = !state.isProcessing,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(text = stringResource(id = R.string.measure_devices_activate))
                                }
                                TextButton(
                                    onClick = { onForget(device) },
                                    enabled = !state.isProcessing,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(text = stringResource(id = R.string.measure_devices_forget))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    if (manualDialogVisible.value) {
        AddDeviceDialog(
            onDismiss = { manualDialogVisible.value = false },
            onConfirm = { name, address, type ->
                onAddManual(name, address, type)
                manualDialogVisible.value = false
            }
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    isLoading: Boolean,
    isEmpty: Boolean,
    emptyMessage: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            when {
                isLoading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                isEmpty -> {
                    Text(
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> content()
            }
        }
    }
}

@Composable
private fun DeviceRow(
    name: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    statusColor: Color,
    secondaryText: String? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(36.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!secondaryText.isNullOrBlank()) {
                Text(
                    text = secondaryText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
private fun AddDeviceDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?, MeasurementDeviceType) -> Unit
) {
    val nameState = rememberSaveable { mutableStateOf("") }
    val addressState = rememberSaveable { mutableStateOf("") }
    val selectedType = rememberSaveable { mutableStateOf(MeasurementDeviceType.WEARABLE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.measure_devices_add_manual_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = nameState.value,
                    onValueChange = { nameState.value = it },
                    label = { Text(text = stringResource(id = R.string.measure_devices_field_name)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = addressState.value,
                    onValueChange = { addressState.value = it },
                    label = { Text(text = stringResource(id = R.string.measure_devices_field_address)) },
                    singleLine = true
                )
                DeviceTypeSelectorField(selected = selectedType)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(nameState.value.trim(), addressState.value.trim().ifBlank { null }, selectedType.value) }) {
                Text(text = stringResource(id = R.string.measure_devices_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.measure_sheet_cancel))
            }
        }
    )
}

@Composable
private fun DeviceTypeSelectorField(selected: MutableState<MeasurementDeviceType>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(id = R.string.measure_devices_field_type), style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            TypeChip(
                label = stringResource(id = R.string.measure_sheet_device_type_wearable),
                selected = selected.value == MeasurementDeviceType.WEARABLE,
                onClick = { selected.value = MeasurementDeviceType.WEARABLE }
            )
            TypeChip(
                label = stringResource(id = R.string.measure_sheet_device_type_non_wearable),
                selected = selected.value == MeasurementDeviceType.NON_WEARABLE,
                onClick = { selected.value = MeasurementDeviceType.NON_WEARABLE }
            )
        }
    }
}

@Composable
private fun TypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val background = if (selected) colors.primary else colors.surfaceVariant
    val contentColor = if (selected) colors.onPrimary else colors.onSurfaceVariant
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(
                role = Role.RadioButton,
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = contentColor)
    }
}

