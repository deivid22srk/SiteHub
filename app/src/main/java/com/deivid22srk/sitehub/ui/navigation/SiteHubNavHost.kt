package com.deivid22srk.sitehub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.deivid22srk.sitehub.ui.screens.HomeScreen
import com.deivid22srk.sitehub.ui.screens.SettingsScreen
import com.deivid22srk.sitehub.ui.screens.WebViewScreen

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val WEBVIEW = "webview/{url}/{title}"

    fun webView(url: String, title: String): String {
        return "webview/${java.net.URLEncoder.encode(url, "UTF-8")}/${java.net.URLEncoder.encode(title, "UTF-8")}"
    }
}

@Composable
fun SiteHubNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToWebView = { url, title ->
                    navController.navigate(Routes.webView(url, title))
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.WEBVIEW,
            arguments = listOf(
                navArgument("url") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val url = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("url") ?: "", "UTF-8")
            val title = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("title") ?: "", "UTF-8")
            WebViewScreen(url = url, title = title, onBack = { navController.popBackStack() })
        }
    }
}
