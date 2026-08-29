package it.uninsubria.drugdose.model

enum class CalculationType {
    WEIGHT_BASED,
    BSA_BASED,
    FIXED_DOSE,
    WEIGHT_BRACKETS
}

data class WeightBracket(
    val minWeight: Double,
    val maxWeight: Double,
    val dose: Double
)

data class Indication(
    val name: String,
    val calculationType: CalculationType,
    val dosePerUnit: Double? = null,
    val unit: String,
    val maxDose: Double? = null,
    val fixedDose: Double? = null,
    val brackets: List<WeightBracket>? = null,
    val pharmaceuticalForm: String? = null,
    val formMultiplier: Double? = null,
    val notes: String? = null
)

data class Drug(
    val id: String,
    val name: String,
    val indications: List<Indication>
)
