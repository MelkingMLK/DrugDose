package it.uninsubria.drugdose.model

import java.util.Locale
import kotlin.math.ceil
import kotlin.math.round
import kotlin.math.sqrt

object DoseCalculator {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    data class CalculationResult(
        val theoreticalDose: Double,
        val targetUnit: String,
        val practicalDoseText: String,
        val bsaCalculated: Double? = null,
        val warnings: List<String> = emptyList(),
        val administrationInfo: String,
        val source: String
    )

    fun validateInputs(drug: Drug, weightKg: Double, heightCm: Double, ageYears: Double): ValidationResult {
        // 1. Limiti Fisiologici Assoluti Generali (Fase 1 della scaletta)
        if (weightKg < 1.0 || weightKg > 230.0) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Peso inserito ($weightKg kg) non valido: deve essere compreso tra 1 e 230 kg."
            )
        }

        if (ageYears < 1.0 || ageYears > 120.0) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Età inserita ($ageYears anni) non valida: deve essere compresa tra 1 e 120 anni."
            )
        }

        if (drug.calculationType == CalculationType.PER_BSA) {
            if (heightCm < 45.0 || heightCm > 225.0) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = "Altezza inserita ($heightCm cm) non valida: per il calcolo BSA deve essere tra 45 e 225 cm."
                )
            }
        }

        // 2. Vincoli Clinici Specifici del Farmaco (da drugs.json)
        if (weightKg < drug.minWeightKg || weightKg > drug.maxWeightKg) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Peso non idoneo per ${drug.name}: intervallo terapeutico consentito ${drug.minWeightKg} - ${drug.maxWeightKg} kg."
            )
        }

        if (ageYears < drug.minAgeYears || ageYears > drug.maxAgeYears) {
            val rangeMsg = if (drug.maxAgeYears < 120.0) {
                "tra ${drug.minAgeYears.toInt()} e ${drug.maxAgeYears.toInt()} anni"
            } else {
                "≥ ${drug.minAgeYears.toInt()} anni"
            }
            return ValidationResult(
                isValid = false,
                errorMessage = "Età non idonea per ${drug.name}: età clinica richiesta $rangeMsg."
            )
        }

        return ValidationResult(isValid = true)
    }

    fun calculateDose(
        drug: Drug,
        weightKg: Double,
        heightCm: Double,
        ageYears: Double
    ): CalculationResult {
        val warnings = mutableListOf<String>()
        warnings.addAll(drug.alerts)

        var theoreticalDose = 0.0
        var bsa: Double? = null

        when (drug.calculationType) {
            CalculationType.PER_KG -> {
                theoreticalDose = drug.unitDose * weightKg
            }
            CalculationType.PER_BSA -> {
                bsa = calculateBSA(heightCm, weightKg)
                theoreticalDose = drug.unitDose * bsa
            }
            CalculationType.FIXED -> {
                theoreticalDose = drug.unitDose
            }
            CalculationType.WEIGHT_BRACKETS -> {
                val bracket = drug.weightBrackets.find { weightKg >= it.minWeightKg && weightKg <= it.maxWeightKg }
                if (bracket != null) {
                    theoreticalDose = bracket.doseValue
                } else {
                    theoreticalDose = 0.0
                    warnings.add("Attenzione: Peso fuori dalle fasce tabellari previste (${drug.minWeightKg}-${drug.maxWeightKg} kg).")
                }
            }
        }

        if (theoreticalDose > drug.maxTotalDose) {
            warnings.add("Dose teorica (${formatDecimals(theoreticalDose)} ${drug.targetUnit}) eccede la dose massima consentita (${drug.maxTotalDose} ${drug.targetUnit}). Applicato limite massimo.")
            theoreticalDose = drug.maxTotalDose
        }

        val practicalText = calculatePracticalForm(drug, theoreticalDose, weightKg)

        return CalculationResult(
            theoreticalDose = theoreticalDose,
            targetUnit = drug.targetUnit,
            practicalDoseText = practicalText,
            bsaCalculated = bsa,
            warnings = warnings,
            administrationInfo = drug.administrationInfo,
            source = drug.source
        )
    }

    fun calculateBSA(heightCm: Double, weightKg: Double): Double {
        if (heightCm <= 0.0 || weightKg <= 0.0) return 0.0
        return sqrt((heightCm * weightKg) / 3600.0)
    }

    private fun calculatePracticalForm(drug: Drug, dose: Double, weightKg: Double): String {
        if (drug.calculationType == CalculationType.WEIGHT_BRACKETS) {
            val bracket = drug.weightBrackets.find { weightKg >= it.minWeightKg && weightKg <= it.maxWeightKg }
            if (bracket != null && bracket.tabletsCount > 0) {
                return "${bracket.tabletsCount}x ${drug.availableForm.formType.lowercase()} (${drug.availableForm.strengthValue} ${drug.availableForm.strengthUnit})"
            }
        }

        val strength = drug.availableForm.strengthValue
        if (strength <= 0.0) return "${formatDecimals(dose)} ${drug.targetUnit}"

        return when (drug.availableForm.formType.uppercase(Locale.ROOT)) {
            "TABLET", "CAPSULE" -> {
                val exactUnits = dose / strength
                val practicalUnits = if (drug.availableForm.isDivisible) {
                    round(exactUnits * 2.0) / 2.0
                } else {
                    ceil(exactUnits).toInt().toDouble()
                }
                val formattedUnits = if (practicalUnits % 1.0 == 0.0) practicalUnits.toInt().toString() else practicalUnits.toString()
                "$formattedUnits ${drug.availableForm.formType.lowercase()} da $strength ${drug.availableForm.strengthUnit}"
            }
            "CREAM" -> {
                "1 applicazione cutanea (${formatDecimals(dose)} ${drug.targetUnit})"
            }
            "SUSPENSION", "SYRUP" -> {
                val ml = (dose / strength) * (if (drug.availableForm.strengthUnit.contains("5ml")) 5.0 else 1.0)
                "${formatDecimals(ml)} ml (${formatDecimals(dose)} ${drug.targetUnit})"
            }
            else -> {
                "${formatDecimals(dose)} ${drug.targetUnit}"
            }
        }
    }

    private fun formatDecimals(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format(Locale.US, "%.2f", value)
        }
    }
}