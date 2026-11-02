package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.QuizViewModel
import com.example.ui.screens.AddCardScreen
import com.example.ui.screens.AllCardsScreen
import com.example.ui.screens.QuizReviewScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    quizViewModel: QuizViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var currentTab by remember { mutableIntStateOf(0) } // 0: Review, 1: Library, 2: Add, 3: Stats

    // طلب صلاحية الإشعارات لأندرويد 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!isGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // مراقبة حالات ViewModel
    val dueQuestions by quizViewModel.dueQuestions.collectAsStateWithLifecycle()
    val allQuestions by quizViewModel.allQuestions.collectAsStateWithLifecycle()
    val currentIndex by quizViewModel.currentQuestionIndex.collectAsStateWithLifecycle()
    val selectedOption by quizViewModel.selectedOption.collectAsStateWithLifecycle()
    val isAnswerRevealed by quizViewModel.isAnswerRevealed.collectAsStateWithLifecycle()
    val isGeneratingAi by quizViewModel.isGeneratingAi.collectAsStateWithLifecycle()
    val generatedAiCard by quizViewModel.generatedAiCard.collectAsStateWithLifecycle()
    val toastMessage by quizViewModel.toastMessage.collectAsStateWithLifecycle()

    // إظهار رسائل Snackbar
    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            quizViewModel.clearToastMessage()
        }
    }

    // دعم زر الرجوع للعودة إلى شاشة المراجعة الرئيسية
    BackHandler(enabled = currentTab != 0) {
        currentTab = 0
    }

    // توفير اتجاه الواجهة من اليمين إلى اليسار (RTL) للنصوص العربية
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "CivicQuiz | اختبار الجنسية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    // تبويب المراجعة
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        label = { Text("المراجعة") },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (dueQuestions.isNotEmpty()) {
                                        Badge { Text("${dueQuestions.size}") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.School, contentDescription = "شاشة المراجعة")
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_review_tab")
                    )

                    // تبويب المكتبة
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        label = { Text("المكتبة") },
                        icon = {
                            Icon(Icons.Default.LibraryBooks, contentDescription = "شاشة جميع البطاقات")
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_library_tab")
                    )

                    // تبويب إضافة بطاقة
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        label = { Text("إضافة/AI") },
                        icon = {
                            Icon(Icons.Default.AddCircle, contentDescription = "شاشة إضافة سؤال")
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_add_tab")
                    )

                    // تبويب الإحصائيات
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { currentTab = 3 },
                        label = { Text("الإحصائيات") },
                        icon = {
                            Icon(Icons.Default.Insights, contentDescription = "شاشة الإحصائيات")
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_stats_tab")
                    )
                }
            }
        ) { innerPadding ->
            val contentModifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)

            when (currentTab) {
                0 -> {
                    QuizReviewScreen(
                        dueQuestions = dueQuestions,
                        currentIndex = currentIndex,
                        selectedOption = selectedOption,
                        isAnswerRevealed = isAnswerRevealed,
                        onOptionSelected = quizViewModel::onOptionSelected,
                        onRevealAnswer = quizViewModel::revealAnswer,
                        onRateQuality = quizViewModel::onRateAndSubmitQuality,
                        onNavigateToAddCard = { currentTab = 2 },
                        modifier = contentModifier
                    )
                }
                1 -> {
                    AllCardsScreen(
                        questions = allQuestions,
                        onDeleteQuestion = quizViewModel::deleteQuestion,
                        modifier = contentModifier
                    )
                }
                2 -> {
                    AddCardScreen(
                        isGeneratingAi = isGeneratingAi,
                        generatedAiCard = generatedAiCard,
                        onGenerateAiCard = quizViewModel::generateQuestionWithAi,
                        onSaveGeneratedAiCard = { onSuccess ->
                            quizViewModel.saveGeneratedAiQuestion(onSuccess)
                        },
                        onClearGeneratedAiCard = quizViewModel::clearGeneratedAiCard,
                        onCreateManualCard = { q, opts, idx, exp, onSuccess ->
                            quizViewModel.createManualQuestion(q, opts, idx, exp, onSuccess)
                        },
                        onCardAddedSuccess = {
                            currentTab = 1 // الانتقال للمكتبة لمشاهدة السؤال المضاف
                        },
                        modifier = contentModifier
                    )
                }
                3 -> {
                    StatsScreen(
                        questions = allQuestions,
                        onTriggerTestNotification = quizViewModel::triggerTestNotification,
                        onResetAllProgress = quizViewModel::resetAllProgress,
                        modifier = contentModifier
                    )
                }
            }
        }
    }
}
