package it.uninsubria.drugdose

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import it.uninsubria.drugdose.model.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _drugs = MutableLiveData<List<Drug>>()
    val drugs: LiveData<List<Drug>> = _drugs

    private val _selectedDrug = MutableLiveData<Drug?>()
    val selectedDrug: LiveData<Drug?> = _selectedDrug

    private val _calculationResult = MutableLiveData<DoseCalculator.CalculationResult?>()
    val calculationResult: LiveData<DoseCalculator.CalculationResult?> = _calculationResult

    private val _validationError = MutableLiveData<String?>()
    val validationError: LiveData<String?> = _validationError

    fun loadDrugs() {
        val parser = DrugParser(getApplication())
        val loadedDrugs = parser.parseDrugsFromAssets("drugs.json")
        _drugs.value = loadedDrugs
    }

    fun selectDrug(drug: Drug?) {
        _selectedDrug.value = drug
        _calculationResult.value = null
        _validationError.value = null
    }

    fun calculate(weight: Double, height: Double, age: Double) {
        val drug = _selectedDrug.value
        if (drug == null) {
            _validationError.value = "Selezionare un medicinale prima di calcolare."
            return
        }

        val validation = DoseCalculator.validateInputs(drug, weight, height, age)
        if (!validation.isValid) {
            _validationError.value = validation.errorMessage
            _calculationResult.value = null
            return
        }

        _validationError.value = null
        val result = DoseCalculator.calculateDose(
            drug = drug,
            weightKg = weight,
            heightCm = height,
            ageYears = age
        )
        _calculationResult.value = result
    }
}