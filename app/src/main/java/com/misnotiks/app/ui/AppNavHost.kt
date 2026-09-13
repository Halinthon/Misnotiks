package com.misnotiks.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.misnotiks.app.data.DataStore

@Composable
fun AppNavHost(navController: NavHostController, store: DataStore, themeState: ThemeState) {
    NavHost(navController = navController, startDestination = "categories") {

        composable("categories") {
            CategoriesScreen(
                store = store,
                themeState = themeState,
                onOpenCategory = { catId -> navController.navigate("entries/$catId") }
            )
        }

        composable("entries/{catId}") { backStackEntry ->
            val catId = backStackEntry.arguments?.getString("catId") ?: return@composable
            EntriesScreen(
                store = store,
                categoryId = catId,
                onBack = { navController.popBackStack() },
                onOpenEntry = { entryId -> navController.navigate("entry/$catId/$entryId") }
            )
        }

        composable("entry/{catId}/{entryId}") { backStackEntry ->
            val catId = backStackEntry.arguments?.getString("catId") ?: return@composable
            val entryId = backStackEntry.arguments?.getString("entryId") ?: return@composable
            EntryScreen(
                store = store,
                categoryId = catId,
                entryId = entryId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
