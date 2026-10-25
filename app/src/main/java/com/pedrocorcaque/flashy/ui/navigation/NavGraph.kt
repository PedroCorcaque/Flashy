package com.pedrocorcaque.flashy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pedrocorcaque.flashy.FlashyApplication
import com.pedrocorcaque.flashy.ui.screens.home.HomeScreen
import com.pedrocorcaque.flashy.ui.screens.home.HomeViewModel
import com.pedrocorcaque.flashy.ui.screens.home.HomeViewModelFactory
import com.pedrocorcaque.flashy.ui.screens.topic.TopicScreen
import com.pedrocorcaque.flashy.ui.screens.topic.TopicViewModel
import com.pedrocorcaque.flashy.ui.screens.topic.TopicViewModelFactory
import com.pedrocorcaque.flashy.ui.screens.flashcard.FlashcardScreen
import com.pedrocorcaque.flashy.ui.screens.flashcard.FlashcardViewModel
import com.pedrocorcaque.flashy.ui.screens.flashcard.FlashcardViewModelFactory
import com.pedrocorcaque.flashy.ui.screens.addedit.AddEditCardScreen
import com.pedrocorcaque.flashy.ui.screens.addedit.AddEditCardViewModel
import com.pedrocorcaque.flashy.ui.screens.addedit.AddEditCardViewModelFactory
import com.pedrocorcaque.flashy.ui.screens.addedit.AddTopicScreen
import com.pedrocorcaque.flashy.ui.screens.addedit.AddTopicViewModel
import com.pedrocorcaque.flashy.ui.screens.addedit.AddTopicViewModelFactory
import com.pedrocorcaque.flashy.ui.screens.settings.SettingsScreen
import com.pedrocorcaque.flashy.ui.screens.summary.SummaryScreen
import com.pedrocorcaque.flashy.ui.screens.summary.SummaryViewModel
import com.pedrocorcaque.flashy.ui.screens.summary.SummaryViewModelFactory

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Topic : Screen("topic/{topicId}") {
        fun createRoute(topicId: String) = "topic/$topicId"
    }
    object Flashcard : Screen("flashcard/{topicId}?inverted={inverted}&shuffle={shuffle}&subtopicId={subtopicId}") {
        fun createRoute(
            topicId: String,
            inverted: Boolean = false,
            subtopicId: String? = null,
            shuffle: Boolean = false
        ): String {
            val base = "flashcard/$topicId?inverted=$inverted&shuffle=$shuffle"
            return if (subtopicId != null) "$base&subtopicId=$subtopicId" else base
        }
    }
    object AddEditCard : Screen("addEditCard/{topicId}?cardId={cardId}&subtopicId={subtopicId}") {
        fun createRoute(topicId: String, cardId: String? = null, subtopicId: String? = null): String {
            val params = listOfNotNull(
                cardId?.let { "cardId=$it" },
                subtopicId?.let { "subtopicId=$it" }
            )
            return "addEditCard/$topicId" + if (params.isEmpty()) "" else "?" + params.joinToString("&")
        }
    }
    object AddTopic : Screen("addTopic?topicId={topicId}") {
        fun createRoute(topicId: String? = null) =
            if (topicId != null) "addTopic?topicId=$topicId" else "addTopic"
    }
    object Settings : Screen("settings")
    object Summary : Screen("summary")
}

@Composable
fun FlashyNavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val repository = (context.applicationContext as FlashyApplication).repository

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))
            HomeScreen(
                viewModel = viewModel,
                onTopicClick = { topicId -> 
                    navController.navigate(Screen.Topic.createRoute(topicId))
                },
                onAddTopicClick = {
                    navController.navigate(Screen.AddTopic.createRoute())
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onSummaryClick = {
                    navController.navigate(Screen.Summary.route)
                },
                onStudyClick = { topicId, shuffle ->
                    navController.navigate(Screen.Flashcard.createRoute(topicId, shuffle = shuffle))
                }
            )
        }
        composable(
            route = Screen.Topic.route,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId") ?: ""
            val viewModel: TopicViewModel = viewModel(
                factory = TopicViewModelFactory(repository, topicId),
                key = topicId
            )
            TopicScreen(
                viewModel = viewModel,
                topicId = topicId,
                onBackClick = { navController.popBackStack() },
                onEditTopicClick = {
                    navController.navigate(Screen.AddTopic.createRoute(topicId))
                },
                onAddCardClick = { subtopicId ->
                    navController.navigate(Screen.AddEditCard.createRoute(topicId, subtopicId = subtopicId))
                },
                onEditCardClick = { cardId ->
                    navController.navigate(Screen.AddEditCard.createRoute(topicId, cardId))
                },
                onStudyClick = { isInverted, subtopicId, shuffle ->
                    navController.navigate(Screen.Flashcard.createRoute(topicId, isInverted, subtopicId, shuffle))
                }
            )
        }
        composable(
            route = Screen.Flashcard.route,
            arguments = listOf(
                navArgument("topicId") { type = NavType.StringType },
                navArgument("inverted") { type = NavType.BoolType; defaultValue = false },
                navArgument("shuffle") { type = NavType.BoolType; defaultValue = false },
                navArgument("subtopicId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId") ?: ""
            val isInverted = backStackEntry.arguments?.getBoolean("inverted") ?: false
            val subtopicId = backStackEntry.arguments?.getString("subtopicId")
            val shuffle = backStackEntry.arguments?.getBoolean("shuffle") ?: false
            val viewModel: FlashcardViewModel = viewModel(
                factory = FlashcardViewModelFactory(repository, topicId, subtopicId, shuffle),
                key = "$topicId-${subtopicId ?: "all"}-$shuffle"
            )
            FlashcardScreen(
                viewModel = viewModel,
                topicId = topicId,
                isInverted = isInverted,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.AddEditCard.route,
            arguments = listOf(
                navArgument("topicId") { type = NavType.StringType },
                navArgument("cardId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("subtopicId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId") ?: ""
            val cardId = backStackEntry.arguments?.getString("cardId")
            val subtopicId = backStackEntry.arguments?.getString("subtopicId")
            val viewModel: AddEditCardViewModel = viewModel(
                factory = AddEditCardViewModelFactory(repository, topicId, cardId, subtopicId),
                key = cardId ?: topicId
            )
            AddEditCardScreen(
                viewModel = viewModel,
                topicId = topicId,
                cardId = cardId,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.AddTopic.route,
            arguments = listOf(
                navArgument("topicId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId")
            val viewModel: AddTopicViewModel = viewModel(
                factory = AddTopicViewModelFactory(repository, topicId),
                key = topicId ?: "new-topic"
            )
            AddTopicScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                isEditing = topicId != null
            )
        }
        composable(Screen.Summary.route) {
            val viewModel: SummaryViewModel = viewModel(factory = SummaryViewModelFactory(repository))
            SummaryScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
