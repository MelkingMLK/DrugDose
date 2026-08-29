package it.uninsubria.drugdose.model

import kotlin.math.sqrt

object DoseCalculator {

    data class CalculationResult(
        val finalDose: Double,
        val unit: String,
        val pharmaceuticalDose: Double? = null,
        val bsa: Double? = null,
        val isMaxDoseApplied: Boolean = false
    )

    /**
     * Calcola la Superficie Corporea (BSA) utilizzando la formula di Mosteller.
     * BSA = sqrt((altezza_cm * peso_kg) / 3600)
     */
    fun calculateBsa(weightKg: Double, heightCm: Double): Double {
        return sqrt((weightKg * heightCm) / 3600.0)
    }

    /**
     * Calcola la dose in base all'indicazione e ai parametri del paziente.
     */
    fun calculateDose(
        indication: Indication,
        weightKg: Double,
        heightCm: Double? = null
    ): CalculationResult {
        var rawDose = 0.0
        var bsa: Double? = null
        var isMaxDoseApplied = false

        when (indication.calculationType) {
            CalculationType.WEIGHT_BASED -> {
                rawDose = (indication.dosePerUnit ?: 0.0) * weightKg
            }
            CalculationType.BSA_BASED -> {
                if (heightCm != null) {
                    bsa = calculateBsa(weightKg, heightCm)
                    rawDose = (indication.dosePerUnit ?: 0.0) * bsa
                }
            }
            CalculationType.FIXED_DOSE -> {
                rawDose = indication.fixedDose ?: 0.0
            }
            CalculationType.WEIGHT_BRACKETS -> {
                indication.brackets?.forEach { bracket ->
                    if (weightKg >= bracket.minWeight && weightKg < bracket.maxWeight) {
                        rawDose = bracket.dose
                    }
                }
            }
        }

        // Applica il limite di dose massima se presente
        indication.maxDose?.let { max ->
            if (rawDose > max) {
                rawDose = max
                isMaxDoseApplied = true
            }
        }

        // Calcolo della forma farmaceutica (es. numero di compresse o ml)
        var pharmaceuticalDose: Double? = null
        if (indication.formMultiplier != null && indication.formMultiplier != 0.0) {
            pharmaceuticalDose = rawDose / indication.formMultiplier
        }

        // Pulizia dell'unità (es. da "mcg/kg" a "mcg")
        val baseUnit = indication.unit.split("/")[0]

        return CalculationResult(
            finalDose = rawDose,
            unit = baseUnit,
            pharmaceuticalDose = pharmaceuticalDose,
            bsa = bsa,
            isMaxDoseApplied = isMaxDoseApplied
        )
    }
}
