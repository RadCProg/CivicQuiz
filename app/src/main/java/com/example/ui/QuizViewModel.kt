package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiCardGenerator
import com.example.data.assets.AssetIngestion
import com.example.data.db.AppDatabase
import com.example.data.model.QuizItem
import com.example.data.repository.QuizRepository
import com.example.logic.SM2Calculator
import com.example.worker.WorkScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel لإدارة منطق العرض وتدفق المراجعة وتطبيق خوارزمية SM-2 وتوليد البطاقات
 */
class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QuizRepository

    // تدفق الأسئلة المستحقة للمراجعة اليوم
    val dueQuestions: StateFlow<List<QuizItem>>

    // تدفق جميع الأسئلة المسجلة في المكتبة
    val allQuestions: StateFlow<List<QuizItem>>

    // مؤشر السؤال الحالي أثناء جلسة المراجعة
    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    // الخيار الذي حدده المستخدم حالياً
    private val _selectedOption = MutableStateFlow<Int?>(null)
    val selectedOption: StateFlow<Int?> = _selectedOption.asStateFlow()

    // هل تم كشف وتأكيد الإجابة لإظهار الشرح وأزرار SM-2
    private val _isAnswerRevealed = MutableStateFlow(false)
    val isAnswerRevealed: StateFlow<Boolean> = _isAnswerRevealed.asStateFlow()

    // حالة توليد بطاقة بالذكاء الاصطناعي
    private val _isGeneratingAi = MutableStateFlow(false)
    val isGeneratingAi: StateFlow<Boolean> = _isGeneratingAi.asStateFlow()

    private val _generatedAiCard = MutableStateFlow<QuizItem?>(null)
    val generatedAiCard: StateFlow<QuizItem?> = _generatedAiCard.asStateFlow()

    // رسائل التنبيه والنجاح المؤقتة
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        val database = AppDatabase.getInstance(application)
        repository = QuizRepository(database.quizDao())

        val currentTime = System.currentTimeMillis()
        dueQuestions = repository.getDueQuestions(currentTime)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        allQuestions = repository.allQuestions
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // تهيئة البيانات الأولية من مجلد assets إن كانت قاعدة البيانات فارغة
        viewModelScope.launch {
            AssetIngestion.ingestQuestionsIfEmpty(application, repository)
        }

        // تسجيل جدولة الفحص اليومي عبر WorkManager
        WorkScheduler.scheduleDailyReviewReminder(application)
    }

    /**
     * اختيار المستخدم لأحد الخيارات
     */
    fun onOptionSelected(index: Int) {
        if (!_isAnswerRevealed.value) {
            _selectedOption.value = index
        }
    }

    /**
     * تأكيد الإجابة وكشف الشرح والتقييم
     */
    fun revealAnswer() {
        if (_selectedOption.value != null) {
            _isAnswerRevealed.value = true
        }
    }

    /**
     * الدالة الأساسية: تقييم السؤال بواسطة المستخدم (quality 0..5)،
     * تطبيق خوارزمية SM-2، وتحديث السؤال في قاعدة بيانات Room عبر الـ DAO.
     */
    fun onRateAndSubmitQuality(item: QuizItem, quality: Int) {
        viewModelScope.launch {
            // 1. تطبيق خوارزمية SuperMemo-2 لحساب الفاصل الزمني وموعد المراجعة القادمة
            val updatedItem = SM2Calculator.calculateSM2(item, quality)

            // 2. تحديث السؤال في قاعدة البيانات عبر Repository و Dao
            repository.update(updatedItem)

            // 3. إعادة ضبط حالة السؤال التالي
            _selectedOption.value = null
            _isAnswerRevealed.value = false

            // إشعار بسيط بالفاصل الزمني الجديد
            val nextDays = updatedItem.interval
            _toastMessage.value = if (quality >= 3) {
                "أحسنت! المراجعة القادمة بعد $nextDays يوم."
            } else {
                "ستتم إعادة مراجعة السؤال غداً للتمكين."
            }
        }
    }

    /**
     * الانتقال إلى السؤال التالي في الجلسة يدويًا
     */
    fun nextQuestion(totalCount: Int) {
        if (_currentQuestionIndex.value < totalCount - 1) {
            _currentQuestionIndex.value += 1
            _selectedOption.value = null
            _isAnswerRevealed.value = false
        }
    }

    /**
     * إنشاء سؤال يدوي وحفظه في Room
     */
    fun createManualQuestion(
        question: String,
        options: List<String>,
        correctIndex: Int,
        explanation: String,
        onSuccess: () -> Unit
    ) {
        if (question.isBlank() || options.size < 2 || options.any { it.isBlank() }) {
            _toastMessage.value = "يرجى كتابة السؤال وخيارين على الأقل."
            return
        }

        viewModelScope.launch {
            val newItem = QuizItem(
                id = 0,
                question = question.trim(),
                options = options.map { it.trim() },
                correctIndex = correctIndex.coerceIn(0, options.size - 1),
                explanation = explanation.trim(),
                interval = 0,
                repetition = 0,
                easeFactor = 2.5f,
                nextReviewDate = System.currentTimeMillis() // متاح للمراجعة فوراً
            )
            repository.insert(newItem)
            _toastMessage.value = "تمت إضافة السؤال بنجاح!"
            onSuccess()
        }
    }

    /**
     * توليد سؤال اختبار الجنسية ذكياً بالذكاء الاصطناعي
     */
    fun generateQuestionWithAi(topic: String) {
        viewModelScope.launch {
            _isGeneratingAi.value = true
            _generatedAiCard.value = null
            try {
                val generated = AiCardGenerator.generateQuizItem(topic)
                _generatedAiCard.value = generated
                _toastMessage.value = "تم توليد السؤال بنجاح! راجع التفاصيل ثم اضغط حفظ."
            } catch (e: Exception) {
                _toastMessage.value = "تعذر التوليد حالياً: ${e.message}"
            } finally {
                _isGeneratingAi.value = false
            }
        }
    }

    /**
     * حفظ السؤال المولد بالذكاء الاصطناعي في قاعدة البيانات
     */
    fun saveGeneratedAiQuestion(onSuccess: () -> Unit) {
        val item = _generatedAiCard.value ?: return
        viewModelScope.launch {
            repository.insert(item)
            _generatedAiCard.value = null
            _toastMessage.value = "تم حفظ سؤال الذكاء الاصطناعي في المكتبة بنجاح!"
            onSuccess()
        }
    }

    fun clearGeneratedAiCard() {
        _generatedAiCard.value = null
    }

    /**
     * حذف سؤال
     */
    fun deleteQuestion(item: QuizItem) {
        viewModelScope.launch {
            repository.delete(item)
            _toastMessage.value = "تم حذف السؤال بنجاح."
        }
    }

    /**
     * إعادة تعيين تقدم التعلم لجميع الأسئلة
     */
    fun resetAllProgress() {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            repository.resetAll(currentTime)
            _currentQuestionIndex.value = 0
            _selectedOption.value = null
            _isAnswerRevealed.value = false
            _toastMessage.value = "تمت إعادة تعيين تقدم جميع الأسئلة بنجاح."
        }
    }

    /**
     * تشغيل إشعار فحص تجريبي للتأكد من عمل WorkManager والإشعارات
     */
    fun triggerTestNotification() {
        WorkScheduler.triggerImmediateCheck(getApplication())
        _toastMessage.value = "تم إطلاق فحص الإشعار التجريبي في الخلفية..."
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }
}
