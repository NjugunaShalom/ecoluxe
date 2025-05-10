package com.example.ecoluxe.navigation

import com.example.ecoluxe.ui.screens.auth.SplashScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import com.example.ecoluxe.ui.screens.auth.LoginScreen
import com.example.ecoluxe.ui.screens.auth.SignupScreen
import com.example.ecoluxe.ui.screens.auth.WelcomeScreen
import com.example.ecoluxe.ui.screens.chat.ChatListScreen
import com.example.ecoluxe.ui.screens.chat.ChatScreen
import com.example.ecoluxe.ui.screens.home.HomeScreen
import com.example.ecoluxe.ui.screens.profile.ProfileScreen
import com.example.ecoluxe.ui.screens.swap.SwapListScreen

@Composable
fun EcoLuxeApp(navController:NavHostController = rememberNavController(),startDestination:String= ROUTE_SPLASH) {
    NavHost(navController=navController, startDestination=startDestination) {
        composable(ROUTE_SPLASH){ SplashScreen {
            navController.navigate(ROUTE_WELCOME) {
                popUpTo(ROUTE_SPLASH){inclusive=true}} }}
        composable(ROUTE_WELCOME) { WelcomeScreen(navController) }
        composable(ROUTE_SIGNUP) { SignupScreen(navController) }
        composable(ROUTE_LOGIN) { LoginScreen(navController) }
        composable(ROUTE_HOME) { HomeScreen(navController) }
        composable (ROUTE_SWAPLIST){ SwapListScreen(navController) }
//      composable (ROUTE_UPLOADSWAPITEM){ UploadSwapItemScreen(navController) }
        composable (ROUTE_PROFILE){ ProfileScreen(navController) }
        composable(
            route = ROUTE_CHAT_WITH_ID,
            arguments = listOf(navArgument("chatId") { defaultValue = "" })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatScreen(navController, chatId)
        }
        composable(ROUTE_CHATLIST) { ChatListScreen(navController) }
    }

}



