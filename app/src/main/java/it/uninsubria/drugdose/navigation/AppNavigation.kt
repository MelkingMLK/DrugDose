package it.uninsubria.drugdose.navigation

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.uninsubria.drugdose.MainActivity
import it.uninsubria.drugdose.ui.DrugListScreen
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

        // Rotta Fase 3 (Lista Farmaci LazyColumn)
        composable(Screen.DrugList.route) {
            DrugListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Placeholder per Fase 4
        composable(Screen.SavedDosages.route) {
            // Verrà implementata nella Fase 4 (Database locale)
        }
    }
}