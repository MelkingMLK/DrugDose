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
import it.uninsubria.drugdose.ui.SavedDosagesScreen

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

        composable(Screen.DrugList.route) {
            DrugListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Rotta Fase 4
        composable(Screen.SavedDosages.route) {
            SavedDosagesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}