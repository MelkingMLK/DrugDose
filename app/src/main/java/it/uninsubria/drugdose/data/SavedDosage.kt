package it.uninsubria.drugdose.data

data class SavedDosage(
    val id: Long = 0,
    val drugName: String,
    val indication: String,
    val patientWeight: Double,
    val patientHeight: Double,
    val patientAge: Double,
    val theoreticalDose: String,
    val practicalDose: String,
    val timestamp: String
)