package com.deivid22srk.sitehub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.deivid22srk.sitehub.ui.screens.HomeScreen
import com.deivid22srk.sitehub.ui.screens.SettingsScreen
import com.deivid22srk.sitehub.ui.screens.UserscriptScreen
import com.deivid22srk.sitehub.ui.screens.WebViewScreen

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val WEBVIEW = "webview/{siteId}/{url}/{title}"
    const val USERSCRIPTS = "userscripts/{siteId}/{siteUrl}/{siteTitle}"

    fun webView(siteId: Long, url: String, title: String): String {
        return "webview/$siteId/${java.net.URLEncoder.encode(url, "UTF-8")}/${java.net.URLEncoder.encode(title, "UTF-8")}"
    }

    fun userscripts(siteId: Long, siteUrl: String, siteTitle: String): String {
        return "userscripts/$siteId/${java.net.URLEncoder.encode(siteUrl, "UTF-8")}/${java.net.URLEncoder.encode(siteTitle, "UTF-8")}"
    }
}

@Composable
fun SiteHubNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToWebView = { siteId, url, title ->
                    navController.navigate(Routes.webView(siteId, url, title))
                },
                onNavigateToUserscripts = { siteId, siteUrl, siteTitle ->
                    navController.navigate(Routes.userscripts(siteId, siteUrl, siteTitle))
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.WEBVIEW,
            arguments = listOf(
                navArgument("siteId") { type = NavType.LongType },
                navArgument("url") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val siteId = backStackEntry.arguments?.getLong("siteId") ?: 0L
            val url = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("url") ?: "", "UTF-8")
            val title = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("title") ?: "", "UTF-8")
            WebViewScreen(siteId = siteId, url = url, title = title, onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.USERSCRIPTS,
            arguments = listOf(
                navArgument("siteId") { type = NavType.LongType },
                navArgument("siteUrl") { type = NavType.StringType },
                navArgument("siteTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val siteId = backStackEntry.arguments?.getLong("siteId") ?: 0L
            val siteUrl = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("siteUrl") ?: "", "UTF-8")
            val siteTitle = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("siteTitle") ?: "", "UTF-8")
            UserscriptScreen(siteId = siteId, siteUrl = siteUrl, siteTitle = siteTitle, onBack = { navController.popBackStack() })
        }
    }
}
