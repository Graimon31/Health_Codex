// app/src/main/java/com/example/healthcodex/ui/measurements/MeasurementsScreen.kt
package com.example.healthcodex.ui.measurements

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.ripple.rememberRipple
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.healthcodex.R
import com.example.healthcodex.ui.theme.GlassButton
import com.example.healthcodex.ui.theme.GlassTextPrimary
import com.example.healthcodex.ui.theme.GlassTextSecondary
import com.example.healthcodex.ui.theme.LiquidGlassSurface
import com.example.healthcodex.data.measurements.ConnectedDevice
import com.example.healthcodex.data.measurements.DeviceTypeFilter
import com.example.healthcodex.data.measurements.MeasurementConfidence
import com.example.healthcodex.data.measurements.MeasurementDeviceType
import com.example.healthcodex.data.measurements.MeasurementFilter
import com.example.healthcodex.data.measurements.MeasurementEntry
import com.example.healthcodex.data.measurements.MeasurementPeriod
import com.example.healthcodex.data.measurements.MeasurementSource
import com.example.healthcodex.data.measurements.MeasurementSourceFilter
import com.example.healthcodex.data.measurements.MeasurementType
import com.example.healthcodex.feature.measurements.MeasurementsExport
import com.example.healthcodex.util.Formatters
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val InteractiveRadius = 12.dp
private val InteractiveShape = RoundedCornerShape(InteractiveRadius)
private val AllMeasurementTypes = MeasurementType.values().toSet()

/**
 * Entry point composable that wires the view model with the screen.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MeasurementsRoute(navController: NavController, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val viewModel: MeasurementsViewModel = viewModel(factory = MeasurementsViewModel.factory(context))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editorState by viewModel.editorState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isSearchVisible by rememberSaveable { mutableStateOf(false) }
    var filterSheetVisible by rememberSaveable { mutableStateOf(false) }
    var exportMenuVisible by remember { mutableStateOf(false) }
    var searchQuery by rememberSaveable(uiState.filter.query) { mutableStateOf(uiState.filter.query) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MeasurementsEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    LaunchedEffect(navController) {
        val handle = navController.currentBackStackEntry?.savedStateHandle
        handle
            ?.getLiveData<String?>("measurements_device_linked")
            ?.asFlow()
            ?.collect { name ->
                if (!name.isNullOrBlank()) {
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.measure_device_connected_toast, name)
                    )
                    handle.set("measurements_device_linked", null)
                }
            }
    }

    editorState?.let { form ->
        MeasurementEditorDialog(
            form = form,
            onDismiss = { viewModel.dismissEditor() },
            onSave = { viewModel.saveMeasurement() },
            onUpdate = { viewModel.updateForm(it) }
        )
    }

    if (filterSheetVisible) {
        MeasurementsFilterSheet(
            state = uiState,
            onDismiss = { filterSheetVisible = false },
            onPeriodSelected = { viewModel.setPeriod(it) },
            onTypeToggle = { viewModel.toggleType(it) },
            onSourceChange = { viewModel.setSourceFilter(it) },
            onDeviceTypeChange = { viewModel.setDeviceTypeFilter(it) },
            onDeviceChange = { deviceId: Long? -> viewModel.setDeviceFilter(deviceId) },
            onToggleAnomaly = { viewModel.toggleAnomalies() },
            onRangeChange = { type, min, max -> viewModel.setRange(type, min, max) },
            onClear = { viewModel.clearFilters() },
            onCustomPeriod = { start, end -> viewModel.setCustomPeriod(start, end) },
            onAddDevice = {
                viewModel.showAddDeviceHint()
                filterSheetVisible = false
                navController.navigate(MeasurementsNav.devices) {
                    launchSingleTop = true
                }
            }
        )
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh() }
    )
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            MeasurementsTopBar(
                isSearchVisible = isSearchVisible,
                onToggleSearch = {
                    isSearchVisible = !isSearchVisible
                    if (!isSearchVisible) {
                        searchQuery = ""
                        viewModel.updateQuery("")
                    }
                },
                onSearchChanged = {
                    searchQuery = it
                    viewModel.updateQuery(it)
                },
                searchQuery = searchQuery,
                onShowFilters = { filterSheetVisible = true },
                onToggleExportMenu = { exportMenuVisible = !exportMenuVisible },
                onExportDismiss = { exportMenuVisible = false },
                onExportFormat = { format ->
                    MeasurementsExport.export(context, uiState.filteredEntries, format)
                    exportMenuVisible = false
                },
                exportMenuVisible = exportMenuVisible,
                bleConnected = uiState.bleConnected,
                bleDeviceName = uiState.bleDeviceName
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            MeasurementsFab(
                connected = uiState.bleConnected,
                deviceName = uiState.bleDeviceName,
                onStartMeasurement = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.measure_ble_stub)
                        )
                    }
                },
                onAddManual = {
                    viewModel.openEditorForNew(MeasurementType.HEART_RATE)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            MeasurementsList(
                uiState = uiState,
                isSearchVisible = isSearchVisible,
                onOpenFilters = { filterSheetVisible = true },
                onOpenDetail = { entry -> navController.navigate(MeasurementsNav.detail(entry.id)) },
                onEdit = { entry -> viewModel.openEditor(entry) },
                onDelete = { entry -> viewModel.delete(entry) },
                onShare = { entry -> shareMeasurement(context, entry) },
                onRetry = { viewModel.refresh() },
                onDismissError = { viewModel.dismissError() }
            )
            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        ),
        color = GlassTextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun SupportLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasurementsTopBar(
    isSearchVisible: Boolean,
    onToggleSearch: () -> Unit,
    onSearchChanged: (String) -> Unit,
    searchQuery: String,
    onShowFilters: () -> Unit,
    onToggleExportMenu: () -> Unit,
    onExportDismiss: () -> Unit,
    onExportFormat: (MeasurementsExport.Format) -> Unit,
    exportMenuVisible: Boolean,
    bleConnected: Boolean,
    bleDeviceName: String?,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets
) {
    Column {
        LargeTopAppBar(
            title = { Text(text = stringResource(id = R.string.measurements_title), color = GlassTextPrimary) },
            colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
            windowInsets = windowInsets,
            actions = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onToggleSearch) {
                        Icon(
                            imageVector = if (isSearchVisible) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = stringResource(id = R.string.measure_action_search)
                        )
                    }
                    Box {
                        IconButton(onClick = onToggleExportMenu) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = stringResource(id = R.string.measure_action_export)
                            )
                        }
                        DropdownMenu(
                            expanded = exportMenuVisible,
                            onDismissRequest = onExportDismiss
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.measure_export_json)) },
                                onClick = { onExportFormat(MeasurementsExport.Format.JSON) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.measure_export_csv)) },
                                onClick = { onExportFormat(MeasurementsExport.Format.CSV) }
                            )
                        }
                    }
                    IconButton(onClick = onShowFilters) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(id = R.string.measure_action_filters)
                        )
                    }
                    val statusColor = if (bleConnected) Color(0xFF10B981) else Color(0xFF9CA3AF)
                    BadgedBox(badge = {
                        Badge(containerColor = statusColor) {}
                    }) {
                        Icon(
                            imageVector = Icons.Default.Bluetooth,
                            tint = statusColor,
                            contentDescription = bleDeviceName ?: stringResource(id = R.string.measure_ble_status)
                        )
                    }
                }
            }
        )
        AnimatedVisibility(visible = isSearchVisible) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                value = searchQuery,
                onValueChange = onSearchChanged,
                placeholder = { Text(text = stringResource(id = R.string.measure_search_hint)) },
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(id = R.string.measure_action_clear_search)
                            )
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MeasurementsList(
    uiState: MeasurementsUiState,
    isSearchVisible: Boolean,
    onOpenFilters: () -> Unit,
    onOpenDetail: (MeasurementEntry) -> Unit,
    onEdit: (MeasurementEntry) -> Unit,
    onDelete: (MeasurementEntry) -> Unit,
    onShare: (MeasurementEntry) -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit
) {
    val listState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        uiState.errorMessage?.let { message ->
            item {
                MeasurementsErrorCard(
                    message = message,
                    onRetry = onRetry,
                    onDismiss = onDismissError
                )
            }
        }
        item {
            FilterSummaryRow(
                filter = uiState.filter,
                connectedDevices = uiState.connectedDevices,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                onOpenFilters = onOpenFilters
            )
        }
        item {
            SummarySection(
                summary = uiState.summary,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp, bottom = 16.dp)
            )
        }
        if (uiState.groups.isEmpty() && !uiState.isLoading) {
            item {
                MeasurementsEmptyState(isSearch = isSearchVisible || uiState.filter.query.isNotBlank())
            }
        } else {
            uiState.groups.forEach { group ->
                item(key = "header_${group.date}") {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            text = Formatters.formatHeaderDate(group.date),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = GlassTextPrimary
                        )
                        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = Color(0x33FFFFFF))
                    }
                }
                items(group.entries, key = { it.id }) { entry ->
                    MeasurementCard(
                        entry = entry,
                        onClick = { onOpenDetail(entry) },
                        onEdit = { onEdit(entry) },
                        onDelete = { onDelete(entry) },
                        onShare = { onShare(entry) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MeasurementsErrorCard(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = InteractiveShape
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Column {
                    Text(
                        text = stringResource(id = R.string.measure_error_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDismiss, shape = InteractiveShape) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(id = R.string.measure_error_dismiss))
                }
                TextButton(onClick = onRetry, shape = InteractiveShape) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(id = R.string.measure_error_retry))
                }
            }
        }
    }
}

@Composable
private fun FilterSummaryRow(
    filter: MeasurementFilter,
    connectedDevices: List<ConnectedDevice>,
    modifier: Modifier = Modifier,
    onOpenFilters: () -> Unit
) {
    val scrollState = rememberScrollState()
    val rangeCount = filter.ranges.values.count { it.min != null || it.max != null }
    val deviceLabel = filter.deviceId?.let { id ->
        connectedDevices.firstOrNull { it.id == id }?.name
    } ?: filter.deviceName
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionTitle(text = stringResource(id = R.string.measure_filter_summary_title))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = onOpenFilters,
                label = {
                    Text(
                        text = stringResource(
                            id = R.string.measure_filter_summary_period,
                            periodSummaryLabel(filter.period)
                        )
                    )
                },
                leadingIcon = { Icon(imageVector = Icons.Default.Schedule, contentDescription = null) },
                shape = InteractiveShape
            )
            val typeLabel = when {
                filter.selectedTypes.isEmpty() -> stringResource(id = R.string.measure_filter_summary_types_none)
                filter.selectedTypes.size == AllMeasurementTypes.size -> stringResource(id = R.string.measure_filter_summary_types_all)
                else -> stringResource(id = R.string.measure_filter_summary_types_count, filter.selectedTypes.size)
            }
            AssistChip(
                onClick = onOpenFilters,
                label = { Text(typeLabel) },
                leadingIcon = { Icon(imageVector = Icons.Default.Check, contentDescription = null) },
                shape = InteractiveShape
            )
            when (filter.deviceType) {
                DeviceTypeFilter.All -> Unit
                DeviceTypeFilter.Wearable -> AssistChip(
                    onClick = onOpenFilters,
                    label = { Text(stringResource(id = R.string.measure_filter_summary_wearable)) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Watch, contentDescription = null) },
                    shape = InteractiveShape
                )
                DeviceTypeFilter.NonWearable -> AssistChip(
                    onClick = onOpenFilters,
                    label = { Text(stringResource(id = R.string.measure_filter_summary_non_wearable)) },
                    leadingIcon = { Icon(imageVector = Icons.Default.DevicesOther, contentDescription = null) },
                    shape = InteractiveShape
                )
            }
            when (val source = filter.source) {
                is MeasurementSourceFilter.Only -> {
                    val (label, icon) = if (source.source == MeasurementSource.DEVICE) {
                        stringResource(id = R.string.measure_filter_summary_source_device) to Icons.Default.Bluetooth
                    } else {
                        stringResource(id = R.string.measure_filter_summary_source_manual) to Icons.Default.Edit
                    }
                    AssistChip(
                        onClick = onOpenFilters,
                        label = { Text(label) },
                        leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
                        shape = InteractiveShape
                    )
                }
                else -> Unit
            }
            deviceLabel?.takeIf { it.isNotBlank() }?.let { name ->
                AssistChip(
                    onClick = onOpenFilters,
                    label = { Text(stringResource(id = R.string.measure_filter_summary_device, name)) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Bluetooth, contentDescription = null) },
                    shape = InteractiveShape
                )
            }
            if (filter.onlyAnomalies) {
                AssistChip(
                    onClick = onOpenFilters,
                    label = { Text(stringResource(id = R.string.measure_filter_summary_anomaly)) },
                    leadingIcon = { Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null) },
                    shape = InteractiveShape
                )
            }
            if (rangeCount > 0) {
                AssistChip(
                    onClick = onOpenFilters,
                    label = { Text(stringResource(id = R.string.measure_filter_summary_ranges, rangeCount)) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Tune, contentDescription = null) },
                    shape = InteractiveShape
                )
            }
            if (filter.query.isNotBlank()) {
                val trimmedQuery = filter.query.trim()
                val displayQuery = if (trimmedQuery.length > 20) {
                    trimmedQuery.take(20) + "…"
                } else {
                    trimmedQuery
                }
                AssistChip(
                    onClick = onOpenFilters,
                    label = { Text(stringResource(id = R.string.measure_filter_summary_query, displayQuery)) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    shape = InteractiveShape
                )
            }
            AssistChip(
                onClick = onOpenFilters,
                label = { Text(stringResource(id = R.string.measure_filter_summary_open)) },
                leadingIcon = { Icon(imageVector = Icons.Default.FilterList, contentDescription = null) },
                shape = InteractiveShape
            )
            if (connectedDevices.isEmpty() && filter.deviceId == null) {
                AssistChip(
                    onClick = onOpenFilters,
                    label = { Text(stringResource(id = R.string.measure_filter_summary_add_device)) },
                    leadingIcon = { Icon(imageVector = Icons.Default.DevicesOther, contentDescription = null) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        leadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = InteractiveShape
                )
            }
        }
    }
}

@Composable
private fun periodSummaryLabel(period: MeasurementPeriod): String = when (period) {
    MeasurementPeriod.Today -> stringResource(id = R.string.measure_period_today)
    MeasurementPeriod.Week -> stringResource(id = R.string.measure_period_week)
    MeasurementPeriod.Month -> stringResource(id = R.string.measure_period_month)
    is MeasurementPeriod.Custom -> stringResource(
        id = R.string.measure_filter_summary_custom_period,
        Formatters.formatDate(period.start),
        Formatters.formatDate(period.end)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasurementsFab(
    connected: Boolean,
    deviceName: String?,
    onStartMeasurement: () -> Unit,
    onAddManual: () -> Unit
) {
    Column(horizontalAlignment = Alignment.End) {
        if (connected) {
            val label = deviceName?.takeIf { it.isNotBlank() }?.let {
                stringResource(id = R.string.measure_action_start_measurement_device, it)
            } ?: stringResource(id = R.string.measure_action_start_measurement)
            GlassButton(
                onClick = onStartMeasurement,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(label, color = Color.White)
            }
        }
        GlassButton(
            onClick = onAddManual
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(id = R.string.measure_action_add_manual), color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSelector(selected: MeasurementPeriod, onPeriodSelected: (MeasurementPeriod) -> Unit) {
    val periods = listOf(
        MeasurementPeriod.Today,
        MeasurementPeriod.Week,
        MeasurementPeriod.Month
    )
    val segmentedColors = SegmentedButtonDefaults.colors(
        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
        activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        activeBorderColor = MaterialTheme.colorScheme.primary,
        inactiveContainerColor = Color.Transparent,
        inactiveContentColor = MaterialTheme.colorScheme.onSurface,
        inactiveBorderColor = MaterialTheme.colorScheme.outline
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(text = stringResource(id = R.string.measure_filters_period))
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .padding(top = 6.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            periods.forEachIndexed { index, period ->
                SegmentedButton(
                    selected = selected::class == period::class,
                    onClick = { onPeriodSelected(period) },
                    shape = SegmentedButtonDefaults.itemShape(index, periods.size, baseShape = InteractiveShape),
                    colors = segmentedColors
                ) {
                    Text(
                        text = stringResource(id = period.labelRes),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            SegmentedButton(
                selected = selected is MeasurementPeriod.Custom,
                onClick = {
                    val custom = selected as? MeasurementPeriod.Custom
                    val start = custom?.start ?: LocalDate.now().minusDays(6)
                    val end = custom?.end ?: LocalDate.now()
                    onPeriodSelected(MeasurementPeriod.Custom(start, end))
                },
                shape = SegmentedButtonDefaults.itemShape(periods.size, periods.size + 1, baseShape = InteractiveShape),
                colors = segmentedColors
            ) {
                Text(
                    text = stringResource(id = R.string.measure_period_custom),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TypeSelector(selectedTypes: Set<MeasurementType>, onTypeToggle: (MeasurementType) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(text = stringResource(id = R.string.measure_filters_type))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth()
        ) {
            MeasurementType.values().forEach { type ->
                val selected = selectedTypes.contains(type)
                MeasurementTypeChip(
                    type = type,
                    selected = selected,
                    onToggle = onTypeToggle
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceTypeSelector(selected: DeviceTypeFilter, onSelected: (DeviceTypeFilter) -> Unit) {
    val entries = listOf(
        DeviceTypeFilter.All to stringResource(id = R.string.measure_sheet_device_type_all),
        DeviceTypeFilter.Wearable to stringResource(id = R.string.measure_sheet_device_type_wearable),
        DeviceTypeFilter.NonWearable to stringResource(id = R.string.measure_sheet_device_type_non_wearable)
    )
    val segmentedColors = SegmentedButtonDefaults.colors(
        activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        activeBorderColor = MaterialTheme.colorScheme.secondary,
        inactiveContainerColor = Color.Transparent,
        inactiveContentColor = MaterialTheme.colorScheme.onSurface,
        inactiveBorderColor = MaterialTheme.colorScheme.outline
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(text = stringResource(id = R.string.measure_sheet_device_type_title))
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .padding(top = 6.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            entries.forEachIndexed { index, (value, label) ->
                SegmentedButton(
                    selected = selected::class == value::class,
                    onClick = { onSelected(value) },
                    shape = SegmentedButtonDefaults.itemShape(index, entries.size, baseShape = InteractiveShape),
                    colors = segmentedColors
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasurementTypeChip(
    type: MeasurementType,
    selected: Boolean,
    onToggle: (MeasurementType) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val background = if (selected) colors.secondaryContainer else colors.surface
    val contentColor = if (selected) colors.onSecondaryContainer else colors.onSurface
    val borderColor = if (selected) colors.secondary else colors.outline
    val stateText = if (selected) {
        stringResource(id = R.string.measure_type_selected_state)
    } else {
        stringResource(id = R.string.measure_type_unselected_state)
    }

    Surface(
        onClick = { onToggle(type) },
        modifier = Modifier.semantics {
            this.role = Role.Checkbox
            this.selected = selected
            stateDescription = stateText
        },
        shape = InteractiveShape,
        color = background,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 40.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selected) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
            }
            Text(
                text = stringResource(id = type.titleRes),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SheetToggleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val background = if (selected) colors.secondaryContainer else colors.surface
    val content = if (selected) colors.onSecondaryContainer else colors.onSurface
    val borderColor = if (selected) colors.secondary else colors.outline
    val stateText = if (selected) {
        stringResource(id = R.string.measure_sheet_chip_selected)
    } else {
        stringResource(id = R.string.measure_sheet_chip_unselected)
    }
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .heightIn(min = 44.dp)
            .clip(InteractiveShape)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true),
                role = Role.Button,
                onClick = onClick
            )
            .semantics {
                this.role = Role.Button
                this.selected = selected
                stateDescription = stateText
            },
        shape = InteractiveShape,
        color = background,
        contentColor = content,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = if (selected) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummarySection(
    summary: com.example.healthcodex.data.measurements.MeasurementSummary,
    modifier: Modifier = Modifier
) {
    val noData = stringResource(id = R.string.measure_summary_no_data)
    val items = listOf(
        SummaryTileData(
            title = stringResource(id = R.string.measure_summary_hr),
            value = summary.averageHr?.let { value ->
                stringResource(id = R.string.measure_summary_unit_bpm, value.roundToInt())
            } ?: noData,
            hasData = summary.averageHr != null,
            icon = Icons.Default.Favorite,
            accentColor = MaterialTheme.colorScheme.error
        ),
        SummaryTileData(
            title = stringResource(id = R.string.measure_summary_steps),
            value = summary.steps?.let { count ->
                stringResource(
                    id = R.string.measure_summary_unit_steps,
                    Formatters.formatInt(count)
                )
            } ?: noData,
            hasData = summary.steps != null,
            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
            accentColor = MaterialTheme.colorScheme.secondary
        ),
        SummaryTileData(
            title = stringResource(id = R.string.measure_summary_calories),
            value = summary.calories?.let { total ->
                stringResource(
                    id = R.string.measure_summary_unit_calories,
                    Formatters.formatInt(total)
                )
            } ?: noData,
            hasData = summary.calories != null,
            icon = Icons.Default.LocalFireDepartment,
            accentColor = MaterialTheme.colorScheme.tertiary
        ),
        SummaryTileData(
            title = stringResource(id = R.string.measure_summary_pressure),
            value = summary.pressure?.let { pair ->
                stringResource(
                    id = R.string.measure_summary_unit_pressure,
                    pair.first.roundToInt(),
                    pair.second.roundToInt()
                )
            } ?: noData,
            hasData = summary.pressure != null,
            icon = Icons.Default.Bloodtype,
            accentColor = MaterialTheme.colorScheme.primary
        ),
        SummaryTileData(
            title = stringResource(id = R.string.measure_summary_weight_delta),
            value = summary.weightDelta?.let { delta ->
                stringResource(id = R.string.measure_summary_unit_weight_delta, delta)
            } ?: noData,
            hasData = summary.weightDelta != null,
            icon = Icons.Default.MonitorWeight,
            accentColor = MaterialTheme.colorScheme.primary
        ),
        SummaryTileData(
            title = stringResource(id = R.string.measure_summary_spo2),
            value = summary.averageSpo2?.let { spo ->
                stringResource(id = R.string.measure_summary_unit_spo2, spo.roundToInt())
            } ?: noData,
            hasData = summary.averageSpo2 != null,
            icon = Icons.Default.FavoriteBorder,
            accentColor = MaterialTheme.colorScheme.secondary
        ),
        SummaryTileData(
            title = stringResource(id = R.string.measure_summary_sleep),
            value = summary.sleepMinutes?.let { Formatters.formatDurationMinutes(it) } ?: noData,
            hasData = summary.sleepMinutes != null,
            icon = Icons.Default.Bedtime,
            accentColor = MaterialTheme.colorScheme.tertiary
        ),
        SummaryTileData(
            title = stringResource(id = R.string.measure_summary_respiratory),
            value = summary.respiratoryRate?.let { rate ->
                stringResource(id = R.string.measure_summary_unit_respiratory, rate.roundToInt())
            } ?: noData,
            hasData = summary.respiratoryRate != null,
            icon = Icons.Default.Air,
            accentColor = MaterialTheme.colorScheme.secondary
        )
    )

    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle(text = stringResource(id = R.string.measure_summary_title))
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val spacing = 8.dp
            val columns = if (maxWidth < 360.dp) 1 else 2
            val itemWidth = remember(maxWidth, columns) {
                if (columns == 1) maxWidth else (maxWidth - spacing) / columns
            }
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalArrangement = Arrangement.spacedBy(spacing),
                maxItemsInEachRow = columns
            ) {
                items.forEach { tile ->
                    SummaryTile(
                        modifier = Modifier.width(itemWidth),
                        title = tile.title,
                        value = tile.value,
                        icon = tile.icon,
                        accentColor = tile.accentColor,
                        hasData = tile.hasData
                    )
                }
            }
        }
    }
}

private data class SummaryTileData(
    val title: String,
    val value: String,
    val hasData: Boolean,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accentColor: Color
)

@Composable
private fun SummaryTile(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    hasData: Boolean,
    modifier: Modifier = Modifier
) {
    val iconTint = if (hasData) accentColor else Color.White.copy(alpha = 0.5f)
    val valueColor = if (hasData) accentColor else GlassTextSecondary

    LiquidGlassSurface(
        modifier = modifier.heightIn(min = 60.dp),
        cornerRadius = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = GlassTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = valueColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun MeasurementCard(
    entry: MeasurementEntry,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    LiquidGlassSurface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { menuExpanded = true }
            ),
        cornerRadius = 12.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(measurementColor(entry.type).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = measurementIcon(entry.type),
                        contentDescription = null,
                        tint = measurementColor(entry.type)
                    )
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(
                        text = stringResource(id = entry.type.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = Formatters.formatInstant(entry.timestamp),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        AssistChip(onClick = {}, label = {
                            Text(
                                text = stringResource(
                                    id = when (entry.source) {
                                        MeasurementSource.DEVICE -> R.string.measure_source_device
                                        MeasurementSource.MANUAL -> R.string.measure_source_manual
                                    }
                                )
                            )
                        }, shape = InteractiveShape)
                        AssistChip(onClick = {}, label = {
                            Text(text = stringResource(id = entry.confidence.titleRes))
                        }, shape = InteractiveShape)
                        if (!entry.deviceName.isNullOrBlank() || entry.deviceType != null) {
                            DeviceBadge(
                                label = entry.deviceName ?: stringResource(id = R.string.measure_sheet_device_all),
                                type = entry.deviceType
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatPrimaryValue(entry),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    entry.details.secondaryValue?.let {
                        Text(
                            text = formatSecondaryValue(entry),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                }
            }
            entry.note?.let { note ->
                if (note.isNotBlank()) {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            if (entry.tags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    entry.tags.forEach { tag ->
                        AssistChip(onClick = {}, label = { Text(tag) }, shape = InteractiveShape)
                    }
                }
            }
        }
    }
    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.measure_action_edit)) },
            onClick = {
                menuExpanded = false
                onEdit()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.measure_action_share)) },
            onClick = {
                menuExpanded = false
                onShare()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.measure_action_delete)) },
            onClick = {
                menuExpanded = false
                confirmDelete = true
            }
        )
    }
    if (confirmDelete) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(id = R.string.measure_delete_confirm_title)) },
            text = { Text(stringResource(id = R.string.measure_delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    },
                    shape = InteractiveShape
                ) {
                    Text(text = stringResource(id = R.string.measure_delete_confirm_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }, shape = InteractiveShape) {
                    Text(text = stringResource(id = R.string.measure_delete_confirm_cancel))
                }
            }
        )
    }
}

@Composable
private fun measurementIcon(type: MeasurementType) = when (type) {
    MeasurementType.HEART_RATE -> Icons.Default.Favorite
    MeasurementType.STEPS -> Icons.AutoMirrored.Filled.DirectionsWalk
    MeasurementType.CALORIES -> Icons.Default.LocalFireDepartment
    MeasurementType.BLOOD_PRESSURE -> Icons.Default.Bloodtype
    MeasurementType.WEIGHT -> Icons.Default.MonitorWeight
    MeasurementType.OXYGEN -> Icons.Default.FavoriteBorder
    MeasurementType.SLEEP -> Icons.Default.Bedtime
    MeasurementType.RESPIRATORY -> Icons.Default.Air
}

@Composable
private fun measurementColor(type: MeasurementType) = when (type) {
    MeasurementType.HEART_RATE -> Color(0xFFEF4444)
    MeasurementType.STEPS -> Color(0xFF10B981)
    MeasurementType.CALORIES -> Color(0xFFFB923C)
    MeasurementType.BLOOD_PRESSURE -> Color(0xFF6366F1)
    MeasurementType.WEIGHT -> Color(0xFF3B82F6)
    MeasurementType.OXYGEN -> Color(0xFF14B8A6)
    MeasurementType.SLEEP -> Color(0xFF6366F1)
    MeasurementType.RESPIRATORY -> Color(0xFF22C55E)
}

@Composable
private fun formatPrimaryValue(entry: MeasurementEntry): String = when (entry.type) {
    MeasurementType.HEART_RATE -> entry.details.primaryValue?.let { String.format("%.0f", it) + " уд/мин" } ?: "—"
    MeasurementType.STEPS -> Formatters.formatInt(entry.details.primaryValue?.toLong() ?: 0L)
    MeasurementType.CALORIES -> Formatters.formatInt(entry.details.primaryValue?.toLong() ?: 0L) + " ккал"
    MeasurementType.BLOOD_PRESSURE -> entry.details.primaryValue?.let { it.toInt().toString() } ?: "—"
    MeasurementType.WEIGHT -> entry.details.primaryValue?.let { String.format("%.1f кг", it) } ?: "—"
    MeasurementType.OXYGEN -> entry.details.primaryValue?.let { String.format("%.0f %%", it) } ?: "—"
    MeasurementType.SLEEP -> entry.details.durationMinutes?.let { Formatters.formatDurationMinutes(it) } ?: "—"
    MeasurementType.RESPIRATORY -> entry.details.primaryValue?.let { String.format("%.0f вдох/мин", it) } ?: (entry.details.statusText ?: "—")
}

@Composable
private fun formatSecondaryValue(entry: MeasurementEntry): String = when (entry.type) {
    MeasurementType.BLOOD_PRESSURE -> entry.details.secondaryValue?.let { "${entry.details.primaryValue?.toInt()}/${it.toInt()} мм рт. ст." } ?: ""
    else -> ""
}

@Composable
private fun MeasurementsEmptyState(isSearch: Boolean) {
    LiquidGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SelfImprovement,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = if (isSearch) stringResource(id = R.string.measure_empty_search) else stringResource(id = R.string.measure_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = GlassTextPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = stringResource(id = R.string.measure_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = GlassTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasurementsFilterSheet(
    state: MeasurementsUiState,
    onDismiss: () -> Unit,
    onPeriodSelected: (MeasurementPeriod) -> Unit,
    onTypeToggle: (MeasurementType) -> Unit,
    onSourceChange: (MeasurementSourceFilter) -> Unit,
    onDeviceTypeChange: (DeviceTypeFilter) -> Unit,
    onDeviceChange: (Long?) -> Unit,
    onToggleAnomaly: () -> Unit,
    onRangeChange: (MeasurementType, Double?, Double?) -> Unit,
    onClear: () -> Unit,
    onCustomPeriod: (LocalDate, LocalDate) -> Unit,
    onAddDevice: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val ranges = state.filter.ranges
    val filter = state.filter
    val customPeriod = filter.period as? MeasurementPeriod.Custom
    val availableDevices = state.connectedDevices
    val listState = rememberLazyListState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Text(
                        text = stringResource(id = R.string.measure_sheet_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                item {
                    PeriodSelector(
                        selected = filter.period,
                        onPeriodSelected = {
                            onPeriodSelected(it)
                            if (it is MeasurementPeriod.Custom) {
                                onCustomPeriod(it.start, it.end)
                            }
                        }
                    )
                }
                if (customPeriod != null) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            DateField(
                                label = stringResource(id = R.string.measure_field_start_date),
                                date = customPeriod.start,
                                onDateSelected = { newStart -> onCustomPeriod(newStart, customPeriod.end) }
                            )
                            DateField(
                                label = stringResource(id = R.string.measure_field_end_date),
                                date = customPeriod.end,
                                onDateSelected = { newEnd -> onCustomPeriod(customPeriod.start, newEnd) }
                            )
                        }
                    }
                }
                item {
                    TypeSelector(
                        selectedTypes = filter.selectedTypes,
                        onTypeToggle = onTypeToggle
                    )
                }
                item {
                    DeviceTypeSelector(
                        selected = filter.deviceType,
                        onSelected = onDeviceTypeChange
                    )
                }
                item {
                    val sourceFilter = filter.source
                    val selectedSource = (sourceFilter as? MeasurementSourceFilter.Only)?.source
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionTitle(text = stringResource(id = R.string.measure_sheet_source))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SheetToggleChip(
                                modifier = Modifier.weight(1f),
                                label = stringResource(id = R.string.measure_sheet_source_all),
                                selected = sourceFilter is MeasurementSourceFilter.All,
                                onClick = { onSourceChange(MeasurementSourceFilter.All) }
                            )
                            SheetToggleChip(
                                modifier = Modifier.weight(1f),
                                label = stringResource(id = R.string.measure_sheet_source_device),
                                selected = selectedSource == MeasurementSource.DEVICE,
                                onClick = { onSourceChange(MeasurementSourceFilter.Only(MeasurementSource.DEVICE)) }
                            )
                            SheetToggleChip(
                                modifier = Modifier.weight(1f),
                                label = stringResource(id = R.string.measure_sheet_source_manual),
                                selected = selectedSource == MeasurementSource.MANUAL,
                                onClick = { onSourceChange(MeasurementSourceFilter.Only(MeasurementSource.MANUAL)) }
                            )
                        }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionTitle(text = stringResource(id = R.string.measure_sheet_anomaly_title))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = filter.onlyAnomalies,
                                onCheckedChange = { onToggleAnomaly() }
                            )
                            SupportLabel(text = stringResource(id = R.string.measure_sheet_anomaly))
                        }
                        SupportLabel(text = stringResource(id = R.string.measure_sheet_anomaly_hint))
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SectionTitle(text = stringResource(id = R.string.measure_sheet_range_title))
                        MeasurementType.values().forEach { type ->
                            val range = ranges[type]
                            val rangeTitle = when (type) {
                                MeasurementType.HEART_RATE -> stringResource(id = R.string.measure_sheet_range_pulse)
                                MeasurementType.STEPS -> stringResource(id = R.string.measure_sheet_range_steps)
                                MeasurementType.CALORIES -> stringResource(id = R.string.measure_sheet_range_calories)
                                MeasurementType.BLOOD_PRESSURE -> stringResource(id = R.string.measure_sheet_range_pressure)
                                MeasurementType.WEIGHT -> stringResource(id = R.string.measure_sheet_range_weight)
                                MeasurementType.OXYGEN -> stringResource(id = R.string.measure_sheet_range_spo2)
                                MeasurementType.SLEEP -> stringResource(id = R.string.measure_sheet_range_sleep)
                                MeasurementType.RESPIRATORY -> stringResource(id = R.string.measure_sheet_range_resp)
                            }
                            var minText by rememberSaveable(key = "min_${type.name}") {
                                mutableStateOf(range?.min?.toString().orEmpty())
                            }
                            var maxText by rememberSaveable(key = "max_${type.name}") {
                                mutableStateOf(range?.max?.toString().orEmpty())
                            }
                            LaunchedEffect(range?.min) {
                                val formatted = range?.min?.toString().orEmpty()
                                if (formatted != minText) {
                                    minText = formatted
                                }
                            }
                            LaunchedEffect(range?.max) {
                                val formatted = range?.max?.toString().orEmpty()
                                if (formatted != maxText) {
                                    maxText = formatted
                                }
                            }
                            RangeRow(
                                title = rangeTitle,
                                minValue = minText,
                                maxValue = maxText,
                                onMinChange = {
                                    minText = it
                                    val normalized = it.replace(',', '.')
                                    if (normalized.isBlank() || normalized.toDoubleOrNull() != null) {
                                        val maxNormalized = maxText.replace(',', '.').toDoubleOrNull()
                                        onRangeChange(type, normalized.toDoubleOrNull(), maxNormalized)
                                    }
                                },
                                onMaxChange = {
                                    maxText = it
                                    val normalized = it.replace(',', '.')
                                    if (normalized.isBlank() || normalized.toDoubleOrNull() != null) {
                                        val minNormalized = minText.replace(',', '.').toDoubleOrNull()
                                        onRangeChange(type, minNormalized, normalized.toDoubleOrNull())
                                    }
                                }
                            )
                        }
                    }
                }
                item {
                    BluetoothDevicesSection(
                        devices = availableDevices,
                        selectedId = filter.deviceId,
                        selectedName = filter.deviceName,
                        deviceFilter = filter.deviceType,
                        sourceFilter = filter.source,
                        onDeviceSelected = onDeviceChange,
                        onAddDevice = {
                            onAddDevice()
                            onDismiss()
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onClear,
                    shape = InteractiveShape,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(id = R.string.measure_sheet_clear))
                }
                Button(
                    onClick = onDismiss,
                    shape = InteractiveShape,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(id = R.string.measure_sheet_apply))
                }
            }
        }
    }
}

@Composable
private fun RangeRow(
    title: String,
    minValue: String,
    maxValue: String,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = InteractiveShape,
        color = colors.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.measure_sheet_range_min),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = minValue,
                        onValueChange = onMinChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.measure_sheet_range_max),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = maxValue,
                        onValueChange = onMaxChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
                    )
                }
            }
        }
    }
}

@Composable
private fun BluetoothDevicesSection(
    devices: List<ConnectedDevice>,
    selectedId: Long?,
    selectedName: String?,
    deviceFilter: DeviceTypeFilter,
    sourceFilter: MeasurementSourceFilter,
    onDeviceSelected: (Long?) -> Unit,
    onAddDevice: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle(text = stringResource(id = R.string.measure_sheet_bt_title))
        SupportLabel(text = stringResource(id = R.string.measure_sheet_bt_subtitle))
        SheetToggleChip(
            label = stringResource(id = R.string.measure_sheet_device_all),
            selected = selectedId == null,
            onClick = { onDeviceSelected(null) },
            modifier = Modifier.fillMaxWidth()
        )
        val baseDevices = if (sourceFilter is MeasurementSourceFilter.Only && sourceFilter.source == MeasurementSource.DEVICE) {
            devices.filter { it.status.isConnected }
        } else {
            devices
        }
        val filteredDevices = baseDevices.filter { it.matchesFilter(deviceFilter) }
        if (filteredDevices.isEmpty()) {
            Surface(
                shape = InteractiveShape,
                tonalElevation = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SupportLabel(text = stringResource(id = R.string.measure_sheet_bt_placeholder))
                    Button(onClick = onAddDevice, shape = InteractiveShape) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(id = R.string.measure_sheet_bt_add))
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredDevices.forEach { device ->
                    val isSelected = selectedId == device.id || (selectedId == null && selectedName == device.name)
                    BluetoothDeviceRow(
                        device = device,
                        selected = isSelected,
                        onClick = { onDeviceSelected(device.id) }
                    )
                }
                Button(onClick = onAddDevice, shape = InteractiveShape) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(id = R.string.measure_sheet_bt_add))
                }
            }
        }
    }
}

@Composable
private fun BluetoothDeviceRow(device: ConnectedDevice, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val background = if (selected) colors.primary.copy(alpha = 0.08f) else colors.surface
    val borderColor = if (selected) colors.primary else colors.outline
    val statusTint = if (device.status.isConnected) colors.secondary else colors.onSurfaceVariant
    val statusText = stringResource(id = device.status.titleRes)
    val lastSyncText = device.lastSync?.let {
        stringResource(id = R.string.measure_sheet_bt_last_sync, Formatters.formatInstant(it))
    }
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(InteractiveShape)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true),
                role = Role.RadioButton,
                onClick = onClick
            ),
        shape = InteractiveShape,
        color = background,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val icon = if (device.type == MeasurementDeviceType.WEARABLE) {
                Icons.Default.Watch
            } else {
                Icons.Default.DevicesOther
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) colors.primary else colors.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (device.status.isConnected) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = statusTint,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = buildString {
                            append(statusText)
                            if (!lastSyncText.isNullOrBlank()) {
                                append(" · ")
                                append(lastSyncText)
                            }
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = statusTint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(id = R.string.measure_sheet_chip_selected),
                    tint = colors.primary
                )
            }
        }
    }
}

private fun ConnectedDevice.matchesFilter(filter: DeviceTypeFilter): Boolean = when (filter) {
    DeviceTypeFilter.All -> true
    DeviceTypeFilter.Wearable -> type == MeasurementDeviceType.WEARABLE
    DeviceTypeFilter.NonWearable -> type == MeasurementDeviceType.NON_WEARABLE
}

@Composable
private fun DeviceBadge(label: String, type: MeasurementDeviceType?) {
    val icon = when (type) {
        MeasurementDeviceType.WEARABLE -> Icons.Default.Watch
        MeasurementDeviceType.NON_WEARABLE -> Icons.Default.DevicesOther
        null -> Icons.Default.Bluetooth
    }
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
        shape = InteractiveShape
    )
}

@Composable
private fun MeasurementEditorDialog(
    form: MeasurementForm,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onUpdate: ((MeasurementForm) -> MeasurementForm) -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onSave, shape = InteractiveShape) { Text(text = stringResource(id = R.string.measure_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = InteractiveShape) { Text(text = stringResource(id = R.string.measure_cancel)) }
        },
        title = {
            Text(
                text = if (form.id == 0L) stringResource(id = R.string.measure_editor_title_new) else stringResource(id = R.string.measure_editor_title_edit)
            )
        },
        text = {
            EditorContent(form = form, onUpdate = onUpdate)
        },
        shape = InteractiveShape
    )
}

@Composable
private fun EditorContent(form: MeasurementForm, onUpdate: ((MeasurementForm) -> MeasurementForm) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MeasurementTypeDropdown(form.type) { newType ->
            onUpdate { it.copy(type = newType) }
        }
        DateField(
            label = stringResource(id = R.string.measure_field_date),
            date = form.date,
            onDateSelected = { newDate -> onUpdate { it.copy(date = newDate) } }
        )
        TimeField(
            label = stringResource(id = R.string.measure_field_time),
            time = form.time,
            onTimeSelected = { newTime -> onUpdate { it.copy(time = newTime) } }
        )
        when (form.type) {
            MeasurementType.SLEEP -> {
                DateField(
                    label = stringResource(id = R.string.measure_field_start_date),
                    date = form.startDate ?: form.date,
                    onDateSelected = { newDate -> onUpdate { it.copy(startDate = newDate) } }
                )
                TimeField(
                    label = stringResource(id = R.string.measure_field_start_time),
                    time = form.startTime ?: LocalTime.of(23, 0),
                    onTimeSelected = { newTime -> onUpdate { it.copy(startTime = newTime) } }
                )
                DateField(
                    label = stringResource(id = R.string.measure_field_end_date),
                    date = form.endDate ?: form.date,
                    onDateSelected = { newDate -> onUpdate { it.copy(endDate = newDate) } }
                )
                TimeField(
                    label = stringResource(id = R.string.measure_field_end_time),
                    time = form.endTime ?: LocalTime.of(7, 0),
                    onTimeSelected = { newTime -> onUpdate { it.copy(endTime = newTime) } }
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.durationMinutes,
                    onValueChange = { value -> onUpdate { it.copy(durationMinutes = value) } },
                    label = { Text(stringResource(id = R.string.measure_duration_label)) },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.statusText,
                    onValueChange = { value -> onUpdate { it.copy(statusText = value) } },
                    label = { Text(stringResource(id = R.string.measure_status_label)) }
                )
            }
            MeasurementType.BLOOD_PRESSURE -> {
                NumericField(
                    value = form.primary,
                    onChange = { value -> onUpdate { it.copy(primary = value) } },
                    label = stringResource(id = R.string.measure_field_systolic)
                )
                NumericField(
                    value = form.secondary,
                    onChange = { value -> onUpdate { it.copy(secondary = value) } },
                    label = stringResource(id = R.string.measure_field_diastolic)
                )
                NumericField(
                    value = form.tertiary,
                    onChange = { value -> onUpdate { it.copy(tertiary = value) } },
                    label = stringResource(id = R.string.measure_field_pulse)
                )
            }
            MeasurementType.RESPIRATORY -> {
                NumericField(
                    value = form.primary,
                    onChange = { value -> onUpdate { it.copy(primary = value) } },
                    label = stringResource(id = R.string.measure_field_respiratory)
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.statusText,
                    onValueChange = { value -> onUpdate { it.copy(statusText = value) } },
                    label = { Text(stringResource(id = R.string.measure_status_label)) }
                )
            }
            else -> {
                NumericField(
                    value = form.primary,
                    onChange = { value -> onUpdate { it.copy(primary = value) } },
                    label = stringResource(id = R.string.measure_field_primary)
                )
                if (form.type == MeasurementType.WEIGHT) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = form.statusText,
                        onValueChange = { value -> onUpdate { it.copy(statusText = value) } },
                        label = { Text(stringResource(id = R.string.measure_status_label)) }
                    )
                }
            }
        }
        SourceSelector(form.source) { source -> onUpdate { it.copy(source = source) } }
        ConfidenceSelector(form.confidence) { conf -> onUpdate { it.copy(confidence = conf) } }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = form.deviceName,
            onValueChange = { value -> onUpdate { it.copy(deviceName = value) } },
            label = { Text(stringResource(id = R.string.measure_device_name)) }
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = form.deviceAddress,
            onValueChange = { value -> onUpdate { it.copy(deviceAddress = value) } },
            label = { Text(stringResource(id = R.string.measure_device_address)) }
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = form.note,
            onValueChange = { value -> onUpdate { it.copy(note = value) } },
            label = { Text(stringResource(id = R.string.measure_note_label)) },
            minLines = 2
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = form.tags,
            onValueChange = { value -> onUpdate { it.copy(tags = value) } },
            label = { Text(stringResource(id = R.string.measure_tags_label)) }
        )
    }
}

@Composable
private fun NumericField(value: String, onChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
    )
}

@Composable
private fun MeasurementTypeDropdown(selected: MeasurementType, onSelected: (MeasurementType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(text = stringResource(id = R.string.measure_field_type), style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = stringResource(id = selected.titleRes),
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        )
        androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            MeasurementType.values().forEach { type ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(stringResource(id = type.titleRes)) },
                    onClick = {
                        expanded = false
                        onSelected(type)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SourceSelector(selected: MeasurementSource, onSelected: (MeasurementSource) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(id = R.string.measure_source_label))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MeasurementSource.values().forEach { source ->
                AssistChip(
                    onClick = { onSelected(source) },
                    label = { Text(text = stringResource(id = source.titleRes)) },
                    leadingIcon = {
                        if (selected == source) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        }
                    },
                    shape = InteractiveShape
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConfidenceSelector(selected: MeasurementConfidence, onSelected: (MeasurementConfidence) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(id = R.string.measure_confidence_label))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MeasurementConfidence.values().forEach { confidence ->
                AssistChip(
                    onClick = { onSelected(confidence) },
                    label = { Text(text = stringResource(id = confidence.titleRes)) },
                    leadingIcon = {
                        if (selected == confidence) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        }
                    },
                    shape = InteractiveShape
                )
            }
        }
    }
}

@Composable
private fun DateField(label: String, date: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val context = LocalContext.current
    val calendar = remember(date) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, date.year)
            set(Calendar.MONTH, date.monthValue - 1)
            set(Calendar.DAY_OF_MONTH, date.dayOfMonth)
        }
    }
    OutlinedTextField(
        value = Formatters.formatDate(date),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }) {
                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
            }
        }
    )
}

@Composable
private fun TimeField(label: String, time: LocalTime, onTimeSelected: (LocalTime) -> Unit) {
    val context = LocalContext.current
    OutlinedTextField(
        value = Formatters.formatTime(time),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = {
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute -> onTimeSelected(LocalTime.of(hourOfDay, minute)) },
                    time.hour,
                    time.minute,
                    true
                ).show()
            }) {
                Icon(imageVector = Icons.Default.Schedule, contentDescription = null)
            }
        }
    )
}

private fun shareMeasurement(context: android.content.Context, entry: MeasurementEntry) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, MeasurementsExport.buildShareText(entry))
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.measure_share_title)))
}
