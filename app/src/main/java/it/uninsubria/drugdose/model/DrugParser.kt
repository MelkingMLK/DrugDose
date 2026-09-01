package it.uninsubria.drugdose.model

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class DrugParser(private val context: Context) {

    fun parseDrugsFromAssets(fileName: String = "drugs.json"): List<Drug> {
        val drugList = mutableListOf<Drug>()
        val jsonString = loadJsonFromAsset(fileName) ?: return emptyList()

        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val drug = parseDrug(obj)
                if (drug != null) {
                    drugList.add(drug)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return drugList
    }

    private fun parseDrug(obj: JSONObject): Drug? {
        return try {
            val id = obj.getString("id")
            val name = obj.getString("name")
            val clinicalIndication = obj.optString("clinicalIndication", "")
            val calcTypeStr = obj.optString("calculationType", "FIXED")
            val calculationType = try {
                CalculationType.valueOf(calcTypeStr)
            } catch (e: Exception) {
                CalculationType.FIXED
            }

            val targetUnit = obj.optString("targetUnit", "mg")
            val unitDose = obj.optDouble("unitDose", 0.0)
            val unitDoseOriginal = obj.optString("unitDoseOriginal", "")
            val maxTotalDose = obj.optDouble("maxTotalDose", Double.MAX_VALUE)
            val minAgeYears = obj.optDouble("minAgeYears", 0.0)
            val minWeightKg = obj.optDouble("minWeightKg", 0.0)
            val maxWeightKg = obj.optDouble("maxWeightKg", 300.0)

            // AvailableForm
            val formObj = obj.optJSONObject("availableForm")
            val availableForm = if (formObj != null) {
                AvailableForm(
                    formType = formObj.optString("formType", "TABLET"),
                    strengthValue = formObj.optDouble("strengthValue", 1.0),
                    strengthUnit = formObj.optString("strengthUnit", "mg"),
                    isDivisible = formObj.optBoolean("isDivisible", false)
                )
            } else {
                AvailableForm("TABLET", 1.0, "mg", false)
            }

            // WeightBrackets
            val bracketsList = mutableListOf<WeightBracket>()
            val bracketsArray = obj.optJSONArray("weightBrackets")
            if (bracketsArray != null) {
                for (j in 0 until bracketsArray.length()) {
                    val bObj = bracketsArray.getJSONObject(j)
                    bracketsList.add(
                        WeightBracket(
                            minWeightKg = bObj.optDouble("minWeightKg", 0.0),
                            maxWeightKg = bObj.optDouble("maxWeightKg", 0.0),
                            doseValue = bObj.optDouble("doseValue", 0.0),
                            unit = bObj.optString("unit", "mg"),
                            tabletsCount = bObj.optInt("tabletsCount", 0)
                        )
                    )
                }
            }

            // Alerts
            val alertsList = mutableListOf<String>()
            val alertsArray = obj.optJSONArray("alerts")
            if (alertsArray != null) {
                for (k in 0 until alertsArray.length()) {
                    alertsList.add(alertsArray.getString(k))
                }
            }

            val administrationInfo = obj.optString("administrationInfo", "")
            val source = obj.optString("source", "")

            Drug(
                id = id,
                name = name,
                clinicalIndication = clinicalIndication,
                calculationType = calculationType,
                targetUnit = targetUnit,
                unitDose = unitDose,
                unitDoseOriginal = unitDoseOriginal,
                maxTotalDose = maxTotalDose,
                minAgeYears = minAgeYears,
                minWeightKg = minWeightKg,
                maxWeightKg = maxWeightKg,
                availableForm = availableForm,
                weightBrackets = bracketsList,
                administrationInfo = administrationInfo,
                alerts = alertsList,
                source = source
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun loadJsonFromAsset(fileName: String): String? {
        return try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }
}