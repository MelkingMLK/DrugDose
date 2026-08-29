package it.uninsubria.drugdose.model

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class DrugParser(private val context: Context) {

    fun parseDrugsFromAssets(fileName: String = "drugs.json"): List<Drug> {
        val drugs = mutableListOf<Drug>()
        try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val drugObj = jsonArray.getJSONObject(i)
                drugs.add(parseDrug(drugObj))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return drugs
    }

    private fun parseDrug(obj: JSONObject): Drug {
        val id = obj.getString("id")
        val name = obj.getString("name")
        val indicationsArray = obj.getJSONArray("indications")
        val indications = mutableListOf<Indication>()

        for (i in 0 until indicationsArray.length()) {
            indications.add(parseIndication(indicationsArray.getJSONObject(i)))
        }

        return Drug(id, name, indications)
    }

    private fun parseIndication(obj: JSONObject): Indication {
        val brackets = mutableListOf<WeightBracket>()
        if (obj.has("brackets")) {
            val bracketsArray = obj.getJSONArray("brackets")
            for (i in 0 until bracketsArray.length()) {
                val b = bracketsArray.getJSONObject(i)
                brackets.add(WeightBracket(
                    b.getDouble("min_weight"),
                    b.getDouble("max_weight"),
                    b.getDouble("dose")
                ))
            }
        }

        return Indication(
            name = obj.getString("name"),
            calculationType = CalculationType.valueOf(obj.getString("calculation_type")),
            dosePerUnit = obj.optDouble("dose_per_unit", -1.0).takeIf { it != -1.0 },
            unit = obj.getString("unit"),
            maxDose = obj.optDouble("max_dose", -1.0).takeIf { it != -1.0 },
            fixedDose = obj.optDouble("fixed_dose", -1.0).takeIf { it != -1.0 },
            brackets = if (brackets.isEmpty()) null else brackets,
            pharmaceuticalForm = obj.optString("pharmaceutical_form", null),
            formMultiplier = obj.optDouble("form_multiplier", -1.0).takeIf { it != -1.0 },
            roundingStep = obj.optDouble("rounding_step", -1.0).takeIf { it != -1.0 },
            minWeight = obj.optDouble("min_weight", -1.0).takeIf { it != -1.0 },
            maxWeight = obj.optDouble("max_weight", -1.0).takeIf { it != -1.0 },
            minAge = obj.optInt("min_age", -1).takeIf { it != -1 },
            maxAge = obj.optInt("max_age", -1).takeIf { it != -1 },
            notes = obj.optString("notes", null)
        )
    }
}
