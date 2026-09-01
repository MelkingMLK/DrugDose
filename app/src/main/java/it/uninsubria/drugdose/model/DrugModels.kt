package it.uninsubria.drugdose.model

enum class CalculationType {
    PER_KG,
    PER_BSA,
    FIXED,
    WEIGHT_BRACKETS
}

data class AvailableForm(
    val formType: String,
    val strengthValue: Double,
    val strengthUnit: String,
    val isDivisible: Boolean
)

data class WeightBracket(
    val minWeightKg: Double,
    val maxWeightKg: Double,
    val doseValue: Double,
    val unit: String,
    val tabletsCount: Int
)

data class Drug(
    val id: String,
    val name: String,
    val clinicalIndication: String,
    val calculationType: CalculationType,
    val targetUnit: String,
    val unitDose: Double,
    val unitDoseOriginal: String,
    val maxTotalDose: Double,
    val minAgeYears: Double,
    val maxAgeYears: Double = 120.0, // Default a 120 anni se assente nel JSON
    val minWeightKg: Double,
    val maxWeightKg: Double,
    val availableForm: AvailableForm,
    val weightBrackets: List<WeightBracket> = emptyList(),
    val administrationInfo: String,
    val alerts: List<String>,
    val source: String
)