package it.uninsubria.drugdose.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class JsonHistoryManager(private val context: Context) {

    private val file = File(context.filesDir, "saved_dosages.json")

    fun saveDosage(dosage: SavedDosage) {
        val list = getAllDosages().toMutableList()
        list.add(dosage)

        val jsonArray = JSONArray()
        list.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id.takeIf { it != 0L } ?: System.currentTimeMillis())
                put("drugName", item.drugName)
                put("indication", item.indication)
                put("patientWeight", item.patientWeight)
                put("patientHeight", item.patientHeight)
                put("patientAge", item.patientAge)
                put("theoreticalDose", item.theoreticalDose)
                put("practicalDose", item.practicalDose)
                put("timestamp", item.timestamp)
            }
            jsonArray.put(obj)
        }

        file.writeText(jsonArray.toString(2))
    }

    fun getAllDosages(): List<SavedDosage> {
        if (!file.exists()) return emptyList()

        val list = mutableListOf<SavedDosage>()
        try {
            val content = file.readText()
            if (content.isBlank()) return emptyList()

            val jsonArray = JSONArray(content)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    SavedDosage(
                        id = obj.optLong("id", 0L),
                        drugName = obj.getString("drugName"),
                        indication = obj.getString("indication"),
                        patientWeight = obj.getDouble("patientWeight"),
                        patientHeight = obj.getDouble("patientHeight"),
                        patientAge = obj.getDouble("patientAge"),
                        theoreticalDose = obj.getString("theoreticalDose"),
                        practicalDose = obj.getString("practicalDose"),
                        timestamp = obj.getString("timestamp")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.reversed()
    }

    fun deleteDosage(id: Long) {
        val list = getAllDosages().filter { it.id != id }
        val jsonArray = JSONArray()
        list.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("drugName", item.drugName)
                put("indication", item.indication)
                put("patientWeight", item.patientWeight)
                put("patientHeight", item.patientHeight)
                put("patientAge", item.patientAge)
                put("theoreticalDose", item.theoreticalDose)
                put("practicalDose", item.practicalDose)
                put("timestamp", item.timestamp)
            }
            jsonArray.put(obj)
        }
        file.writeText(jsonArray.toString(2))
    }
}