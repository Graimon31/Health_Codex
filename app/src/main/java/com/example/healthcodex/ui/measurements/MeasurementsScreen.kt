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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.healthcodex.R
import com.example.healthcodex.data.measurements.MeasurementConfidence
import com.example.healthcodex.data.measurements.MeasurementEntry
import com.example.healthcodex.data.measurements.MeasurementPeriod
import com.example.healthcodex.data.measurements.MeasurementSource
import com.example.healthcodex.data.measurements.MeasurementSourceFilter
import com.example.healthcodex.data.measurements.MeasurementType
import com.example.healthcodex.feature.measurements.MeasurementsExport
import com.example.healthcodex.ui.theme.HealthCodexTheme
import com.example.healthcodex.util.Formatters
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val InteractiveRadius = 12.dp
private val InteractiveShape = RoundedCornerShape(InteractiveRadius)

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
            onSourceChange = { viewModel.setSourceFilter(it) },
            onDeviceChange = { viewModel.setDeviceFilter(it) },
            onToggleAnomaly = { viewModel.toggleAnomalies() },
            onRangeChange = { type, min, max -> viewModel.setRange(type, min, max) },
            onClear = { viewModel.clearFilters() },
            onCustomPeriod = { start, end -> viewModel.setCustomPeriod(start, end) }
        )
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh() }
    )
    val screenBackground = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.background
    } else {
        Color(0xFFF9FAFB)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBackground)
            .padding(paddingValues),
        containerColor = screenBackground,
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
                backgroundColor = screenBackground,
                onPeriodSelected = { viewModel.setPeriod(it) },
                onTypeToggle = { viewModel.toggleType(it) },
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
    bleDeviceName: String?
) {
    Column {
        LargeTopAppBar(
            title = { Text(text = stringResource(id = R.string.measurements_title)) },
            colors = TopAppBarDefaults.largeTopAppBarColors(),
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
    backgroundColor: Color,
    onPeriodSelected: (MeasurementPeriod) -> Unit,
    onTypeToggle: (MeasurementType) -> Unit,
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
        stickyHeader {
            Surface(color = backgroundColor) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                PeriodSelector(
                    selected = uiState.filter.period,
                    onPeriodSelected = onPeriodSelected
                )
                TypeSelector(
                    selectedTypes = uiState.filter.selectedTypes,
                    onTypeToggle = onTypeToggle
                )
            }
        }
        }
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
                            fontWeight = FontWeight.SemiBold
                        )
                        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
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
            ExtendedFloatingActionButton(
                text = { Text(label) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                },
                onClick = onStartMeasurement,
                shape = InteractiveShape,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        ExtendedFloatingActionButton(
            text = { Text(text = stringResource(id = R.string.measure_action_add_manual)) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            },
            onClick = onAddManual,
            shape = InteractiveShape,
            modifier = Modifier.sizeIn(minWidth = 56.dp, minHeight = 56.dp)
        )
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
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(text = stringResource(id = R.string.measure_filters_period))
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .padding(top = 8.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            periods.forEachIndexed { index, period ->
                SegmentedButton(
                    selected = selected::class == period::class,
                    onClick = { onPeriodSelected(period) },
                    shape = SegmentedButtonDefaults.itemShape(index, periods.size, baseShape = InteractiveShape),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primary,
                        activeContentColor = MaterialTheme.colorScheme.onPrimary,
                        activeBorderColor = MaterialTheme.colorScheme.primary,
                        inactiveContainerColor = Color.Transparent,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                        inactiveBorderColor = MaterialTheme.colorScheme.outline
                    )
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
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primary,
                    activeContentColor = MaterialTheme.colorScheme.onPrimary,
                    activeBorderColor = MaterialTheme.colorScheme.primary,
                    inactiveContainerColor = Color.Transparent,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                    inactiveBorderColor = MaterialTheme.colorScheme.outline
                )
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
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
private fun MeasurementTypeChip(
    type: MeasurementType,
    selected: Boolean,
    onToggle: (MeasurementType) -> Unit
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val stateText = if (selected) {
        stringResource(id = R.string.measure_type_selected_state)
    } else {
        stringResource(id = R.string.measure_type_unselected_state)
    }

    Surface(
        shape = InteractiveShape,
        color = background,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = if (selected) 2.dp else 0.dp,
        onClick = { onToggle(type) },
        modifier = Modifier.semantics {
            role = Role.Checkbox
            this.selected = selected
            stateDescription = stateText
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummarySection(
    summary: com.example.healthcodex.data.measurements.MeasurementSummary,
    modifier: Modifier = Modifier
) {
    val noData = stringResource(id = R.string.measure_summary_no_data)
    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle(text = stringResource(id = R.string.measure_summary_title))
        FlowRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 2
        ) {
            SummaryTile(
                title = stringResource(id = R.string.measure_summary_hr),
                value = summary.averageHr?.let { value ->
                    stringResource(id = R.string.measure_summary_unit_bpm, value.roundToInt())
                } ?: noData,
                color = Color(0xFFEF4444),
                icon = Icons.Default.Favorite
            )
            SummaryTile(
                title = stringResource(id = R.string.measure_summary_steps),
                value = summary.steps?.let { count ->
                    stringResource(
                        id = R.string.measure_summary_unit_steps,
                        Formatters.formatInt(count)
                    )
                } ?: noData,
                color = Color(0xFF10B981),
                icon = Icons.AutoMirrored.Filled.DirectionsWalk
            )
            SummaryTile(
                title = stringResource(id = R.string.measure_summary_calories),
                value = summary.calories?.let { total ->
                    stringResource(
                        id = R.string.measure_summary_unit_calories,
                        Formatters.formatInt(total)
                    )
                } ?: noData,
                color = Color(0xFFFB923C),
                icon = Icons.Default.LocalFireDepartment
            )
            SummaryTile(
                title = stringResource(id = R.string.measure_summary_pressure),
                value = summary.pressure?.let { pair ->
                    stringResource(
                        id = R.string.measure_summary_unit_pressure,
                        pair.first.roundToInt(),
                        pair.second.roundToInt()
                    )
                } ?: noData,
                color = Color(0xFF6366F1),
                icon = Icons.Default.Bloodtype
            )
            SummaryTile(
                title = stringResource(id = R.string.measure_summary_weight_delta),
                value = summary.weightDelta?.let { delta ->
                    stringResource(id = R.string.measure_summary_unit_weight_delta, delta)
                } ?: noData,
                color = Color(0xFF3B82F6),
                icon = Icons.Default.MonitorWeight
            )
            SummaryTile(
                title = stringResource(id = R.string.measure_summary_spo2),
                value = summary.averageSpo2?.let { spo ->
                    stringResource(id = R.string.measure_summary_unit_spo2, spo.roundToInt())
                } ?: noData,
                color = Color(0xFF14B8A6),
                icon = Icons.Default.FavoriteBorder
            )
            SummaryTile(
                title = stringResource(id = R.string.measure_summary_sleep),
                value = summary.sleepMinutes?.let { Formatters.formatDurationMinutes(it) } ?: noData,
                color = Color(0xFF6366F1),
                icon = Icons.Default.Bedtime
            )
            SummaryTile(
                title = stringResource(id = R.string.measure_summary_respiratory),
                value = summary.respiratoryRate?.let { rate ->
                    stringResource(id = R.string.measure_summary_unit_respiratory, rate.roundToInt())
                } ?: noData,
                color = Color(0xFF22C55E),
                icon = Icons.Default.Air
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SummaryTile(title: String, value: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ElevatedCard(
        modifier = Modifier.widthIn(min = 160.dp, max = 240.dp),
        shape = InteractiveShape,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
    Card(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { menuExpanded = true }
            ),
        shape = InteractiveShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.SelfImprovement,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (isSearch) stringResource(id = R.string.measure_empty_search) else stringResource(id = R.string.measure_empty_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = stringResource(id = R.string.measure_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasurementsFilterSheet(
    state: MeasurementsUiState,
    onDismiss: () -> Unit,
    onSourceChange: (MeasurementSourceFilter) -> Unit,
    onDeviceChange: (String?) -> Unit,
    onToggleAnomaly: () -> Unit,
    onRangeChange: (MeasurementType, Double?, Double?) -> Unit,
    onClear: () -> Unit,
    onCustomPeriod: (LocalDate, LocalDate) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val ranges = state.filter.ranges
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionTitle(text = stringResource(id = R.string.measure_sheet_title))
            SectionTitle(text = stringResource(id = R.string.measure_filters_period))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(
                    onClick = {
                        val custom = state.filter.period as? MeasurementPeriod.Custom
                        val start = custom?.start ?: LocalDate.now().minusDays(6)
                        val end = custom?.end ?: LocalDate.now()
                        onCustomPeriod(start, end)
                    },
                    shape = InteractiveShape
                ) {
                    Text(text = stringResource(id = R.string.measure_sheet_custom_period))
                }
            }
            SectionTitle(text = stringResource(id = R.string.measure_sheet_source))
            val sourceFilter = state.filter.source
            val selectedSource = (sourceFilter as? MeasurementSourceFilter.Only)?.source
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = sourceFilter is MeasurementSourceFilter.All,
                    onClick = { onSourceChange(MeasurementSourceFilter.All) },
                    label = { Text(text = stringResource(id = R.string.measure_sheet_source_all)) },
                    shape = InteractiveShape
                )
                FilterChip(
                    selected = selectedSource == MeasurementSource.DEVICE,
                    onClick = { onSourceChange(MeasurementSourceFilter.Only(MeasurementSource.DEVICE)) },
                    label = { Text(text = stringResource(id = R.string.measure_sheet_source_device)) },
                    shape = InteractiveShape
                )
                FilterChip(
                    selected = selectedSource == MeasurementSource.MANUAL,
                    onClick = { onSourceChange(MeasurementSourceFilter.Only(MeasurementSource.MANUAL)) },
                    label = { Text(text = stringResource(id = R.string.measure_sheet_source_manual)) },
                    shape = InteractiveShape
                )
            }
            if (state.availableDevices.isNotEmpty()) {
                SectionTitle(text = stringResource(id = R.string.measure_sheet_device))
                var expanded by remember { mutableStateOf(false) }
                val selectedDevice = state.filter.deviceName.orEmpty()
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = selectedDevice,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(id = R.string.measure_sheet_select_device)) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selectedDevice.isNotEmpty()) {
                                IconButton(onClick = {
                                    onDeviceChange(null)
                                    expanded = false
                                }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(id = R.string.measure_sheet_device_all))
                                }
                            }
                            IconButton(onClick = { expanded = true }) {
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.measure_sheet_device_all)) },
                        onClick = {
                            onDeviceChange(null)
                            expanded = false
                        }
                    )
                    state.availableDevices.forEach { device ->
                        DropdownMenuItem(
                            text = { Text(device) },
                            onClick = {
                                onDeviceChange(device)
                                expanded = false
                            }
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = state.filter.onlyAnomalies, onCheckedChange = { onToggleAnomaly() })
                SupportLabel(text = stringResource(id = R.string.measure_sheet_anomaly))
            }
            SectionTitle(text = stringResource(id = R.string.measure_sheet_range_title))
            MeasurementType.values().forEach { type ->
                val range = ranges[type]
                var minText by rememberSaveable("min_${type.name}") { mutableStateOf(range?.min?.toString().orEmpty()) }
                var maxText by rememberSaveable("max_${type.name}") { mutableStateOf(range?.max?.toString().orEmpty()) }
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
                    title = stringResource(id = type.titleRes),
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
            HorizontalDivider()
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onClear, shape = InteractiveShape) { Text(text = stringResource(id = R.string.measure_sheet_clear)) }
                TextButton(onClick = onDismiss, shape = InteractiveShape) { Text(text = stringResource(id = R.string.measure_sheet_close)) }
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
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
            OutlinedTextField(
                value = minValue,
                onValueChange = onMinChange,
                label = { Text(stringResource(id = R.string.measure_sheet_range_min)) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = maxValue,
                onValueChange = onMaxChange,
                label = { Text(stringResource(id = R.string.measure_sheet_range_max)) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
            )
        }
    }
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
        }
    )
}

@Composable
private fun EditorContent(form: MeasurementForm, onUpdate: ((MeasurementForm) -> MeasurementForm) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    value = form.durationMinutes,
                    onValueChange = { value -> onUpdate { it.copy(durationMinutes = value) } },
                    label = { Text(stringResource(id = R.string.measure_duration_label)) },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
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
            value = form.deviceName,
            onValueChange = { value -> onUpdate { it.copy(deviceName = value) } },
            label = { Text(stringResource(id = R.string.measure_device_name)) }
        )
        OutlinedTextField(
            value = form.deviceAddress,
            onValueChange = { value -> onUpdate { it.copy(deviceAddress = value) } },
            label = { Text(stringResource(id = R.string.measure_device_address)) }
        )
        OutlinedTextField(
            value = form.note,
            onValueChange = { value -> onUpdate { it.copy(note = value) } },
            label = { Text(stringResource(id = R.string.measure_note_label)) },
            minLines = 2
        )
        OutlinedTextField(
            value = form.tags,
            onValueChange = { value -> onUpdate { it.copy(tags = value) } },
            label = { Text(stringResource(id = R.string.measure_tags_label)) }
        )
    }
}

@Composable
private fun NumericField(value: String, onChange: (String) -> Unit, label: String) {
    OutlinedTextField(
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

@Composable
private fun SourceSelector(selected: MeasurementSource, onSelected: (MeasurementSource) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(id = R.string.measure_source_label))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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

@Composable
private fun ConfidenceSelector(selected: MeasurementConfidence, onSelected: (MeasurementConfidence) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(id = R.string.measure_confidence_label))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
