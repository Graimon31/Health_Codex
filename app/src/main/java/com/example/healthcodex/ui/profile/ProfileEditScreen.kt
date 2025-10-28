// app/src/main/java/com/example/healthcodex/ui/profile/ProfileEditScreen.kt
package com.example.healthcodex.ui.profile

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.healthcodex.data.profile.Sex
import com.example.healthcodex.data.profile.Units

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditRoute(navController: NavController, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.factory(application))
    val editState by viewModel.editState.collectAsState()
    val effect by viewModel.effects.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) { viewModel.startEditing() }
    LaunchedEffect(effect) {
        if (effect is ProfileEvent.ProfileSaved) {
            navController.popBackStack()
            viewModel.clearEffects()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Редактирование профиля") })
        },
        modifier = Modifier.padding(paddingValues)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            PersonalSection(editState.form, editState.errors, viewModel::updateField)
            Spacer(modifier = Modifier.height(16.dp))
            MedicalSection(editState.form, viewModel::addCondition, viewModel::removeCondition, viewModel::addAllergy, viewModel::removeAllergy, viewModel::addMedication, viewModel::updateMedication, viewModel::removeMedication)
            Spacer(modifier = Modifier.height(16.dp))
            BaselineSection(editState.form, editState.errors, viewModel::updateField)
            Spacer(modifier = Modifier.height(16.dp))
            ContactsSection(editState.form, editState.errors, viewModel::updateField)
            Spacer(modifier = Modifier.height(16.dp))
            PrivacySection(editState.form, editState.errors, viewModel::updateField)
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { viewModel.saveProfile() }, modifier = Modifier.weight(1f)) {
                    Text("Сохранить")
                }
                OutlinedButton(onClick = { viewModel.cancelEditing(); navController.popBackStack() }, modifier = Modifier.weight(1f)) {
                    Text("Отмена")
                }
            }
            editState.errors["general"]?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun PersonalSection(form: ProfileForm, errors: Map<String, String>, updateField: ((ProfileForm) -> ProfileForm) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Личные данные", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = form.fullName,
            onValueChange = { value -> updateField { form -> form.copy(fullName = value) } },
            label = { Text("ФИО") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = form.birthDate,
            onValueChange = { value -> updateField { form -> form.copy(birthDate = value) } },
            label = { Text("Дата рождения (дд.мм.гггг)") },
            supportingText = { errors["birthDate"]?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            isError = errors.containsKey("birthDate"),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        SexSelector(form.sex) { selection -> updateField { form -> form.copy(sex = selection) } }
        Spacer(modifier = Modifier.height(8.dp))
        UnitsSelector(form.units) { selection -> updateField { form -> form.copy(units = selection) } }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = form.height,
                onValueChange = { value -> updateField { form -> form.copy(height = value) } },
                label = { Text("Рост (${if (form.units == Units.METRIC) "см" else "in"})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { errors["height"]?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                isError = errors.containsKey("height"),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = form.weight,
                onValueChange = { value -> updateField { form -> form.copy(weight = value) } },
                label = { Text("Вес (${if (form.units == Units.METRIC) "кг" else "lbs"})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { errors["weight"]?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                isError = errors.containsKey("weight"),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SexSelector(selected: Sex, onSelected: (Sex) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            label = { Text("Пол") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            readOnly = true,
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Sex.values().forEach { option ->
                DropdownMenuItem(text = { Text(option.name) }, onClick = {
                    onSelected(option)
                    expanded = false
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitsSelector(selected: Units, onSelected: (Units) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            label = { Text("Единицы") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            readOnly = true,
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Units.values().forEach { option ->
                DropdownMenuItem(text = { Text(option.name) }, onClick = {
                    onSelected(option)
                    expanded = false
                })
            }
        }
    }
}

@Composable
private fun MedicalSection(
    form: ProfileForm,
    addCondition: (String) -> Unit,
    removeCondition: (Int) -> Unit,
    addAllergy: (String) -> Unit,
    removeAllergy: (Int) -> Unit,
    addMedication: () -> Unit,
    updateMedication: (Int, (MedicationForm) -> MedicationForm) -> Unit,
    removeMedication: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Медицинский статус", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        EditableChips(title = "Диагнозы", items = form.conditions, onAdd = addCondition, onRemove = removeCondition)
        Spacer(modifier = Modifier.height(8.dp))
        EditableChips(title = "Аллергии", items = form.allergies, onAdd = addAllergy, onRemove = removeAllergy)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Медикаменты", style = MaterialTheme.typography.titleSmall)
        form.medications.forEachIndexed { index, medication ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = medication.name,
                    onValueChange = { value -> updateMedication(index) { it.copy(name = value) } },
                    label = { Text("Название") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = medication.dose,
                    onValueChange = { value -> updateMedication(index) { it.copy(dose = value) } },
                    label = { Text("Дозировка") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = medication.scheduleNote,
                onValueChange = { value -> updateMedication(index) { it.copy(scheduleNote = value) } },
                label = { Text("Примечание") },
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = { removeMedication(index) }) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Удалить")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        OutlinedButton(onClick = addMedication) {
            Text("Добавить медикамент")
        }
    }
}

@Composable
private fun EditableChips(title: String, items: List<String>, onAdd: (String) -> Unit, onRemove: (Int) -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(4.dp))
        items.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item, modifier = Modifier.weight(1f))
                TextButton(onClick = { onRemove(index) }) { Text("Удалить") }
            }
        }
        var newItem by remember { mutableStateOf("") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = newItem,
                onValueChange = { newItem = it },
                label = { Text("Новое значение") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                if (newItem.isNotBlank()) {
                    onAdd(newItem)
                    newItem = ""
                }
            }) {
                Text("Добавить")
            }
        }
    }
}

@Composable
private fun BaselineSection(form: ProfileForm, errors: Map<String, String>, updateField: ((ProfileForm) -> ProfileForm) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Нормы и пороги", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        NumericField(label = "Пульс в покое", value = form.restingHr, error = errors["restingHr"], onChange = { value -> updateField { form -> form.copy(restingHr = value) } })
        NumericField(label = "САД базовое", value = form.bpBaselineSystolic, error = errors["bpBaselineSystolic"], onChange = { value -> updateField { form -> form.copy(bpBaselineSystolic = value) } })
        NumericField(label = "ДАД базовое", value = form.bpBaselineDiastolic, error = errors["bpBaselineDiastolic"], onChange = { value -> updateField { form -> form.copy(bpBaselineDiastolic = value) } })
        NumericField(label = "Порог HR", value = form.hrHigh, error = errors["hrHigh"], onChange = { value -> updateField { form -> form.copy(hrHigh = value) } })
        NumericField(label = "Порог САД", value = form.bpSysHigh, error = errors["bpSysHigh"], onChange = { value -> updateField { form -> form.copy(bpSysHigh = value) } })
        NumericField(label = "Порог ДАД", value = form.bpDiaHigh, error = errors["bpDiaHigh"], onChange = { value -> updateField { form -> form.copy(bpDiaHigh = value) } })
    }
}

@Composable
private fun NumericField(label: String, value: String, error: String?, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
        isError = error != null,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun ContactsSection(form: ProfileForm, errors: Map<String, String>, updateField: ((ProfileForm) -> ProfileForm) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Контакты", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = form.emergencyName,
            onValueChange = { value -> updateField { form -> form.copy(emergencyName = value) } },
            label = { Text("ICE имя") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = form.emergencyPhone,
            onValueChange = { value -> updateField { form -> form.copy(emergencyPhone = value) } },
            label = { Text("ICE телефон") },
            supportingText = { errors["emergencyPhone"]?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            isError = errors.containsKey("emergencyPhone"),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = { /* заглушка */ }) {
            Text("Импорт из контактов")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = form.doctorName,
            onValueChange = { value -> updateField { form -> form.copy(doctorName = value) } },
            label = { Text("Имя врача") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = form.doctorPhone,
            onValueChange = { value -> updateField { form -> form.copy(doctorPhone = value) } },
            label = { Text("Телефон врача") },
            supportingText = { errors["doctorPhone"]?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            isError = errors.containsKey("doctorPhone"),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PrivacySection(form: ProfileForm, errors: Map<String, String>, updateField: ((ProfileForm) -> ProfileForm) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Конфиденциальность", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = form.shareWithDoctor, onCheckedChange = { checked -> updateField { form -> form.copy(shareWithDoctor = checked) } })
            Spacer(modifier = Modifier.width(8.dp))
            Text("Поделиться данными с врачом")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = form.consentAccepted, onCheckedChange = { checked -> updateField { form -> form.copy(consentAccepted = checked) } })
            Spacer(modifier = Modifier.width(8.dp))
            Text("Согласие принято")
        }
        OutlinedTextField(
            value = form.consentVersion,
            onValueChange = { value -> updateField { form -> form.copy(consentVersion = value) } },
            label = { Text("Версия согласия") },
            supportingText = { errors["consentVersion"]?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            isError = errors.containsKey("consentVersion"),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
