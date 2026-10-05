package com.chhabinath.pocketspend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chhabinath.pocketspend.ai.LocalLLM
import com.chhabinath.pocketspend.ai.ModelManager
import com.chhabinath.pocketspend.data.AppDatabase
import com.chhabinath.pocketspend.data.Expense
import com.chhabinath.pocketspend.ui.theme.PocketSpendTheme
import com.chhabinath.pocketspend.userinterface.AddExpenseScreen
import com.chhabinath.pocketspend.userinterface.ReceiptScreen
import com.chhabinath.pocketspend.viewmodel.ExpenseViewModel
import com.chhabinath.pocketspend.viewmodel.ExpenseViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale


class MainActivity : ComponentActivity() {

    private val expenseViewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory(
            AppDatabase
                .getDatabase(applicationContext)
                .expenseDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            PocketSpendTheme {
                PocketSpendApp(
                    viewModel = expenseViewModel,
                    context = this@MainActivity
                )
            }
        }
    }
}


/* ============================================================
   MAIN APP NAVIGATION
   ============================================================ */

@Composable
fun PocketSpendApp(
    viewModel: ExpenseViewModel,
    context: MainActivity
) {

    var showAddExpense by remember {
        mutableStateOf(false)
    }

    var showReceiptScreen by remember {
        mutableStateOf(false)
    }

    var showAIScreen by remember {
        mutableStateOf(false)
    }

    /*
     * Text coming from receipt OCR.
     * This will be passed to the local AI.
     */
    var aiText by remember {
        mutableStateOf("")
    }


    when {

        /*
         * AI SCREEN
         */
        showAIScreen -> {

            PocketSpendAIScreen(
                context = context,
                inputText = aiText,
                onBack = {
                    showAIScreen = false
                }
            )
        }


        /*
         * RECEIPT SCREEN
         */
        showReceiptScreen -> {

            ReceiptScreen(
                onBack = {
                    showReceiptScreen = false
                },

                /*
                 * ReceiptScreen sends extracted OCR text here.
                 */
                onAnalyzeWithAI = { text ->

                    aiText = text

                    showReceiptScreen = false

                    showAIScreen = true
                }
            )
        }


        /*
         * ADD EXPENSE SCREEN
         */
        showAddExpense -> {

            AddExpenseScreen(
                viewModel = viewModel,
                onBack = {
                    showAddExpense = false
                }
            )
        }


        /*
         * HOME SCREEN
         */
        else -> {

            PocketSpendHome(
                viewModel = viewModel,

                onAddExpense = {
                    showAddExpense = true
                },

                onScanReceipt = {
                    showReceiptScreen = true
                },

                onOpenAI = {
                    /*
                     * Open AI directly without receipt text.
                     */
                    aiText = ""
                    showAIScreen = true
                }
            )
        }
    }
}


/* ============================================================
   HOME SCREEN
   ============================================================ */

@Composable
fun PocketSpendHome(
    viewModel: ExpenseViewModel,
    onAddExpense: () -> Unit,
    onScanReceipt: () -> Unit,
    onOpenAI: () -> Unit
) {

    val expenses by viewModel.expenses.collectAsState()

    val totalAmount by viewModel.totalAmount.collectAsState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        /*
         * TITLE
         */

        Text(
            text = "PocketSpend",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )


        Text(
            text = "Private AI Expense Tracker",
            style = MaterialTheme.typography.bodyMedium
        )


        Spacer(
            modifier = Modifier.height(32.dp)
        )


        /*
         * TOTAL SPENDING CARD
         */

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "October Spending",
                    style = MaterialTheme.typography.titleMedium
                )


                Spacer(
                    modifier = Modifier.height(8.dp)
                )


                Text(
                    text = "₹${
                        String.format(
                            Locale.getDefault(),
                            "%.2f",
                            totalAmount
                        )
                    }",

                    style = MaterialTheme.typography.headlineLarge,

                    fontWeight = FontWeight.Bold
                )
            }
        }


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        /*
         * ACTION BUTTONS
         */

        Row(
            modifier = Modifier.fillMaxWidth(),

            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            /*
             * SCAN RECEIPT
             */

            Button(
                onClick = onScanReceipt,

                modifier = Modifier.weight(1f)
            ) {

                Text("Scan")
            }


            /*
             * ADD EXPENSE
             */

            Button(
                onClick = onAddExpense,

                modifier = Modifier.weight(1f)
            ) {

                Text("Add")
            }


            /*
             * AI
             */

            Button(
                onClick = onOpenAI,

                modifier = Modifier.weight(1f)
            ) {

                Text("AI")
            }
        }


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        /*
         * RECENT EXPENSES
         */

        Text(
            text = "Recent Expenses",

            style = MaterialTheme.typography.titleLarge,

            fontWeight = FontWeight.Bold
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        if (expenses.isEmpty()) {

            Text(
                text = "No expenses yet.",

                style = MaterialTheme.typography.bodyLarge
            )

        } else {

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {

                items(
                    items = expenses,

                    key = { expense ->
                        expense.id
                    }

                ) { expense ->

                    ExpenseItem(
                        expense = expense
                    )
                }
            }
        }
    }
}


/* ============================================================
   EXPENSE ITEM
   ============================================================ */

@Composable
fun ExpenseItem(
    expense: Expense
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {

                Text(
                    text = expense.merchant,

                    fontWeight = FontWeight.Bold
                )


                Text(
                    text = expense.category
                )


                Text(
                    text = expense.date
                )
            }


            Text(
                text = "₹${
                    String.format(
                        Locale.getDefault(),
                        "%.2f",
                        expense.amount
                    )
                }",

                fontWeight = FontWeight.Bold
            )
        }
    }
}


/* ============================================================
   LOCAL AI SCREEN
   ============================================================ */

@Composable
fun PocketSpendAIScreen(
    context: MainActivity,

    /*
     * OCR text from receipt.
     *
     * Empty = normal AI test.
     */
    inputText: String,

    onBack: () -> Unit
) {

    var aiResult by remember {
        mutableStateOf("Loading local AI...")
    }


    /*
     * Run the local LLM only when inputText changes.
     */
    LaunchedEffect(inputText) {

        aiResult = withContext(Dispatchers.IO) {

            try {

                /*
                 * Get GGUF model path.
                 */
                val modelPath =
                    ModelManager.getModelPath(context)


                /*
                 * ====================================================
                 * PROMPT
                 * ====================================================
                 *
                 * This is the important part.
                 *
                 * We explicitly tell Qwen:
                 *
                 * - You are PocketSpend AI
                 * - Offline/private
                 * - Do not repeat tokens
                 * - Keep answers short
                 * - Analyze receipt text when provided
                 */

                val prompt: String

                if (inputText.isBlank()) {

                    prompt = """
                        You are PocketSpend AI,
                        a private offline financial assistant.

                        Say hello to the user.

                        Rules:
                        - Use one short sentence.
                        - Do not use emojis.
                        - Do not repeat words.
                        - Do not produce a long response.
                        - Return only one sentence.
                    """.trimIndent()

                } else {

                    prompt = """
                        You are PocketSpend AI,
                        a private offline financial assistant.

                        Analyze the following receipt OCR text.

                        Extract useful information such as:
                        - Merchant
                        - Total amount
                        - Date
                        - Main spending category

                        Then give a very short spending summary.

                        Rules:
                        - Use plain text.
                        - Do not use emojis.
                        - Do not repeat words.
                        - Do not hallucinate missing information.
                        - If information is missing, say "Not available".
                        - Keep the answer concise.
                        - Do not output unnecessary explanations.

                        Receipt OCR:
                        $inputText
                    """.trimIndent()
                }


                /*
                 * Run local llama.cpp model.
                 */
                LocalLLM.generate(
                    modelPath,
                    prompt
                )

            } catch (e: Exception) {

                "ERROR: ${e.message ?: "Failed to run local AI"}"
            }
        }
    }


    /*
     * ============================================================
     * UI
     * ============================================================
     */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {


        /*
         * BACK BUTTON
         */

        Button(
            onClick = onBack
        ) {

            Text("Back")
        }


        Spacer(
            modifier = Modifier.height(32.dp)
        )


        /*
         * TITLE
         */

        Text(
            text = "PocketSpend AI",

            style = MaterialTheme.typography.headlineMedium,

            fontWeight = FontWeight.Bold
        )


        Spacer(
            modifier = Modifier.height(32.dp)
        )


        /*
         * AI RESULT
         */

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = aiResult,

                modifier = Modifier.padding(24.dp),

                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}


/* ============================================================
   PREVIEW
   ============================================================ */

@Preview(
    showBackground = true
)
@Composable
fun PocketSpendPreview() {

    PocketSpendTheme {

        Text(
            text = "PocketSpend"
        )
    }
}