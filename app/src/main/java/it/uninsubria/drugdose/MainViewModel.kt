package it.uninsubria.drugdose

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import it.uninsubria.drugdose.model.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Lista dei farmaci
    private val _drugs = MutableLiveData<List<Drug>>()
    val drugs: LiveData<List<Drug>> = _drugs

    // Farmaco selezionato
    private val _selectedDrug = MutableLiveData<Drug?>()
    val selectedDrug: LiveData<Drug?> = _selectedDrug

    // Indicazione selezionata
    private val _selectedIndication = MutableLiveData<Indication?>()
    val selectedIndication: LiveData<Indication?> = _selectedIndication

    // Risultato del calcolo
    private val _calculationResult = MutableLiveData<DoseCalculator.CalculationResult?>()
    val calculationResult: LiveData<DoseCalculator.CalculationResult?> = _calculationResult

    /**
     * Carica i farmaci dal file JSON negli assets
     */
    fun loadDrugs() {
        val parser = DrugParser(getApplication())
        val loadedDrugs = parser.parseDrugsFromAssets("drugs.json")
        _drugs.value = loadedDrugs
    }

    /**
     * Imposta il farmaco selezionato e resetta l'indicazione
     */
    fun selectDrug(drug: Drug?) {
        _selectedDrug.value = drug
        _selectedIndication.value = null
        _calculationResult.value = null
    }

    /**
     * Imposta l'indicazione selezionata
     */
    fun selectIndication(indication: Indication?) {
        _selectedIndication.value = indication
        _calculationResult.value = null
    }

    /**
     * Esegue il calcolo della dose usando il DoseCalculator
     */
    fun calculate(weight: Double, height: Double, age: Int) {
        val indication = _selectedIndication.value
        if (indication != null) {
            val result = DoseCalculator.calculateDose(
                indication = indication,
                weightKg = weight,
                heightCm = height,
                age = age
            )
            _calculationResult.value = result
        }
    }
}
