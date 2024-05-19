package com.example.inquizitiveapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.inquizitiveapp.ui.theme.InQuizitiveAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Quiz(
    val questions: Array<String>,
    val options: Array<Array<String>>,
    val correctAnswers: Array<Int>
) {
    var currentQuestionIndex = 0
    var score = 0
    var sot: Long = 0L
    var totalTime: Long = 0L
}

class MainActivity : ComponentActivity() {

    val ques = arrayOf(
        "Which of these characters was almost added into Super Smash Bros. Melee, but not included as the game was too far in development?",
        "Which Mario Kart game introduced the concept of racing on anti-gravity tracks?",
        "What is the name of the item in Mario Kart that can protect the player from incoming projectiles by circling around them?",
        "Which character has been a playable driver in every main series Mario Kart game?",
        "What is the name of the rainbow-colored track that appears in nearly every Mario Kart game?"
    )
    val opts = arrayOf(
        arrayOf("Solid Snake", "Pit", "Meta Knight", "R.O.B."),
        arrayOf("Double Dash!!", "Mario Kart 7", "Mario Kart 8", "Mario Kart Wii"),
        arrayOf("Triple Shells", "Banana Peel", "Mushroom", "Lightning Bolt"),
        arrayOf("Toadette", "Mario", "Rosalina", "King Boo"),
        arrayOf("Yoshi Circuit", "Bowser's Castle", "Mushroom Gorge", "Rainbow Road")
    )
    val correctans = arrayOf(0, 2, 0, 1, 3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val quiz = Quiz(ques, opts, correctans)
        setContent {
            InQuizitiveAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val (currentScreen, setCurrentScreen) = remember { mutableStateOf("Home") }
                    when (currentScreen) {
                        "Home" -> HomeScreen { setCurrentScreen("Quiz") }
                        "Quiz" -> BackgroundImageWithQuiz(quiz) {
                            setCurrentScreen("Result")
                        }
                        "Result" -> QuizResultScreen(
                            score = quiz.score,
                            totalQuestions = ques.size,sot = quiz.sot,
                            totalTime = quiz.totalTime

                        ) {
                            quiz.currentQuestionIndex = 0
                            quiz.score = 0
                            setCurrentScreen("Home")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun HomeScreen(onStartQuiz: () -> Unit) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.quizimg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to InQuizitive!",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                Button(onClick = onStartQuiz,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .size(2.1f * ButtonDefaults.MinWidth),
                    colors = buttonColors(containerColor = Color(0xFFBE5504))) {
                    Text(text = "Start Quiz")
                }
            }
        }
    }

    @Composable
    fun BackgroundImageWithQuiz(quiz: Quiz, onQuizEnd: () -> Unit) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.bgg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            QuizScreen(quiz, onQuizEnd, Modifier.align(Alignment.Center))
        }
    }

    @Composable
    fun QuizScreen(quiz: Quiz, onQuizEnd: () -> Unit, modifier: Modifier = Modifier) {
        val question = quiz.questions[quiz.currentQuestionIndex]
        val options = quiz.options[quiz.currentQuestionIndex]
        var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
        var showCorrectAnswer by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        var timeLeft by remember { mutableStateOf(30) } // 30 seconds for each question
        var startTime by remember { mutableStateOf(System.currentTimeMillis()) }

        LaunchedEffect(key1 = quiz.currentQuestionIndex) {
            timeLeft = 30
            startTime = System.currentTimeMillis()
            showCorrectAnswer = false
            while (timeLeft > 0 && !showCorrectAnswer) {
                delay(1000L)
                timeLeft -= 1
            }
            if (timeLeft == 0) {
                showCorrectAnswer = true
                scope.launch {
                    delay(2000)
                    if (quiz.currentQuestionIndex < quiz.questions.size - 1) {
                        quiz.currentQuestionIndex++
                        selectedOptionIndex = null
                        showCorrectAnswer = false
                    } else {
                        onQuizEnd()
                    }
                }
            }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(350.dp))

            Text(
                text = "Time left: $timeLeft",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Spacer(modifier = Modifier.height(15.dp))


            Text(
                text = question,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            options.forEachIndexed { index, option ->
                val backgroundColor = when {
                    showCorrectAnswer && index == quiz.correctAnswers[quiz.currentQuestionIndex] -> Color.Green
                    selectedOptionIndex == index && index != quiz.correctAnswers[quiz.currentQuestionIndex] -> Color.Red
                    else -> Color.LightGray
                }
                Button(
                    onClick = {
                        if (!showCorrectAnswer) {
                            selectedOptionIndex = index
                            val endTime = System.currentTimeMillis()
                            val timeTaken = endTime - startTime
                            quiz.totalTime += timeTaken
                            if (index == quiz.correctAnswers[quiz.currentQuestionIndex]) {
                                quiz.score++
                                quiz.sot += timeTaken
                            }
                            showCorrectAnswer = true
                            scope.launch {
                                delay(1000)
                                if (quiz.currentQuestionIndex < quiz.questions.size - 1) {
                                    quiz.currentQuestionIndex++
                                    selectedOptionIndex = null
                                    showCorrectAnswer = false
                                } else {
                                    onQuizEnd()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .width(150.dp)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedOptionIndex == index) backgroundColor else Color(0xFF111E6C)),
                ) {
                    Text(text = option)
                }
            }
        }
    }


    @Composable
    fun QuizResultScreen(score: Int, totalQuestions: Int, sot: Long, totalTime: Long, onRestart: () -> Unit) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.quizimg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Quiz Completed!",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Your Score: $score / $totalQuestions",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                // sot /= 1000
                Text(
                    text = "Time taken for correct answers: ${sot/1000} s",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // totalTime /= 1000
                Text(
                    text = "Total time taken: ${totalTime/1000} s",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .size(2.6f * ButtonDefaults.MinWidth),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBE5504))
                ) {
                    Text(text = "Restart Quiz")
                }
            }}
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewHomeScreen() {
        InQuizitiveAppTheme {
            HomeScreen {}
        }
    }
}


