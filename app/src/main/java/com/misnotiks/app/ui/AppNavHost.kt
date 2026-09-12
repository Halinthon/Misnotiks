package com.misnotiks.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.misnotiks.app.data.DataStore

@Composable
fun AppNavHost(navController: NavHostController, store: DataStore) {
    var refreshKey by remember { mutableStateOf(0) }
    val refresh: () -> Unit = { refreshKey++ }

    NavHost(navController = navController, startDestination = "categories") {

        composable("categories") {
            CategoriesScreen(
                store = store,
                refreshKey = refreshKey,
                onOpenCategory = { catId -> navController.navigate("entries/$catId") },
                onChanged = refresh
            )
        }

        composable("entries/{catId}") { backStackEntry ->
            val catId = backStackEntry.arguments?.getString("catId") ?: return@composable
            EntriesScreen(
                store = store,
                categoryId = catId,
                refreshKey = refreshKey,
                onBack = { navController.popBackStack() },
                onOpenEntry = { entryId -> navController.navigate("entry/$catId/$entryId") },
                onChanged = refresh
            )
        }

        composable("entry/{catId}/{entryId}") { backStackEntry ->
            val catId = backStackEntry.arguments?.getString("catId") ?: return@composable
            val entryId = backStackEntry.arguments?.getString("entryId") ?: return@composable
            EntryScreen(
                store = store,
                categoryId = catId,
                entryId = entryId,
                onBack = { navController.popBackStack() },
                onChanged = refresh
            )
        }
    }
}
