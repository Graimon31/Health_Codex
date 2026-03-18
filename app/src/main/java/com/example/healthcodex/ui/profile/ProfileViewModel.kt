// app/src/main/java/com/example/healthcodex/ui/profile/ProfileViewModel.kt
package com.example.healthcodex.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthcodex.HealthCodexApp
import com.example.healthcodex.data.profile.Medication
import com.example.healthcodex.data.profile.ProfileRepository
import com.example.healthcodex.data.profile.Sex
import com.example.healthcodex.data.profile.Units
import com.example.healthcodex.data.profile.UserProfile
import com.example.healthcodex.util.Formatters
import com.example.healthcodex.util.Validation
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel encapsulating profile state and editing logic.
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as? HealthCodexApp)?.profileRepository
        ?: ProfileRepository(application.applicationContext)

    private val _viewState = MutableStateFlow(ProfileViewState())
    val viewState: StateFlow<ProfileViewState> = _viewState.asStateFlow()

    private val _editState = MutableStateFlow(ProfileEditState())
    val editState: StateFlow<ProfileEditState> = _editState.asStateFlow()

    private val _securityState = MutableStateFlow(ProfileSecurityState())
    val securityState: StateFlow<ProfileSecurityState> = _securityState.asStateFlow()

    private val _effects = MutableStateFlow<ProfileEvent?>(null)
    val effects: StateFlow<ProfileEvent?> = _effects.asStateFlow()

    init {
        observeProfile()
        observeSecurity()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            repository.profileFlow.collectLatest { profile ->
                _viewState.value = _viewState.value.copy(
                    isLoading = false,
                    profile = profile,
                    bmi = Formatters.calculateBmi(profile?.heightCm, profile?.weightKg),
                    age = profile?.birthDate?.let { Validation.calculateAge(it) }
                )
            }
        }
    }

    private fun observeSecurity() {
        viewModelScope.launch {
            repository.demoMode.collectLatest { demo ->
                _securityState.value = _securityState.value.copy(demoMode = demo)
            }
        }
        viewModelScope.launch {
            repository.allowHttp.collectLatest { allow ->
                _securityState.value = _securityState.value.copy(allowHttp = allow)
            }
        }
        viewModelScope.launch {
            repository.biometricLockEnabled.collectLatest { enabled ->
                _securityState.value = _securityState.value.copy(biometricLockEnabled = enabled)
                _viewState.value = _viewState.value.copy(
                    biometricLockEnabled = enabled,
                    isAuthenticated = if (enabled) false else true
                )
            }
        }
    }

    fun clearEffects() {
        _effects.value = null
    }

    fun startEditing() {
        val current = viewState.value.profile ?: createEmptyProfile()
        _editState.value = ProfileEditState(
            form = ProfileForm.fromProfile(current),
            isDirty = false,
            errors = emptyMap()
        )
    }

    fun updateField(transform: (ProfileForm) -> ProfileForm) {
        _editState.value = _editState.value.copy(
            form = transform(_editState.value.form),
            isDirty = true
        )
    }

    fun addCondition(condition: String) = updateField { form ->
        form.copy(conditions = form.conditions + condition)
    }

    fun removeCondition(index: Int) = updateField { form ->
        form.copy(conditions = form.conditions.filterIndexed { i, _ -> i != index })
    }

    fun addAllergy(allergy: String) = updateField { form ->
        form.copy(allergies = form.allergies + allergy)
    }

    fun removeAllergy(index: Int) = updateField { form ->
        form.copy(allergies = form.allergies.filterIndexed { i, _ -> i != index })
    }

    fun addMedication() = updateField { form ->
        form.copy(medications = form.medications + MedicationForm())
    }

    fun updateMedication(index: Int, transform: (MedicationForm) -> MedicationForm) = updateField { form ->
        form.copy(
            medications = form.medications.mapIndexed { i, med ->
                if (i == index) transform(med) else med
            }
        )
    }

    fun removeMedication(index: Int) = updateField { form ->
        form.copy(medications = form.medications.filterIndexed { i, _ -> i != index })
    }

    fun cancelEditing() {
        _editState.value = ProfileEditState()
    }

    fun saveProfile() {
        val form = editState.value.form
        val currentProfile = viewState.value.profile
        val result = form.toDomain(currentProfile)
        val errors = form.validate()
        if (errors.isNotEmpty()) {
            _editState.value = _editState.value.copy(errors = errors)
            return
        }
        viewModelScope.launch {
            try {
                repository.upsertProfile(result)
                _effects.value = ProfileEvent.ProfileSaved
                cancelEditing()
            } catch (ex: IllegalArgumentException) {
                _editState.value = _editState.value.copy(
                    errors = mapOf("general" to ex.message.orEmpty())
                )
            }
        }
    }

    fun linkBleDevice(name: String, address: String) {
        viewModelScope.launch {
            repository.linkBleDevice(name, address)
        }
    }

    fun linkDoctor(code: String) {
        viewModelScope.launch {
            _effects.value = ProfileEvent.DoctorLinking
            repository.linkDoctor(code).fold(
                onSuccess = { doctorName ->
                    _effects.value = ProfileEvent.DoctorLinked(doctorName)
                },
                onFailure = { error ->
                    _effects.value = ProfileEvent.DoctorLinkError(error.message ?: "Ошибка привязки")
                }
            )
        }
    }

    fun fetchProfileFromServer() {
        viewModelScope.launch {
            repository.fetchProfileFromServer()
        }
    }

    fun setDemoMode(enabled: Boolean) {
        viewModelScope.launch { repository.setDemoMode(enabled) }
    }

    fun setAllowHttp(enabled: Boolean) {
        viewModelScope.launch { repository.setAllowHttp(enabled) }
    }

    fun setBiometricLock(enabled: Boolean) {
        viewModelScope.launch { repository.setBiometricLock(enabled) }
    }

    fun unlockProfile() {
        _viewState.value = _viewState.value.copy(isAuthenticated = true)
    }

    private fun createEmptyProfile(): UserProfile = UserProfile(
        userId = "default",
        fullName = "",
        birthDate = null,
        sex = Sex.OTHER,
        heightCm = null,
        weightKg = null,
        units = Units.METRIC,
        conditions = emptyList(),
        allergies = emptyList(),
        medications = emptyList(),
        restingHr = null,
        bpBaselineSystolic = null,
        bpBaselineDiastolic = null,
        hrHigh = null,
        bpSysHigh = null,
        bpDiaHigh = null,
        emergencyName = null,
        emergencyPhone = null,
        doctorName = null,
        doctorPhone = null,
        bleDeviceName = null,
        bleDeviceAddress = null,
        shareWithDoctor = false,
        consentAccepted = false,
        consentVersion = "1.0",
        consentTimestamp = null
    )
    companion object {
        fun factory(application: Application): androidx.lifecycle.ViewModelProvider.Factory =
            object : androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return ProfileViewModel(application) as T
                    }
                    return super.create(modelClass)
                }
            }
    }
}

/** View state for the read-only profile screen. */
data class ProfileViewState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val age: Int? = null,
    val bmi: String = "—",
    val biometricLockEnabled: Boolean = false,
    val isAuthenticated: Boolean = true
)

/** Editing state containing form data and validation errors. */
data class ProfileEditState(
    val form: ProfileForm = ProfileForm(),
    val errors: Map<String, String> = emptyMap(),
    val isDirty: Boolean = false
)

data class ProfileSecurityState(
    val demoMode: Boolean = false,
    val allowHttp: Boolean = false,
    val biometricLockEnabled: Boolean = false
)

sealed class ProfileEvent {
    data object ProfileSaved : ProfileEvent()
    data object DoctorLinking : ProfileEvent()
    data class DoctorLinked(val doctorName: String) : ProfileEvent()
    data class DoctorLinkError(val message: String) : ProfileEvent()
}

data class ProfileForm(
    val userId: String = "default",
    val fullName: String = "",
    val birthDate: String = "",
    val sex: Sex = Sex.OTHER,
    val units: Units = Units.METRIC,
    val height: String = "",
    val weight: String = "",
    val conditions: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    val medications: List<MedicationForm> = emptyList(),
    val restingHr: String = "",
    val bpBaselineSystolic: String = "",
    val bpBaselineDiastolic: String = "",
    val hrHigh: String = "",
    val bpSysHigh: String = "",
    val bpDiaHigh: String = "",
    val emergencyName: String = "",
    val emergencyPhone: String = "",
    val doctorName: String = "",
    val doctorPhone: String = "",
    val bleDeviceName: String = "",
    val bleDeviceAddress: String = "",
    val shareWithDoctor: Boolean = false,
    val consentAccepted: Boolean = false,
    val consentVersion: String = "1.0",
    val consentTimestamp: Instant? = null
) {
    fun validate(): Map<String, String> {
        val issues = mutableMapOf<String, String>()
        parseBirthDate()?.let {
            val age = Validation.calculateAge(it)
            if (age !in 0..120) issues["birthDate"] = "Возраст вне диапазона"
        }
        parseHeight()?.let {
            if (it !in 50..250) issues["height"] = "Рост 50-250"
        }
        parseWeight()?.let {
            if (it !in 20f..400f) issues["weight"] = "Вес 20-400"
        }
        listOfNotNull(
            parseInt(restingHr)?.takeIf { it !in 30..220 }?.let { "restingHr" to "Пульс 30-220" },
            parseInt(hrHigh)?.takeIf { it !in 30..220 }?.let { "hrHigh" to "Порог HR 30-220" },
            parseInt(bpBaselineSystolic)?.takeIf { it !in 70..250 }?.let { "bpBaselineSystolic" to "САД 70-250" },
            parseInt(bpBaselineDiastolic)?.takeIf { it !in 40..150 }?.let { "bpBaselineDiastolic" to "ДАД 40-150" },
            parseInt(bpSysHigh)?.takeIf { it !in 70..250 }?.let { "bpSysHigh" to "Порог САД 70-250" },
            parseInt(bpDiaHigh)?.takeIf { it !in 40..150 }?.let { "bpDiaHigh" to "Порог ДАД 40-150" }
        ).forEach { pair ->
            issues[pair.first] = pair.second
        }
        if (emergencyPhone.isNotEmpty() && emergencyPhone.length < 5) {
            issues["emergencyPhone"] = "Телефон слишком короткий"
        }
        if (doctorPhone.isNotEmpty() && doctorPhone.length < 5) {
            issues["doctorPhone"] = "Телефон слишком короткий"
        }
        if (consentAccepted && consentVersion.isBlank()) {
            issues["consentVersion"] = "Укажите версию согласия"
        }
        return issues
    }

    private fun parseInt(value: String): Int? = value.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
    private fun parseFloat(value: String): Float? = value.trim().takeIf { it.isNotEmpty() }?.toFloatOrNull()
    fun parseBirthDate(): LocalDate? = birthDate.takeIf { it.isNotBlank() }?.let {
        try {
            LocalDate.parse(it, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        } catch (_: DateTimeParseException) {
            null
        }
    }

    fun parseHeight(): Int? {
        val base = parseFloat(height)?.takeIf { it > 0f } ?: return null
        return if (units == Units.IMPERIAL) {
            (base * 2.54f).toInt()
        } else {
            base.toInt()
        }
    }

    fun parseWeight(): Float? {
        val base = parseFloat(weight)?.takeIf { it > 0f } ?: return null
        return if (units == Units.IMPERIAL) {
            base * 0.453592f
        } else {
            base
        }
    }

    fun toDomain(current: UserProfile?): UserProfile {
        val existing = current ?: UserProfile(
            userId = userId,
            fullName = fullName,
            birthDate = null,
            sex = sex,
            heightCm = null,
            weightKg = null,
            units = units,
            conditions = emptyList(),
            allergies = emptyList(),
            medications = emptyList(),
            restingHr = null,
            bpBaselineSystolic = null,
            bpBaselineDiastolic = null,
            hrHigh = null,
            bpSysHigh = null,
            bpDiaHigh = null,
            emergencyName = null,
            emergencyPhone = null,
            doctorName = null,
            doctorPhone = null,
            bleDeviceName = bleDeviceName.takeIf { it.isNotBlank() },
            bleDeviceAddress = bleDeviceAddress.takeIf { it.isNotBlank() },
            shareWithDoctor = shareWithDoctor,
            consentAccepted = consentAccepted,
            consentVersion = consentVersion,
            consentTimestamp = consentTimestamp
        )
        return existing.copy(
            userId = userId,
            fullName = fullName,
            birthDate = parseBirthDate(),
            sex = sex,
            units = units,
            heightCm = parseHeight(),
            weightKg = parseWeight(),
            conditions = conditions.filter { it.isNotBlank() },
            allergies = allergies.filter { it.isNotBlank() },
            medications = medications.filter { it.name.isNotBlank() }
                .map { Medication(name = it.name, dose = it.dose, scheduleNote = it.scheduleNote) },
            restingHr = parseInt(restingHr),
            bpBaselineSystolic = parseInt(bpBaselineSystolic),
            bpBaselineDiastolic = parseInt(bpBaselineDiastolic),
            hrHigh = parseInt(hrHigh),
            bpSysHigh = parseInt(bpSysHigh),
            bpDiaHigh = parseInt(bpDiaHigh),
            emergencyName = emergencyName.takeIf { it.isNotBlank() },
            emergencyPhone = emergencyPhone.takeIf { it.isNotBlank() },
            doctorName = doctorName.takeIf { it.isNotBlank() },
            doctorPhone = doctorPhone.takeIf { it.isNotBlank() },
            shareWithDoctor = shareWithDoctor,
            consentAccepted = consentAccepted,
            consentVersion = consentVersion,
            consentTimestamp = if (consentAccepted) consentTimestamp ?: Instant.now() else null
        )
    }

    companion object {
        fun fromProfile(profile: UserProfile): ProfileForm = ProfileForm(
            userId = profile.userId,
            fullName = profile.fullName,
            birthDate = profile.birthDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "",
            sex = profile.sex,
            units = profile.units,
            height = profile.heightCm?.let {
                if (profile.units == Units.IMPERIAL) String.format("%.1f", it / 2.54f) else it.toString()
            } ?: "",
            weight = profile.weightKg?.let {
                if (profile.units == Units.IMPERIAL) String.format("%.1f", it / 0.453592f) else it.toString()
            } ?: "",
            conditions = profile.conditions,
            allergies = profile.allergies,
            medications = profile.medications.map {
                MedicationForm(name = it.name, dose = it.dose, scheduleNote = it.scheduleNote)
            },
            restingHr = profile.restingHr?.toString() ?: "",
            bpBaselineSystolic = profile.bpBaselineSystolic?.toString() ?: "",
            bpBaselineDiastolic = profile.bpBaselineDiastolic?.toString() ?: "",
            hrHigh = profile.hrHigh?.toString() ?: "",
            bpSysHigh = profile.bpSysHigh?.toString() ?: "",
            bpDiaHigh = profile.bpDiaHigh?.toString() ?: "",
            emergencyName = profile.emergencyName.orEmpty(),
            emergencyPhone = profile.emergencyPhone.orEmpty(),
            doctorName = profile.doctorName.orEmpty(),
            doctorPhone = profile.doctorPhone.orEmpty(),
            bleDeviceName = profile.bleDeviceName.orEmpty(),
            bleDeviceAddress = profile.bleDeviceAddress.orEmpty(),
            shareWithDoctor = profile.shareWithDoctor,
            consentAccepted = profile.consentAccepted,
            consentVersion = profile.consentVersion,
            consentTimestamp = profile.consentTimestamp
        )
    }
}

data class MedicationForm(
    val name: String = "",
    val dose: String = "",
    val scheduleNote: String = ""
)
