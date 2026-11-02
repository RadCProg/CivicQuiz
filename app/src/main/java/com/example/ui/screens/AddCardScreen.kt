package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiCardGenerator
import com.example.data.model.QuizItem
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.CorrectGreenContainer

@Composable
fun AddCardScreen(
    isGeneratingAi: Boolean,
    generatedAiCard: QuizItem?,
    onGenerateAiCard: (String) -> Unit,
    onSaveGeneratedAiCard: (onSuccess: () -> Unit) -> Unit,
    onClearGeneratedAiCard: () -> Unit,
    onCreateManualCard: (question: String, options: List<String>, correctIndex: Int, explanation: String, onSuccess: () -> Unit) -> Unit,
    onCardAddedSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: AI Generation, 1: Manual

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // شريط التبويب
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("الذكاء الاصطناعي ✨", fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إنشاء يدوي ✍️", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            AiGeneratorTab(
                isGenerating = isGeneratingAi,
                generatedCard = generatedAiCard,
                onGenerate = onGenerateAiCard,
                onSave = { onSaveGeneratedAiCard(onCardAddedSuccess) },
                onDiscard = onClearGeneratedAiCard
            )
        } else {
            ManualCardTab(
                onCreateManual = { q, opts, idx, exp ->
                    onCreateManualCard(q, opts, idx, exp, onCardAddedSuccess)
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AiGeneratorTab(
    isGenerating: Boolean,
    generatedCard: QuizItem?,
    onGenerate: (String) -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit
) {
    var selectedTopic by remember { mutableStateOf(AiCardGenerator.availableTopics.first()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "توليد أسئلة الجنسية بالذكاء الاصطناعي",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "اختر الموضوع وسيقوم الذكاء الاصطناعي بصياغة سؤال دقيق مع 4 خيارات وتوضيح قانوني",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // خيارات المواضيع المتاحة
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AiCardGenerator.availableTopics) { topic ->
                FilterChip(
                    selected = selectedTopic == topic,
                    onClick = { selectedTopic = topic },
                    label = { Text(topic) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onGenerate(selectedTopic) },
            enabled = !isGenerating,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("ai_generate_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("جاري توليد السؤال بالذكاء الاصطناعي...")
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("توليد سؤال جديد في: $selectedTopic", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // معاينة السؤال المولد
        if (generatedCard != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_generated_preview_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "تم التوليد بنجاح ✨",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = generatedCard.question,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    generatedCard.options.forEachIndexed { index, option ->
                        val isCorrect = index == generatedCard.correctIndex
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCorrect) CorrectGreenContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, if (isCorrect) CorrectGreen else Color.Transparent)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isCorrect) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = CorrectGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isCorrect) CorrectGreen else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    if (generatedCard.explanation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "الشرح: ${generatedCard.explanation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDiscard,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تجاهل")
                        }

                        Button(
                            onClick = onSave,
                            modifier = Modifier
                                .weight(2f)
                                .testTag("save_ai_card_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen)
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حفظ في قاعدة البيانات", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualCardTab(
    onCreateManual: (question: String, options: List<String>, correctIndex: Int, explanation: String) -> Unit
) {
    var questionText by remember { mutableStateOf("") }
    var option0 by remember { mutableStateOf("") }
    var option1 by remember { mutableStateOf("") }
    var option2 by remember { mutableStateOf("") }
    var option3 by remember { mutableStateOf("") }
    var correctIndex by remember { mutableIntStateOf(0) }
    var explanationText by remember { mutableStateOf("") }

    val options = listOf(option0, option1, option2, option3)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "إنشاء بطاقة سؤال يدوي",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "أدخل نص السؤال، وأربعة خيارات للإجابة، وحدد الخيار الصحيح",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // حقل نص السؤال
        OutlinedTextField(
            value = questionText,
            onValueChange = { questionText = it },
            label = { Text("نص السؤال *") },
            placeholder = { Text("مثال: كم عدد أعضاء مجلس النواب الأمريكي؟") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("manual_question_input"),
            shape = RoundedCornerShape(12.dp),
            minLines = 2
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "الخيارات (اختر الإجابة الصحيحة بالضغط على الدائرة):",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // الخيار الأول
        OptionInputField(
            index = 0,
            text = option0,
            onTextChange = { option0 = it },
            isCorrect = correctIndex == 0,
            onSelectCorrect = { correctIndex = 0 }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // الخيار الثاني
        OptionInputField(
            index = 1,
            text = option1,
            onTextChange = { option1 = it },
            isCorrect = correctIndex == 1,
            onSelectCorrect = { correctIndex = 1 }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // الخيار الثالث
        OptionInputField(
            index = 2,
            text = option2,
            onTextChange = { option2 = it },
            isCorrect = correctIndex == 2,
            onSelectCorrect = { correctIndex = 2 }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // الخيار الرابع
        OptionInputField(
            index = 3,
            text = option3,
            onTextChange = { option3 = it },
            isCorrect = correctIndex == 3,
            onSelectCorrect = { correctIndex = 3 }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // الشرح والتوضيح
        OutlinedTextField(
            value = explanationText,
            onValueChange = { explanationText = it },
            label = { Text("الشرح والتوضيح (اختياري)") },
            placeholder = { Text("معلومات قانونية أو تاريخية إضافية تشرح سبب الإجابة...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("manual_explanation_input"),
            shape = RoundedCornerShape(12.dp),
            minLines = 2
        )

        Spacer(modifier = Modifier.height(20.dp))

        val isValid = questionText.isNotBlank() && option0.isNotBlank() && option1.isNotBlank()

        Button(
            onClick = {
                val finalOptions = options.filter { it.isNotBlank() }
                val safeCorrect = if (correctIndex >= finalOptions.size) 0 else correctIndex
                onCreateManual(questionText, finalOptions, safeCorrect, explanationText)
                // إعادة ضبط الحقول
                questionText = ""
                option0 = ""
                option1 = ""
                option2 = ""
                option3 = ""
                explanationText = ""
                correctIndex = 0
            },
            enabled = isValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_manual_card_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.AddCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("حفظ السؤال في بنك الأسئلة", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OptionInputField(
    index: Int,
    text: String,
    onTextChange: (String) -> Unit,
    isCorrect: Boolean,
    onSelectCorrect: () -> Unit
) {
    val labels = listOf("الخيار الأول", "الخيار الثاني", "الخيار الثالث", "الخيار الرابع")
    val label = labels.getOrElse(index) { "خيار ${index + 1}" }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isCorrect,
            onClick = onSelectCorrect,
            modifier = Modifier.testTag("radio_correct_$index")
        )
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            label = { Text(if (isCorrect) "$label (الإجابة الصحيحة)" else label) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("option_input_$index"),
            shape = RoundedCornerShape(10.dp)
        )
    }
}
