package com.example.fairsplit.composeUi

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.fairsplit.composeUi.screens.AddGroupScreen
import com.example.fairsplit.composeUi.screens.AddSpendingScreen
import com.example.fairsplit.composeUi.screens.EditMembersScreen
import com.example.fairsplit.composeUi.screens.FriendsScreen
import com.example.fairsplit.composeUi.screens.GroupDetailScreen
import com.example.fairsplit.composeUi.screens.LoadingScreen
import com.example.fairsplit.composeUi.screens.LoginScreen
import com.example.fairsplit.composeUi.screens.MainScreen
import com.example.fairsplit.composeUi.screens.RegistrationScreen
import com.example.fairsplit.composeUi.screens.SettleUpScreen
import com.example.fairsplit.model.Firebase
import com.example.fairsplit.session.SessionManager
import com.example.fairsplit.view.model.GroupViewModel
import com.example.fairsplit.view.model.LoginViewModel
import com.example.fairsplit.view.model.RegistrationViewModel
import com.example.fairsplit.view.model.SpendingViewModel
import com.example.fairsplit.view.model.UserViewModel

//Enthält die Navigation für die App und reicht die ViewModels an die Composables durch
@Composable
fun FairSplitApp(deepLinkGroupId: String? = null) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val db = remember { Firebase() }
    val sm = remember { SessionManager(db) }
    val loginVM = remember { LoginViewModel(db, sm) }
    val regVM = remember { RegistrationViewModel(db) }
    val gVM = remember { GroupViewModel(db, sm) }
    val sVM = remember { SpendingViewModel(db) }
    val uVM = remember { UserViewModel(sm, db)}

    val persistentGroupId = remember { mutableStateOf(deepLinkGroupId) }

    val user by sm.currentUser
    var isSessionLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit){
        sm.loadSession(context) { loadedUser ->
            isSessionLoading = false
        }
    }

    LaunchedEffect(isSessionLoading) {
        if (!isSessionLoading) {
            if (user != null) {  //Wenn man bereits eingelogt ist
                if (navController.currentBackStackEntry?.destination?.route != "main") { //Gelangt man sofort auf den MainScreen
                    navController.navigate("main") {
                        popUpTo("loading") { inclusive = true } //LoginScreen wird aus dem Backstack genommen, damit man mit dem ZurückButton nicht wieder dorthin kommt
                        launchSingleTop = true
                    }
                }
                deepLinkGroupId?.let { id ->   //Wenn die App mit einem DeepLink geöffnet wird, wird die entsprechende Gruppe angezeigt
                    navController.navigate("groupdetails/$id")
                }
            } else {  //Wenn man noch nicht eingelogt ist, gelangt man zum login
                navController.navigate("login") {
                    popUpTo("loading") { inclusive = true }
                }
            }
        }
    }


    AppNavigation(
        context,
        navController = navController,
        logVM = loginVM,
        regVM = regVM,
        gVM = gVM,
        sVM = sVM,
        uVM = uVM,
        sm = sm,
        deepLinkGroupId = persistentGroupId.value,
        startDestination = "loading",
    )
}

@Composable
fun AppNavigation(
    context: Context,
    navController: NavHostController,
    logVM: LoginViewModel,
    regVM: RegistrationViewModel,
    gVM: GroupViewModel,
    sVM: SpendingViewModel,
    uVM: UserViewModel,
    sm: SessionManager,
    deepLinkGroupId: String? = null,
    startDestination: String,
) {
    var groupId by remember { mutableStateOf("") }
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        //NavigationGraph
        composable("loading"){
            LoadingScreen()
        }
        composable("login") {
            Log.d("UserDebugg","Current user: " + sm.getCurrentUser())
            LoginScreen(
                logVM = logVM,
                onLoginSuccess = {
                    if (deepLinkGroupId != null) { //Wenn die App mit einem DeepLink geöffnet wurde, wird nach dem Login zur Gruppe navigiert
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                        navController.navigate("groupdetails/$deepLinkGroupId")
                    } else { //Sonst normal zum MainScreen
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                onRegisterClick = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegistrationScreen(
                regVM = regVM,
                onRegistrationSuccess = { navController.navigate("login"){ popUpTo("register") { inclusive = true } } }
            )
        }
        composable("main") {
            MainScreen(
                uVM = uVM,
                gVM = gVM,
                lVM = logVM,
                onAddGroupClick = { navController.navigate("addGroup") },
                onAddSpendingClick = { navController.navigate("addSpending") },
                onGroupDetailsClick = { groupId -> navController.navigate("groupdetails/$groupId") },
                onSignOut = {
                    logVM.logout(context)
                    navController.navigate("login"){
                        popUpTo(0) { inclusive = true }
                    }
                },
                onFriendList = { navController.navigate("friends")},
            )
        }
        composable("friends"){
            FriendsScreen(
                viewModel = uVM,
                onReturn = { navController.navigate("profile")},
                onBack = {navController.popBackStack()}
            )
        }
        composable("addGroup") {
            AddGroupScreen(
                currentUser = sm.getCurrentUser(),
                onBack = { navController.popBackStack() },
                gVM = gVM,
                uVM = uVM,
                onAddFriends = { navController.navigate("friends")}
            )
            Log.d("SessionManager","AddGroupScreen currentUserparameter: " + sm.getCurrentUser())
        }
        composable("addSpending") {
            AddSpendingScreen(
                activeUser = sm.getCurrentUser(), 
                sVM = sVM,
                gVM = gVM,
                onBack = { navController.popBackStack() },
                groupId = groupId
            )
        }
        composable(
            "settleUp/{groupId}",
            arguments = listOf(
                navArgument("groupId"){
                    type = NavType.StringType
                },
            )
        ) { backStackEntry ->
            val newGroundId = backStackEntry.arguments?.getString("groupId") ?: ""
            LaunchedEffect(newGroundId) {
                groupId = newGroundId
            }
            SettleUpScreen(
                groupId = newGroundId,
                onBack = { navController.popBackStack() },
                gVM = gVM,
                sVM = sVM,
                navController = navController
            )
        }
        composable(
            route = "groupdetails/{groupId}", //route enthält neben dem destination name noch die groupID

            /*
            DeepLink wird schon im LaunchedEffect abgehandelt, der auskommentierte Code würde
            den Screen ein zweites Mal rendern und zu Flackern führen
             */

//            deepLinks = listOf(
//                navDeepLink {
//                    uriPattern = "https://fairsplit-dbf6e.web.app/group/{groupId}"
//                    action = Intent.ACTION_VIEW
//                }
//            ),
            arguments = listOf(
                navArgument("groupId") {
                    type = NavType.StringType
                },
            )
        ) { backStackEntry ->
            val newGroupId = backStackEntry.arguments?.getString("groupId") ?: ""  //groupID wird von rute ausgelesen
            LaunchedEffect(newGroupId) {
                groupId = newGroupId
            }
            GroupDetailScreen(
                groupId = newGroupId,
                onBack = { navController.popBackStack() },
                onAddExpense = {
                    navController.navigate("addSpending")
                },
                gVM = gVM,
                onSuccess = {},
                onFailure = {},
                onSettleUp = {navController.navigate("settleUp/$groupId")},
                onEditMembers = {navController.navigate("editMembers/$groupId")}
            )
        }
        composable("editMembers/{groupId}"){
            backStackEntry -> val groupID = backStackEntry.arguments?.getString("groupId") ?: ""

            EditMembersScreen(
                groupID = groupID,
                gVM = gVM,
                onBack = { navController.popBackStack()},
                uVM = uVM,
                onAddFriends = {navController.navigate("friends")})
        }
    }
}
