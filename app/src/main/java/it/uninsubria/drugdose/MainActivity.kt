package it.uninsubria.drugdose

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import it.uninsubria.drugdose.model.CalculationType

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_DrugDose)
        setContentView(R.layout.activity_main)

        viewModel.loadDrugs()

        setupDropdowns()
        setupInfoCard()
        setupCalculation()
    }

    private fun setupDropdowns() {
        val spinnerDrug = findViewById<AutoCompleteTextView>(R.id.spinnerDrug)
        val spinnerIndication = findViewById<AutoCompleteTextView>(R.id.spinnerIndication)

        viewModel.drugs.observe(this) { drugs ->
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, drugs.map { it.name })
            spinnerDrug.setAdapter(adapter)
        }

        spinnerDrug.setOnItemClickListener { _, _, position, _ ->
            val selectedDrug = viewModel.drugs.value?.get(position)
            viewModel.selectDrug(selectedDrug)
            spinnerIndication.setText("", false)
        }

        viewModel.selectedDrug.observe(this) { drug ->
            if (drug != null) {
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, drug.indications.map { it.name })
                spinnerIndication.setAdapter(adapter)
                spinnerIndication.isEnabled = true
            } else {
                spinnerIndication.setAdapter(null)
                spinnerIndication.isEnabled = false
            }
        }

        spinnerIndication.setOnItemClickListener { _, _, position, _ ->
            val selectedIndication = viewModel.selectedDrug.value?.indications?.get(position)
            viewModel.selectIndication(selectedIndication)
        }
    }

    private fun setupInfoCard() {
        val tvFormulaInfo = findViewById<TextView>(R.id.tvFormulaInfo)

        viewModel.selectedIndication.observe(this) { indication ->
            if (indication == null) {
                tvFormulaInfo.text = getString(R.string.formula_vincoli)
                return@observe
            }

            val infoText = StringBuilder()
            val typeDesc = when (indication.calculationType) {
                CalculationType.WEIGHT_BASED -> "🔹 Calcolo su base Peso"
                CalculationType.BSA_BASED -> "🔹 Calcolo BSA (Mosteller)"
                CalculationType.FIXED_DOSE -> "🔹 Dose fissa"
                CalculationType.WEIGHT_BRACKETS -> "🔹 Calcolo per fasce di peso"
            }
            infoText.append(typeDesc).append("\n")

            when (indication.calculationType) {
                CalculationType.FIXED_DOSE -> 
                    infoText.append("Dose: ${indication.fixedDose} ${indication.unit}\n")
                CalculationType.WEIGHT_BRACKETS -> 
                    infoText.append("Dose variabile in base alla fascia\n")
                else -> 
                    infoText.append("Dose base: ${indication.dosePerUnit} ${indication.unit}\n")
            }

            val alerts = mutableListOf<String>()
            indication.maxDose?.let { alerts.add("Dose massima: $it ${indication.unit.split("/")[0]}") }
            indication.minWeight?.let { alerts.add("Peso min: $it kg") }
            indication.maxWeight?.let { alerts.add("Peso max: $it kg") }

            if (alerts.isNotEmpty()) {
                infoText.append("\nLimitazioni cliniche:\n")
                alerts.forEach { infoText.append("• $it\n") }
            }

            tvFormulaInfo.text = infoText.toString().trim()
        }
    }

    private fun setupCalculation() {
        val etWeight = findViewById<TextInputEditText>(R.id.etWeight)
        val etHeight = findViewById<TextInputEditText>(R.id.etHeight)
        val etAge = findViewById<TextInputEditText>(R.id.etAge)
        val btnCalculate = findViewById<MaterialButton>(R.id.btnCalculate)
        val tvResult = findViewById<TextView>(R.id.tvResult)
        val tvResultDetails = findViewById<TextView>(R.id.tvResultDetails)

        viewModel.calculationResult.observe(this) { result ->
            if (result != null) {
                // Risultato principale
                tvResult.text = "${result.finalDose} ${result.unit}"
                
                // Colore di stato
                if (result.isMaxDoseApplied) {
                    tvResult.setTextColor(ContextCompat.getColor(this, R.color.medical_error))
                } else {
                    tvResult.setTextColor(ContextCompat.getColor(this, R.color.medical_primary))
                }

                // Dettagli secondari
                val details = StringBuilder()
                
                val practicalDose = result.roundedPharmaceuticalDose ?: result.pharmaceuticalDose
                if (practicalDose != null) {
                    details.append("Dose Pratica consigliata: $practicalDose unità/vol\n")
                }
                
                if (result.bsa != null) {
                    details.append("BSA calcolata: %.2f m²\n".format(result.bsa))
                }

                if (result.alerts.isNotEmpty()) {
                    details.append("\n⚠️ AVVERTENZE CLINICHE:\n")
                    result.alerts.forEach { alert ->
                        details.append("• $alert\n")
                    }
                }

                tvResultDetails.text = details.toString().trim()
                Toast.makeText(this, "Calcolo completato", Toast.LENGTH_SHORT).show()
            } else {
                tvResult.text = getString(R.string.risultato)
                tvResult.setTextColor(ContextCompat.getColor(this, R.color.text_sub))
                tvResultDetails.text = ""
            }
        }

        btnCalculate.setOnClickListener {
            etWeight.error = null
            etHeight.error = null
            etAge.error = null

            val weight = etWeight.text?.toString()?.toDoubleOrNull()
            val height = etHeight.text?.toString()?.toDoubleOrNull()
            val age = etAge.text?.toString()?.toIntOrNull()

            var isValid = true
            if (weight == null || weight <= 0 || weight > 300) {
                etWeight.error = "Campo obbligatorio (0-300)"
                isValid = false
            }
            if (height == null || height <= 0 || height > 250) {
                etHeight.error = "Campo obbligatorio (0-250)"
                isValid = false
            }
            if (age == null || age <= 0 || age > 120) {
                etAge.error = "Campo obbligatorio (0-120)"
                isValid = false
            }

            if (!isValid) return@setOnClickListener

            if (viewModel.selectedIndication.value != null) {
                viewModel.calculate(weight!!, height!!, age!!)
            } else {
                Toast.makeText(this, "Selezionare farmaco e indicazione", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
