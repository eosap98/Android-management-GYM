package com.gymku.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gymku.app.ui.screens.dashboard.DashboardScreen
import com.gymku.app.ui.screens.login.LoginScreen
import com.gymku.app.ui.screens.member.AddMemberScreen
import com.gymku.app.ui.screens.member.MemberDetailScreen
import com.gymku.app.ui.screens.member.MemberListScreen
import com.gymku.app.ui.screens.report.ReportScreen
import com.gymku.app.ui.screens.scanner.ScannerScreen
import com.gymku.app.ui.screens.setup.FirebaseSetupScreen
import com.gymku.app.ui.screens.splash.SplashScreen
import com.gymku.app.ui.screens.visitor.AddVisitorScreen
import com.gymku.app.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onFirebaseNotConfigured = {
                    navController.navigate(Screen.FirebaseSetup.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNotLoggedIn = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onAlreadyLoggedIn = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.FirebaseSetup.route) {
            FirebaseSetupScreen(
                onSetupComplete = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.FirebaseSetup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                authViewModel = authViewModel,
                onNavigateToScanner   = { navController.navigate(Screen.Scanner.route) },
                onNavigateToMembers   = { navController.navigate(Screen.MemberList.route) },
                onNavigateToReport    = { navController.navigate(Screen.Report.route) },
                onNavigateToAddMember = { navController.navigate(Screen.AddMember.route) },
                onNavigateToAddVisitor = { navController.navigate(Screen.AddVisitor.route) },
                onNavigateToAdminMenu = { navController.navigate(Screen.AdminMenu.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Scanner.route) {
            ScannerScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRenew = { memberId ->
                    navController.navigate(Screen.MemberDetail.createRoute(memberId))
                }
            )
        }

        composable(Screen.MemberList.route) {
            MemberListScreen(
                onNavigateBack    = { navController.popBackStack() },
                onNavigateToDetail = { memberId ->
                    navController.navigate(Screen.MemberDetail.createRoute(memberId))
                },
                onNavigateToAdd   = { navController.navigate(Screen.AddMember.route) }
            )
        }

        composable(
            route = Screen.MemberDetail.route,
            arguments = listOf(navArgument("memberId") { type = NavType.StringType })
        ) { backStack ->
            val memberId = backStack.arguments?.getString("memberId") ?: ""
            MemberDetailScreen(
                memberId = memberId,
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EditMember.createRoute(id))
                }
            )
        }

        composable(Screen.AddMember.route) {
            AddMemberScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onMemberAdded  = { navController.popBackStack() }
            )
        }

        composable(Screen.AddVisitor.route) {
            AddVisitorScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onVisitorAdded = { navController.popBackStack() }
            )
        }

        composable(Screen.Report.route) {
            ReportScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditMember.route,
            arguments = listOf(navArgument("memberId") { type = NavType.StringType })
        ) { backStack ->
            val memberId = backStack.arguments?.getString("memberId") ?: ""
            AddMemberScreen(
                memberId = memberId, // New parameter
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onMemberAdded  = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminMenu.route) {
            com.gymku.app.ui.screens.setup.AdminMenuScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSchedule = { navController.navigate(Screen.StaffSchedule.route) }
            )
        }

        composable(Screen.StaffSchedule.route) {
            com.gymku.app.ui.screens.setup.StaffScheduleScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
