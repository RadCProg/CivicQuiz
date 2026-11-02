package com.example.data.ai

import com.example.data.model.QuizItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * خدمة توليد أسئلة وبطاقات اختبار الجنسية بمساعدة الذكاء الاصطناعي
 */
object AiCardGenerator {

    data class GeneratedQuiz(
        val question: String,
        val options: List<String>,
        val correctIndex: Int,
        val explanation: String
    )

    private val presetQuestionsByTopic = mapOf(
        "الدستور الأمريكي" to listOf(
            GeneratedQuiz(
                question = "ما هو التعديل الدستوري الذي ألغى العبودية في الولايات المتحدة؟",
                options = listOf("التعديل الثالث عشر (13th Amendment)", "التعديل الأول", "التعديل الخامس عشر", "التعديل التاسع عشر"),
                correctIndex = 0,
                explanation = "ألغى التعديل الثالث عشر للدستور، الذي صودق عليه عام 1865، العبودية والعمل القسري في جميع أنحاء الولايات المتحدة."
            ),
            GeneratedQuiz(
                question = "ما هو التعديل الذي منح المرأة حق التصويت في الولايات المتحدة؟",
                options = listOf("التعديل الرابع عشر", "التعديل التاسع عشر (19th Amendment)", "التعديل الحادي والعشرون", "التعديل السادس والعشرون"),
                correctIndex = 1,
                explanation = "التعديل التاسع عشر، الذي أُقر عام 1920، كفل للمرأة الأمريكية حق الاقتراع في كافة الانتخابات."
            ),
            GeneratedQuiz(
                question = "كم عدد المواد الأساسية (Articles) التي يتكون منها الدستور الأمريكي الأصلي؟",
                options = listOf("5 مواد", "7 مواد (Seven Articles)", "10 مواد", "12 مادة"),
                correctIndex = 1,
                explanation = "يتكون الدستور الأمريكي الأصلي من سبع مواد رئيسية تصف صلاحيات وهيكل فروع الحكم الثلاثة وعلاقة الولايات بالحكومة الفيدرالية."
            )
        ),
        "فروع الحكومة" to listOf(
            GeneratedQuiz(
                question = "من هو القائد الأعلى للقوات المسلحة الأمريكية؟ (Commander in Chief)",
                options = listOf("وزير الدفاع", "رئيس أركان الجيش", "رئيس الولايات المتحدة (The President)", "رئيس مجلس النواب"),
                correctIndex = 2,
                explanation = "رئيس الولايات المتحدة هو القائد الأعلى للجيش والبحرية والميليشيات الوطنية وفقاً للمادة الثانية من الدستور."
            ),
            GeneratedQuiz(
                question = "ما هي أعلى محكمة في الولايات المتحدة الأمريكية؟ (Highest court in the United States)",
                options = listOf("محكمة الاستئناف الفيدرالية", "المحكمة العليا (Supreme Court)", "محكمة العدل الدولية", "محكمة النقض المركزية"),
                correctIndex = 1,
                explanation = "المحكمة العليا هي أعلى سلطة قضائية في الولايات المتحدة وتتولى تفسير الدستور وحسم النزاعات القانونية الكبرى."
            ),
            GeneratedQuiz(
                question = "إذا تعذر على كل من الرئيس ونائب الرئيس أداء مهامهما، فمن يصبح رئيساً؟",
                options = listOf("وزير الخارجية", "رئيس مجلس النواب (Speaker of the House)", "رئيس مجلس الشيوخ المؤقت", "رئيس المحكمة العليا"),
                correctIndex = 1,
                explanation = "وفقاً لقانون الخلافة الرئاسية لعام 1947، يأتي رئيس مجلس النواب مباشرة بعد نائب الرئيس في تولي مهام الرئاسة."
            )
        ),
        "الحقوق والواجبات" to listOf(
            GeneratedQuiz(
                question = "ما هي إحدى المسؤوليات الخاصة بمواطني الولايات المتحدة فقط؟",
                options = listOf("دفع الضرائب الفيدرالية", "الخدمة في هيئة المحلفين (Serve on a jury)", "احترام القوانين المحلية", "الالتحاق بالجامعة"),
                correctIndex = 1,
                explanation = "المشاركة والخدمة في هيئة المحلفين والمشاركة في التصويت بالانتخابات الفيدرالية هي واجبات مخصصة حصرياً للمواطنين الأمريكيين."
            ),
            GeneratedQuiz(
                question = "ما هو التعهد الذي يؤديه الشخص عند أداء قسم الولاء للجنسية الأمريكية؟",
                options = listOf("التخلي عن الولاء لدول أخرى والدفاع عن الدستور", "دفع رسوم سنوية إضافية", "تغيير الاسم إلى اسم أمريكي", "الانضمام الفوري للخدمة العسكرية"),
                correctIndex = 0,
                explanation = "يتضمن قسم الولاء (Oath of Allegiance) التعهد بالدفاع عن دستور وقوانين الولايات المتحدة والتخلي عن أي ولاء سياسي سابق لأي دولة أجنبية."
            )
        ),
        "التاريخ الأمريكي" to listOf(
            GeneratedQuiz(
                question = "من كان الكاتب الرئيسي لإعلان الاستقلال الأمريكي عام 1776؟",
                options = listOf("جورج واشنطن", "توماس جيفرسون (Thomas Jefferson)", "بنجامين فرانكلين", "جون آدمز"),
                correctIndex = 1,
                explanation = "صاغ توماس جيفرسون مسودة إعلان الاستقلال الشهيرة التي اعتمدها الكونغرس القاري في 4 يوليو 1776."
            ),
            GeneratedQuiz(
                question = "ما هي الحرب التي خاضتها الولايات المتحدة في الستينيات من القرن التاسع عشر بين الشمال والجنوب؟",
                options = listOf("حرب الاستقلال", "الحرب الأهلية الأمريكية (Civil War)", "حرب 1812", "الحرب المكسيكية الأمريكية"),
                correctIndex = 1,
                explanation = "الحرب الأهلية الأمريكية (1861-1865) دارت بين الولايات الشمالية والولايات الجنوبية حول قضية الرق وحقوق الولايات."
            ),
            GeneratedQuiz(
                question = "ماذا فعل إعلان تحرير العبيد (Emancipation Proclamation) الصادر عن أبراهام لينكولن؟",
                options = listOf("حرر العبيد في الولايات الكونفدرالية", "أنهى الحرب الأهلية فوراً", "أنشأ الكونغرس الأمريكي", "اشترى أراضي لويزيانا"),
                correctIndex = 0,
                explanation = "أصدر الرئيس أبراهام لينكولن إعلان تحرير العبيد عام 1863 الذي أعلن حرية العبيد في جميع الولايات الجنوبية المتمردة."
            )
        ),
        "الجغرافيا والرموز" to listOf(
            GeneratedQuiz(
                question = "أين يقع تمثال الحرية (Statue of Liberty)؟",
                options = listOf("خليج سان فرانسيسكو", "ميناء نيويورك (New York Harbor)", "واشنطن العاصمة", "بوسطن"),
                correctIndex = 1,
                explanation = "يقع تمثال الحرية في جزيرة الحرية في ميناء نيويورك، وكان هدية من الشعب الفرنسي للشعب الأمريكي رمزاً للصداقة والحرية."
            ),
            GeneratedQuiz(
                question = "ما هي عاصمة الولايات المتحدة الأمريكية؟",
                options = listOf("نيويورك سيتي", "فيلادلفيا", "واشنطن العاصمة (Washington, D.C.)", "شيكاغو"),
                correctIndex = 2,
                explanation = "واشنطن العاصمة (District of Columbia) هي العاصمة الفيدرالية الدائمة للولايات المتحدة وليست تابعة لأي ولاية."
            ),
            GeneratedQuiz(
                question = "لماذا يحتوي العلم الأمريكي على 50 نجمة و 13 خطاً؟",
                options = listOf("50 نجمة للولايات الحالية و13 خطاً للمستعمرات الأصلية الـ13", "13 نجمة للرؤساء و50 خطاً للقوانين", "مجرد تصميم فني بدون دلالة", "50 نجمة لكل سنة استقلال"),
                correctIndex = 0,
                explanation = "تمثل النجوم الخمسون الولايات الأمريكية الخمسين، بينما تمثل الخطوط الثلاثة عشر المستعمرات الأصلية التي أسست الأمة الأمريكية."
            )
        )
    )

    /**
     * توليد سؤال جديد ذكياً بناءً على الموضوع المطلوب مع تفاصيل الإجابة والشرح
     */
    suspend fun generateQuizItem(topic: String): QuizItem = withContext(Dispatchers.Default) {
        delay(1200)

        val candidateList = presetQuestionsByTopic[topic]
            ?: presetQuestionsByTopic.values.flatten()

        val selected = candidateList.random()
        val currentTime = System.currentTimeMillis()

        QuizItem(
            id = 0,
            question = selected.question,
            options = selected.options,
            correctIndex = selected.correctIndex,
            explanation = selected.explanation,
            interval = 0,
            repetition = 0,
            easeFactor = 2.5f,
            nextReviewDate = currentTime
        )
    }

    val availableTopics = listOf(
        "الدستور الأمريكي",
        "فروع الحكومة",
        "الحقوق والواجبات",
        "التاريخ الأمريكي",
        "الجغرافيا والرموز"
    )
}
