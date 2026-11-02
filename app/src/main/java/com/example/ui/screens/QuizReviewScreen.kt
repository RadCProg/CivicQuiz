package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuizItem
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.CorrectGreenContainer
import com.example.ui.theme.EasyGreen
import com.example.ui.theme.GoodBlue
import com.example.ui.theme.HardOrange
import com.example.ui.theme.WrongRed
import com.example.ui.theme.WrongRedContainer

@Composable
fun QuizReviewScreen(
    dueQuestions: List<QuizItem>,
    currentIndex: Int,
    selectedOption: Int?,
    isAnswerRevealed: Boolean,
    onOptionSelected: (Int) -> Unit,
    onRevealAnswer: () -> Unit,
    onRateQuality: (QuizItem, Int) -> Unit,
    onNavigateToAddCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (dueQuestions.isEmpty()) {
        // حالة عدم وجود أسئلة مستحقة للمراجعة اليوم
        EmptyReviewState(onNavigateToAddCard = onNavigateToAddCard, modifier = modifier)
    } else {
        val safeIndex = currentIndex.coerceIn(0, dueQuestions.size - 1)
        val currentItem = dueQuestions[safeIndex]

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // شريط التقدم والعدّاد
            ReviewHeader(
                currentIndex = safeIndex + 1,
                totalCount = dueQuestions.size,
                repetition = currentItem.repetition,
                intervalDays = currentItem.interval
            )

            Spacer(modifier = Modifier.height(16.dp))

            // بطاقة السؤال الرئيسية
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quiz_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // وسم اختبار الجنسية
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "اختبار الجنسية الأمريكية (Civics)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // نص السؤال
                    Text(
                        text = currentItem.question,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 26.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // قائمة الخيارات
                    currentItem.options.forEachIndexed { index, optionText ->
                        OptionButton(
                            optionIndex = index,
                            optionText = optionText,
                            isSelected = selectedOption == index,
                            isAnswerRevealed = isAnswerRevealed,
                            isCorrect = index == currentItem.correctIndex,
                            onClick = { onOptionSelected(index) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // زر كشف وتأكيد الإجابة
                    if (!isAnswerRevealed) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onRevealAnswer,
                            enabled = selectedOption != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("check_answer_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedOption == null) "اختر إجابة للتحقق" else "تأكيد الإجابة",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // قسم الشرح وأزرار خوارزمية SM-2 عند كشف الإجابة
            AnimatedVisibility(
                visible = isAnswerRevealed,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // بطاقة الشرح والتوضيح
                    if (currentItem.explanation.isNotBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("explanation_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "الشرح والتوضيح القانوني:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentItem.explanation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // بطاقة تقييم خوارزمية SM-2
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sm2_rating_section"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "قيّم سهولة التذكر (SuperMemo-2):",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "تحدد الخوارزمية موعد المراجعة القادمة حسب دقة وسرعة تذكرك",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            // أزرار التقييم الأربعة (إعادة، صعب، جيد، سهل)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // زر إعادة (Again - Quality 1)
                                Sm2Button(
                                    label = "إعادة",
                                    subtext = "غداً",
                                    backgroundColor = WrongRed,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sm2_again_button"),
                                    onClick = { onRateQuality(currentItem, 1) }
                                )

                                // زر صعب (Hard - Quality 2)
                                Sm2Button(
                                    label = "صعب",
                                    subtext = "يوم 1",
                                    backgroundColor = HardOrange,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sm2_hard_button"),
                                    onClick = { onRateQuality(currentItem, 2) }
                                )

                                // زر جيد (Good - Quality 4)
                                Sm2Button(
                                    label = "جيد",
                                    subtext = if (currentItem.repetition == 0) "يوم 1" else if (currentItem.repetition == 1) "6 أيام" else "+فترة",
                                    backgroundColor = GoodBlue,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sm2_good_button"),
                                    onClick = { onRateQuality(currentItem, 4) }
                                )

                                // زر سهل (Easy - Quality 5)
                                Sm2Button(
                                    label = "سهل جداً",
                                    subtext = "مضاعف",
                                    backgroundColor = EasyGreen,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sm2_easy_button"),
                                    onClick = { onRateQuality(currentItem, 5) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReviewHeader(
    currentIndex: Int,
    totalCount: Int,
    repetition: Int,
    intervalDays: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "السؤال $currentIndex من $totalCount",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "التكرار: $repetition | الفاصل: ${intervalDays}د",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { currentIndex.toFloat() / totalCount.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun OptionButton(
    optionIndex: Int,
    optionText: String,
    isSelected: Boolean,
    isAnswerRevealed: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val optionLetters = listOf("أ", "ب", "ج", "د")
    val letter = optionLetters.getOrElse(optionIndex) { "${optionIndex + 1}" }

    val (containerColor, borderColor, contentColor) = when {
        isAnswerRevealed && isCorrect -> {
            Triple(CorrectGreenContainer, CorrectGreen, CorrectGreen)
        }
        isAnswerRevealed && isSelected && !isCorrect -> {
            Triple(WrongRedContainer, WrongRed, WrongRed)
        }
        isSelected -> {
            Triple(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primary
            )
        }
        else -> {
            Triple(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                MaterialTheme.colorScheme.onSurface
            )
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = !isAnswerRevealed, onClick = onClick)
            .testTag("option_$optionIndex"),
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected || (isAnswerRevealed && isCorrect)) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // مؤشر الحرف (أ، ب، ج، د)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .then(
                        if (isAnswerRevealed && isCorrect) Modifier else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = if (isSelected || (isAnswerRevealed && isCorrect)) borderColor else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isAnswerRevealed && isCorrect) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        } else if (isAnswerRevealed && isSelected) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = letter,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // نص الخيار
            Text(
                text = optionText,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Sm2Button(
    label: String,
    subtext: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun EmptyReviewState(
    onNavigateToAddCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(90.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "رائع! أنهيت مراجعة اليوم 🎉",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "لا توجد أسئلة مستحقة للمراجعة في الوقت الحالي حسب خوارزمية SM-2. تم نقل الأسئلة لمواعيدها القادمة لتثبيت الذاكرة.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedButton(
                onClick = onNavigateToAddCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("empty_state_add_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إضافة أو توليد أسئلة جديدة بالذكاء الاصطناعي")
            }
        }
    }
}
