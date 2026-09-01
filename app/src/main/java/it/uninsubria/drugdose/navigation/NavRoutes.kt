package it.uninsubria.drugdose.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home_screen")
    object Calculator : Screen("calculator_screen")
    object DrugList : Screen("drug_list_screen")
    object SavedDosages : Screen("saved_dosages_screen")
}