package it.uninsubria.drugdose.model

import kotlin.math.round
import kotlin.math.sqrt

object DoseCalculator {

    const val MIN_WEIGHT_KG = 1.0
    const val MAX_WEIGHT_KG = 230.0
    const val MIN_HEIGHT_CM = 45.0
    const val MAX_HEIGHT_CM = 225.0
    const val MIN_AGE_YEARS = 1
    const val MAX_AGE_YEARS = 120

    data class ValidationResult(
        val isWeightValid: Boolean,
        val isHeightValid: Boolean,
        val isAgeValid: Boolean
    ) {
        val isValid: Boolean get() = isWeightValid && isHeightValid && isAgeValid
    }

    fun isValidWeight(weightKg: Double?): Boolean =
        weightKg != null && weightKg in MIN_WEIGHT_KG..MAX_WEIGHT_KG

    fun isValidHeight(heightCm: Double?): Boolean =
        heightCm != null && heightCm in MIN_HEIGHT_CM..MAX_HEIGHT_CM

    fun isValidAge(ageYears: Int?): Boolean =
        ageYears != null && ageYears in MIN_AGE_YEARS..MAX_AGE_YEARS

    fun validateInputs(weightKg: Double?, heightCm: Double?, ageYears: Int?): ValidationResult {
        return ValidationResult(
            isWeightValid = isValidWeight(weightKg),
            isHeightValid = isValidHeight(heightCm),
            isAgeValid = isValidAge(ageYears)
        )
    }

    data class CalculationResult(
        val finalDose: Double,
        val unit: String,
        val pharmaceuticalDose: Double? = null,
        val roundedPharmaceuticalDose: Double? = null,
        val bsa: Double? = null,
        val isMaxDoseApplied: Boolean = false,
        val alerts: List<String> = emptyList()
    )

    fun calculateBsa(weightKg: Double, heightCm: Double): Double {
        return sqrt((weightKg * heightCm) / 3600.0)
    }

    fun calculateDose(
        indication: Indication,
        weightKg: Double,
        heightCm: Double? = null,
        age: Int? = null
    ): CalculationResult {
        var rawDose = 0.0
        var bsa: Double? = null
        var isMaxDoseApplied = false
        val alerts = mutableListOf<String>()

        if (!isValidWeight(weightKg)) {
            alerts.add("Peso fuori dai limiti clinici consentiti ($MIN_WEIGHT_KG-$MAX_WEIGHT_KG kg)")
        }
        heightCm?.let {
            if (!isValidHeight(it)) {
                alerts.add("Altezza fuori dai limiti clinici consentiti ($MIN_HEIGHT_CM-$MAX_HEIGHT_CM cm)")
            }
        }
        age?.let {
            if (!isValidAge(it)) {
                alerts.add("Età fuori dai limiti clinici consentiti ($MIN_AGE_YEARS-$MAX_AGE_YEARS anni)")
            }
        }

        indication.minWeight?.let { if (weightKg < it) alerts.add("Peso inferiore al minimo consigliato ($it kg)") }
        indication.maxWeight?.let { if (weightKg > it) alerts.add("Peso superiore al massimo consigliato ($it kg)") }
        age?.let { a ->
            indication.minAge?.let { if (a < it) alerts.add("Età inferiore al minimo consigliato ($it anni)") }
            indication.maxAge?.let { if (a > it) alerts.add("Età superiore al massimo consigliato ($it anni)") }
        }


        when (indication.calculationType) {
            CalculationType.WEIGHT_BASED -> {
                rawDose = (indication.dosePerUnit ?: 0.0) * weightKg
            }
            CalculationType.BSA_BASED -> {
                if (heightCm != null) {
                    bsa = calculateBsa(weightKg, heightCm)
                    rawDose = (indication.dosePerUnit ?: 0.0) * bsa
                } else {
                    alerts.add("Altezza mancante per calcolo basato su BSA")
                }
            }
            CalculationType.FIXED_DOSE -> {
                rawDose = indication.fixedDose ?: 0.0
            }
            CalculationType.WEIGHT_BRACKETS -> {
                var found = false
                indication.brackets?.forEach { bracket ->
                    if (weightKg >= bracket.minWeight && weightKg < bracket.maxWeight) {
                        rawDose = bracket.dose
                        found = true
                    }
                }
                if (!found) alerts.add("Peso fuori dalle fasce di dosaggio previste")
            }
        }


        indication.maxDose?.let { max ->
            if (rawDose > max) {
                rawDose = max
                isMaxDoseApplied = true
                alerts.add("Dose limitata al massimo ammissibile ($max ${indication.unit.split("/")[0]})")
            }
        }


        var pharmaceuticalDose: Double? = null
        var roundedPharmaceuticalDose: Double? = null
        
        if (indication.formMultiplier != null && indication.formMultiplier != 0.0) {
            pharmaceuticalDose = rawDose / indication.formMultiplier
            

            val step = indication.roundingStep ?: 0.0
            roundedPharmaceuticalDose = if (step > 0) {
                round(pharmaceuticalDose / step) * step
            } else {
                pharmaceuticalDose
            }
        }

        val baseUnit = indication.unit.split("/")[0]

        return CalculationResult(
            finalDose = rawDose,
            unit = baseUnit,
            pharmaceuticalDose = pharmaceuticalDose,
            roundedPharmaceuticalDose = roundedPharmaceuticalDose,
            bsa = bsa,
            isMaxDoseApplied = isMaxDoseApplied,
            alerts = alerts
        )
    }
}
