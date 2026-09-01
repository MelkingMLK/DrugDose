package it.uninsubria.drugdose.navigation

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.uninsubria.drugdose.MainActivity
import it.uninsubria.drugdose.ui.HomeScreen

@Composable
fun AppNavigation(context: Context) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCalculator = {
                    // Avvio della MainActivity (Calcolatore basato su Layout XML)
                    context.startActivity(Intent(context, MainActivity::class.java))
                },
                onNavigateToDrugList = {
                    navController.navigate(Screen.DrugList.route)
                },
                onNavigateToSavedDosages = {
                    navController.navigate(Screen.SavedDosages.route)
                }
            )
        }

        // Placeholder per le fasi successive
        composable(Screen.DrugList.route) {
            // Verrà implementata nella Fase 3 (LazyColumn)
        }

        composable(Screen.SavedDosages.route) {
            // Verrà implementata nella Fase 4 (Database locale)
        }
    }
}