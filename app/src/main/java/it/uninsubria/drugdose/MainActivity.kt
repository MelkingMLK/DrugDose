package it.uninsubria.drugdose

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import it.uninsubria.drugdose.R
import it.uninsubria.drugdose.data.JsonHistoryManager
import it.uninsubria.drugdose.data.SavedDosage
import it.uninsubria.drugdose.model.CalculationType
import it.uninsubria.drugdose.model.DoseCalculator
import it.uninsubria.drugdose.model.Drug
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var historyManager: JsonHistoryManager

    private lateinit var spinnerDrug: AutoCompleteTextView
    private lateinit var spinnerIndication: AutoCompleteTextView
    private lateinit var etWeight: TextInputEditText
    private lateinit var etHeight: TextInputEditText
    private lateinit var etAge: TextInputEditText
    private lateinit var tvFormulaInfo: TextView
    private lateinit var btnCalculate: MaterialButton
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Abilita la Toolbar con freccia indietro verso la Home
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        historyManager = JsonHistoryManager(this)

        initViews()
        setupObservers()
        setupListeners()

        viewModel.loadDrugs()
    }
    override fun onSupportNavigateUp(): Boolean {
        finish() // Chiude MainActivity e ritorna regolarmente su HomeActivity nello stack
        return true
    }
    private fun initViews() {
        spinnerDrug = findViewById(R.id.spinnerDrug)
        spinnerIndication = findViewById(R.id.spinnerIndication)
        etWeight = findViewById(R.id.etWeight)
        etHeight = findViewById(R.id.etHeight)
        etAge = findViewById(R.id.etAge)
        tvFormulaInfo = findViewById(R.id.tvFormulaInfo)
        btnCalculate = findViewById(R.id.btnCalculate)
        tvResult = findViewById(R.id.tvResult)
    }

    private fun setupObservers() {
        // 1. Popolamento Dropdown Farmaci
        viewModel.drugs.observe(this) { drugList ->
            if (drugList.isNotEmpty()) {
                val drugNames = drugList.map { it.name }
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, drugNames)
                spinnerDrug.setAdapter(adapter)

                spinnerDrug.setOnItemClickListener { _, _, position, _ ->
                    val selectedDrug = drugList[position]
                    viewModel.selectDrug(selectedDrug)

                    // Popola automaticamente il menu dell'indicazione clinica
                    val indicationAdapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        listOf(selectedDrug.clinicalIndication)
                    )
                    spinnerIndication.setAdapter(indicationAdapter)
                    spinnerIndication.setText(selectedDrug.clinicalIndication, false)
                }
            }
        }

        // 2. Aggiornamento Info Card e Visibilità Altezza
        viewModel.selectedDrug.observe(this) { drug ->
            if (drug != null) {
                updateDrugCardInfo(drug)
                tvResult.text = getString(R.string.risultato)
                tvResult.setTextColor(getColor(R.color.text_main))
            } else {
                tvFormulaInfo.text = getString(R.string.formula_vincoli)
                spinnerIndication.setText("", false)
                tvResult.text = getString(R.string.risultato)
            }
        }

        // 3. Risultato del Calcolo e Scrittura su JSON
        viewModel.calculationResult.observe(this) { result ->
            if (result != null) {
                tvResult.setTextColor(getColor(R.color.text_main))

                val doseFormatted = if (result.theoreticalDose % 1.0 == 0.0) {
                    result.theoreticalDose.toInt().toString()
                } else {
                    String.format(Locale.US, "%.2f", result.theoreticalDose)
                }

                val bsaSuffix = if (result.bsaCalculated != null) {
                    getString(R.string.fmt_bsa_suffix, result.bsaCalculated)
                } else {
                    ""
                }

                val out = buildString {
                    append(getString(R.string.fmt_dose_teorica, doseFormatted, result.targetUnit, bsaSuffix)).append("\n\n")
                    append(getString(R.string.fmt_dose_pratica, result.practicalDoseText)).append("\n\n")
                    append(getString(R.string.fmt_somministrazione, result.administrationInfo)).append("\n\n")
                    append(getString(R.string.fmt_fonte, result.source))

                    if (result.warnings.isNotEmpty()) {
                        append("\n\n").append(getString(R.string.fmt_avvertenze, result.warnings.joinToString("\n• ")))
                    }
                }
                tvResult.text = out

                // Persistenza su file JSON
                val currentDrug = viewModel.selectedDrug.value
                val weight = etWeight.text.toString().toDoubleOrNull() ?: 0.0
                val height = etHeight.text.toString().toDoubleOrNull() ?: 0.0
                val age = etAge.text.toString().toDoubleOrNull() ?: 0.0

                if (currentDrug != null) {
                    saveCalculationToHistory(currentDrug, result, weight, height, age)
                }
            }
        }

        // 4. Gestione Errori di Validazione
        viewModel.validationError.observe(this) { errorMsg ->
            if (errorMsg != null) {
                tvResult.text = errorMsg
                tvResult.setTextColor(getColor(R.color.medical_error))
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateDrugCardInfo(drug: Drug) {
        val formulaDesc = when (drug.calculationType) {
            CalculationType.PER_KG -> "Dose per peso corporeo (${drug.unitDoseOriginal})"
            CalculationType.PER_BSA -> "Dose per BSA (${drug.unitDoseOriginal}) - Formula Mosteller"
            CalculationType.FIXED -> "Dose fissa (${drug.unitDoseOriginal})"
            CalculationType.WEIGHT_BRACKETS -> "Dose a scaglioni di peso"
        }

        val parentLayout = etHeight.parent?.parent as? View
        if (drug.calculationType == CalculationType.PER_BSA) {
            etHeight.visibility = View.VISIBLE
            parentLayout?.visibility = View.VISIBLE
        } else {
            etHeight.visibility = View.GONE
            parentLayout?.visibility = View.GONE
        }

        tvFormulaInfo.text = getString(
            R.string.fmt_info_card,
            drug.clinicalIndication,
            formulaDesc,
            drug.minWeightKg,
            drug.maxWeightKg,
            drug.minAgeYears.toInt(),
            drug.maxTotalDose,
            drug.targetUnit
        )
    }

    private fun setupListeners() {
        btnCalculate.setOnClickListener {
            val weightStr = etWeight.text?.toString() ?: ""
            val heightStr = etHeight.text?.toString() ?: ""
            val ageStr = etAge.text?.toString() ?: ""

            val weight = weightStr.toDoubleOrNull()
            val height = heightStr.toDoubleOrNull() ?: 0.0
            val age = ageStr.toDoubleOrNull()

            if (weight == null || age == null) {
                Toast.makeText(this, getString(R.string.err_input_invalid), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.calculate(weight, height, age)
        }
    }

    private fun saveCalculationToHistory(
        drug: Drug,
        result: DoseCalculator.CalculationResult,
        weight: Double,
        height: Double,
        age: Double
    ) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val timestamp = dateFormat.format(Date())

        val record = SavedDosage(
            id = System.currentTimeMillis(),
            drugName = drug.name,
            indication = drug.clinicalIndication,
            patientWeight = weight,
            patientHeight = height,
            patientAge = age,
            theoreticalDose = "${result.theoreticalDose} ${result.targetUnit}",
            practicalDose = result.practicalDoseText,
            timestamp = timestamp
        )

        historyManager.saveDosage(record)
    }
}