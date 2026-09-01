package it.uninsubria.drugdose.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.uninsubria.drugdose.data.JsonHistoryManager
import it.uninsubria.drugdose.data.SavedDosage

private val MedicalPrimary = Color(0xFF0C604E)
private val MedicalBackground = Color(0xFFF5F6F4)
private val MedicalOutline = Color(0xFFD1D5DB)
private val TextMain = Color(0xFF111827)
private val TextSub = Color(0xFF4B5563)
private val MedicalError = Color(0xFFB91C1C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedDosagesScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val historyManager = remember { JsonHistoryManager(context) }
    var historyList by remember { mutableStateOf<List<SavedDosage>>(emptyList()) }

    // Ricarica la lista ogni volta che si entra nella schermata
    LaunchedEffect(Unit) {
        historyList = historyManager.getAllDosages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Storico Dosaggi (${historyList.size})",
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
        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nessun dosaggio salvato nello storico.",
                    fontSize = 15.sp,
                    color = TextSub
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyList, key = { it.id }) { item ->
                    SavedDosageItemCard(
                        dosage = item,
                        onDelete = {
                            historyManager.deleteDosage(item.id)
                            historyList = historyManager.getAllDosages()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SavedDosageItemCard(
    dosage: SavedDosage,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dosage.drugName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MedicalPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dosage.timestamp,
                        fontSize = 12.sp,
                        color = TextSub
                    )
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Elimina",
                            tint = MedicalError
                        )
                    }
                }
            }

            Text(
                text = dosage.indication,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextMain
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Paziente: ${dosage.patientWeight} kg" +
                        (if (dosage.patientHeight > 0) ", ${dosage.patientHeight} cm" else "") +
                        ", ${dosage.patientAge.toInt()} anni",
                fontSize = 12.5.sp,
                color = TextSub
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Prescrizione: ${dosage.practicalDose}",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MedicalPrimary
            )
        }
    }
}