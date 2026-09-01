package it.uninsubria.drugdose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.uninsubria.drugdose.R

// Colori istituzionali definiti nel progetto
private val MedicalPrimary = Color(0xFF0C604E)
private val MedicalBackground = Color(0xFFF5F6F4)
private val TextMain = Color(0xFF111827)
private val TextSub = Color(0xFF4B5563)

@Composable
fun HomeScreen(
    onNavigateToCalculator: () -> Unit,
    onNavigateToDrugList: () -> Unit,
    onNavigateToSavedDosages: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MedicalBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Logo e Titoli Istituzionali
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_insubria),
                    contentDescription = "Logo Università dell'Insubria",
                    modifier = Modifier
                        .height(90.dp)
                        .padding(bottom = 16.dp)
                )

                Text(
                    text = "DrugDose",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MedicalPrimary
                )

                Text(
                    text = "Calcolatore Dermatologico Clinico",
                    fontSize = 15.sp,
                    color = TextSub,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Menu di Navigazione Principale
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MenuNavigationButton(
                    title = "Calcola Dose",
                    subtitle = "Esegui calcolo posologico per paziente",
                    onClick = onNavigateToCalculator
                )

                MenuNavigationButton(
                    title = "Elenco Medicinali",
                    subtitle = "Consulta le schede dei 20 farmaci",
                    onClick = onNavigateToDrugList
                )

                MenuNavigationButton(
                    title = "Dosaggi Salvati",
                    subtitle = "Visualizza lo storico prescrizioni",
                    onClick = onNavigateToSavedDosages
                )
            }

            // Footer informativo
            Text(
                text = "Università degli Studi dell'Insubria\nCorso di Programmazione Dispositivi Mobili",
                fontSize = 12.sp,
                color = TextSub,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun MenuNavigationButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = TextMain
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MedicalPrimary
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = TextSub
            )
        }
    }
}