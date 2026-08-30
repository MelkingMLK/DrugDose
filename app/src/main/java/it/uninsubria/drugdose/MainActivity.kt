package it.uninsubria.drugdose

import android.graphics.Color
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

    // Inizializzazione del ViewModel usando il delegato activity-ktx
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Caricamento dei dati all'avvio
        viewModel.loadDrugs()

        // 2. Inizializzazione dei componenti della UI
        setupDropdowns()
        setupInfoCard()
        setupCalculation()
    }

    /**
     * Configura i menu a tendina per la selezione di farmaco e indicazione
     */
    private fun setupDropdowns() {
        val spinnerDrug = findViewById<AutoCompleteTextView>(R.id.spinnerDrug)
        val spinnerIndication = findViewById<AutoCompleteTextView>(R.id.spinnerIndication)

        // Popola la lista farmaci
        viewModel.drugs.observe(this) { drugs ->
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, drugs.map { it.name })
            spinnerDrug.setAdapter(adapter)
        }

        // Quando viene scelto un farmaco
        spinnerDrug.setOnItemClickListener { _, _, position, _ ->
            val selectedDrug = viewModel.drugs.value?.get(position)
            viewModel.selectDrug(selectedDrug)
            
            // Reset dello spinner indicazioni
            spinnerIndication.setText("", false)
        }

        // Popola le indicazioni in base al farmaco scelto
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

        // Quando viene scelta un'indicazione
        spinnerIndication.setOnItemClickListener { _, _, position, _ ->
            val selectedIndication = viewModel.selectedDrug.value?.indications?.get(position)
            viewModel.selectIndication(selectedIndication)
        }
    }

    /**
     * Aggiorna la card informativa in base all'indicazione scelta
     */
    private fun setupInfoCard() {
        val tvFormulaInfo = findViewById<TextView>(R.id.tvFormulaInfo)

        viewModel.selectedIndication.observe(this) { indication ->
            if (indication == null) {
                tvFormulaInfo.text = getString(R.string.formula_vincoli)
                return@observe
            }

            val infoText = StringBuilder()

            // Tipo di calcolo
            val typeDesc = when (indication.calculationType) {
                CalculationType.WEIGHT_BASED -> "🔹 Calcolo su base Peso"
                CalculationType.BSA_BASED -> "🔹 Calcolo BSA (Formula di Mosteller)"
                CalculationType.FIXED_DOSE -> "🔹 Dose fissa"
                CalculationType.WEIGHT_BRACKETS -> "🔹 Calcolo per fasce di peso"
            }
            infoText.append(typeDesc).append("\n")

            // Dose base
            when (indication.calculationType) {
                CalculationType.FIXED_DOSE -> 
                    infoText.append("Dose: ${indication.fixedDose} ${indication.unit}\n")
                CalculationType.WEIGHT_BRACKETS -> 
                    infoText.append("Dose variabile in base alla fascia\n")
                else -> 
                    infoText.append("Dose base: ${indication.dosePerUnit} ${indication.unit}\n")
            }

            // Alert preventivi
            val alerts = mutableListOf<String>()
            indication.maxDose?.let { alerts.add("Dose massima: $it ${indication.unit.split("/")[0]}") }
            indication.minWeight?.let { alerts.add("Peso min: $it kg") }
            indication.maxWeight?.let { alerts.add("Peso max: $it kg") }

            if (alerts.isNotEmpty()) {
                infoText.append("\n⚠️ Alert clinici:\n")
                alerts.forEach { infoText.append("- $it\n") }
            }

            tvFormulaInfo.text = infoText.toString().trim()
        }
    }

    /**
     * Gestisce la validazione e l'output del calcolo
     */
    private fun setupCalculation() {
        val etWeight = findViewById<TextInputEditText>(R.id.etWeight)
        val etHeight = findViewById<TextInputEditText>(R.id.etHeight)
        val etAge = findViewById<TextInputEditText>(R.id.etAge)
        val btnCalculate = findViewById<MaterialButton>(R.id.btnCalculate)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        // Osservazione e formattazione del risultato
        viewModel.calculationResult.observe(this) { result ->
            if (result != null) {
                val sb = StringBuilder()
                
                // Dose Teorica
                sb.append("Dose Teorica: ${result.finalDose} ${result.unit}\n")

                // Dose Pratica (se presente)
                val practicalDose = result.roundedPharmaceuticalDose ?: result.pharmaceuticalDose
                if (practicalDose != null) {
                    sb.append("Dose Pratica consigliata: $practicalDose unità/vol\n")
                }

                // Alert Clinici nel risultato
                if (result.alerts.isNotEmpty()) {
                    sb.append("\n⚠️ ALERT CLINICI:\n")
                    result.alerts.forEach { alert ->
                        sb.append("- $alert\n")
                    }
                }

                tvResult.text = sb.toString().trim()

                // Colore rosso se raggiunta la dose massima
                if (result.isMaxDoseApplied) {
                    tvResult.setTextColor(Color.RED)
                } else {
                    tvResult.setTextColor(ContextCompat.getColor(this, R.color.medical_primary))
                }

                Toast.makeText(this, "Calcolo completato", Toast.LENGTH_SHORT).show()
            } else {
                tvResult.text = getString(R.string.risultato)
                tvResult.setTextColor(ContextCompat.getColor(this, R.color.medical_primary))
            }
        }

        btnCalculate.setOnClickListener {
            // Reset errori
            etWeight.error = null
            etHeight.error = null
            etAge.error = null

            val weight = etWeight.text?.toString()?.toDoubleOrNull()
            val height = etHeight.text?.toString()?.toDoubleOrNull()
            val age = etAge.text?.toString()?.toIntOrNull()

            // Validazione
            var isValid = true
            if (weight == null || weight <= 0 || weight > 300) {
                etWeight.error = "Peso non valido"
                isValid = false
            }
            if (height == null || height <= 0 || height > 250) {
                etHeight.error = "Altezza non valida"
                isValid = false
            }
            if (age == null || age <= 0 || age > 120) {
                etAge.error = "Età non valida"
                isValid = false
            }

            if (!isValid) return@setOnClickListener

            // Esecuzione calcolo se la selezione è completa
            if (viewModel.selectedIndication.value != null) {
                viewModel.calculate(weight!!, height!!, age!!)
            } else {
                Toast.makeText(this, "Seleziona farmaco e indicazione", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
