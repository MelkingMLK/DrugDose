package it.uninsubria.drugdose

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Collegamento delle view
        val etWeight = findViewById<TextInputEditText>(R.id.etWeight)
        val etHeight = findViewById<TextInputEditText>(R.id.etHeight)
        val etAge = findViewById<TextInputEditText>(R.id.etAge)
        val btnCalculate = findViewById<MaterialButton>(R.id.btnCalculate)

        btnCalculate.setOnClickListener {
            // Reset errori
            etWeight.error = null
            etHeight.error = null
            etAge.error = null

            // Lettura e parsing dei valori
            val weightStr = etWeight.text?.toString() ?: ""
            val heightStr = etHeight.text?.toString() ?: ""
            val ageStr = etAge.text?.toString() ?: ""

            val weight = weightStr.toDoubleOrNull()
            val height = heightStr.toDoubleOrNull()
            val age = ageStr.toIntOrNull()

            // Validazione Peso
            if (weight == null || weight <= 0 || weight > 300) {
                etWeight.error = when {
                    weightStr.isEmpty() -> "Inserire il peso"
                    weight == null -> "Formato non valido"
                    weight <= 0 -> "Il peso deve essere maggiore di 0"
                    else -> "Il peso non può superare i 300 kg"
                }
                return@setOnClickListener
            }

            // Validazione Altezza
            if (height == null || height <= 0 || height > 250) {
                etHeight.error = when {
                    heightStr.isEmpty() -> "Inserire l'altezza"
                    height == null -> "Formato non valido"
                    height <= 0 -> "L'altezza deve essere maggiore di 0"
                    else -> "L'altezza non può superare i 250 cm"
                }
                return@setOnClickListener
            }

            // Validazione Età
            if (age == null || age <= 0 || age > 120) {
                etAge.error = when {
                    ageStr.isEmpty() -> "Inserire l'età"
                    age == null -> "Formato non valido"
                    age <= 0 -> "L'età deve essere maggiore di 0"
                    else -> "L'età non può superare i 120 anni"
                }
                return@setOnClickListener
            }

            // Se tutto è valido
            Toast.makeText(this, "Dati validi, calcolo in corso...", Toast.LENGTH_SHORT).show()
        }
    }
}
