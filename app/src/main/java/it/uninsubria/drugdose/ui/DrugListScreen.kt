package it.uninsubria.drugdose.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.uninsubria.drugdose.model.CalculationType
import it.uninsubria.drugdose.model.Drug
import it.uninsubria.drugdose.model.DrugParser

// Palette cromatica istituzionale
private val MedicalPrimary = Color(0xFF0C604E)
private val MedicalBackground = Color(0xFFF5F6F4)
private val MedicalOutline = Color(0xFFD1D5DB)
private val TextMain = Color(0xFF111827)
private val TextSub = Color(0xFF4B5563)
private val MedicalError = Color(0xFFB91C1C)
private val MedicalErrorBg = Color(0xFFFEE2E2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugListScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val drugList = remember {
        val parser = DrugParser(context)
        parser.parseDrugsFromAssets("drugs.json")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Elenco Farmaci (${drugList.size})",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Torna Indietro",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MedicalPrimary
                )
            )
        },
        containerColor = MedicalBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(drugList, key = { it.id }) { drug ->
                DrugItemCard(drug = drug)
            }
        }
    }
}

@Composable
fun DrugItemCard(drug: Drug) {
    var isExpanded by remember { mutableStateOf(false) }

    val formulaDesc = when (drug.calculationType) {
        CalculationType.PER_KG -> "Dose per peso (${drug.unitDoseOriginal})"
        CalculationType.PER_BSA -> "Dose BSA (${drug.unitDoseOriginal}) - Mosteller"
        CalculationType.FIXED -> "Dose fissa (${drug.unitDoseOriginal})"
        CalculationType.WEIGHT_BRACKETS -> "Dose a scaglioni di peso"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MedicalOutline))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Intestazione Card: Nome e Icona Espansione
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = drug.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MedicalPrimary
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Comprimi" else "Espandi",
                    tint = TextSub
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Indicazione Clinica
            Text(
                text = drug.clinicalIndication,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = TextMain
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Tag Tipologia Calcolo
            Surface(
                color = MedicalBackground,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = formulaDesc,
                    fontSize = 12.sp,
                    color = MedicalPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Dettagli Espandibili
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MedicalOutline, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Vincoli Clinici
                    Text(
                        text = "Limiti Fisiologici:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                    Text(
                        text = "• Peso ammesso: ${drug.minWeightKg} - ${drug.maxWeightKg} kg\n" +
                                "• Età minima: ${drug.minAgeYears.toInt()} anni\n" +
                                "• Dose massima: ${drug.maxTotalDose} ${drug.targetUnit}",
                        fontSize = 12.sp,
                        color = TextSub,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Modalità di Somministrazione
                    Text(
                        text = "Modalità d'uso:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                    Text(
                        text = drug.administrationInfo,
                        fontSize = 12.sp,
                        color = TextSub,
                        lineHeight = 16.sp
                    )

                    // Box Avvertenze Cliniche (se presenti)
                    if (drug.alerts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MedicalErrorBg,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Avvertenze:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MedicalError
                                )
                                Text(
                                    text = "• " + drug.alerts.joinToString("\n• "),
                                    fontSize = 11.sp,
                                    color = MedicalError,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tracciabilità Fonte
                    Text(
                        text = "Fonte: ${drug.source}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Light,
                        color = TextSub
                    )
                }
            }
        }
    }
}